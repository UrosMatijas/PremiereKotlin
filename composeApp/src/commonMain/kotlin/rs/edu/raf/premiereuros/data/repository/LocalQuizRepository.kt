package rs.edu.raf.premiereuros.data.repository

import kotlin.math.max
import kotlin.random.Random
import rs.edu.raf.premiereuros.core.db.dao.MovieDao
import rs.edu.raf.premiereuros.core.db.entity.QuizSessionEntity
import rs.edu.raf.premiereuros.core.db.entity.QuizStatsEntity
import rs.edu.raf.premiereuros.domain.model.QuizLocalStats
import rs.edu.raf.premiereuros.domain.model.QuizQuestion
import rs.edu.raf.premiereuros.domain.model.QuizQuestionType
import rs.edu.raf.premiereuros.domain.model.QuizResultSummary
import rs.edu.raf.premiereuros.domain.model.QuizSession
import rs.edu.raf.premiereuros.domain.repository.PremiereRepository
import rs.edu.raf.premiereuros.domain.repository.QuizRepository
import rs.edu.raf.premiereuros.domain.repository.ShowtimeRepository

class LocalQuizRepository(
    private val movieDao: MovieDao,
    private val premiereRepository: PremiereRepository,
    private val showtimeRepository: ShowtimeRepository
) : QuizRepository {

    override suspend fun hasEnoughLocalMovies(minCount: Int): Boolean {
        return movieDao.getMovieCountWithAtLeastOneImage() >= minCount
    }

    override suspend fun createSession(questionCount: Int): QuizSession? {
        val movies = movieDao.getMoviesWithGenres()
            .map { movie ->
                QuizMovieCandidate(
                    imdbId = movie.movie.imdbId,
                    title = movie.movie.title,
                    year = movie.movie.year,
                    posterPath = movie.movie.posterPath,
                    backdropPath = movie.movie.backdropPath
                )
            }
            .filter { it.imagePath() != null }
            .distinctBy { it.imdbId }

        if (movies.size < questionCount) return null

        val actorByMovie = buildActorPool(movies)
        val questions = buildQuestions(
            movies = movies,
            actorByMovie = actorByMovie,
            questionCount = questionCount
        )

        if (questions.size < questionCount) return null

        return QuizSession(questions = questions, totalSeconds = 60)
    }

    override suspend fun saveResult(summary: QuizResultSummary) {
        val currentStats = movieDao.getQuizStats() ?: QuizStatsEntity()
        val updatedStats = currentStats.copy(
            bestScore = max(currentStats.bestScore, summary.score),
            playedCount = currentStats.playedCount + 1
        )
        movieDao.upsertQuizStats(updatedStats)

        movieDao.insertQuizSession(
            QuizSessionEntity(
                score = summary.score,
                correctAnswers = summary.correctAnswers,
                incorrectAnswers = summary.incorrectAnswers,
                usedSeconds = summary.usedSeconds,
                createdAtEpochMillis = updatedStats.playedCount.toLong()
            )
        )

        runCatching {
            showtimeRepository.postLeaderboard(score = summary.score, category = 1)
        }
    }

    override suspend fun getLocalStats(): QuizLocalStats {
        val stats = movieDao.getQuizStats() ?: QuizStatsEntity()
        return QuizLocalStats(
            bestScore = stats.bestScore,
            playedCount = stats.playedCount
        )
    }

    private suspend fun buildActorPool(movies: List<QuizMovieCandidate>): Map<String, String> {
        val actorByMovie = mutableMapOf<String, String>()
        movies.shuffled().take(40).forEach { movie ->
            val cast = runCatching { premiereRepository.getMovieCast(movie.imdbId) }
                .getOrDefault(emptyList())
            val lead = cast.firstOrNull { it.name.isNotBlank() }?.name
            if (lead != null) {
                actorByMovie[movie.imdbId] = lead
            }
        }
        return actorByMovie
    }

    private fun buildQuestions(
        movies: List<QuizMovieCandidate>,
        actorByMovie: Map<String, String>,
        questionCount: Int
    ): List<QuizQuestion> {
        val usedMovieIds = mutableSetOf<String>()
        val usedImagePaths = mutableSetOf<String>()
        val typeCount = mutableMapOf(
            QuizQuestionType.GuessMovie to 0,
            QuizQuestionType.GuessYear to 0,
            QuizQuestionType.GuessLeadActor to 0
        )
        val questions = mutableListOf<QuizQuestion>()
        val blockedTypes = mutableSetOf<QuizQuestionType>()

        while (questions.size < questionCount) {
            val availableTypes = QuizQuestionType.entries
                .filter { it !in blockedTypes }
                .filter { (typeCount[it] ?: 0) < 4 }
            if (availableTypes.isEmpty()) break

            val chosenType = availableTypes.random()
            val nextQuestion = when (chosenType) {
                QuizQuestionType.GuessMovie -> buildGuessMovieQuestion(
                    movies = movies,
                    usedMovieIds = usedMovieIds,
                    usedImagePaths = usedImagePaths
                )

                QuizQuestionType.GuessYear -> buildGuessYearQuestion(
                    movies = movies,
                    usedMovieIds = usedMovieIds,
                    usedImagePaths = usedImagePaths
                )

                QuizQuestionType.GuessLeadActor -> buildGuessActorQuestion(
                    movies = movies,
                    actorByMovie = actorByMovie,
                    usedMovieIds = usedMovieIds,
                    usedImagePaths = usedImagePaths
                )
            }

            if (nextQuestion == null) {
                blockedTypes += chosenType
                continue
            }

            val q = nextQuestion.copy(id = questions.size + 1)
            questions += q
            usedMovieIds += q.movieImdbId
            usedImagePaths += q.imagePath
            typeCount[chosenType] = (typeCount[chosenType] ?: 0) + 1
        }

        return questions
    }

    private fun buildGuessMovieQuestion(
        movies: List<QuizMovieCandidate>,
        usedMovieIds: Set<String>,
        usedImagePaths: Set<String>
    ): QuizQuestion? {
        val candidate = movies.asSequence()
            .filter { it.imdbId !in usedMovieIds }
            .mapNotNull { movie ->
                val imagePath = movie.imagePath() ?: return@mapNotNull null
                if (imagePath in usedImagePaths) return@mapNotNull null
                movie to imagePath
            }
            .toList()
            .shuffled()
            .firstOrNull()
            ?: return null

        val correctMovie = candidate.first
        val imagePath = candidate.second
        val wrongTitles = movies
            .asSequence()
            .filter { it.imdbId != correctMovie.imdbId }
            .map { it.title }
            .distinct()
            .shuffled()
            .take(3)
            .toList()
        if (wrongTitles.size < 3) return null

        val options = (wrongTitles + correctMovie.title).shuffled()
        val correctIndex = options.indexOf(correctMovie.title)
        if (correctIndex < 0) return null

        return QuizQuestion(
            id = 0,
            type = QuizQuestionType.GuessMovie,
            movieImdbId = correctMovie.imdbId,
            imagePath = imagePath,
            questionText = "Guess the movie",
            titleHint = null,
            options = options,
            correctOptionIndex = correctIndex
        )
    }

    private fun buildGuessYearQuestion(
        movies: List<QuizMovieCandidate>,
        usedMovieIds: Set<String>,
        usedImagePaths: Set<String>
    ): QuizQuestion? {
        val candidate = movies.asSequence()
            .filter { it.imdbId !in usedMovieIds && it.year != null }
            .mapNotNull { movie ->
                val imagePath = movie.imagePath() ?: return@mapNotNull null
                if (imagePath in usedImagePaths) return@mapNotNull null
                movie to imagePath
            }
            .toList()
            .shuffled()
            .firstOrNull()
            ?: return null

        val correctMovie = candidate.first
        val imagePath = candidate.second
        val correctYear = correctMovie.year ?: return null

        val wrongYears = movies
            .asSequence()
            .mapNotNull { it.year }
            .filter { it != correctYear }
            .distinct()
            .shuffled()
            .take(3)
            .toMutableList()

        var delta = 1
        while (wrongYears.size < 3 && delta <= 10) {
            val candidateYearA = correctYear + delta
            val candidateYearB = correctYear - delta
            if (candidateYearA !in wrongYears && candidateYearA != correctYear) wrongYears += candidateYearA
            if (wrongYears.size < 3 && candidateYearB > 0 && candidateYearB !in wrongYears && candidateYearB != correctYear) {
                wrongYears += candidateYearB
            }
            delta += 1
        }
        if (wrongYears.size < 3) return null

        val options = (wrongYears.take(3).map { it.toString() } + correctYear.toString()).shuffled()
        val correctIndex = options.indexOf(correctYear.toString())
        if (correctIndex < 0) return null

        return QuizQuestion(
            id = 0,
            type = QuizQuestionType.GuessYear,
            movieImdbId = correctMovie.imdbId,
            imagePath = imagePath,
            questionText = "Guess the movie year",
            titleHint = correctMovie.title,
            options = options,
            correctOptionIndex = correctIndex
        )
    }

    private fun buildGuessActorQuestion(
        movies: List<QuizMovieCandidate>,
        actorByMovie: Map<String, String>,
        usedMovieIds: Set<String>,
        usedImagePaths: Set<String>
    ): QuizQuestion? {
        val actorCandidates = movies.asSequence()
            .filter { it.imdbId !in usedMovieIds && actorByMovie.containsKey(it.imdbId) }
            .mapNotNull { movie ->
                val imagePath = movie.imagePath() ?: return@mapNotNull null
                if (imagePath in usedImagePaths) return@mapNotNull null
                val actor = actorByMovie[movie.imdbId] ?: return@mapNotNull null
                Triple(movie, imagePath, actor)
            }
            .toList()
            .shuffled()
        val candidate = actorCandidates.firstOrNull() ?: return null

        val actorPool = actorByMovie.values.distinct()
        if (actorPool.size < 4) return null

        val wrongActors = actorPool
            .asSequence()
            .filter { it != candidate.third }
            .shuffled()
            .take(3)
            .toList()
        if (wrongActors.size < 3) return null

        val options = (wrongActors + candidate.third).shuffled()
        val correctIndex = options.indexOf(candidate.third)
        if (correctIndex < 0) return null

        return QuizQuestion(
            id = 0,
            type = QuizQuestionType.GuessLeadActor,
            movieImdbId = candidate.first.imdbId,
            imagePath = candidate.second,
            questionText = "Guess the lead actor",
            titleHint = candidate.first.title,
            options = options,
            correctOptionIndex = correctIndex
        )
    }

    private fun <T> Sequence<T>.shuffled(): List<T> = toList().shuffled(Random.Default)

    private data class QuizMovieCandidate(
        val imdbId: String,
        val title: String,
        val year: Int?,
        val posterPath: String?,
        val backdropPath: String?
    ) {
        fun imagePath(): String? = posterPath ?: backdropPath
    }
}
