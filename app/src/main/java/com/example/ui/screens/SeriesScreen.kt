package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WatchlistEntity
import com.example.data.model.TmdbTv
import com.example.ui.components.HeroBillboard
import com.example.ui.components.MediaPosterCard
import com.example.ui.components.SubmittedTvResultsGrid
import com.example.ui.theme.*
import com.example.viewmodel.GenreItem

@Composable
fun SeriesScreen(
    heroTv: TmdbTv?,
    newReleaseTv: List<TmdbTv> = emptyList(),
    trendingTv: List<TmdbTv>,
    popularTv: List<TmdbTv>,
    topRatedTv: List<TmdbTv>,
    genreTv: List<TmdbTv>,
    selectedGenreId: Int?,
    genres: List<GenreItem>,
    searchQuery: String,
    searchResults: List<TmdbTv>,
    isSearchSubmitted: Boolean = false,
    isSearching: Boolean,
    isLoading: Boolean,
    watchlist: List<WatchlistEntity>,
    onGenreSelected: (Int?) -> Unit,
    onTvClick: (Int) -> Unit,
    onToggleWatchlist: (TmdbTv) -> Unit,
    onRefresh: () -> Unit,
    onReportSearch: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isSaved = { id: Int -> watchlist.any { it.id == id } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .testTag("series_screen")
    ) {
        if (isLoading && trendingTv.isEmpty() && searchResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NetflixRed)
            }
            return
        }

        // Search mode (State B: Submitted Search Grid - only shown when explicitly submitted)
        if (isSearchSubmitted && searchQuery.isNotBlank()) {
            SubmittedTvResultsGrid(
                searchQuery = searchQuery,
                results = searchResults,
                isSearching = isSearching,
                isSaved = isSaved,
                onTvClick = onTvClick,
                onToggleWatchlist = onToggleWatchlist,
                onReportSearch = onReportSearch
            )
            return
        }

        // Genre filter mode
        if (selectedGenreId != null) {
            GenreFilteredTvView(
                genreName = genres.firstOrNull { it.id == selectedGenreId }?.name ?: "Genre",
                tvList = genreTv,
                genres = genres,
                selectedGenreId = selectedGenreId,
                isSaved = isSaved,
                onGenreSelected = onGenreSelected,
                onTvClick = onTvClick,
                onToggleWatchlist = onToggleWatchlist
            )
            return
        }

        // Main Series Feed
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Featured Hero Billboard
            if (heroTv != null) {
                item {
                    HeroBillboard(
                        title = heroTv.displayTitle,
                        secondaryTitle = heroTv.originalName,
                        backdropUrl = heroTv.fullBackdropUrl,
                        overview = heroTv.overview,
                        rating = heroTv.voteAverage,
                        year = heroTv.year,
                        isSaved = isSaved(heroTv.id),
                        onPlayClick = { onTvClick(heroTv.id) },
                        onWatchlistToggle = { onToggleWatchlist(heroTv) },
                        onInfoClick = { onTvClick(heroTv.id) }
                    )
                }
            }

            // Genre Chips
            item {
                GenresScrollRow(
                    genres = genres,
                    selectedGenreId = selectedGenreId,
                    onGenreSelected = onGenreSelected,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            // New Releases Row (latest TV shows on the air, showing full series)
            if (newReleaseTv.isNotEmpty()) {
                item {
                    TvSectionRow(
                        title = "New Releases",
                        icon = Icons.Default.Tv,
                        iconTint = NetflixRed,
                        items = newReleaseTv,
                        isSaved = isSaved,
                        showRank = false,
                        onItemClick = { onTvClick(it.id) },
                        onBookmarkToggle = { onToggleWatchlist(it) }
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            // Trending TV Shows (Top 10 Badges)
            if (trendingTv.isNotEmpty()) {
                item {
                    TvSectionRow(
                        title = "Trending TV Shows",
                        icon = Icons.Default.Tv,
                        iconTint = NetflixRed,
                        items = trendingTv,
                        isSaved = isSaved,
                        showRank = true,
                        onItemClick = { onTvClick(it.id) },
                        onBookmarkToggle = { onToggleWatchlist(it) }
                    )
                }
            }

            // Popular TV Shows
            if (popularTv.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    TvSectionRow(
                        title = "Popular Series",
                        icon = Icons.Default.LocalFireDepartment,
                        iconTint = NetflixGold,
                        items = popularTv,
                        isSaved = isSaved,
                        onItemClick = { onTvClick(it.id) },
                        onBookmarkToggle = { onToggleWatchlist(it) }
                    )
                }
            }

            // Top Rated TV Shows
            if (topRatedTv.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    TvSectionRow(
                        title = "Critically Acclaimed Series",
                        icon = Icons.Default.Star,
                        iconTint = NetflixGold,
                        items = topRatedTv,
                        isSaved = isSaved,
                        onItemClick = { onTvClick(it.id) },
                        onBookmarkToggle = { onToggleWatchlist(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun TvSectionRow(
    title: String,
    icon: ImageVector? = null,
    iconTint: Color = NetflixRed,
    items: List<TmdbTv>,
    isSaved: (Int) -> Boolean,
    showRank: Boolean = false,
    onItemClick: (TmdbTv) -> Unit,
    onBookmarkToggle: (TmdbTv) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = title,
                color = NetflixTextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(items, key = { _, item -> item.id }) { index, tv ->
                MediaPosterCard(
                    title = tv.displayTitle,
                    secondaryTitle = tv.originalName,
                    posterUrl = tv.fullPosterUrl,
                    rating = tv.voteAverage,
                    year = tv.year,
                    isSaved = isSaved(tv.id),
                    rank = if (showRank) index + 1 else null,
                    onClick = { onItemClick(tv) },
                    onPlayClick = { onItemClick(tv) },
                    onBookmarkToggle = { onBookmarkToggle(tv) },
                    modifier = Modifier.width(115.dp)
                )
            }
        }
    }
}

@Composable
fun GenreFilteredTvView(
    genreName: String,
    tvList: List<TmdbTv>,
    genres: List<GenreItem>,
    selectedGenreId: Int?,
    isSaved: (Int) -> Boolean,
    onGenreSelected: (Int?) -> Unit,
    onTvClick: (Int) -> Unit,
    onToggleWatchlist: (TmdbTv) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp, bottom = 16.dp)
    ) {
        GenresScrollRow(
            genres = genres,
            selectedGenreId = selectedGenreId,
            onGenreSelected = onGenreSelected,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Text(
            text = "$genreName Shows",
            color = NetflixTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(110.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(tvList, key = { it.id }) { tv ->
                MediaPosterCard(
                    title = tv.displayTitle,
                    secondaryTitle = tv.originalName,
                    posterUrl = tv.fullPosterUrl,
                    rating = tv.voteAverage,
                    year = tv.year,
                    isSaved = isSaved(tv.id),
                    onClick = { onTvClick(tv.id) },
                    onPlayClick = { onTvClick(tv.id) },
                    onBookmarkToggle = { onToggleWatchlist(tv) }
                )
            }
        }
    }
}
