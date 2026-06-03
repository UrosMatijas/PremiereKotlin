package rs.edu.raf.premiereuros.auth.signup

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import rs.edu.raf.premiereuros.core.auth.SessionManager
import rs.edu.raf.premiereuros.core.mvi.BaseMviViewModel
import rs.edu.raf.premiereuros.domain.repository.AuthFailure
import rs.edu.raf.premiereuros.domain.repository.AuthRepository
import rs.edu.raf.premiereuros.domain.repository.AuthResult

class SignupViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : BaseMviViewModel<SignupContract.UiState, SignupContract.UiEvent, SignupContract.SideEffect>(
    initialState = SignupContract.UiState()
) {

    override suspend fun onEvent(event: SignupContract.UiEvent) {
        when (event) {
            is SignupContract.UiEvent.ChangeFullName -> setState {
                copy(fullName = event.value, errorMessage = null)
            }

            is SignupContract.UiEvent.ChangeUsername -> setState {
                copy(username = event.value, errorMessage = null)
            }

            is SignupContract.UiEvent.ChangePassword -> setState {
                copy(password = event.value, errorMessage = null)
            }

            SignupContract.UiEvent.Submit -> submit()
            SignupContract.UiEvent.Back -> emitEffect(SignupContract.SideEffect.NavigateBack)
            SignupContract.UiEvent.OpenLogin -> emitEffect(SignupContract.SideEffect.NavigateToLogin)
        }
    }

    private fun submit() {
        val fullName = currentState.fullName.trim()
        val username = currentState.username.trim()
        val password = currentState.password

        val validationError = when {
            fullName.isBlank() -> "Full name is required."
            username.isBlank() -> "Username is required."
            !USERNAME_REGEX.matches(username) -> "Username must use letters, digits, or underscore."
            username.length < 3 -> "Username must have at least 3 characters."
            password.isBlank() -> "Password is required."
            password.length < 8 -> "Password must have at least 8 characters."
            else -> null
        }

        if (validationError != null) {
            setState { copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }

            when (val result = authRepository.signup(fullName, username, password)) {
                is AuthResult.Success -> {
                    sessionManager.onLogin(result.token)
                    setState { copy(isLoading = false) }
                    emitEffect(SignupContract.SideEffect.NavigateToApp)
                }

                is AuthResult.Failure -> {
                    val message = when (result.reason) {
                        AuthFailure.UsernameTaken -> "Username is already taken."
                        AuthFailure.Network -> "Network error. Check your connection and try again."
                        AuthFailure.InvalidCredentials,
                        is AuthFailure.Unknown -> "Could not create account. Please try a different username."
                    }
                    setState { copy(isLoading = false, errorMessage = message) }
                }
            }
        }
    }

    private companion object {
        val USERNAME_REGEX = Regex("^[a-zA-Z0-9_]+$")
    }
}
