package rs.edu.raf.premiereuros.core.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import rs.edu.raf.premiereuros.core.db.dao.MovieDao
import rs.edu.raf.premiereuros.core.db.entity.FavoriteMovieEntity
import rs.edu.raf.premiereuros.core.db.entity.GenreEntity
import rs.edu.raf.premiereuros.core.db.entity.MovieCastEntity
import rs.edu.raf.premiereuros.core.db.entity.MovieEntity
import rs.edu.raf.premiereuros.core.db.entity.MovieGenreCrossRefEntity
import rs.edu.raf.premiereuros.core.db.entity.MovieImageEntity
import rs.edu.raf.premiereuros.core.db.entity.MovieVideoEntity
import rs.edu.raf.premiereuros.core.db.entity.QuizSessionEntity
import rs.edu.raf.premiereuros.core.db.entity.QuizStatsEntity
import rs.edu.raf.premiereuros.core.db.entity.WatchlistMovieEntity

@Database(
    entities = [
        MovieEntity::class,
        GenreEntity::class,
        MovieGenreCrossRefEntity::class,
        MovieCastEntity::class,
        MovieImageEntity::class,
        MovieVideoEntity::class,
        FavoriteMovieEntity::class,
        WatchlistMovieEntity::class,
        QuizStatsEntity::class,
        QuizSessionEntity::class
    ],
    version = 2,
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
