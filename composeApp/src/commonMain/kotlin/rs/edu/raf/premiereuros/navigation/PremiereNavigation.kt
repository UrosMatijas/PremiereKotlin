package rs.edu.raf.premiereuros.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.koin.compose.koinInject
import rs.edu.raf.premiereuros.auth.landing.AuthLandingRoute as AuthLandingScreenRoute
import rs.edu.raf.premiereuros.auth.login.LoginRoute as LoginScreenRoute
import rs.edu.raf.premiereuros.auth.signup.SignupRoute as SignupScreenRoute
import rs.edu.raf.premiereuros.core.auth.SessionEvent
import rs.edu.raf.premiereuros.core.auth.SessionManager
import rs.edu.raf.premiereuros.details.PremiereDetailsScreenRoute
import rs.edu.raf.premiereuros.filter.PremiereFilterRoute as PremiereFilterScreenRoute
import rs.edu.raf.premiereuros.favorites.FavoritesRoute as FavoritesScreenRoute
import rs.edu.raf.premiereuros.list.PremiereListRoute as PremiereListScreenRoute
import rs.edu.raf.premiereuros.profile.ProfileRoute as ProfileScreenRoute
import rs.edu.raf.premiereuros.quiz.QuizResultRoute as QuizResultScreenRoute
import rs.edu.raf.premiereuros.quiz.QuizRoute as QuizScreenRoute
import rs.edu.raf.premiereuros.watchlist.WatchlistRoute as WatchlistScreenRoute

@Composable
fun PremiereNavigation() {
    val navController = rememberNavController()
    val sessionManager: SessionManager = koinInject()

    LaunchedEffect(sessionManager) {
        sessionManager.events.collect { event ->
            when (event) {
                SessionEvent.LoggedIn -> Unit
                SessionEvent.LoggedOut,
                SessionEvent.ForcedLogoutUnauthorized -> {
                    navController.navigate(AuthLandingRoute) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AuthLandingRoute
    ) {
        composable<AuthLandingRoute> {
            AuthLandingScreenRoute(
                onOpenLogin = {
                    navController.navigate(LoginRoute)
                },
                onOpenSignup = {
                    navController.navigate(SignupRoute)
                },
                onAlreadyLoggedIn = {
                    navController.navigate(PremiereListRoute) {
                        popUpTo<AuthLandingRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<LoginRoute> {
            LoginScreenRoute(
                onBack = { navController.popBackStack() },
                onOpenSignup = {
                    navController.navigate(SignupRoute)
                },
                onLoggedIn = {
                    navController.navigate(PremiereListRoute) {
                        popUpTo<AuthLandingRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<SignupRoute> {
            SignupScreenRoute(
                onBack = { navController.popBackStack() },
                onOpenLogin = {
                    navController.navigate(LoginRoute)
                },
                onSignedUp = {
                    navController.navigate(PremiereListRoute) {
                        popUpTo<AuthLandingRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<PremiereListRoute> {
            PremiereListScreenRoute(
                onMovieClick = { imdbId ->
                    navController.navigate(PremiereDetailsRoute(imdbId))
                },
                onFilterClick = {
                    navController.navigate(PremiereFilterRoute)
                },
                onProfileClick = {
                    navController.navigate(ProfileRoute)
                },
                onQuizClick = {
                    navController.navigate(QuizRoute)
                }
            )
        }

        composable<PremiereFilterRoute> {
            PremiereFilterScreenRoute(
                onBack = { navController.popBackStack() }
            )
        }

        composable<PremiereDetailsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<PremiereDetailsRoute>()

            PremiereDetailsScreenRoute(
                imdbId = route.imdbId,
                onBack = { navController.popBackStack() }
            )
        }

        composable<ProfileRoute> {
            ProfileScreenRoute(
                onBack = { navController.popBackStack() },
                onOpenFavorites = {
                    navController.navigate(FavoritesRoute)
                },
                onOpenWatchlist = {
                    navController.navigate(WatchlistRoute)
                },
                onLoggedOut = {
                    navController.navigate(AuthLandingRoute) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<FavoritesRoute> {
            FavoritesScreenRoute(
                onBack = { navController.popBackStack() },
                onOpenDetails = { imdbId ->
                    navController.navigate(PremiereDetailsRoute(imdbId))
                }
            )
        }

        composable<WatchlistRoute> {
            WatchlistScreenRoute(
                onBack = { navController.popBackStack() },
                onOpenDetails = { imdbId ->
                    navController.navigate(PremiereDetailsRoute(imdbId))
                }
            )
        }

        composable<QuizRoute> {
            QuizScreenRoute(
                onBack = { navController.popBackStack() },
                onNavigateToResult = { result ->
                    navController.navigate(
                        QuizResultRoute(
                            score = result.score,
                            correctAnswers = result.correctAnswers,
                            incorrectAnswers = result.incorrectAnswers,
                            usedSeconds = result.usedSeconds,
                            remainingSeconds = result.remainingSeconds
                        )
                    )
                }
            )
        }

        composable<QuizResultRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<QuizResultRoute>()
            QuizResultScreenRoute(
                score = route.score,
                correctAnswers = route.correctAnswers,
                incorrectAnswers = route.incorrectAnswers,
                usedSeconds = route.usedSeconds,
                remainingSeconds = route.remainingSeconds,
                onDone = {
                    navController.navigate(PremiereListRoute) {
                        popUpTo<PremiereListRoute> { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
