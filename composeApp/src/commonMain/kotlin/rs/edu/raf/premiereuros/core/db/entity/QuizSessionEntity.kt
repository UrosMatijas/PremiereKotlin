package rs.edu.raf.premiereuros.core.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_sessions")
data class QuizSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val score: Float,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val usedSeconds: Int,
    val createdAtEpochMillis: Long
)
