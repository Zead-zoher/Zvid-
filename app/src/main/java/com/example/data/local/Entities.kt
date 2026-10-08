package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey
    val id: Int, // TMDB ID
    val mediaType: String, // "movie" or "tv"
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val voteAverage: Double,
    val releaseDate: String?,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_history")
data class RecentHistoryEntity(
    @PrimaryKey
    val historyId: String, // e.g., "movie_550" or "tv_1399_s1_e1"
    val mediaId: Int,
    val mediaType: String, // "movie" or "tv"
    val title: String,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val episodeTitle: String? = null,
    val posterPath: String?,
    val backdropPath: String?,
    val lastPositionSeconds: Long,
    val totalDurationSeconds: Long,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_universes")
data class SavedUniverseEntity(
    @PrimaryKey
    val universeId: String,
    val name: String,
    val arabicName: String,
    val tagLine: String,
    val bannerUrl: String,
    val posterUrl: String,
    val logoText: String,
    val category: String,
    val savedAt: Long = System.currentTimeMillis()
)
