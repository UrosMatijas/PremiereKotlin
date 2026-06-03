package rs.edu.raf.premiereuros.data.local.mappers

import rs.edu.raf.premiereuros.core.db.entity.GenreEntity
import rs.edu.raf.premiereuros.core.db.entity.MovieCastEntity
import rs.edu.raf.premiereuros.core.db.entity.MovieEntity
import rs.edu.raf.premiereuros.core.db.entity.MovieImageEntity
import rs.edu.raf.premiereuros.core.db.entity.MovieVideoEntity
import rs.edu.raf.premiereuros.core.db.model.MovieWithGenres
import rs.edu.raf.premiereuros.domain.model.Genre
import rs.edu.raf.premiereuros.domain.model.MovieCastMember
import rs.edu.raf.premiereuros.domain.model.MovieDetails
import rs.edu.raf.premiereuros.domain.model.MovieImage
import rs.edu.raf.premiereuros.domain.model.MovieListItem
import rs.edu.raf.premiereuros.domain.model.MovieVideo

fun Genre.toEntity(): GenreEntity = GenreEntity(
    id = id,
    name = name
)

fun GenreEntity.toDomain(): Genre = Genre(
    id = id,
    name = name
)

fun MovieListItem.toEntity(): MovieEntity = MovieEntity(
    imdbId = imdbId,
    tmdbId = null,
    title = title,
    originalTitle = null,
    overview = null,
    tagline = null,
    year = year,
    runtime = runtime,
    budget = null,
    revenue = null,
    languageCode = null,
    popularity = null,
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    tmdbRating = null,
    tmdbVotes = null,
    posterPath = posterPath,
    backdropPath = null
)

fun MovieDetails.toEntity(): MovieEntity = MovieEntity(
    imdbId = imdbId,
    tmdbId = tmdbId,
    title = title,
    originalTitle = originalTitle,
    overview = overview,
    tagline = tagline,
    year = year,
    runtime = runtime,
    budget = budget,
    revenue = revenue,
    languageCode = languageCode,
    popularity = popularity,
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    tmdbRating = tmdbRating,
    tmdbVotes = tmdbVotes,
    posterPath = posterPath,
    backdropPath = backdropPath
)

fun MovieWithGenres.toListItem(): MovieListItem = MovieListItem(
    imdbId = movie.imdbId,
    title = movie.title,
    year = movie.year,
    runtime = movie.runtime,
    imdbRating = movie.imdbRating,
    imdbVotes = movie.imdbVotes,
    posterPath = movie.posterPath,
    genres = genres.map { it.toDomain() }
)

fun MovieWithGenres.toDetails(): MovieDetails = MovieDetails(
    imdbId = movie.imdbId,
    tmdbId = movie.tmdbId,
    title = movie.title,
    originalTitle = movie.originalTitle,
    overview = movie.overview,
    tagline = movie.tagline,
    year = movie.year,
    runtime = movie.runtime,
    budget = movie.budget,
    revenue = movie.revenue,
    languageCode = movie.languageCode,
    popularity = movie.popularity,
    imdbRating = movie.imdbRating,
    imdbVotes = movie.imdbVotes,
    tmdbRating = movie.tmdbRating,
    tmdbVotes = movie.tmdbVotes,
    posterPath = movie.posterPath,
    backdropPath = movie.backdropPath,
    genres = genres.map { it.toDomain() }
)

fun MovieCastMember.toEntity(movieImdbId: String): MovieCastEntity = MovieCastEntity(
    movieImdbId = movieImdbId,
    personImdbId = imdbId,
    name = name,
    department = department,
    profilePath = profilePath
)

fun MovieCastEntity.toDomain(): MovieCastMember = MovieCastMember(
    imdbId = personImdbId,
    name = name,
    department = department,
    profilePath = profilePath
)

fun MovieImage.toEntity(movieImdbId: String): MovieImageEntity = MovieImageEntity(
    movieImdbId = movieImdbId,
    filePath = filePath,
    width = width,
    height = height
)

fun MovieImageEntity.toDomain(): MovieImage = MovieImage(
    filePath = filePath,
    width = width,
    height = height
)

fun MovieVideo.toEntity(movieImdbId: String): MovieVideoEntity = MovieVideoEntity(
    movieImdbId = movieImdbId,
    videoKey = key,
    site = site,
    name = name,
    type = type,
    official = official
)

fun MovieVideoEntity.toDomain(): MovieVideo = MovieVideo(
    key = videoKey,
    site = site,
    name = name,
    type = type,
    official = official
)
