package rs.edu.raf.premiereuros.filter

import rs.edu.raf.premiereuros.core.mvi.MviEffect
import rs.edu.raf.premiereuros.core.mvi.MviEvent
import rs.edu.raf.premiereuros.core.mvi.MviState
import rs.edu.raf.premiereuros.domain.model.Genre

interface PremiereFilterContract {

    enum class ContentState {
        Loading,
        Content,
        Empty,
        Error
    }

    data class UiState(
        val contentState: ContentState = ContentState.Loading,
        val query: String = "",
        val genres: List<Genre> = emptyList(),
        val selectedGenreId: Int? = null,
        val minYear: String = "",
        val maxYear: String = "",
        val minRating: Float = 0f,
        val statusMessage: String? = null
    ) : MviState

    sealed interface UiEvent : MviEvent {
        data object LoadGenres : UiEvent
        data class ChangeQuery(val value: String) : UiEvent
        data class ChangeGenre(val genreId: Int?) : UiEvent
        data class ChangeMinYear(val value: String) : UiEvent
        data class ChangeMaxYear(val value: String) : UiEvent
        data class ChangeMinRating(val value: Float) : UiEvent
        data object Apply : UiEvent
        data object Clear : UiEvent
        data object Back : UiEvent
    }

    sealed interface SideEffect : MviEffect {
        data object NavigateBack : SideEffect
    }
}
