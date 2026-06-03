package rs.edu.raf.premiereuros.core.auth

interface SessionDataCleaner {
    suspend fun clearUserScopedData()
}
