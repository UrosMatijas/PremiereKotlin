package rs.edu.raf.premiereuros.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import rs.edu.raf.premiereuros.core.db.dao.MovieDao
import rs.edu.raf.premiereuros.data.local.mappers.toDetails
import rs.edu.raf.premiereuros.data.local.mappers.toDomain
import rs.edu.raf.premiereuros.data.local.mappers.toEntity
import rs.edu.raf.premiereuros.data.local.mappers.toListItem
import rs.edu.raf.premiereuros.data.remote.PremiereApi
import rs.edu.raf.premiereuros.data.remote.mappers.toDomainConfig
import rs.edu.raf.premiereuros.domain.model.Genre
import rs.edu.raf.premiereuros.domain.model.ImageConfig
import rs.edu.raf.premiereuros.domain.model.MovieCastMember
import rs.edu.raf.premiereuros.domain.model.MovieDetails
import rs.edu.raf.premiereuros.domain.model.MovieFilter
import rs.edu.raf.premiereuros.domain.model.MovieImage
import rs.edu.raf.premiereuros.domain.model.MovieListItem
import rs.edu.raf.premiereuros.domain.model.MovieVideo
import rs.edu.raf.premiereuros.domain.model.SortOption
import rs.edu.raf.premiereuros.domain.repository.PremiereRepository
import rs.edu.raf.premiereuros.data.remote.mappers.toDomain as toRemoteDomain

class RemotePremiereRepository(
    private val api: PremiereApi,
    private val movieDao: MovieDao
) : PremiereRepository {

    private val _appliedFilter = MutableStateFlow(MovieFilter())
    override val appliedFilter: StateFlow<MovieFilter> = _appliedFilter.asStateFlow()

    private var cachedConfig: ImageConfig? = null

    override fun observeMovies(sort: SortOption): Flow<List<MovieListItem>> {
        return movieDao.observeMoviesWithGenres()
            .combine(appliedFilter) { moviesWithGenres, filter ->
                moviesWithGenres
                    .map { it.toListItem() }
                    .filterBy(filter)
                    .sortedBy(sort)
            }
    }

    override fun observeGenres(): Flow<List<Genre>> {
        return movieDao.observeAllGenres()
            .map { genres -> genres.map { it.toDomain() } }
    }

    override suspend fun setAppliedFilter(filter: MovieFilter) {
        _appliedFilter.value = filter
    }

    override suspend fun getMovies(sort: SortOption): List<MovieListItem> {
        val filter = _appliedFilter.value

        runCatching {
            syncMoviesFromRemote(sort = sort, filter = filter)
        }

        return movieDao.getMoviesWithGenres()
            .map { it.toListItem() }
            .filterBy(filter)
            .sortedBy(sort)
    }

    override suspend fun getGenres(): List<Genre> {
        runCatching { syncGenresFromRemote() }

        return movieDao.getAllGenres()
            .map { it.toDomain() }
    }

    override suspend fun getImageConfig(): ImageConfig {
        cachedConfig?.let { return it }

        return api.getConfig()
            .value
            .toDomainConfig()
            .also { cachedConfig = it }
    }

    override suspend fun getMovieDetails(imdbId: String): MovieDetails? {
        runCatching { syncMovieDetailsFromRemote(imdbId) }

        return movieDao.getMovieWithGenres(imdbId)?.toDetails()
    }

    override suspend fun getMovieCast(imdbId: String): List<MovieCastMember> {
        runCatching { syncMovieCastFromRemote(imdbId) }

        return movieDao.getCastForMovie(imdbId)
            .map { it.toDomain() }
    }

    override suspend fun getMovieImages(imdbId: String): List<MovieImage> {
        runCatching { syncMovieImagesFromRemote(imdbId) }

        return movieDao.getImagesForMovie(imdbId)
            .map { it.toDomain() }
    }

    override suspend fun getMovieVideos(imdbId: String): List<MovieVideo> {
        runCatching { syncMovieVideosFromRemote(imdbId) }

        return movieDao.getVideosForMovie(imdbId)
            .map { it.toDomain() }
    }

    private suspend fun syncMoviesFromRemote(
        sort: SortOption,
        filter: MovieFilter
    ) {
        val remoteMovies = api.getMovies(
            pageSize = 30,
            sortBy = sort.apiValue,
            sortOrder = "desc",
            genreId = filter.selectedGenreId,
            query = filter.query.takeIf { it.isNotBlank() },
            minYear = filter.minYear.toIntOrNull(),
            maxYear = filter.maxYear.toIntOrNull(),
            minRating = filter.minRating.takeIf { it > 0f }
        ).items.map { it.toRemoteDomain() }

        if (remoteMovies.isEmpty()) return

        movieDao.upsertMovies(remoteMovies.map { it.toEntity() })
        movieDao.replaceMovieGenres(
            remoteMovies.associate { movie ->
                movie.imdbId to movie.genres.map { genre -> genre.toEntity() }
            }
        )
    }

    private suspend fun syncGenresFromRemote() {
        val genres = api.getGenres().map { it.toRemoteDomain() }
        movieDao.upsertGenres(genres.map { it.toEntity() })
    }

    private suspend fun syncMovieDetailsFromRemote(imdbId: String) {
        val details = api.getMovieDetails(imdbId).toRemoteDomain()
        movieDao.upsertMovies(listOf(details.toEntity()))
        movieDao.replaceMovieGenres(
            movieIdToGenres = mapOf(
                details.imdbId to details.genres.map { it.toEntity() }
            )
        )
    }

    private suspend fun syncMovieCastFromRemote(imdbId: String) {
        val cast = api.getMovieCast(imdbId, pageSize = 10)
            .items
            .filter { it.department == "Acting" }
            .map { it.toRemoteDomain() }

        movieDao.clearCastForMovie(imdbId)
        movieDao.upsertCast(cast.map { it.toEntity(movieImdbId = imdbId) })
    }

    private suspend fun syncMovieImagesFromRemote(imdbId: String) {
        val images = api.getMovieImages(imdbId, type = "backdrop")
            .backdrops
            .map { it.toRemoteDomain() }

        movieDao.clearImagesForMovie(imdbId)
        movieDao.upsertImages(images.map { it.toEntity(movieImdbId = imdbId) })
    }

    private suspend fun syncMovieVideosFromRemote(imdbId: String) {
        val videos = api.getMovieVideos(imdbId, type = "Trailer")
            .map { it.toRemoteDomain() }

        movieDao.clearVideosForMovie(imdbId)
        movieDao.upsertVideos(videos.map { it.toEntity(movieImdbId = imdbId) })
    }
}

private fun List<MovieListItem>.filterBy(filter: MovieFilter): List<MovieListItem> {
    return filter { movie ->
        val queryOk = filter.query.isBlank() || movie.title.contains(filter.query, ignoreCase = true)
        val genreOk = filter.selectedGenreId == null || movie.genres.any { it.id == filter.selectedGenreId }
        val minYearOk = filter.minYear.toIntOrNull()?.let { movie.year != null && movie.year >= it } ?: true
        val maxYearOk = filter.maxYear.toIntOrNull()?.let { movie.year != null && movie.year <= it } ?: true
        val minRatingOk = (movie.imdbRating ?: 0f) >= filter.minRating

        queryOk && genreOk && minYearOk && maxYearOk && minRatingOk
    }
}

private fun List<MovieListItem>.sortedBy(sort: SortOption): List<MovieListItem> {
    return when (sort) {
        SortOption.Rating -> sortedByDescending { it.imdbRating ?: 0f }
        SortOption.Year -> sortedByDescending { it.year ?: 0 }
        SortOption.Title -> sortedBy { it.title }
        SortOption.Popularity -> this
    }
}
