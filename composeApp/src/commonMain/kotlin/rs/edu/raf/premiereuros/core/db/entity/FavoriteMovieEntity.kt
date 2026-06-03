package rs.edu.raf.premiereuros.core.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_movies")
data class FavoriteMovieEntity(
    @PrimaryKey val imdbId: String,
    val addedAtEpochMillis: Long
)
