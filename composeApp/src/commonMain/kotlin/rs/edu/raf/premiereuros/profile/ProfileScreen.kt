package rs.edu.raf.premiereuros.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.compose.koinInject

@Composable
fun ProfileRoute(
    onBack: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenWatchlist: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val eventFlow = remember(viewModel) {
        MutableSharedFlow<ProfileContract.UiEvent>(extraBufferCapacity = 32)
    }

    LaunchedEffect(viewModel, eventFlow) {
        eventFlow.collect { event -> viewModel.setEvent(event) }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ProfileContract.SideEffect.NavigateBack -> onBack()
                ProfileContract.SideEffect.NavigateToFavorites -> onOpenFavorites()
                ProfileContract.SideEffect.NavigateToWatchlist -> onOpenWatchlist()
                ProfileContract.SideEffect.NavigateToAuth -> onLoggedOut()
            }
        }
    }

    ProfileScreen(state = state, eventFlow = eventFlow)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileContract.UiState,
    eventFlow: MutableSharedFlow<ProfileContract.UiEvent>
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profile") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator()
                }

                state.errorMessage != null -> {
                    Text(state.errorMessage)
                    Button(
                        onClick = { eventFlow.tryEmit(ProfileContract.UiEvent.Retry) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Retry")
                    }
                }

                else -> {
                    Text("Full name: ${state.fullName}")
                    Text("Username: ${state.username}")
                    Text("Favorite movies: ${state.favoritesCount}")
                    Text("Watchlist movies: ${state.watchlistCount}")
                    Text("Best quiz score: ${state.bestScore}")
                    Text("Played quizzes: ${state.playedQuizzes}")

                    Button(
                        onClick = { eventFlow.tryEmit(ProfileContract.UiEvent.OpenFavorites) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open Favorites")
                    }

                    Button(
                        onClick = { eventFlow.tryEmit(ProfileContract.UiEvent.OpenWatchlist) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open Watchlist")
                    }
                }
            }

            Button(
                onClick = { eventFlow.tryEmit(ProfileContract.UiEvent.Logout) },
                enabled = !state.isLoggingOut,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoggingOut) {
                    CircularProgressIndicator(modifier = Modifier.padding(2.dp))
                } else {
                    Text("Logout")
                }
            }

            Button(
                onClick = { eventFlow.tryEmit(ProfileContract.UiEvent.Back) },
                enabled = !state.isLoggingOut,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back")
            }
        }
    }
}
