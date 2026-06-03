package rs.edu.raf.premiereuros.core.auth

import kotlinx.coroutines.flow.StateFlow

interface TokenStorage {
    val tokenFlow: StateFlow<String?>

    suspend fun saveToken(token: String)
    suspend fun clearToken()
}
