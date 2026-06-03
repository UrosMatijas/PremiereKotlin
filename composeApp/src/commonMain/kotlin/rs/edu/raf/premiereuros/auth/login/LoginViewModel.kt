package rs.edu.raf.premiereuros.auth.login

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import rs.edu.raf.premiereuros.core.auth.SessionManager
import rs.edu.raf.premiereuros.core.mvi.BaseMviViewModel
import rs.edu.raf.premiereuros.domain.repository.AuthFailure
import rs.edu.raf.premiereuros.domain.repository.AuthRepository
import rs.edu.raf.premiereuros.domain.repository.AuthResult

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : BaseMviViewModel<LoginContract.UiState, LoginContract.UiEvent, LoginContract.SideEffect>(
    initialState = LoginContract.UiState()
) {

    override suspend fun onEvent(event: LoginContract.UiEvent) {
        when (event) {
            is LoginContract.UiEvent.ChangeUsername -> setState {
                copy(username = event.value, errorMessage = null)
            }

            is LoginContract.UiEvent.ChangePassword -> setState {
                copy(password = event.value, errorMessage = null)
            }

            LoginContract.UiEvent.Submit -> submit()
            LoginContract.UiEvent.Back -> emitEffect(LoginContract.SideEffect.NavigateBack)
            LoginContract.UiEvent.OpenSignup -> emitEffect(LoginContract.SideEffect.NavigateToSignup)
        }
    }

    private fun submit() {
        val username = currentState.username.trim()
        val password = currentState.password

        val validationError = when {
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

            when (val result = authRepository.login(username = username, password = password)) {
                is AuthResult.Success -> {
                    sessionManager.onLogin(result.token)
                    setState { copy(isLoading = false) }
                    emitEffect(LoginContract.SideEffect.NavigateToApp)
                }

                is AuthResult.Failure -> {
                    val message = when (result.reason) {
                        AuthFailure.InvalidCredentials,
                        AuthFailure.UsernameTaken,
                        is AuthFailure.Unknown -> "Invalid username or password."
                        AuthFailure.Network -> "Network error. Check your connection and try again."
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
