package rs.edu.raf.premiereuros.filter

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import rs.edu.raf.premiereuros.core.mvi.BaseMviViewModel
import rs.edu.raf.premiereuros.domain.model.MovieFilter
import rs.edu.raf.premiereuros.domain.repository.PremiereRepository

class PremiereFilterViewModel(
    private val repository: PremiereRepository
) : BaseMviViewModel<PremiereFilterContract.UiState, PremiereFilterContract.UiEvent, PremiereFilterContract.SideEffect>(
    initialState = PremiereFilterContract.UiState()
) {
    private var observeGenresJob: Job? = null

    init {
        observeGenres()
        loadInitialData()
    }

    override suspend fun onEvent(event: PremiereFilterContract.UiEvent) {
        when (event) {
            PremiereFilterContract.UiEvent.LoadGenres -> loadGenres()

            is PremiereFilterContract.UiEvent.ChangeQuery -> setState { copy(query = event.value) }
            is PremiereFilterContract.UiEvent.ChangeGenre -> setState { copy(selectedGenreId = event.genreId) }
            is PremiereFilterContract.UiEvent.ChangeMinYear -> setState { copy(minYear = event.value) }
            is PremiereFilterContract.UiEvent.ChangeMaxYear -> setState { copy(maxYear = event.value) }
            is PremiereFilterContract.UiEvent.ChangeMinRating -> setState { copy(minRating = event.value) }

            PremiereFilterContract.UiEvent.Clear -> {
                setState {
                    copy(
                        query = "",
                        selectedGenreId = null,
                        minYear = "",
                        maxYear = "",
                        minRating = 0f
                    )
                }
            }

            PremiereFilterContract.UiEvent.Apply -> applyFilter()

            PremiereFilterContract.UiEvent.Back -> {
                emitEffect(PremiereFilterContract.SideEffect.NavigateBack)
            }
        }
    }

    private fun loadInitialData() {
        loadGenres()
        viewModelScope.launch {
            val filter = repository.appliedFilter.first()
            setState {
                copy(
                    query = filter.query,
                    selectedGenreId = filter.selectedGenreId,
                    minYear = filter.minYear,
                    maxYear = filter.maxYear,
                    minRating = filter.minRating
                )
            }
        }
    }

    private fun observeGenres() {
        observeGenresJob?.cancel()
        observeGenresJob = viewModelScope.launch {
            repository.observeGenres().collect { genres ->
                setState {
                    copy(
                        genres = genres,
                        contentState = if (genres.isEmpty()) {
                            PremiereFilterContract.ContentState.Empty
                        } else {
                            PremiereFilterContract.ContentState.Content
                        }
                    )
                }
            }
        }
    }

    private fun loadGenres() {
        viewModelScope.launch {
            setState {
                copy(
                    contentState = PremiereFilterContract.ContentState.Loading,
                    statusMessage = null
                )
            }

            runCatching { repository.getGenres() }
                .onSuccess { _ ->
                    setState {
                        copy(
                            contentState = if (genres.isEmpty()) {
                                PremiereFilterContract.ContentState.Empty
                            } else {
                                PremiereFilterContract.ContentState.Content
                            },
                            statusMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    setState {
                        copy(
                            contentState = PremiereFilterContract.ContentState.Error,
                            statusMessage = error.message ?: "Error loading genres."
                        )
                    }
                }
        }
    }

    private fun applyFilter() {
        viewModelScope.launch {
            repository.setAppliedFilter(
                MovieFilter(
                    query = currentState.query,
                    selectedGenreId = currentState.selectedGenreId,
                    minYear = currentState.minYear,
                    maxYear = currentState.maxYear,
                    minRating = currentState.minRating
                )
            )
            emitEffect(PremiereFilterContract.SideEffect.NavigateBack)
        }
    }
}
