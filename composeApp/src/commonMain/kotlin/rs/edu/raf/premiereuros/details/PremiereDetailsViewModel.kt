package rs.edu.raf.premiereuros.details

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import rs.edu.raf.premiereuros.core.mvi.BaseMviViewModel
import rs.edu.raf.premiereuros.domain.repository.PremiereRepository
import rs.edu.raf.premiereuros.domain.repository.ShowtimeRepository

class PremiereDetailsViewModel(
    private val repository: PremiereRepository,
    private val showtimeRepository: ShowtimeRepository
) : BaseMviViewModel<PremiereDetailsContract.UiState, PremiereDetailsContract.UiEvent, PremiereDetailsContract.SideEffect>(
    initialState = PremiereDetailsContract.UiState()
) {

    private var lastImdbId: String? = null

    override suspend fun onEvent(event: PremiereDetailsContract.UiEvent) {
        when (event) {
            is PremiereDetailsContract.UiEvent.Load -> {
                lastImdbId = event.imdbId
                loadMovie(event.imdbId)
            }

            PremiereDetailsContract.UiEvent.Retry -> {
                lastImdbId?.let { loadMovie(it) }
            }

            PremiereDetailsContract.UiEvent.ToggleFavorite -> {
                toggleFavorite()
            }

            PremiereDetailsContract.UiEvent.ToggleWatchlist -> {
                toggleWatchlist()
            }

            PremiereDetailsContract.UiEvent.Back -> {
                emitEffect(PremiereDetailsContract.SideEffect.NavigateBack)
            }
        }
    }

    private fun loadMovie(imdbId: String) {
        viewModelScope.launch {
            setState {
                copy(
                    contentState = PremiereDetailsContract.ContentState.Loading,
                    statusMessage = null
                )
            }

            runCatching {
                val movie = withTimeoutOrNull(10_000L) {
                    repository.getMovieDetails(imdbId)
                }

                val castDeferred = async {
                    safeLoad(fallback = emptyList()) {
                        repository.getMovieCast(imdbId).take(10)
                    }
                }
                val imagesDeferred = async {
                    safeLoad(fallback = emptyList()) {
                        repository.getMovieImages(imdbId).take(3)
                    }
                }
                val videosDeferred = async {
                    safeLoad(fallback = emptyList()) {
                        repository.getMovieVideos(imdbId)
                    }
                }
                val configDeferred = async {
                    safeLoad(fallback = null) {
                        repository.getImageConfig()
                    }
                }
                val isFavoriteDeferred = async {
                    safeLoad(fallback = false) {
                        showtimeRepository.isFavorite(imdbId)
                    }
                }
                val isInWatchlistDeferred = async {
                    safeLoad(fallback = false) {
                        showtimeRepository.isInWatchlist(imdbId)
                    }
                }

                PremiereDetailsContract.UiState(
                    contentState = PremiereDetailsContract.ContentState.Content,
                    movie = movie,
                    cast = castDeferred.await(),
                    images = imagesDeferred.await(),
                    videos = videosDeferred.await(),
                    imageConfig = configDeferred.await(),
                    isFavorite = isFavoriteDeferred.await(),
                    isInWatchlist = isInWatchlistDeferred.await(),
                    statusMessage = null
                )
            }.onSuccess { newState ->
                if (newState.movie == null) {
                    setState {
                        copy(
                            contentState = PremiereDetailsContract.ContentState.Empty,
                            statusMessage = "Movie not found."
                        )
                    }
                } else {
                    replaceState(newState)
                }
            }.onFailure { error ->
                val hasLocalData = currentState.movie != null
                setState {
                    copy(
                        contentState = if (hasLocalData) {
                            PremiereDetailsContract.ContentState.Offline
                        } else {
                            PremiereDetailsContract.ContentState.Error
                        },
                        statusMessage = error.message ?: if (hasLocalData) {
                            "Offline mode: showing cached details."
                        } else {
                            "Could not load movie details."
                        }
                    )
                }
            }
        }
    }

    private suspend fun <T> safeLoad(
        timeoutMillis: Long = 5_000L,
        fallback: T,
        block: suspend () -> T
    ): T {
        return withTimeoutOrNull(timeoutMillis) {
            runCatching { block() }.getOrDefault(fallback)
        } ?: fallback
    }

    private fun toggleFavorite() {
        val movie = currentState.movie ?: return
        if (currentState.isFavoriteUpdating) return

        val previous = currentState.isFavorite
        val optimistic = !previous

        viewModelScope.launch {
            setState {
                copy(
                    isFavorite = optimistic,
                    isFavoriteUpdating = true,
                    statusMessage = null
                )
            }

            runCatching {
                if (optimistic) {
                    showtimeRepository.addFavorite(movie.imdbId)
                } else {
                    showtimeRepository.removeFavorite(movie.imdbId)
                }
            }.onSuccess {
                setState { copy(isFavoriteUpdating = false) }
            }.onFailure { error ->
                setState {
                    copy(
                        isFavorite = previous,
                        isFavoriteUpdating = false,
                        statusMessage = error.message ?: "Failed to update favorites."
                    )
                }
            }
        }
    }

    private fun toggleWatchlist() {
        val movie = currentState.movie ?: return
        if (currentState.isWatchlistUpdating) return

        val previous = currentState.isInWatchlist
        val optimistic = !previous

        viewModelScope.launch {
            setState {
                copy(
                    isInWatchlist = optimistic,
                    isWatchlistUpdating = true,
                    statusMessage = null
                )
            }

            runCatching {
                if (optimistic) {
                    showtimeRepository.addWatchlist(movie.imdbId)
                } else {
                    showtimeRepository.removeWatchlist(movie.imdbId)
                }
            }.onSuccess {
                setState { copy(isWatchlistUpdating = false) }
            }.onFailure { error ->
                setState {
                    copy(
                        isInWatchlist = previous,
                        isWatchlistUpdating = false,
                        statusMessage = error.message ?: "Failed to update watchlist."
                    )
                }
            }
        }
    }
}
