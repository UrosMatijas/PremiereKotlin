package rs.edu.raf.premiereuros.quiz

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rs.edu.raf.premiereuros.core.mvi.BaseMviViewModel
import rs.edu.raf.premiereuros.domain.model.QuizQuestion
import rs.edu.raf.premiereuros.domain.model.QuizResultSummary
import rs.edu.raf.premiereuros.domain.repository.PremiereRepository
import rs.edu.raf.premiereuros.domain.repository.QuizRepository

class QuizViewModel(
    private val quizRepository: QuizRepository,
    private val premiereRepository: PremiereRepository
) : BaseMviViewModel<QuizContract.UiState, QuizContract.UiEvent, QuizContract.SideEffect>(
    initialState = QuizContract.UiState()
) {

    private var timerJob: Job? = null
    private var questions: List<QuizQuestion> = emptyList()
    private var totalSeconds: Int = 60
    private var quizFinished: Boolean = false

    init {
        setEvent(QuizContract.UiEvent.Load)
    }

    override suspend fun onEvent(event: QuizContract.UiEvent) {
        when (event) {
            QuizContract.UiEvent.Load -> loadQuiz()
            QuizContract.UiEvent.Retry -> loadQuiz()
            is QuizContract.UiEvent.SelectOption -> onSelectOption(event.optionIndex)
            QuizContract.UiEvent.RequestAbandon -> setState { copy(showAbandonDialog = true) }
            QuizContract.UiEvent.DismissAbandon -> setState { copy(showAbandonDialog = false) }
            QuizContract.UiEvent.ConfirmAbandon -> {
                timerJob?.cancel()
                setState { copy(showAbandonDialog = false) }
                emitEffect(QuizContract.SideEffect.NavigateBack)
            }
        }
    }

    private fun loadQuiz() {
        timerJob?.cancel()
        viewModelScope.launch {
            setState {
                QuizContract.UiState(
                    isLoading = true,
                    remainingSeconds = 60
                )
            }
            quizFinished = false

            runCatching {
                val hasEnoughData = quizRepository.hasEnoughLocalMovies(minCount = 10)
                if (!hasEnoughData) {
                    return@runCatching Triple(null, null, "Browse the catalog first to load at least 10 movies with images.")
                }
                val imageConfig = premiereRepository.getImageConfig()
                val session = quizRepository.createSession(questionCount = 10)
                Triple(session, imageConfig, null)
            }.onSuccess { (session, imageConfig, guardMessage) ->
                if (guardMessage != null) {
                    setState {
                        copy(
                            isLoading = false,
                            entryGuardMessage = guardMessage
                        )
                    }
                    return@onSuccess
                }

                if (session == null || session.questions.isEmpty()) {
                    setState {
                        copy(
                            isLoading = false,
                            errorMessage = "Could not create a valid quiz session."
                        )
                    }
                    return@onSuccess
                }

                questions = session.questions
                totalSeconds = session.totalSeconds
                quizFinished = false

                setState {
                    copy(
                        isLoading = false,
                        question = questions.first(),
                        imageConfig = imageConfig,
                        currentIndex = 0,
                        totalQuestions = questions.size,
                        remainingSeconds = totalSeconds,
                        selectedOptionIndex = null,
                        isAnswerLocked = false,
                        feedbackText = null,
                        correctAnswers = 0,
                        incorrectAnswers = 0,
                        entryGuardMessage = null,
                        errorMessage = null
                    )
                }

                startTimer()
            }.onFailure { error ->
                setState {
                    copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to start quiz."
                    )
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (currentState.remainingSeconds > 0) {
                delay(1_000)
                val next = currentState.remainingSeconds - 1
                setState { copy(remainingSeconds = next) }
                if (next <= 0) {
                    finishQuiz(cancelTimer = false)
                    break
                }
            }
        }
    }

    private fun onSelectOption(optionIndex: Int) {
        val question = currentState.question ?: return
        if (currentState.isAnswerLocked || quizFinished) return

        val isCorrect = optionIndex == question.correctOptionIndex
        val nextCorrect = currentState.correctAnswers + if (isCorrect) 1 else 0
        val nextIncorrect = currentState.incorrectAnswers + if (isCorrect) 0 else 1

        viewModelScope.launch {
            setState {
                copy(
                    selectedOptionIndex = optionIndex,
                    isAnswerLocked = true,
                    feedbackText = if (isCorrect) "Correct!" else "Wrong!",
                    correctAnswers = nextCorrect,
                    incorrectAnswers = nextIncorrect
                )
            }

            delay(700)
            advanceOrFinish()
        }
    }

    private suspend fun advanceOrFinish() {
        val nextIndex = currentState.currentIndex + 1
        if (nextIndex >= questions.size || currentState.remainingSeconds <= 0) {
            finishQuiz(cancelTimer = true)
            return
        }

        setState {
            copy(
                currentIndex = nextIndex,
                question = questions[nextIndex],
                selectedOptionIndex = null,
                isAnswerLocked = false,
                feedbackText = null
            )
        }
    }

    private suspend fun finishQuiz(cancelTimer: Boolean) {
        if (quizFinished) return
        quizFinished = true
        if (cancelTimer) {
            timerJob?.cancel()
        }

        val remaining = currentState.remainingSeconds.coerceAtLeast(0)
        val correct = currentState.correctAnswers
        val incorrect = (questions.size - correct).coerceAtLeast(0)
        val usedSeconds = (totalSeconds - remaining).coerceAtLeast(0)
        val score = calculateScore(correct = correct, remainingSeconds = remaining)

        val summary = QuizResultSummary(
            score = score,
            correctAnswers = correct,
            incorrectAnswers = incorrect,
            usedSeconds = usedSeconds,
            remainingSeconds = remaining
        )

        runCatching { quizRepository.saveResult(summary) }
        emitEffect(QuizContract.SideEffect.NavigateToResult(summary))
    }

    private fun calculateScore(correct: Int, remainingSeconds: Int): Float {
        val raw = correct * (9f + (remainingSeconds / 60f))
        return raw.coerceAtMost(100f)
    }
}
