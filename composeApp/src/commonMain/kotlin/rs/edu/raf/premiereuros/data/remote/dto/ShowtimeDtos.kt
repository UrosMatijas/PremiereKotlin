package rs.edu.raf.premiereuros.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignupRequestDto(
    @SerialName("full_name") val fullName: String,
    @SerialName("username") val username: String,
    @SerialName("password") val password: String
)

@Serializable
data class LoginRequestDto(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String
)

@Serializable
data class AuthResponseDto(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("expires_in") val expiresIn: Long? = null,
    @SerialName("user") val user: UserDto? = null
)

@Serializable
data class UserDto(
    @SerialName("id") val id: Int,
    @SerialName("username") val username: String,
    @SerialName("full_name") val fullName: String
)

@Serializable
data class LeaderboardEntryDto(
    @SerialName("rank") val rank: Int,
    @SerialName("user_id") val userId: Int,
    @SerialName("username") val username: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("score") val score: Float,
    @SerialName("played_at") val playedAt: Long,
    @SerialName("total_plays") val totalPlays: Int
)

@Serializable
data class PostLeaderboardRequestDto(
    @SerialName("score") val score: Float,
    @SerialName("category") val category: Int
)

@Serializable
data class QuizResultDto(
    @SerialName("id") val id: Long,
    @SerialName("category") val category: Int,
    @SerialName("score") val score: Float,
    @SerialName("played_at") val playedAt: Long
)

@Serializable
data class PostQuizResultResponseDto(
    @SerialName("result") val result: QuizResultDto,
    @SerialName("ranking") val ranking: Int
)

@Serializable
data class ShowtimePaginatedResponseDto<T>(
    @SerialName("page") val page: Int,
    @SerialName("page_size") val pageSize: Int,
    @SerialName("total_items") val totalItems: Int,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("items") val items: List<T>
)
