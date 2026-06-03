package rs.edu.raf.premiereuros.watchlist

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import rs.edu.raf.premiereuros.core.mvi.BaseMviViewModel
import rs.edu.raf.premiereuros.domain.repository.PremiereRepository
import rs.edu.raf.premiereuros.domain.repository.ShowtimeRepository

class WatchlistViewModel(
    private val showtimeRepository: ShowtimeRepository,
    private val premiereRepository: PremiereRepository
) : BaseMviViewModel<WatchlistContract.UiState, WatchlistContract.UiEvent, WatchlistContract.SideEffect>(
    initialState = WatchlistContract.UiState()
) {

    init {
        setEvent(WatchlistContract.UiEvent.Load)
    }

    override suspend fun onEvent(event: WatchlistContract.UiEvent) {
        when (event) {
            WatchlistContract.UiEvent.Load -> loadWatchlist()
            WatchlistContract.UiEvent.Retry -> loadWatchlist()
            is WatchlistContract.UiEvent.OpenMovie -> {
                emitEffect(WatchlistContract.SideEffect.NavigateToDetails(event.imdbId))
            }

            is WatchlistContract.UiEvent.RemoveMovie -> removeWatchlistItem(event.imdbId)
            WatchlistContract.UiEvent.Back -> emitEffect(WatchlistContract.SideEffect.NavigateBack)
        }
    }

    private fun loadWatchlist() {
        viewModelScope.launch {
            setState {
                copy(
                    contentState = WatchlistContract.ContentState.Loading,
                    statusMessage = null
                )
            }

            runCatching {
                val watchlistDeferred = async { showtimeRepository.getWatchlist() }
                val imageConfigDeferred = async { premiereRepository.getImageConfig() }

                Pair(watchlistDeferred.await(), imageConfigDeferred.await())
            }.onSuccess { (movies, imageConfig) ->
                setState {
                    copy(
                        contentState = if (movies.isEmpty()) {
                            WatchlistContract.ContentState.Empty
                        } else {
                            WatchlistContract.ContentState.Content
                        },
                        movies = movies,
                        imageConfig = imageConfig,
                        statusMessage = null
                    )
                }
            }.onFailure { error ->
                setState {
                    copy(
                        contentState = WatchlistContract.ContentState.Error,
                        statusMessage = error.message ?: "Could not load watchlist."
                    )
                }
            }
        }
    }

    private fun removeWatchlistItem(imdbId: String) {
        if (currentState.removingIds.contains(imdbId)) return

        viewModelScope.launch {
            setState { copy(removingIds = removingIds + imdbId, statusMessage = null) }

            runCatching {
                showtimeRepository.removeWatchlist(imdbId)
            }.onSuccess {
                val updatedMovies = currentState.movies.filterNot { it.imdbId == imdbId }
                setState {
                    copy(
                        movies = updatedMovies,
                        removingIds = removingIds - imdbId,
                        contentState = if (updatedMovies.isEmpty()) {
                            WatchlistContract.ContentState.Empty
                        } else {
                            WatchlistContract.ContentState.Content
                        }
                    )
                }
            }.onFailure { error ->
                setState {
                    copy(
                        removingIds = removingIds - imdbId,
                        statusMessage = error.message ?: "Failed to remove from watchlist."
                    )
                }
            }
        }
    }
}
