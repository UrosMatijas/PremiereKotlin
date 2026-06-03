package rs.edu.raf.premiereuros.core.db.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import rs.edu.raf.premiereuros.core.db.entity.GenreEntity
import rs.edu.raf.premiereuros.core.db.entity.MovieEntity
import rs.edu.raf.premiereuros.core.db.entity.MovieGenreCrossRefEntity

data class MovieWithGenres(
    @Embedded val movie: MovieEntity,
    @Relation(
        parentColumn = "imdbId",
        entityColumn = "id",
        associateBy = Junction(
            value = MovieGenreCrossRefEntity::class,
            parentColumn = "movieImdbId",
            entityColumn = "genreId"
        )
    )
    val genres: List<GenreEntity>
)
