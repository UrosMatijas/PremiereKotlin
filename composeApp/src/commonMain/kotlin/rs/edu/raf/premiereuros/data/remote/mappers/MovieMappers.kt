package rs.edu.raf.premiereuros.data.remote.mappers

import rs.edu.raf.premiereuros.data.remote.dto.ConfigEntryDto
import rs.edu.raf.premiereuros.data.remote.dto.GenreDto
import rs.edu.raf.premiereuros.data.remote.dto.MovieCastMemberDto
import rs.edu.raf.premiereuros.data.remote.dto.MovieDetailsDto
import rs.edu.raf.premiereuros.data.remote.dto.MovieImageDto
import rs.edu.raf.premiereuros.data.remote.dto.MovieListItemDto
import rs.edu.raf.premiereuros.data.remote.dto.MovieVideoDto
import rs.edu.raf.premiereuros.domain.model.Genre
import rs.edu.raf.premiereuros.domain.model.ImageConfig
import rs.edu.raf.premiereuros.domain.model.MovieCastMember
import rs.edu.raf.premiereuros.domain.model.MovieDetails
import rs.edu.raf.premiereuros.domain.model.MovieImage
import rs.edu.raf.premiereuros.domain.model.MovieListItem
import rs.edu.raf.premiereuros.domain.model.MovieVideo

fun GenreDto.toDomain(): Genre {
    return Genre(
        id = id,
        name = name
    )
}

fun MovieListItemDto.toDomain(): MovieListItem {
    return MovieListItem(
        imdbId = imdbId,
        title = title,
        year = year,
        runtime = null,
        imdbRating = imdbRating,
        imdbVotes = imdbVotes,
        posterPath = posterPath,
        genres = genres.map { it.toDomain() }
    )
}

fun MovieDetailsDto.toDomain(): MovieDetails {
    return MovieDetails(
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
        backdropPath = backdropPath,
        genres = genres.map { it.toDomain() }
    )
}

fun MovieCastMemberDto.toDomain(): MovieCastMember {
    return MovieCastMember(
        imdbId = imdbId,
        name = name,
        department = department,
        profilePath = profilePath
    )
}

fun MovieImageDto.toDomain(): MovieImage {
    return MovieImage(
        filePath = filePath,
        width = width,
        height = height
    )
}

fun MovieVideoDto.toDomain(): MovieVideo {
    return MovieVideo(
        key = key,
        site = site,
        name = name,
        type = type,
        official = official
    )
}

fun List<ConfigEntryDto>.toDomainConfig(): ImageConfig {
    val map = associate { it.key to it.value }

    return ImageConfig(
        imageBaseUrl = map["image_base_url"].orEmpty(),
        posterSizes = map["poster_sizes"].orEmpty().split(",").filter { it.isNotBlank() },
        backdropSizes = map["backdrop_sizes"].orEmpty().split(",").filter { it.isNotBlank() },
        profileSizes = map["profile_sizes"].orEmpty().split(",").filter { it.isNotBlank() }
    )
}