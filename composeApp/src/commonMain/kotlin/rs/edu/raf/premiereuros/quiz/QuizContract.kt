package rs.edu.raf.premiereuros.quiz

import rs.edu.raf.premiereuros.core.mvi.MviEffect
import rs.edu.raf.premiereuros.core.mvi.MviEvent
import rs.edu.raf.premiereuros.core.mvi.MviState
import rs.edu.raf.premiereuros.domain.model.ImageConfig
import rs.edu.raf.premiereuros.domain.model.QuizQuestion
import rs.edu.raf.premiereuros.domain.model.QuizResultSummary

interface QuizContract {

    data class UiState(
        val isLoading: Boolean = true,
        val question: QuizQuestion? = null,
        val imageConfig: ImageConfig? = null,
        val currentIndex: Int = 0,
        val totalQuestions: Int = 0,
        val remainingSeconds: Int = 60,
        val selectedOptionIndex: Int? = null,
        val isAnswerLocked: Boolean = false,
        val feedbackText: String? = null,
        val correctAnswers: Int = 0,
        val incorrectAnswers: Int = 0,
        val entryGuardMessage: String? = null,
        val errorMessage: String? = null,
        val showAbandonDialog: Boolean = false
    ) : MviState

    sealed interface UiEvent : MviEvent {
        data object Load : UiEvent
        data class SelectOption(val optionIndex: Int) : UiEvent
        data object Retry : UiEvent
        data object RequestAbandon : UiEvent
        data object DismissAbandon : UiEvent
        data object ConfirmAbandon : UiEvent
    }

    sealed interface SideEffect : MviEffect {
        data class NavigateToResult(val result: QuizResultSummary) : SideEffect
        data object NavigateBack : SideEffect
    }
}
