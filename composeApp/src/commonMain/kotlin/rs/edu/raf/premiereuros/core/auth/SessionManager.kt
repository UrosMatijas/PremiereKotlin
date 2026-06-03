package rs.edu.raf.premiereuros.core.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class SessionManager(
    private val tokenStorage: TokenStorage,
    private val sessionDataCleaner: SessionDataCleaner
) {
    val tokenFlow: StateFlow<String?> = tokenStorage.tokenFlow
    val authStateFlow: Flow<AuthState> = tokenFlow.map { token ->
        if (token.isNullOrBlank()) {
            AuthState.LoggedOut
        } else {
            AuthState.LoggedIn(token)
        }
    }

    private val _events = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SessionEvent> = _events

    fun currentToken(): String? = tokenFlow.value

    suspend fun onLogin(token: String) {
        tokenStorage.saveToken(token)
        _events.emit(SessionEvent.LoggedIn)
    }

    suspend fun logout() {
        tokenStorage.clearToken()
        sessionDataCleaner.clearUserScopedData()
        _events.emit(SessionEvent.LoggedOut)
    }

    suspend fun forceLogoutUnauthorized() {
        tokenStorage.clearToken()
        sessionDataCleaner.clearUserScopedData()
        _events.emit(SessionEvent.ForcedLogoutUnauthorized)
    }
}

sealed interface SessionEvent {
    data object LoggedIn : SessionEvent
    data object LoggedOut : SessionEvent
    data object ForcedLogoutUnauthorized : SessionEvent
}
