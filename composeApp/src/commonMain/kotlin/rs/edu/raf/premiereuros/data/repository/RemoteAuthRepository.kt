package rs.edu.raf.premiereuros.data.repository

import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException
import rs.edu.raf.premiereuros.data.remote.ShowtimeApi
import rs.edu.raf.premiereuros.data.remote.dto.LoginRequestDto
import rs.edu.raf.premiereuros.data.remote.dto.SignupRequestDto
import rs.edu.raf.premiereuros.domain.repository.AuthFailure
import rs.edu.raf.premiereuros.domain.repository.AuthRepository
import rs.edu.raf.premiereuros.domain.repository.AuthResult

class RemoteAuthRepository(
    private val showtimeApi: ShowtimeApi
) : AuthRepository {

    override suspend fun login(
        username: String,
        password: String
    ): AuthResult {
        return try {
            val response = showtimeApi.login(
                LoginRequestDto(
                    username = username,
                    password = password
                )
            )
            response.accessToken?.let { token ->
                AuthResult.Success(token = token)
            } ?: AuthResult.Failure(AuthFailure.InvalidCredentials)
        } catch (error: Throwable) {
            error.toLoginFailure()
        }
    }

    override suspend fun signup(
        fullName: String,
        username: String,
        password: String
    ): AuthResult {
        return try {
            val response = showtimeApi.signup(
                SignupRequestDto(
                    fullName = fullName,
                    username = username,
                    password = password
                )
            )
            response.accessToken?.let { token ->
                AuthResult.Success(token = token)
            } ?: AuthResult.Failure(AuthFailure.UsernameTaken)
        } catch (error: Throwable) {
            error.toSignupFailure()
        }
    }
}

private fun Throwable.toLoginFailure(): AuthResult.Failure {
    val responseException = this as? ResponseException
    return when (responseException?.response?.status) {
        HttpStatusCode.Unauthorized -> AuthResult.Failure(AuthFailure.InvalidCredentials)
        null -> if (isNetworkFailure()) {
            AuthResult.Failure(AuthFailure.Network)
        } else {
            AuthResult.Failure(
                AuthFailure.Unknown(message = message ?: "Login failed.")
            )
        }
        else -> AuthResult.Failure(
            AuthFailure.Unknown(message = message ?: "Login failed.")
        )
    }
}

private fun Throwable.toSignupFailure(): AuthResult.Failure {
    val responseException = this as? ResponseException
    return when (responseException?.response?.status) {
        HttpStatusCode.Conflict -> AuthResult.Failure(AuthFailure.UsernameTaken)
        HttpStatusCode.BadRequest -> AuthResult.Failure(
            AuthFailure.Unknown("Validation failed. Please check input values.")
        )
        null -> if (isNetworkFailure()) {
            AuthResult.Failure(AuthFailure.Network)
        } else {
            AuthResult.Failure(
                AuthFailure.Unknown(message = message ?: "Signup failed.")
            )
        }
        else -> AuthResult.Failure(
            AuthFailure.Unknown(message = message ?: "Signup failed.")
        )
    }
}

private fun Throwable.isNetworkFailure(): Boolean {
    var current: Throwable? = this
    while (current != null) {
        if (current is IOException) return true
        current = current.cause
    }
    return false
}
