package rs.edu.raf.premiereuros.core.db.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "movie_genre_cross_refs",
    indices = [Index("genreId")],
    primaryKeys = ["movieImdbId", "genreId"]
)
data class MovieGenreCrossRefEntity(
    val movieImdbId: String,
    val genreId: Int
)
