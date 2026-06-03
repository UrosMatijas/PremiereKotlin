package rs.edu.raf.premiereuros.core.db.entity

import androidx.room.Entity

@Entity(
    tableName = "movie_cast",
    primaryKeys = ["movieImdbId", "personImdbId"]
)
data class MovieCastEntity(
    val movieImdbId: String,
    val personImdbId: String,
    val name: String,
    val department: String?,
    val profilePath: String?
)
