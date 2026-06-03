package rs.edu.raf.premiereuros.data.local

import rs.edu.raf.premiereuros.core.auth.SessionDataCleaner
import rs.edu.raf.premiereuros.core.db.dao.MovieDao

class SessionDataCleanerImpl(
    private val movieDao: MovieDao
) : SessionDataCleaner {
    override suspend fun clearUserScopedData() {
        movieDao.clearFavorites()
        movieDao.clearWatchlist()
    }
}
