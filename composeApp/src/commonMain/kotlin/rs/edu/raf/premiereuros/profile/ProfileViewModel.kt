package rs.edu.raf.premiereuros.profile

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import rs.edu.raf.premiereuros.core.auth.SessionManager
import rs.edu.raf.premiereuros.core.mvi.BaseMviViewModel
import rs.edu.raf.premiereuros.domain.repository.QuizRepository
import rs.edu.raf.premiereuros.domain.repository.ShowtimeRepository

class ProfileViewModel(
    private val showtimeRepository: ShowtimeRepository,
    private val quizRepository: QuizRepository,
    private val sessionManager: SessionManager
) : BaseMviViewModel<ProfileContract.UiState, ProfileContract.UiEvent, ProfileContract.SideEffect>(
    initialState = ProfileContract.UiState()
) {

    init {
        setEvent(ProfileContract.UiEvent.Load)
    }

    override suspend fun onEvent(event: ProfileContract.UiEvent) {
        when (event) {
            ProfileContract.UiEvent.Load -> loadProfile()
            ProfileContract.UiEvent.Retry -> loadProfile()
            ProfileContract.UiEvent.OpenFavorites -> emitEffect(ProfileContract.SideEffect.NavigateToFavorites)
            ProfileContract.UiEvent.OpenWatchlist -> emitEffect(ProfileContract.SideEffect.NavigateToWatchlist)
            ProfileContract.UiEvent.Logout -> logout()
            ProfileContract.UiEvent.Back -> emitEffect(ProfileContract.SideEffect.NavigateBack)
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }

            runCatching {
                val me = async { showtimeRepository.getMe() }
                val favorites = async { showtimeRepository.getFavorites() }
                val watchlist = async { showtimeRepository.getWatchlist() }
                val stats = async { quizRepository.getLocalStats() }

                Quadruple(
                    me.await(),
                    favorites.await().size,
                    watchlist.await().size,
                    stats.await()
                )
            }.onSuccess { (me, favoritesCount, watchlistCount, stats) ->
                setState {
                    copy(
                        isLoading = false,
                        fullName = me.fullName,
                        username = me.username,
                        favoritesCount = favoritesCount,
                        watchlistCount = watchlistCount,
                        bestScore = stats.bestScore,
                        playedQuizzes = stats.playedCount,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                setState {
                    copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load profile."
                    )
                }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            setState { copy(isLoggingOut = true, errorMessage = null) }
            runCatching { sessionManager.logout() }
                .onSuccess {
                    setState { copy(isLoggingOut = false) }
                    emitEffect(ProfileContract.SideEffect.NavigateToAuth)
                }
                .onFailure { error ->
                    setState {
                        copy(
                            isLoggingOut = false,
                            errorMessage = error.message ?: "Failed to logout."
                        )
                    }
                }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
