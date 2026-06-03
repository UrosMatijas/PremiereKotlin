package rs.edu.raf.premiereuros.list

import rs.edu.raf.premiereuros.core.mvi.MviEffect
import rs.edu.raf.premiereuros.core.mvi.MviEvent
import rs.edu.raf.premiereuros.core.mvi.MviState
import rs.edu.raf.premiereuros.domain.model.ImageConfig
import rs.edu.raf.premiereuros.domain.model.MovieListItem
import rs.edu.raf.premiereuros.domain.model.SortOption

interface PremiereListContract {

    enum class LayoutMode {
        List,
        Grid
    }

    enum class ContentState {
        Loading,
        Content,
        Empty,
        Offline,
        Error
    }

    data class UiState(
        val contentState: ContentState = ContentState.Loading,
        val movies: List<MovieListItem> = emptyList(),
        val totalMoviesLabel: String = "0 movies",
        val selectedSort: SortOption = SortOption.Rating,
        val isSortMenuExpanded: Boolean = false,
        val layoutMode: LayoutMode = LayoutMode.List,
        val appliedFilterCount: Int = 0,
        val imageConfig: ImageConfig? = null,
        val statusMessage: String? = null
    ) : MviState

    sealed interface UiEvent : MviEvent {
        data object Retry : UiEvent
        data object ToggleSortMenu : UiEvent
        data object DismissSortMenu : UiEvent
        data class ChangeSort(val sortOption: SortOption) : UiEvent
        data class OnMovieClick(val imdbId: String) : UiEvent
        data object OnFilterClick : UiEvent
        data object ToggleLayoutMode : UiEvent
        data object OnProfileClick : UiEvent
        data object OnQuizClick : UiEvent
    }

    sealed interface SideEffect : MviEffect {
        data class NavigateToDetails(val imdbId: String) : SideEffect
        data object NavigateToFilter : SideEffect
        data object NavigateToProfile : SideEffect
        data object NavigateToQuiz : SideEffect
    }
}
