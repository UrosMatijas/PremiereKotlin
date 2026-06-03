package rs.edu.raf.premiereuros.domain.model

data class MovieListItem(
    val imdbId: String,
    val title: String,
    val year: Int?,
    val runtime: Int? = null,
    val imdbRating: Float?,
    val imdbVotes: Int?,
    val posterPath: String?,
    val genres: List<Genre>
)