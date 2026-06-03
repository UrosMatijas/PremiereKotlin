package rs.edu.raf.premiereuros.data.repository

import rs.edu.raf.premiereuros.core.db.dao.MovieDao
import rs.edu.raf.premiereuros.core.db.entity.FavoriteMovieEntity
import rs.edu.raf.premiereuros.core.db.entity.WatchlistMovieEntity
import rs.edu.raf.premiereuros.data.local.mappers.toEntity
import rs.edu.raf.premiereuros.data.local.mappers.toListItem
import rs.edu.raf.premiereuros.data.remote.ShowtimeApi
import rs.edu.raf.premiereuros.data.remote.dto.PostLeaderboardRequestDto
import rs.edu.raf.premiereuros.data.remote.mappers.toDomain
import rs.edu.raf.premiereuros.domain.model.LeaderboardEntry
import rs.edu.raf.premiereuros.domain.model.MovieListItem
import rs.edu.raf.premiereuros.domain.model.PostedQuizResult
import rs.edu.raf.premiereuros.domain.model.QuizResult
import rs.edu.raf.premiereuros.domain.model.ShowtimePage
import rs.edu.raf.premiereuros.domain.model.ShowtimeUser
import rs.edu.raf.premiereuros.domain.repository.ShowtimeRepository
import rs.edu.raf.premiereuros.data.remote.mappers.toDomain as toMovieDomain

class RemoteShowtimeRepository(
    private val showtimeApi: ShowtimeApi,
    private val movieDao: MovieDao
) : ShowtimeRepository {

    override suspend fun getMe(): ShowtimeUser {
        return showtimeApi.getMe().toDomain()
    }

    override suspend fun getFavorites(): List<MovieListItem> {
        val syncError = runCatching { syncFavoritesFromRemote() }.exceptionOrNull()
        val local = movieDao.getFavoriteMoviesWithGenres().map { it.toListItem() }
        if (local.isNotEmpty() || syncError == null) return local
        throw syncError
    }

    override suspend fun isFavorite(movieId: String): Boolean {
        return movieDao.isFavorite(movieId)
    }

    override suspend fun addFavorite(movieId: String): MovieListItem {
        movieDao.addToFavorites(FavoriteMovieEntity(movieId, latestOrderingStamp()))

        return runCatching {
            val remoteMovie = showtimeApi.addFavorite(movieId).toMovieDomain()
            syncMoviesIntoCache(listOf(remoteMovie))
            remoteMovie
        }.getOrElse { error ->
            movieDao.removeFromFavorites(movieId)
            throw error
        }
    }

    override suspend fun removeFavorite(movieId: String) {
        movieDao.removeFromFavorites(movieId)
        runCatching {
            showtimeApi.removeFavorite(movieId)
        }.getOrElse { error ->
            movieDao.addToFavorites(FavoriteMovieEntity(movieId, latestOrderingStamp()))
            throw error
        }
    }

    override suspend fun getWatchlist(): List<MovieListItem> {
        val syncError = runCatching { syncWatchlistFromRemote() }.exceptionOrNull()
        val local = movieDao.getWatchlistMoviesWithGenres().map { it.toListItem() }
        if (local.isNotEmpty() || syncError == null) return local
        throw syncError
    }

    override suspend fun isInWatchlist(movieId: String): Boolean {
        return movieDao.isInWatchlist(movieId)
    }

    override suspend fun addWatchlist(movieId: String): MovieListItem {
        movieDao.addToWatchlist(WatchlistMovieEntity(movieId, latestOrderingStamp()))

        return runCatching {
            val remoteMovie = showtimeApi.addWatchlist(movieId).toMovieDomain()
            syncMoviesIntoCache(listOf(remoteMovie))
            remoteMovie
        }.getOrElse { error ->
            movieDao.removeFromWatchlist(movieId)
            throw error
        }
    }

    override suspend fun removeWatchlist(movieId: String) {
        movieDao.removeFromWatchlist(movieId)
        runCatching {
            showtimeApi.removeWatchlist(movieId)
        }.getOrElse { error ->
            movieDao.addToWatchlist(WatchlistMovieEntity(movieId, latestOrderingStamp()))
            throw error
        }
    }

    override suspend fun getLeaderboard(
        category: Int,
        page: Int,
        pageSize: Int
    ): ShowtimePage<LeaderboardEntry> {
        return showtimeApi
            .getLeaderboard(category, page, pageSize)
            .toDomain { it.toDomain() }
    }

    override suspend fun postLeaderboard(
        score: Float,
        category: Int
    ): PostedQuizResult {
        return showtimeApi
            .postLeaderboard(
                request = PostLeaderboardRequestDto(
                    score = score,
                    category = category
                )
            )
            .toDomain()
    }

    override suspend fun getMyQuizResults(
        page: Int,
        pageSize: Int
    ): ShowtimePage<QuizResult> {
        return showtimeApi
            .getMyQuizResults(page, pageSize)
            .toDomain { it.toDomain() }
    }

    private suspend fun syncFavoritesFromRemote() {
        val remoteMovies = showtimeApi.getFavorites().map { it.toMovieDomain() }
        syncMoviesIntoCache(remoteMovies)

        movieDao.clearFavorites()
        if (remoteMovies.isNotEmpty()) {
            movieDao.upsertFavorites(
                remoteMovies.mapIndexed { index, movie ->
                    FavoriteMovieEntity(
                        imdbId = movie.imdbId,
                        addedAtEpochMillis = (remoteMovies.size - index).toLong()
                    )
                }
            )
        }
    }

    private suspend fun syncWatchlistFromRemote() {
        val remoteMovies = showtimeApi.getWatchlist().map { it.toMovieDomain() }
        syncMoviesIntoCache(remoteMovies)

        movieDao.clearWatchlist()
        if (remoteMovies.isNotEmpty()) {
            movieDao.upsertWatchlist(
                remoteMovies.mapIndexed { index, movie ->
                    WatchlistMovieEntity(
                        imdbId = movie.imdbId,
                        addedAtEpochMillis = (remoteMovies.size - index).toLong()
                    )
                }
            )
        }
    }

    private suspend fun syncMoviesIntoCache(movies: List<MovieListItem>) {
        if (movies.isEmpty()) return

        movieDao.upsertMovies(movies.map { it.toEntity() })
        movieDao.replaceMovieGenres(
            movies.associate { movie ->
                movie.imdbId to movie.genres.map { it.toEntity() }
            }
        )
    }

    private fun latestOrderingStamp(): Long = Long.MAX_VALUE
}
