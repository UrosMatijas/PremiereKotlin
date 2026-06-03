package rs.edu.raf.premiereuros.list

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import rs.edu.raf.premiereuros.core.mvi.BaseMviViewModel
import rs.edu.raf.premiereuros.domain.repository.PremiereRepository

class PremiereListViewModel(
    private val repository: PremiereRepository
) : BaseMviViewModel<PremiereListContract.UiState, PremiereListContract.UiEvent, PremiereListContract.SideEffect>(
    initialState = PremiereListContract.UiState()
) {
    private var observeMoviesJob: Job? = null

    init {
        observeMoviesForCurrentSort()
        observeAppliedFilter()
        loadMovies()
    }

    override suspend fun onEvent(event: PremiereListContract.UiEvent) {
        when (event) {
            PremiereListContract.UiEvent.Retry -> loadMovies()

            PremiereListContract.UiEvent.ToggleSortMenu -> {
                setState { copy(isSortMenuExpanded = !isSortMenuExpanded) }
            }

            PremiereListContract.UiEvent.DismissSortMenu -> {
                setState { copy(isSortMenuExpanded = false) }
            }

            is PremiereListContract.UiEvent.ChangeSort -> {
                setState {
                    copy(
                        selectedSort = event.sortOption,
                        isSortMenuExpanded = false
                    )
                }
                observeMoviesForCurrentSort()
                loadMovies()
            }

            is PremiereListContract.UiEvent.OnMovieClick -> {
                emitEffect(PremiereListContract.SideEffect.NavigateToDetails(event.imdbId))
            }

            PremiereListContract.UiEvent.OnFilterClick -> {
                emitEffect(PremiereListContract.SideEffect.NavigateToFilter)
            }

            PremiereListContract.UiEvent.ToggleLayoutMode -> {
                setState {
                    copy(
                        layoutMode = when (layoutMode) {
                            PremiereListContract.LayoutMode.List -> PremiereListContract.LayoutMode.Grid
                            PremiereListContract.LayoutMode.Grid -> PremiereListContract.LayoutMode.List
                        }
                    )
                }
            }

            PremiereListContract.UiEvent.OnProfileClick -> {
                emitEffect(PremiereListContract.SideEffect.NavigateToProfile)
            }

            PremiereListContract.UiEvent.OnQuizClick -> {
                emitEffect(PremiereListContract.SideEffect.NavigateToQuiz)
            }
        }
    }

    private fun observeAppliedFilter() {
        viewModelScope.launch {
            repository.appliedFilter.collect { filter ->
                setState { copy(appliedFilterCount = filter.activeCount()) }
                loadMovies()
            }
        }
    }

    private fun observeMoviesForCurrentSort() {
        observeMoviesJob?.cancel()
        observeMoviesJob = viewModelScope.launch {
            repository.observeMovies(currentState.selectedSort).collect { movies ->
                setState {
                    copy(
                        movies = movies,
                        totalMoviesLabel = "${movies.size} movies",
                        contentState = when {
                            contentState == PremiereListContract.ContentState.Offline &&
                                movies.isNotEmpty() -> PremiereListContract.ContentState.Offline
                            movies.isEmpty() -> PremiereListContract.ContentState.Empty
                            else -> PremiereListContract.ContentState.Content
                        }
                    )
                }
            }
        }
    }

    private fun loadMovies() {
        viewModelScope.launch {
            setState {
                copy(
                    contentState = PremiereListContract.ContentState.Loading,
                    statusMessage = null
                )
            }

            runCatching {
                val config = repository.getImageConfig()
                repository.getMovies(currentState.selectedSort)
                config
            }.onSuccess { config ->
                setState {
                    copy(
                        imageConfig = config,
                        contentState = if (movies.isEmpty()) {
                            PremiereListContract.ContentState.Empty
                        } else {
                            PremiereListContract.ContentState.Content
                        },
                        statusMessage = null
                    )
                }
            }.onFailure { error ->
                val hasLocalData = currentState.movies.isNotEmpty()
                setState {
                    copy(
                        contentState = if (hasLocalData) {
                            PremiereListContract.ContentState.Offline
                        } else {
                            PremiereListContract.ContentState.Error
                        },
                        statusMessage = error.message ?: if (hasLocalData) {
                            "Offline mode: showing cached movies."
                        } else {
                            "Could not load movies."
                        }
                    )
                }
            }
        }
    }
}
