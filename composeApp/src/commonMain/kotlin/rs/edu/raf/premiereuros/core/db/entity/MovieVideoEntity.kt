package rs.edu.raf.premiereuros.core.db.entity

import androidx.room.Entity

@Entity(
    tableName = "movie_videos",
    primaryKeys = ["movieImdbId", "videoKey"]
)
data class MovieVideoEntity(
    val movieImdbId: String,
    val videoKey: String,
    val site: String,
    val name: String?,
    val type: String?,
    val official: Boolean
)
