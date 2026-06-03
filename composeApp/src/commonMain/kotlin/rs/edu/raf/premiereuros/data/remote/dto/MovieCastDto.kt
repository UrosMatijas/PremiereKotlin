package rs.edu.raf.premiereuros.data.remote.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class MovieCastMemberDto(
    @SerialName("imdbId")
    @JsonNames("imdb_id")
    val imdbId: String,
    val name: String,
    val professions: String? = null,
    val department: String? = null,
    @SerialName("profilePath")
    @JsonNames("profile_path")
    val profilePath: String? = null
)
