package rs.edu.raf.premiereuros.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import rs.edu.raf.premiereuros.domain.model.Genre
import rs.edu.raf.premiereuros.domain.model.ImageConfig
import rs.edu.raf.premiereuros.domain.model.MovieCastMember
import rs.edu.raf.premiereuros.domain.model.MovieDetails
import rs.edu.raf.premiereuros.domain.model.MovieFilter
import rs.edu.raf.premiereuros.domain.model.MovieImage
import rs.edu.raf.premiereuros.domain.model.MovieListItem
import rs.edu.raf.premiereuros.domain.model.MovieVideo
import rs.edu.raf.premiereuros.domain.model.SortOption

interface PremiereRepository {
    val appliedFilter: StateFlow<MovieFilter>
    fun observeMovies(sort: SortOption): Flow<List<MovieListItem>>
    fun observeGenres(): Flow<List<Genre>>

    suspend fun setAppliedFilter(filter: MovieFilter)
    suspend fun getMovies(sort: SortOption): List<MovieListItem>
    suspend fun getGenres(): List<Genre>
    suspend fun getImageConfig(): ImageConfig
    suspend fun getMovieDetails(imdbId: String): MovieDetails?
    suspend fun getMovieCast(imdbId: String): List<MovieCastMember>
    suspend fun getMovieImages(imdbId: String): List<MovieImage>
    suspend fun getMovieVideos(imdbId: String): List<MovieVideo>
}
