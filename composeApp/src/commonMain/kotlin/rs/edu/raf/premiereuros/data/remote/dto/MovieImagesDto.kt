package rs.edu.raf.premiereuros.data.remote.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class MovieImagesDto(
    val posters: List<MovieImageDto> = emptyList(),
    val backdrops: List<MovieImageDto> = emptyList(),
    val logos: List<MovieImageDto> = emptyList()
)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class MovieImageDto(
    @SerialName("filePath")
    @JsonNames("file_path")
    val filePath: String,
    val width: Int? = null,
    val height: Int? = null,
    @SerialName("voteAverage")
    @JsonNames("vote_average")
    val voteAverage: Float? = null,
    val language: String? = null
)
