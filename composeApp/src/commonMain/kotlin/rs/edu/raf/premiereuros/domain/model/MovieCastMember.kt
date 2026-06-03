package rs.edu.raf.premiereuros.domain.model

data class MovieCastMember(
    val imdbId: String,
    val name: String,
    val department: String?,
    val profilePath: String?
)