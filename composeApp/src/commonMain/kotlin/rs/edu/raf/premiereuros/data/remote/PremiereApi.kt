package rs.edu.raf.premiereuros.data.remote

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import rs.edu.raf.premiereuros.data.remote.dto.ConfigResponseDto
import rs.edu.raf.premiereuros.data.remote.dto.GenreDto
import rs.edu.raf.premiereuros.data.remote.dto.MovieCastMemberDto
import rs.edu.raf.premiereuros.data.remote.dto.MovieDetailsDto
import rs.edu.raf.premiereuros.data.remote.dto.MovieImagesDto
import rs.edu.raf.premiereuros.data.remote.dto.MovieListItemDto
import rs.edu.raf.premiereuros.data.remote.dto.MovieVideoDto
import rs.edu.raf.premiereuros.data.remote.dto.PaginatedResponseDto

interface PremiereApi {

    @GET("movies")
    suspend fun getMovies(
        @Query("page_size") pageSize: Int = 30,
        @Query("sort_by") sortBy: String,
        @Query("sort_order") sortOrder: String = "desc",
        @Query("genre_id") genreId: Int? = null,
        @Query("query") query: String? = null,
        @Query("min_year") minYear: Int? = null,
        @Query("max_year") maxYear: Int? = null,
        @Query("min_rating") minRating: Float? = null
    ): PaginatedResponseDto<MovieListItemDto>

    @GET("movies/{id}")
    suspend fun getMovieDetails(
        @Path("id") imdbId: String
    ): MovieDetailsDto

    @GET("movies/{id}/cast")
    suspend fun getMovieCast(
        @Path("id") imdbId: String,
        @Query("page_size") pageSize: Int = 10
    ): PaginatedResponseDto<MovieCastMemberDto>

    @GET("movies/{id}/images")
    suspend fun getMovieImages(
        @Path("id") imdbId: String,
        @Query("type") type: String = "backdrop"
    ): MovieImagesDto

    @GET("movies/{id}/videos")
    suspend fun getMovieVideos(
        @Path("id") imdbId: String,
        @Query("type") type: String = "Trailer"
    ): List<MovieVideoDto>

    @GET("genres")
    suspend fun getGenres(): List<GenreDto>

    @GET("config")
    suspend fun getConfig(): ConfigResponseDto
}
