package rs.edu.raf.premiereuros.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import rs.edu.raf.premiereuros.core.db.entity.FavoriteMovieEntity
import rs.edu.raf.premiereuros.core.db.entity.GenreEntity
import rs.edu.raf.premiereuros.core.db.entity.MovieCastEntity
import rs.edu.raf.premiereuros.core.db.entity.MovieEntity
import rs.edu.raf.premiereuros.core.db.entity.MovieGenreCrossRefEntity
import rs.edu.raf.premiereuros.core.db.entity.MovieImageEntity
import rs.edu.raf.premiereuros.core.db.entity.MovieVideoEntity
import rs.edu.raf.premiereuros.core.db.entity.QuizSessionEntity
import rs.edu.raf.premiereuros.core.db.entity.QuizStatsEntity
import rs.edu.raf.premiereuros.core.db.entity.WatchlistMovieEntity
import rs.edu.raf.premiereuros.core.db.model.MovieWithGenres

@Dao
interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMovies(movies: List<MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGenres(genres: List<GenreEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMovieGenreCrossRefs(crossRefs: List<MovieGenreCrossRefEntity>)

    @Query("DELETE FROM movie_genre_cross_refs WHERE movieImdbId IN (:movieImdbIds)")
    suspend fun deleteGenreRefsForMovies(movieImdbIds: List<String>)

    @Transaction
    @Query("SELECT * FROM movies")
    suspend fun getMoviesWithGenres(): List<MovieWithGenres>

    @Transaction
    @Query("SELECT * FROM movies")
    fun observeMoviesWithGenres(): Flow<List<MovieWithGenres>>

    @Query("SELECT * FROM genres ORDER BY name ASC")
    suspend fun getAllGenres(): List<GenreEntity>

    @Query("SELECT * FROM genres ORDER BY name ASC")
    fun observeAllGenres(): Flow<List<GenreEntity>>

    @Transaction
    @Query("SELECT * FROM movies WHERE imdbId = :imdbId LIMIT 1")
    suspend fun getMovieWithGenres(imdbId: String): MovieWithGenres?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCast(cast: List<MovieCastEntity>)

    @Query("DELETE FROM movie_cast WHERE movieImdbId = :imdbId")
    suspend fun clearCastForMovie(imdbId: String)

    @Query("SELECT * FROM movie_cast WHERE movieImdbId = :imdbId ORDER BY name ASC")
    suspend fun getCastForMovie(imdbId: String): List<MovieCastEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertImages(images: List<MovieImageEntity>)

    @Query("DELETE FROM movie_images WHERE movieImdbId = :imdbId")
    suspend fun clearImagesForMovie(imdbId: String)

    @Query("SELECT * FROM movie_images WHERE movieImdbId = :imdbId")
    suspend fun getImagesForMovie(imdbId: String): List<MovieImageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVideos(videos: List<MovieVideoEntity>)

    @Query("DELETE FROM movie_videos WHERE movieImdbId = :imdbId")
    suspend fun clearVideosForMovie(imdbId: String)

    @Query("SELECT * FROM movie_videos WHERE movieImdbId = :imdbId")
    suspend fun getVideosForMovie(imdbId: String): List<MovieVideoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavorites(movie: FavoriteMovieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFavorites(movies: List<FavoriteMovieEntity>)

    @Query("DELETE FROM favorite_movies WHERE imdbId = :imdbId")
    suspend fun removeFromFavorites(imdbId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_movies WHERE imdbId = :imdbId)")
    suspend fun isFavorite(imdbId: String): Boolean

    @Query("DELETE FROM favorite_movies")
    suspend fun clearFavorites()

    @Query("SELECT COUNT(*) FROM favorite_movies")
    suspend fun getFavoriteCount(): Int

    @Transaction
    @Query(
        """
        SELECT m.* FROM movies m
        INNER JOIN favorite_movies f ON m.imdbId = f.imdbId
        ORDER BY f.addedAtEpochMillis DESC
        """
    )
    suspend fun getFavoriteMoviesWithGenres(): List<MovieWithGenres>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(movie: WatchlistMovieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatchlist(movies: List<WatchlistMovieEntity>)

    @Query("DELETE FROM watchlist_movies WHERE imdbId = :imdbId")
    suspend fun removeFromWatchlist(imdbId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_movies WHERE imdbId = :imdbId)")
    suspend fun isInWatchlist(imdbId: String): Boolean

    @Query("DELETE FROM watchlist_movies")
    suspend fun clearWatchlist()

    @Query("SELECT COUNT(*) FROM watchlist_movies")
    suspend fun getWatchlistCount(): Int

    @Transaction
    @Query(
        """
        SELECT m.* FROM movies m
        INNER JOIN watchlist_movies w ON m.imdbId = w.imdbId
        ORDER BY w.addedAtEpochMillis DESC
        """
    )
    suspend fun getWatchlistMoviesWithGenres(): List<MovieWithGenres>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuizStats(stats: QuizStatsEntity)

    @Query("SELECT * FROM quiz_stats WHERE id = 1 LIMIT 1")
    suspend fun getQuizStats(): QuizStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizSession(session: QuizSessionEntity)

    @Query("SELECT * FROM quiz_sessions ORDER BY createdAtEpochMillis DESC")
    suspend fun getQuizSessions(): List<QuizSessionEntity>

    @Query("SELECT COUNT(*) FROM movies WHERE posterPath IS NOT NULL OR backdropPath IS NOT NULL")
    suspend fun getMovieCountWithAtLeastOneImage(): Int

    @Transaction
    suspend fun replaceMovieGenres(movieIdToGenres: Map<String, List<GenreEntity>>) {
        val movieIds = movieIdToGenres.keys.toList()
        if (movieIds.isEmpty()) return

        upsertGenres(movieIdToGenres.values.flatten().distinctBy { it.id })
        deleteGenreRefsForMovies(movieIds)

        val refs = buildList {
            movieIdToGenres.forEach { (movieId, genres) ->
                genres.forEach { genre ->
                    add(
                        MovieGenreCrossRefEntity(
                            movieImdbId = movieId,
                            genreId = genre.id
                        )
                    )
                }
            }
        }
        upsertMovieGenreCrossRefs(refs)
    }
}
