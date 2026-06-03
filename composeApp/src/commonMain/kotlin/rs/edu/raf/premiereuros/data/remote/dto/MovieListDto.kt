package rs.edu.raf.premiereuros.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNames

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class MovieListItemDto(
    @SerialName("imdbId")
    @JsonNames("imdb_id")
    val imdbId: String,
    @SerialName("title")
    val title: String,
    @SerialName("year")
    val year: Int? = null,
    @SerialName("imdbRating")
    @JsonNames("imdb_rating")
    val imdbRating: Float? = null,
    @SerialName("imdbVotes")
    @JsonNames("imdb_votes")
    val imdbVotes: Int? = null,
    @SerialName("posterPath")
    @JsonNames("poster_path")
    val posterPath: String? = null,
    @SerialName("genres")
    val genres: List<GenreDto> = emptyList()
)
