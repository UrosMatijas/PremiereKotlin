package rs.edu.raf.premiereuros.core.auth

sealed interface AuthState {
    data object LoggedOut : AuthState
    data class LoggedIn(val token: String) : AuthState
}
