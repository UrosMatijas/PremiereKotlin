package rs.edu.raf.premiereuros.navigation

import kotlinx.serialization.Serializable

@Serializable
data object AuthLandingRoute

@Serializable
data object LoginRoute

@Serializable
data object SignupRoute

@Serializable
data object ProfileRoute

@Serializable
data object FavoritesRoute

@Serializable
data object WatchlistRoute

@Serializable
data object QuizRoute

@Serializable
data class QuizResultRoute(
    val score: Float,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val usedSeconds: Int,
    val remainingSeconds: Int
)

@Serializable
data object PremiereListRoute

@Serializable
data object PremiereFilterRoute

@Serializable
data class PremiereDetailsRoute(
    val imdbId: String
)
