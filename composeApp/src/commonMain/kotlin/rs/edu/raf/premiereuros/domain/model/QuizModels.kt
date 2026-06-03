package rs.edu.raf.premiereuros.domain.model

enum class QuizQuestionType {
    GuessMovie,
    GuessYear,
    GuessLeadActor
}

data class QuizQuestion(
    val id: Int,
    val type: QuizQuestionType,
    val movieImdbId: String,
    val imagePath: String,
    val questionText: String,
    val titleHint: String?,
    val options: List<String>,
    val correctOptionIndex: Int
)

data class QuizSession(
    val questions: List<QuizQuestion>,
    val totalSeconds: Int = 60
)

data class QuizResultSummary(
    val score: Float,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val usedSeconds: Int,
    val remainingSeconds: Int
)

data class QuizLocalStats(
    val bestScore: Float,
    val playedCount: Int
)
