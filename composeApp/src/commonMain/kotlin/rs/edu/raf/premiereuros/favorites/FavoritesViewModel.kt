package rs.edu.raf.premiereuros.favorites

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import rs.edu.raf.premiereuros.core.mvi.BaseMviViewModel
import rs.edu.raf.premiereuros.domain.repository.PremiereRepository
import rs.edu.raf.premiereuros.domain.repository.ShowtimeRepository

class FavoritesViewModel(
    private val showtimeRepository: ShowtimeRepository,
    private val premiereRepository: PremiereRepository
) : BaseMviViewModel<FavoritesContract.UiState, FavoritesContract.UiEvent, FavoritesContract.SideEffect>(
    initialState = FavoritesContract.UiState()
) {

    init {
        setEvent(FavoritesContract.UiEvent.Load)
    }

    override suspend fun onEvent(event: FavoritesContract.UiEvent) {
        when (event) {
            FavoritesContract.UiEvent.Load -> loadFavorites()
            FavoritesContract.UiEvent.Retry -> loadFavorites()
            is FavoritesContract.UiEvent.OpenMovie -> {
                emitEffect(FavoritesContract.SideEffect.NavigateToDetails(event.imdbId))
            }

            is FavoritesContract.UiEvent.RemoveMovie -> removeFavorite(event.imdbId)
            FavoritesContract.UiEvent.Back -> emitEffect(FavoritesContract.SideEffect.NavigateBack)
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            setState {
                copy(
                    contentState = FavoritesContract.ContentState.Loading,
                    statusMessage = null
                )
            }

            runCatching {
                val favoritesDeferred = async { showtimeRepository.getFavorites() }
                val imageConfigDeferred = async { premiereRepository.getImageConfig() }

                Pair(favoritesDeferred.await(), imageConfigDeferred.await())
            }.onSuccess { (movies, imageConfig) ->
                setState {
                    copy(
                        contentState = if (movies.isEmpty()) {
                            FavoritesContract.ContentState.Empty
                        } else {
                            FavoritesContract.ContentState.Content
                        },
                        movies = movies,
                        imageConfig = imageConfig,
                        statusMessage = null
                    )
                }
            }.onFailure { error ->
                setState {
                    copy(
                        contentState = FavoritesContract.ContentState.Error,
                        statusMessage = error.message ?: "Could not load favorites."
                    )
                }
            }
        }
    }

    private fun removeFavorite(imdbId: String) {
        if (currentState.removingIds.contains(imdbId)) return

        viewModelScope.launch {
            setState { copy(removingIds = removingIds + imdbId, statusMessage = null) }

            runCatching {
                showtimeRepository.removeFavorite(imdbId)
            }.onSuccess {
                val updatedMovies = currentState.movies.filterNot { it.imdbId == imdbId }
                setState {
                    copy(
                        movies = updatedMovies,
                        removingIds = removingIds - imdbId,
                        contentState = if (updatedMovies.isEmpty()) {
                            FavoritesContract.ContentState.Empty
                        } else {
                            FavoritesContract.ContentState.Content
                        }
                    )
                }
            }.onFailure { error ->
                setState {
                    copy(
                        removingIds = removingIds - imdbId,
                        statusMessage = error.message ?: "Failed to remove from favorites."
                    )
                }
            }
        }
    }
}
