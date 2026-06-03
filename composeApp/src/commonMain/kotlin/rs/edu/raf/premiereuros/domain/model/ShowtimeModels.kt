package rs.edu.raf.premiereuros.domain.model

data class ShowtimeUser(
    val id: Int,
    val username: String,
    val fullName: String
)

data class LeaderboardEntry(
    val rank: Int,
    val userId: Int,
    val username: String,
    val fullName: String,
    val score: Float,
    val playedAt: Long,
    val totalPlays: Int
)

data class QuizResult(
    val id: Long,
    val category: Int,
    val score: Float,
    val playedAt: Long
)

data class PostedQuizResult(
    val result: QuizResult,
    val ranking: Int
)

data class ShowtimePage<T>(
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int,
    val items: List<T>
)
