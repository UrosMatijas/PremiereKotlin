package rs.edu.raf.premiereuros.di

import org.koin.dsl.module
import rs.edu.raf.premiereuros.core.db.AppDatabase
import rs.edu.raf.premiereuros.core.db.createAppDatabase

val appDatabaseModule = module {
    single<AppDatabase> { createAppDatabase() }
    single { get<AppDatabase>().movieDao() }
}
