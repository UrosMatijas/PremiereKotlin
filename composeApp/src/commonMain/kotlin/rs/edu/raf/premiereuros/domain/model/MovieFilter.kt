package rs.edu.raf.premiereuros.domain.model

data class MovieFilter(
    val query: String = "",
    val selectedGenreId: Int? = null,
    val minYear: String = "",
    val maxYear: String = "",
    val minRating: Float = 0f
) {
    fun activeCount(): Int {
        var count = 0
        if (query.isNotBlank()) count++
        if (selectedGenreId != null) count++
        if (minYear.isNotBlank()) count++
        if (maxYear.isNotBlank()) count++
        if (minRating > 0f) count++
        return count
    }
}