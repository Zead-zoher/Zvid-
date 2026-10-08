package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_companies")
data class SavedCompanyEntity(
    @PrimaryKey
    val companyId: Int,
    val name: String,
    val arabicName: String = "",
    val description: String = "",
    val logoPath: String? = null,
    val originCountry: String = "",
    val savedAt: Long = System.currentTimeMillis()
)

@Dao
interface SavedCompanyDao {
    @Query("SELECT * FROM saved_companies ORDER BY savedAt DESC")
    fun getAllSavedCompanies(): Flow<List<SavedCompanyEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_companies WHERE companyId = :companyId)")
    fun isCompanySaved(companyId: Int): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_companies WHERE companyId = :companyId)")
    suspend fun isCompanySavedSync(companyId: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCompany(company: SavedCompanyEntity)

    @Query("DELETE FROM saved_companies WHERE companyId = :companyId")
    suspend fun removeCompany(companyId: Int)
}
