package rs.edu.raf.premiereuros.di

import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module
import rs.edu.raf.premiereuros.auth.login.LoginViewModel
import rs.edu.raf.premiereuros.auth.signup.SignupViewModel
import rs.edu.raf.premiereuros.data.repository.RemoteAuthRepository
import rs.edu.raf.premiereuros.data.repository.LocalQuizRepository
import rs.edu.raf.premiereuros.data.repository.RemotePremiereRepository
import rs.edu.raf.premiereuros.data.repository.RemoteShowtimeRepository
import rs.edu.raf.premiereuros.details.PremiereDetailsViewModel
import rs.edu.raf.premiereuros.domain.repository.AuthRepository
import rs.edu.raf.premiereuros.domain.repository.PremiereRepository
import rs.edu.raf.premiereuros.domain.repository.QuizRepository
import rs.edu.raf.premiereuros.domain.repository.ShowtimeRepository
import rs.edu.raf.premiereuros.filter.PremiereFilterViewModel
import rs.edu.raf.premiereuros.favorites.FavoritesViewModel
import rs.edu.raf.premiereuros.list.PremiereListViewModel
import rs.edu.raf.premiereuros.profile.ProfileViewModel
import rs.edu.raf.premiereuros.quiz.QuizViewModel
import rs.edu.raf.premiereuros.watchlist.WatchlistViewModel

private val repositoryModule = module {
    single { RemotePremiereRepository(get(), get()) } bind PremiereRepository::class
    single { RemoteAuthRepository(get()) } bind AuthRepository::class
    single { RemoteShowtimeRepository(get(), get()) } bind ShowtimeRepository::class
    single { LocalQuizRepository(get(), get(), get()) } bind QuizRepository::class
}

private val viewModelModule = module {
    viewModelOf(::PremiereListViewModel)
    viewModelOf(::PremiereFilterViewModel)
    viewModelOf(::PremiereDetailsViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignupViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::FavoritesViewModel)
    viewModelOf(::WatchlistViewModel)
    viewModelOf(::QuizViewModel)
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            appDatabaseModule,
            authModule,
            appNetworkingModule,
            repositoryModule,
            viewModelModule
        )
    }
}
