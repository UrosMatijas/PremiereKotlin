package rs.edu.raf.premiereuros.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.compose.koinInject
import rs.edu.raf.premiereuros.core.ui.PlatformBackHandler
import rs.edu.raf.premiereuros.domain.model.QuizResultSummary

@Composable
fun QuizRoute(
    onBack: () -> Unit,
    onNavigateToResult: (QuizResultSummary) -> Unit,
    viewModel: QuizViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val eventFlow = remember(viewModel) {
        MutableSharedFlow<QuizContract.UiEvent>(extraBufferCapacity = 32)
    }

    LaunchedEffect(viewModel, eventFlow) {
        eventFlow.collect { event -> viewModel.setEvent(event) }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                QuizContract.SideEffect.NavigateBack -> onBack()
                is QuizContract.SideEffect.NavigateToResult -> onNavigateToResult(effect.result)
            }
        }
    }

    QuizScreen(
        state = state,
        eventFlow = eventFlow
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    state: QuizContract.UiState,
    eventFlow: MutableSharedFlow<QuizContract.UiEvent>
) {
    PlatformBackHandler(enabled = state.question != null) {
        eventFlow.tryEmit(QuizContract.UiEvent.RequestAbandon)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz") },
                actions = {
                    TextButton(
                        onClick = { eventFlow.tryEmit(QuizContract.UiEvent.RequestAbandon) },
                        enabled = state.question != null
                    ) {
                        Text("Quit")
                    }
                }
            )
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
            when {
                state.isLoading -> {
                    CircularProgressIndicator()
                }

                state.entryGuardMessage != null -> {
                    Text(state.entryGuardMessage)
                    Button(
                        onClick = { eventFlow.tryEmit(QuizContract.UiEvent.Retry) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Retry") }
                }

                state.errorMessage != null -> {
                    Text(state.errorMessage)
                    Button(
                        onClick = { eventFlow.tryEmit(QuizContract.UiEvent.Retry) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Retry") }
                }

                state.question != null -> {
                    val question = state.question
                    Text("Question ${state.currentIndex + 1}/${state.totalQuestions}")
                    Text("Remaining: ${state.remainingSeconds}s")

                    AsyncImage(
                        model = state.imageConfig?.posterUrl(question.imagePath, "w500")
                            ?: state.imageConfig?.backdropUrl(question.imagePath, "w780"),
                        contentDescription = "Quiz image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentScale = ContentScale.Crop
                    )

                    if (question.titleHint != null) {
                        Text("Movie: ${question.titleHint}")
                    }
                    Text(question.questionText)

                    question.options.forEachIndexed { index, option ->
                        val isCorrectOption = index == question.correctOptionIndex
                        val isSelectedOption = index == state.selectedOptionIndex
                        val buttonColors = if (state.isAnswerLocked && isCorrectOption) {
                            ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        } else if (state.isAnswerLocked && isSelectedOption && !isCorrectOption) {
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        } else {
                            ButtonDefaults.buttonColors()
                        }

                        Button(
                            onClick = {
                                eventFlow.tryEmit(QuizContract.UiEvent.SelectOption(index))
                            },
                            enabled = !state.isAnswerLocked,
                            modifier = Modifier.fillMaxWidth(),
                            colors = buttonColors
                        ) {
                            Text(option)
                        }
                    }

                    if (state.feedbackText != null) {
                        Text(state.feedbackText)
                    }
                }
            }
        }
    }

    if (state.showAbandonDialog) {
        AlertDialog(
            onDismissRequest = {
                eventFlow.tryEmit(QuizContract.UiEvent.DismissAbandon)
            },
            title = { Text("Abandon quiz?") },
            text = { Text("Current quiz session will be discarded.") },
            confirmButton = {
                TextButton(
                    onClick = { eventFlow.tryEmit(QuizContract.UiEvent.ConfirmAbandon) }
                ) { Text("Abandon") }
            },
            dismissButton = {
                TextButton(
                    onClick = { eventFlow.tryEmit(QuizContract.UiEvent.DismissAbandon) }
                ) { Text("Cancel") }
            }
        )
    }
}
