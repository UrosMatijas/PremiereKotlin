package rs.edu.raf.premiereuros.domain.repository

import rs.edu.raf.premiereuros.domain.model.QuizLocalStats
import rs.edu.raf.premiereuros.domain.model.QuizResultSummary
import rs.edu.raf.premiereuros.domain.model.QuizSession

interface QuizRepository {
    suspend fun hasEnoughLocalMovies(minCount: Int = 10): Boolean
    suspend fun createSession(questionCount: Int = 10): QuizSession?
    suspend fun saveResult(summary: QuizResultSummary)
    suspend fun getLocalStats(): QuizLocalStats
}
