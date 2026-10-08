package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WatchlistEntity::class, RecentHistoryEntity::class, SavedUniverseEntity::class, SavedCompanyEntity::class],
    version = 3,
    exportSchema = false
)
abstract class ZvidDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao
    abstract fun recentHistoryDao(): RecentHistoryDao
    abstract fun savedUniverseDao(): SavedUniverseDao
    abstract fun savedCompanyDao(): SavedCompanyDao

    companion object {
        @Volatile
        private var INSTANCE: ZvidDatabase? = null

        fun getDatabase(context: Context): ZvidDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZvidDatabase::class.java,
                    "zvid_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
