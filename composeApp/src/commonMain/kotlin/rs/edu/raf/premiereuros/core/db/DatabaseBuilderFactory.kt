package rs.edu.raf.premiereuros.core.db

import androidx.room.RoomDatabase

expect fun createPlatformDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>
