package rs.edu.raf.premiereuros.data.remote.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class MovieVideoDto(
    val key: String,
    val site: String,
    val name: String? = null,
    val type: String? = null,
    val official: Boolean = false,
    @SerialName("publishedAt")
    @JsonNames("published_at")
    val publishedAt: String? = null
)
