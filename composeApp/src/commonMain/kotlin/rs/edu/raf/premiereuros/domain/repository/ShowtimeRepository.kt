package rs.edu.raf.premiereuros.domain.repository

import rs.edu.raf.premiereuros.domain.model.LeaderboardEntry
import rs.edu.raf.premiereuros.domain.model.MovieListItem
import rs.edu.raf.premiereuros.domain.model.PostedQuizResult
import rs.edu.raf.premiereuros.domain.model.QuizResult
import rs.edu.raf.premiereuros.domain.model.ShowtimePage
import rs.edu.raf.premiereuros.domain.model.ShowtimeUser

interface ShowtimeRepository {
    suspend fun getMe(): ShowtimeUser

    suspend fun getFavorites(): List<MovieListItem>
    suspend fun isFavorite(movieId: String): Boolean
    suspend fun addFavorite(movieId: String): MovieListItem
    suspend fun removeFavorite(movieId: String)

    suspend fun getWatchlist(): List<MovieListItem>
    suspend fun isInWatchlist(movieId: String): Boolean
    suspend fun addWatchlist(movieId: String): MovieListItem
    suspend fun removeWatchlist(movieId: String)

    suspend fun getLeaderboard(
        category: Int = 1,
        page: Int = 1,
        pageSize: Int = 20
    ): ShowtimePage<LeaderboardEntry>

    suspend fun postLeaderboard(
        score: Float,
        category: Int = 1
    ): PostedQuizResult

    suspend fun getMyQuizResults(
        page: Int = 1,
        pageSize: Int = 20
    ): ShowtimePage<QuizResult>
}
