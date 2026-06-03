package rs.edu.raf.premiereuros.domain.repository

interface AuthRepository {
    suspend fun login(
        username: String,
        password: String
    ): AuthResult

    suspend fun signup(
        fullName: String,
        username: String,
        password: String
    ): AuthResult
}

sealed interface AuthResult {
    data class Success(val token: String) : AuthResult
    data class Failure(val reason: AuthFailure) : AuthResult
}

sealed interface AuthFailure {
    data object InvalidCredentials : AuthFailure
    data object UsernameTaken : AuthFailure
    data object Network : AuthFailure
    data class Unknown(val message: String) : AuthFailure
}
