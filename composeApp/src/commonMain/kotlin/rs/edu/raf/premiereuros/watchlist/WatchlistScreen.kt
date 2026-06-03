package rs.edu.raf.premiereuros.watchlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.compose.koinInject

@Composable
fun WatchlistRoute(
    onBack: () -> Unit,
    onOpenDetails: (String) -> Unit,
    viewModel: WatchlistViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val eventFlow = remember(viewModel) {
        MutableSharedFlow<WatchlistContract.UiEvent>(extraBufferCapacity = 32)
    }

    LaunchedEffect(viewModel, eventFlow) {
        eventFlow.collect { event -> viewModel.setEvent(event) }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                WatchlistContract.SideEffect.NavigateBack -> onBack()
                is WatchlistContract.SideEffect.NavigateToDetails -> onOpenDetails(effect.imdbId)
            }
        }
    }

    WatchlistScreen(state = state, eventFlow = eventFlow)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    state: WatchlistContract.UiState,
    eventFlow: MutableSharedFlow<WatchlistContract.UiEvent>
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Watchlist") },
                navigationIcon = {
                    TextButton(onClick = { eventFlow.tryEmit(WatchlistContract.UiEvent.Back) }) {
                        Text("Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state.contentState) {
                WatchlistContract.ContentState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                WatchlistContract.ContentState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.statusMessage ?: "Could not load watchlist.")
                        Button(onClick = { eventFlow.tryEmit(WatchlistContract.UiEvent.Retry) }) {
                            Text("Retry")
                        }
                    }
                }

                WatchlistContract.ContentState.Empty -> {
                    Text(
                        text = "No watchlist movies yet.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                WatchlistContract.ContentState.Content -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (state.statusMessage != null) {
                            Text(state.statusMessage)
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.movies, key = { it.imdbId }) { movie ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            eventFlow.tryEmit(
                                                WatchlistContract.UiEvent.OpenMovie(movie.imdbId)
                                            )
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = state.imageConfig?.posterUrl(movie.posterPath, "w185"),
                                        contentDescription = movie.title,
                                        modifier = Modifier.size(80.dp),
                                        contentScale = ContentScale.Crop
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 12.dp)
                                    ) {
                                        Text(movie.title)
                                        Text("Year: ${movie.year ?: "-"}")
                                        Text("IMDb: ${movie.imdbRating ?: "-"}")
                                    }

                                    TextButton(
                                        onClick = {
                                            eventFlow.tryEmit(
                                                WatchlistContract.UiEvent.RemoveMovie(movie.imdbId)
                                            )
                                        },
                                        enabled = !state.removingIds.contains(movie.imdbId)
                                    ) {
                                        Text(
                                            if (state.removingIds.contains(movie.imdbId)) {
                                                "Removing..."
                                            } else {
                                                "Remove"
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
