package rs.edu.raf.premiereuros.data.remote.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class MovieDetailsDto(
    @SerialName("imdbId")
    @JsonNames("imdb_id")
    val imdbId: String,
    @SerialName("tmdbId")
    @JsonNames("tmdb_id")
    val tmdbId: Int? = null,
    val title: String,
    @SerialName("originalTitle")
    @JsonNames("original_title")
    val originalTitle: String? = null,
    val overview: String? = null,
    val tagline: String? = null,
    @SerialName("releaseDate")
    @JsonNames("release_date")
    val releaseDate: String? = null,
    val year: Int? = null,
    val runtime: Int? = null,
    val budget: Long? = null,
    val revenue: Long? = null,
    @SerialName("languageCode")
    @JsonNames("language_code")
    val languageCode: String? = null,
    val popularity: Float? = null,
    @SerialName("imdbRating")
    @JsonNames("imdb_rating")
    val imdbRating: Float? = null,
    @SerialName("imdbVotes")
    @JsonNames("imdb_votes")
    val imdbVotes: Int? = null,
    @SerialName("tmdbRating")
    @JsonNames("tmdb_rating")
    val tmdbRating: Float? = null,
    @SerialName("tmdbVotes")
    @JsonNames("tmdb_votes")
    val tmdbVotes: Int? = null,
    @SerialName("posterPath")
    @JsonNames("poster_path")
    val posterPath: String? = null,
    @SerialName("backdropPath")
    @JsonNames("backdrop_path")
    val backdropPath: String? = null,
    val homepage: String? = null,
    val genres: List<GenreDto> = emptyList()
)
