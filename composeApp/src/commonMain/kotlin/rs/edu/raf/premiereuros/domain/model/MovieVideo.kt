package rs.edu.raf.premiereuros.domain.model

data class MovieVideo(
    val key: String,
    val site: String,
    val name: String?,
    val type: String?,
    val official: Boolean
) {
    val youtubeUrl: String?
        get() = if (site == "YouTube") {
            "https://www.youtube.com/watch?v=$key"
        } else {
            null
        }
}