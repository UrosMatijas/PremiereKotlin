package rs.edu.raf.premiereuros.profile

import rs.edu.raf.premiereuros.core.mvi.MviEffect
import rs.edu.raf.premiereuros.core.mvi.MviEvent
import rs.edu.raf.premiereuros.core.mvi.MviState

interface ProfileContract {

    data class UiState(
        val isLoading: Boolean = false,
        val isLoggingOut: Boolean = false,
        val fullName: String = "",
        val username: String = "",
        val favoritesCount: Int = 0,
        val watchlistCount: Int = 0,
        val bestScore: Float = 0f,
        val playedQuizzes: Int = 0,
        val errorMessage: String? = null
    ) : MviState

    sealed interface UiEvent : MviEvent {
        data object Load : UiEvent
        data object Retry : UiEvent
        data object OpenFavorites : UiEvent
        data object OpenWatchlist : UiEvent
        data object Logout : UiEvent
        data object Back : UiEvent
    }

    sealed interface SideEffect : MviEffect {
        data object NavigateBack : SideEffect
        data object NavigateToFavorites : SideEffect
        data object NavigateToWatchlist : SideEffect
        data object NavigateToAuth : SideEffect
    }
}
