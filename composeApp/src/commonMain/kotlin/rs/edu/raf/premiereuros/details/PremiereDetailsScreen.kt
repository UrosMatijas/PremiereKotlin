package rs.edu.raf.premiereuros.details

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.compose.koinInject

@Composable
fun PremiereDetailsScreenRoute(
    imdbId: String,
    onBack: () -> Unit,
    viewModel: PremiereDetailsViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val eventFlow = remember(viewModel) {
        MutableSharedFlow<PremiereDetailsContract.UiEvent>(extraBufferCapacity = 32)
    }

    LaunchedEffect(viewModel, eventFlow) {
        eventFlow.collect { event -> viewModel.setEvent(event) }
    }

    LaunchedEffect(imdbId) {
        eventFlow.emit(PremiereDetailsContract.UiEvent.Load(imdbId))
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                PremiereDetailsContract.SideEffect.NavigateBack -> onBack()
            }
        }
    }

    PremiereDetailsScreen(
        state = state,
        eventFlow = eventFlow
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiereDetailsScreen(
    state: PremiereDetailsContract.UiState,
    eventFlow: MutableSharedFlow<PremiereDetailsContract.UiEvent>
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Details") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (state.contentState) {
                PremiereDetailsContract.ContentState.Loading -> {
                    CircularProgressIndicator()
                }

                PremiereDetailsContract.ContentState.Error -> {
                    Text(state.statusMessage ?: "Could not load movie details.")
                    Button(onClick = { eventFlow.tryEmit(PremiereDetailsContract.UiEvent.Retry) }) {
                        Text("Retry")
                    }
                }

                PremiereDetailsContract.ContentState.Empty -> {
                    Text(state.statusMessage ?: "Movie not found.")
                    Button(onClick = { eventFlow.tryEmit(PremiereDetailsContract.UiEvent.Back) }) {
                        Text("Back")
                    }
                }

                PremiereDetailsContract.ContentState.Offline,
                PremiereDetailsContract.ContentState.Content -> {
                    Button(
                        onClick = {
                            eventFlow.tryEmit(PremiereDetailsContract.UiEvent.Back)
                        }
                    ) {
                        Text("Back")
                    }

                    if (state.contentState == PremiereDetailsContract.ContentState.Offline) {
                        Text(
                            text = state.statusMessage ?: "Offline mode: showing cached details.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    val movie = state.movie ?: return@Column

                    AsyncImage(
                        model = state.imageConfig?.backdropUrl(movie.backdropPath, "w780"),
                        contentDescription = "${movie.title} backdrop",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )

                    AsyncImage(
                        model = state.imageConfig?.posterUrl(movie.posterPath, "w342"),
                        contentDescription = "${movie.title} poster",
                        modifier = Modifier.size(160.dp),
                        contentScale = ContentScale.Crop
                    )

                    Text(movie.title)
                    Text("${movie.year ?: "-"} | ${movie.runtime ?: "-"} min")
                    Text("IMDb ${movie.imdbRating ?: "-"} (${movie.imdbVotes ?: 0})")
                    Text("TMDB ${movie.tmdbRating ?: "-"} (${movie.tmdbVotes ?: 0})")

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                eventFlow.tryEmit(PremiereDetailsContract.UiEvent.ToggleFavorite)
                            },
                            enabled = !state.isFavoriteUpdating
                        ) {
                            Text(
                                if (state.isFavoriteUpdating) {
                                    "Updating..."
                                } else if (state.isFavorite) {
                                    "Remove Favorite"
                                } else {
                                    "Add Favorite"
                                }
                            )
                        }

                        Button(
                            onClick = {
                                eventFlow.tryEmit(PremiereDetailsContract.UiEvent.ToggleWatchlist)
                            },
                            enabled = !state.isWatchlistUpdating
                        ) {
                            Text(
                                if (state.isWatchlistUpdating) {
                                    "Updating..."
                                } else if (state.isInWatchlist) {
                                    "Remove Watchlist"
                                } else {
                                    "Add Watchlist"
                                }
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        movie.genres.forEach { genre ->
                            AssistChip(
                                onClick = {},
                                label = { Text(genre.name) }
                            )
                        }
                    }

                    Text(movie.overview.orEmpty())

                    Text("Info")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Budget: ${movie.budget ?: 0}")
                        Text("Revenue: ${movie.revenue ?: 0}")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Language: ${movie.languageCode ?: "-"}")
                        Text("Popularity: ${movie.popularity ?: 0f}")
                    }

                    Text("Images")
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        state.images.forEach { image ->
                            AsyncImage(
                                model = state.imageConfig?.backdropUrl(image.filePath, "w300"),
                                contentDescription = "Movie image",
                                modifier = Modifier.size(180.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Text("Cast")
                    state.cast.forEach { actor ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AsyncImage(
                                model = state.imageConfig?.profileUrl(actor.profilePath, "w185"),
                                contentDescription = actor.name,
                                modifier = Modifier.size(72.dp),
                                contentScale = ContentScale.Crop
                            )
                            Text(actor.name)
                        }
                    }
                }
            }
        }
    }
}
