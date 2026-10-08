package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentHistoryDao {
    @Query("SELECT * FROM recent_history ORDER BY updatedAt DESC")
    fun getAllHistory(): Flow<List<RecentHistoryEntity>>

    @Query("SELECT * FROM recent_history WHERE historyId = :historyId LIMIT 1")
    suspend fun getHistoryItem(historyId: String): RecentHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHistory(item: RecentHistoryEntity)

    @Query("DELETE FROM recent_history WHERE historyId = :historyId")
    suspend fun deleteHistoryItem(historyId: String)

    @Query("DELETE FROM recent_history")
    suspend fun clearAllHistory()
}
