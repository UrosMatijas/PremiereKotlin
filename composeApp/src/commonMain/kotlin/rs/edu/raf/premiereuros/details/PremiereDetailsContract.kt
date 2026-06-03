package rs.edu.raf.premiereuros.details

import rs.edu.raf.premiereuros.core.mvi.MviEffect
import rs.edu.raf.premiereuros.core.mvi.MviEvent
import rs.edu.raf.premiereuros.core.mvi.MviState
import rs.edu.raf.premiereuros.domain.model.ImageConfig
import rs.edu.raf.premiereuros.domain.model.MovieCastMember
import rs.edu.raf.premiereuros.domain.model.MovieDetails
import rs.edu.raf.premiereuros.domain.model.MovieImage
import rs.edu.raf.premiereuros.domain.model.MovieVideo

interface PremiereDetailsContract {

    enum class ContentState {
        Loading,
        Content,
        Empty,
        Offline,
        Error
    }

    data class UiState(
        val contentState: ContentState = ContentState.Loading,
        val movie: MovieDetails? = null,
        val cast: List<MovieCastMember> = emptyList(),
        val images: List<MovieImage> = emptyList(),
        val videos: List<MovieVideo> = emptyList(),
        val imageConfig: ImageConfig? = null,
        val isFavorite: Boolean = false,
        val isInWatchlist: Boolean = false,
        val isFavoriteUpdating: Boolean = false,
        val isWatchlistUpdating: Boolean = false,
        val statusMessage: String? = null
    ) : MviState

    sealed interface UiEvent : MviEvent {
        data class Load(val imdbId: String) : UiEvent
        data object Retry : UiEvent
        data object ToggleFavorite : UiEvent
        data object ToggleWatchlist : UiEvent
        data object Back : UiEvent
    }

    sealed interface SideEffect : MviEffect {
        data object NavigateBack : SideEffect
    }
}
