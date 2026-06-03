package rs.edu.raf.premiereuros.data.remote.mappers

import rs.edu.raf.premiereuros.data.remote.dto.LeaderboardEntryDto
import rs.edu.raf.premiereuros.data.remote.dto.PostQuizResultResponseDto
import rs.edu.raf.premiereuros.data.remote.dto.QuizResultDto
import rs.edu.raf.premiereuros.data.remote.dto.ShowtimePaginatedResponseDto
import rs.edu.raf.premiereuros.data.remote.dto.UserDto
import rs.edu.raf.premiereuros.domain.model.LeaderboardEntry
import rs.edu.raf.premiereuros.domain.model.PostedQuizResult
import rs.edu.raf.premiereuros.domain.model.QuizResult
import rs.edu.raf.premiereuros.domain.model.ShowtimePage
import rs.edu.raf.premiereuros.domain.model.ShowtimeUser

fun UserDto.toDomain(): ShowtimeUser {
    return ShowtimeUser(
        id = id,
        username = username,
        fullName = fullName
    )
}

fun LeaderboardEntryDto.toDomain(): LeaderboardEntry {
    return LeaderboardEntry(
        rank = rank,
        userId = userId,
        username = username,
        fullName = fullName,
        score = score,
        playedAt = playedAt,
        totalPlays = totalPlays
    )
}

fun QuizResultDto.toDomain(): QuizResult {
    return QuizResult(
        id = id,
        category = category,
        score = score,
        playedAt = playedAt
    )
}

fun PostQuizResultResponseDto.toDomain(): PostedQuizResult {
    return PostedQuizResult(
        result = result.toDomain(),
        ranking = ranking
    )
}

fun <From, To> ShowtimePaginatedResponseDto<From>.toDomain(
    transform: (From) -> To
): ShowtimePage<To> {
    return ShowtimePage(
        page = page,
        pageSize = pageSize,
        totalItems = totalItems,
        totalPages = totalPages,
        items = items.map(transform)
    )
}
