package rs.edu.raf.premiereuros.auth.signup

import rs.edu.raf.premiereuros.core.mvi.MviEffect
import rs.edu.raf.premiereuros.core.mvi.MviEvent
import rs.edu.raf.premiereuros.core.mvi.MviState

interface SignupContract {

    data class UiState(
        val fullName: String = "",
        val username: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    ) : MviState

    sealed interface UiEvent : MviEvent {
        data class ChangeFullName(val value: String) : UiEvent
        data class ChangeUsername(val value: String) : UiEvent
        data class ChangePassword(val value: String) : UiEvent
        data object Submit : UiEvent
        data object Back : UiEvent
        data object OpenLogin : UiEvent
    }

    sealed interface SideEffect : MviEffect {
        data object NavigateToApp : SideEffect
        data object NavigateBack : SideEffect
        data object NavigateToLogin : SideEffect
    }
}
