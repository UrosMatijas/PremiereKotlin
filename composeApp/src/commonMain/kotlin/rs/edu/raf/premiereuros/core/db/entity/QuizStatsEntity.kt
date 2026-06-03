package rs.edu.raf.premiereuros.core.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_stats")
data class QuizStatsEntity(
    @PrimaryKey val id: Int = 1,
    val bestScore: Float = 0f,
    val playedCount: Int = 0
)
