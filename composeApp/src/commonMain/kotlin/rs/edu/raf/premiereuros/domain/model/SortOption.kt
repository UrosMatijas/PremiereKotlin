package rs.edu.raf.premiereuros.domain.model

enum class SortOption(val apiValue: String) {
    Rating("imdb_rating"),
    Year("year"),
    Title("title"),
    Popularity("popularity")
}