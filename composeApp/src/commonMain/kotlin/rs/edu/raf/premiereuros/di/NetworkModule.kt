package rs.edu.raf.premiereuros.di

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import rs.edu.raf.premiereuros.core.auth.SessionManager
import rs.edu.raf.premiereuros.data.remote.PremiereApi
import rs.edu.raf.premiereuros.data.remote.ShowtimeApi
import rs.edu.raf.premiereuros.data.remote.createPremiereApiClient
import rs.edu.raf.premiereuros.data.remote.createShowtimeApiClient

val appNetworkingModule = module {

    single<Json> {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    single<HttpClient> {
        val sessionManager: SessionManager = get()

        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(get<Json>())
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 15_000L
                connectTimeoutMillis = 10_000L
                socketTimeoutMillis = 15_000L
            }

            install(DefaultRequest) {
                sessionManager.currentToken()?.let { token ->
                    headers.remove(HttpHeaders.Authorization)
                    headers.append(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            HttpResponseValidator {
                handleResponseExceptionWithRequest { cause, _ ->
                    val responseException = cause as? ResponseException ?: return@handleResponseExceptionWithRequest
                    if (responseException.response.status == HttpStatusCode.Unauthorized) {
                        sessionManager.forceLogoutUnauthorized()
                    }
                }
            }
        }
    }

    single<Ktorfit> {
        Ktorfit.Builder()
            .baseUrl("https://rma.finlab.rs/")
            .httpClient(get<HttpClient>())
            .build()
    }

    single<PremiereApi> {
        createPremiereApiClient(get<Ktorfit>())
    }

    single<ShowtimeApi> {
        createShowtimeApiClient(get<Ktorfit>())
    }
}
