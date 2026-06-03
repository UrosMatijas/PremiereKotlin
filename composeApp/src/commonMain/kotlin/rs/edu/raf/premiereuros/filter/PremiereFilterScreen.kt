package rs.edu.raf.premiereuros.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import kotlin.math.roundToInt
import org.koin.compose.koinInject

@Composable
fun PremiereFilterRoute(
    onBack: () -> Unit,
    viewModel: PremiereFilterViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val eventFlow = remember(viewModel) {
        MutableSharedFlow<PremiereFilterContract.UiEvent>(extraBufferCapacity = 32)
    }

    LaunchedEffect(viewModel, eventFlow) {
        eventFlow.collect { event -> viewModel.setEvent(event) }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                PremiereFilterContract.SideEffect.NavigateBack -> onBack()
            }
        }
    }

    PremiereFilterScreen(
        state = state,
        eventFlow = eventFlow
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiereFilterScreen(
    state: PremiereFilterContract.UiState,
    eventFlow: MutableSharedFlow<PremiereFilterContract.UiEvent>
) {
    val roundedRating = (state.minRating * 10).roundToInt() / 10f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filter") },
                navigationIcon = {
                    Button(
                        onClick = {
                            eventFlow.tryEmit(PremiereFilterContract.UiEvent.Back)
                        }
                    ) {
                        Text("Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (state.contentState) {
            PremiereFilterContract.ContentState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            PremiereFilterContract.ContentState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(state.statusMessage ?: "Could not load genres.")
                    Button(onClick = { eventFlow.tryEmit(PremiereFilterContract.UiEvent.LoadGenres) }) {
                        Text("Retry")
                    }
                }
            }

            PremiereFilterContract.ContentState.Empty,
            PremiereFilterContract.ContentState.Content -> {
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
                    if (state.contentState == PremiereFilterContract.ContentState.Empty) {
                        Text("No genres available. You can still filter by query, year, and rating.")
                    }

                    OutlinedTextField(
                        value = state.query,
                        onValueChange = {
                            eventFlow.tryEmit(PremiereFilterContract.UiEvent.ChangeQuery(it))
                        },
                        label = { Text("Search") }
                    )

                    Text("Genre")

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.genres.forEach { genre ->
                            AssistChip(
                                onClick = {
                                    eventFlow.tryEmit(
                                        PremiereFilterContract.UiEvent.ChangeGenre(
                                            if (state.selectedGenreId == genre.id) null else genre.id
                                        )
                                    )
                                },
                                label = {
                                    val selectedMark = if (state.selectedGenreId == genre.id) "[x] " else ""
                                    Text("$selectedMark${genre.name}")
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = state.minYear,
                        onValueChange = {
                            eventFlow.tryEmit(PremiereFilterContract.UiEvent.ChangeMinYear(it))
                        },
                        label = { Text("Min year") }
                    )

                    OutlinedTextField(
                        value = state.maxYear,
                        onValueChange = {
                            eventFlow.tryEmit(PremiereFilterContract.UiEvent.ChangeMaxYear(it))
                        },
                        label = { Text("Max year") }
                    )

                    Text("Minimum rating: $roundedRating")

                    Slider(
                        value = state.minRating,
                        onValueChange = {
                            eventFlow.tryEmit(PremiereFilterContract.UiEvent.ChangeMinRating(it))
                        },
                        valueRange = 0f..10f
                    )

                    Button(
                        onClick = {
                            eventFlow.tryEmit(PremiereFilterContract.UiEvent.Apply)
                        }
                    ) {
                        Text("Apply Filters")
                    }

                    Button(
                        onClick = {
                            eventFlow.tryEmit(PremiereFilterContract.UiEvent.Clear)
                        }
                    ) {
                        Text("Clear All")
                    }
                }
            }
        }
    }
}
