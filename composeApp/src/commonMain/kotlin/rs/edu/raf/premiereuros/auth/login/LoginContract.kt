package rs.edu.raf.premiereuros.auth.login

import rs.edu.raf.premiereuros.core.mvi.MviEffect
import rs.edu.raf.premiereuros.core.mvi.MviEvent
import rs.edu.raf.premiereuros.core.mvi.MviState

interface LoginContract {

    data class UiState(
        val username: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    ) : MviState

    sealed interface UiEvent : MviEvent {
        data class ChangeUsername(val value: String) : UiEvent
        data class ChangePassword(val value: String) : UiEvent
        data object Submit : UiEvent
        data object Back : UiEvent
        data object OpenSignup : UiEvent
    }

    sealed interface SideEffect : MviEffect {
        data object NavigateToApp : SideEffect
        data object NavigateBack : SideEffect
        data object NavigateToSignup : SideEffect
    }
}
