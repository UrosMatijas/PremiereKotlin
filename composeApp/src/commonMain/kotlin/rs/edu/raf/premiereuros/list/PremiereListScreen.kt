package rs.edu.raf.premiereuros.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import rs.edu.raf.premiereuros.domain.model.SortOption

@Composable
fun PremiereListRoute(
    onMovieClick: (String) -> Unit,
    onFilterClick: () -> Unit,
    onProfileClick: () -> Unit,
    onQuizClick: () -> Unit,
    viewModel: PremiereListViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val eventFlow = remember(viewModel) {
        MutableSharedFlow<PremiereListContract.UiEvent>(extraBufferCapacity = 32)
    }

    LaunchedEffect(viewModel, eventFlow) {
        eventFlow.collect { event -> viewModel.setEvent(event) }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PremiereListContract.SideEffect.NavigateToDetails -> onMovieClick(effect.imdbId)
                PremiereListContract.SideEffect.NavigateToFilter -> onFilterClick()
                PremiereListContract.SideEffect.NavigateToProfile -> onProfileClick()
                PremiereListContract.SideEffect.NavigateToQuiz -> onQuizClick()
            }
        }
    }

    PremiereListScreen(
        state = state,
        eventFlow = eventFlow
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiereListScreen(
    state: PremiereListContract.UiState,
    eventFlow: MutableSharedFlow<PremiereListContract.UiEvent>
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premiere") },
                actions = {
                    TextButton(
                        onClick = { eventFlow.tryEmit(PremiereListContract.UiEvent.OnQuizClick) }
                    ) {
                        Text("Quiz")
                    }
                    TextButton(
                        onClick = { eventFlow.tryEmit(PremiereListContract.UiEvent.OnProfileClick) }
                    ) {
                        Text("Profile")
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
                PremiereListContract.ContentState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                PremiereListContract.ContentState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.statusMessage ?: "Could not load movies.")
                        Button(onClick = { eventFlow.tryEmit(PremiereListContract.UiEvent.Retry) }) {
                            Text("Retry")
                        }
                    }
                }

                PremiereListContract.ContentState.Empty -> {
                    Text(
                        text = "No movies found",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                PremiereListContract.ContentState.Content,
                PremiereListContract.ContentState.Offline -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                Button(
                                    onClick = {
                                        eventFlow.tryEmit(PremiereListContract.UiEvent.ToggleSortMenu)
                                    }
                                ) {
                                    Text("Sort: ${state.selectedSort}")
                                }

                                DropdownMenu(
                                    expanded = state.isSortMenuExpanded,
                                    onDismissRequest = {
                                        eventFlow.tryEmit(PremiereListContract.UiEvent.DismissSortMenu)
                                    }
                                ) {
                                    SortOption.entries.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.name) },
                                            onClick = {
                                                eventFlow.tryEmit(PremiereListContract.UiEvent.ChangeSort(option))
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                onClick = {
                                    eventFlow.tryEmit(PremiereListContract.UiEvent.OnFilterClick)
                                }
                            ) {
                                Text("Filter (${state.appliedFilterCount})")
                            }

                            Button(
                                onClick = {
                                    eventFlow.tryEmit(PremiereListContract.UiEvent.ToggleLayoutMode)
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    when (state.layoutMode) {
                                        PremiereListContract.LayoutMode.List -> "Grid"
                                        PremiereListContract.LayoutMode.Grid -> "List"
                                    }
                                )
                            }
                        }

                        if (state.contentState == PremiereListContract.ContentState.Offline) {
                            Text(
                                text = state.statusMessage ?: "Offline mode: showing cached data.",
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Text(state.totalMoviesLabel)

                        when (state.layoutMode) {
                            PremiereListContract.LayoutMode.List -> {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(state.movies, key = { it.imdbId }) { movie ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    eventFlow.tryEmit(
                                                        PremiereListContract.UiEvent.OnMovieClick(movie.imdbId)
                                                    )
                                                }
                                        ) {
                                            AsyncImage(
                                                model = state.imageConfig?.posterUrl(movie.posterPath, "w185"),
                                                contentDescription = movie.title,
                                                modifier = Modifier.size(80.dp),
                                                contentScale = ContentScale.Crop
                                            )

                                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                                Text(movie.title)
                                                Text("${movie.year ?: "-"} | ${movie.runtime ?: "-"} min")
                                                Text("IMDb: ${movie.imdbRating ?: "-"} (${movie.imdbVotes ?: 0})")
                                                Text(movie.genres.joinToString { it.name })
                                            }
                                        }
                                    }
                                }
                            }

                            PremiereListContract.LayoutMode.Grid -> {
                                LazyVerticalGrid(
                                    modifier = Modifier.weight(1f),
                                    columns = GridCells.Adaptive(minSize = 140.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(state.movies, key = { it.imdbId }) { movie ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    eventFlow.tryEmit(
                                                        PremiereListContract.UiEvent.OnMovieClick(movie.imdbId)
                                                    )
                                                }
                                        ) {
                                            AsyncImage(
                                                model = state.imageConfig?.posterUrl(movie.posterPath, "w342"),
                                                contentDescription = movie.title,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .size(180.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                            Text(movie.title)
                                            Text("${movie.year ?: "-"}")
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
}
