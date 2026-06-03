package rs.edu.raf.premiereuros.data.remote

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import rs.edu.raf.premiereuros.data.remote.dto.AuthResponseDto
import rs.edu.raf.premiereuros.data.remote.dto.LeaderboardEntryDto
import rs.edu.raf.premiereuros.data.remote.dto.LoginRequestDto
import rs.edu.raf.premiereuros.data.remote.dto.MovieListItemDto
import rs.edu.raf.premiereuros.data.remote.dto.PostLeaderboardRequestDto
import rs.edu.raf.premiereuros.data.remote.dto.PostQuizResultResponseDto
import rs.edu.raf.premiereuros.data.remote.dto.QuizResultDto
import rs.edu.raf.premiereuros.data.remote.dto.ShowtimePaginatedResponseDto
import rs.edu.raf.premiereuros.data.remote.dto.SignupRequestDto
import rs.edu.raf.premiereuros.data.remote.dto.UserDto

interface ShowtimeApi {

    @Headers("Content-Type: application/json")
    @POST("auth/signup")
    suspend fun signup(
        @Body request: SignupRequestDto
    ): AuthResponseDto

    @Headers("Content-Type: application/json")
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): AuthResponseDto

    @GET("me")
    suspend fun getMe(): UserDto

    @GET("me/favorites")
    suspend fun getFavorites(): List<MovieListItemDto>

    @POST("me/favorites/{movie_id}")
    suspend fun addFavorite(
        @Path("movie_id") movieId: String
    ): MovieListItemDto

    @DELETE("me/favorites/{movie_id}")
    suspend fun removeFavorite(
        @Path("movie_id") movieId: String
    )

    @GET("me/watchlist")
    suspend fun getWatchlist(): List<MovieListItemDto>

    @POST("me/watchlist/{movie_id}")
    suspend fun addWatchlist(
        @Path("movie_id") movieId: String
    ): MovieListItemDto

    @DELETE("me/watchlist/{movie_id}")
    suspend fun removeWatchlist(
        @Path("movie_id") movieId: String
    )

    @GET("leaderboard")
    suspend fun getLeaderboard(
        @Query("category") category: Int = 1,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): ShowtimePaginatedResponseDto<LeaderboardEntryDto>

    @Headers("Content-Type: application/json")
    @POST("leaderboard")
    suspend fun postLeaderboard(
        @Body request: PostLeaderboardRequestDto
    ): PostQuizResultResponseDto

    @GET("me/quiz-results")
    suspend fun getMyQuizResults(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): ShowtimePaginatedResponseDto<QuizResultDto>
}
