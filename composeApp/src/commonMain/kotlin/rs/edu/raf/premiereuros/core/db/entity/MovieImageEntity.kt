package rs.edu.raf.premiereuros.core.db.entity

import androidx.room.Entity

@Entity(
    tableName = "movie_images",
    primaryKeys = ["movieImdbId", "filePath"]
)
data class MovieImageEntity(
    val movieImdbId: String,
    val filePath: String,
    val width: Int?,
    val height: Int?
)
