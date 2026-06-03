package rs.edu.raf.premiereuros.watchlist

import rs.edu.raf.premiereuros.core.mvi.MviEffect
import rs.edu.raf.premiereuros.core.mvi.MviEvent
import rs.edu.raf.premiereuros.core.mvi.MviState
import rs.edu.raf.premiereuros.domain.model.ImageConfig
import rs.edu.raf.premiereuros.domain.model.MovieListItem

interface WatchlistContract {

    enum class ContentState {
        Loading,
        Content,
        Empty,
        Error
    }

    data class UiState(
        val contentState: ContentState = ContentState.Loading,
        val movies: List<MovieListItem> = emptyList(),
        val imageConfig: ImageConfig? = null,
        val removingIds: Set<String> = emptySet(),
        val statusMessage: String? = null
    ) : MviState

    sealed interface UiEvent : MviEvent {
        data object Load : UiEvent
        data object Retry : UiEvent
        data class OpenMovie(val imdbId: String) : UiEvent
        data class RemoveMovie(val imdbId: String) : UiEvent
        data object Back : UiEvent
    }

    sealed interface SideEffect : MviEffect {
        data class NavigateToDetails(val imdbId: String) : SideEffect
        data object NavigateBack : SideEffect
    }
}
