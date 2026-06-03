package rs.edu.raf.premiereuros.core.auth

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import rs.edu.raf.premiereuros.core.platform.requireApplicationContext

private const val AUTH_STORE_NAME = "showtime_auth"
private const val KEY_TOKEN = "auth_token"
private val TOKEN_KEY = stringPreferencesKey(KEY_TOKEN)

private val Context.authDataStore by preferencesDataStore(
    name = AUTH_STORE_NAME,
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, AUTH_STORE_NAME))
    }
)

private class DataStoreTokenStorage(
    context: Context
) : TokenStorage {
    private val dataStore = context.authDataStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _tokenFlow = MutableStateFlow(
        runBlocking {
            dataStore.data.first()[TOKEN_KEY]
        }
    )

    override val tokenFlow = _tokenFlow.asStateFlow()

    init {
        scope.launch {
            dataStore.data
                .map { prefs -> prefs[TOKEN_KEY] }
                .distinctUntilChanged()
                .collect { token ->
                    _tokenFlow.value = token
                }
        }
    }

    override suspend fun saveToken(token: String) {
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
        _tokenFlow.value = token
    }

    override suspend fun clearToken() {
        dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
        _tokenFlow.value = null
    }
}

actual fun createTokenStorage(): TokenStorage {
    return DataStoreTokenStorage(requireApplicationContext())
}
