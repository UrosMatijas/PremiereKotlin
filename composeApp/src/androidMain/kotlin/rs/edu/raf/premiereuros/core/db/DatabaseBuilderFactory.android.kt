package rs.edu.raf.premiereuros.core.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import rs.edu.raf.premiereuros.core.platform.requireApplicationContext

actual fun createPlatformDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val context: Context = requireApplicationContext()
    val databasePath = context.getDatabasePath("showtime.db").absolutePath
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = databasePath,
        factory = AppDatabaseConstructor::initialize
    )
}
