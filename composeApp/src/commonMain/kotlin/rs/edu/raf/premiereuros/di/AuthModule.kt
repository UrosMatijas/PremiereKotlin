package rs.edu.raf.premiereuros.di

import org.koin.dsl.module
import rs.edu.raf.premiereuros.core.auth.SessionDataCleaner
import rs.edu.raf.premiereuros.core.auth.SessionManager
import rs.edu.raf.premiereuros.core.auth.TokenStorage
import rs.edu.raf.premiereuros.core.auth.createTokenStorage
import rs.edu.raf.premiereuros.data.local.SessionDataCleanerImpl

val authModule = module {
    single<TokenStorage> { createTokenStorage() }
    single<SessionDataCleaner> { SessionDataCleanerImpl(get()) }
    single { SessionManager(get(), get()) }
}
