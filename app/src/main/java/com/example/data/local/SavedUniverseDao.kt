package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedUniverseDao {
    @Query("SELECT * FROM saved_universes ORDER BY savedAt DESC")
    fun getAllSavedUniverses(): Flow<List<SavedUniverseEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_universes WHERE universeId = :universeId)")
    fun isUniverseSaved(universeId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_universes WHERE universeId = :universeId)")
    suspend fun isUniverseSavedSync(universeId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUniverse(universe: SavedUniverseEntity)

    @Query("DELETE FROM saved_universes WHERE universeId = :universeId")
    suspend fun removeUniverse(universeId: String)
}
