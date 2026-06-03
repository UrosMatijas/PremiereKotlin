package rs.edu.raf.premiereuros.domain.model

data class ImageConfig(
    val imageBaseUrl: String,
    val posterSizes: List<String>,
    val backdropSizes: List<String>,
    val profileSizes: List<String>
) {
    fun posterUrl(path: String?, size: String = "w185"): String? =
        path?.let { "$imageBaseUrl$size$it" }

    fun backdropUrl(path: String?, size: String = "w780"): String? =
        path?.let { "$imageBaseUrl$size$it" }

    fun profileUrl(path: String?, size: String = "w185"): String? =
        path?.let { "$imageBaseUrl$size$it" }
}