package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WatchlistEntity
import com.example.data.model.TmdbMovie
import com.example.ui.components.HeroBillboard
import com.example.ui.components.MediaPosterCard
import com.example.ui.components.SubmittedMovieResultsGrid
import com.example.ui.theme.*
import com.example.viewmodel.GenreItem

@Composable
fun MoviesScreen(
    heroMovie: TmdbMovie?,
    newReleaseMovies: List<TmdbMovie> = emptyList(),
    trendingMovies: List<TmdbMovie>,
    popularMovies: List<TmdbMovie>,
    topRatedMovies: List<TmdbMovie>,
    genreMovies: List<TmdbMovie>,
    selectedGenreId: Int?,
    genres: List<GenreItem>,
    searchQuery: String,
    searchResults: List<TmdbMovie>,
    isSearchSubmitted: Boolean = false,
    isSearching: Boolean,
    isLoading: Boolean,
    watchlist: List<WatchlistEntity>,
    onGenreSelected: (Int?) -> Unit,
    onMovieClick: (Int) -> Unit,
    onPlayMovie: (TmdbMovie) -> Unit,
    onToggleWatchlist: (TmdbMovie) -> Unit,
    onRefresh: () -> Unit,
    onReportSearch: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isSaved = { id: Int -> watchlist.any { it.id == id } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .testTag("movies_screen")
    ) {
        if (isLoading && trendingMovies.isEmpty() && searchResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NetflixRed)
            }
            return
        }

        // Check if user is searching (State B: Submitted Search Grid - only shown when explicitly submitted)
        if (isSearchSubmitted && searchQuery.isNotBlank()) {
            SubmittedMovieResultsGrid(
                searchQuery = searchQuery,
                results = searchResults,
                isSearching = isSearching,
                isSaved = isSaved,
                onMovieClick = onMovieClick,
                onPlayMovie = onPlayMovie,
                onToggleWatchlist = onToggleWatchlist,
                onReportSearch = onReportSearch
            )
            return
        }

        // Check if a specific genre is filtered
        if (selectedGenreId != null) {
            GenreFilteredMoviesView(
                genreName = genres.firstOrNull { it.id == selectedGenreId }?.name ?: "Genre",
                movies = genreMovies,
                genres = genres,
                selectedGenreId = selectedGenreId,
                isSaved = isSaved,
                onGenreSelected = onGenreSelected,
                onMovieClick = onMovieClick,
                onPlayMovie = onPlayMovie,
                onToggleWatchlist = onToggleWatchlist
            )
            return
        }

        // Standard Netflix Catalog Feed
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Hero Billboard
            if (heroMovie != null) {
                item {
                    HeroBillboard(
                        title = heroMovie.displayTitle,
                        secondaryTitle = heroMovie.originalTitle,
                        backdropUrl = heroMovie.fullBackdropUrl,
                        overview = heroMovie.overview,
                        rating = heroMovie.voteAverage,
                        year = heroMovie.year,
                        isSaved = isSaved(heroMovie.id),
                        onPlayClick = { onPlayMovie(heroMovie) },
                        onWatchlistToggle = { onToggleWatchlist(heroMovie) },
                        onInfoClick = { onMovieClick(heroMovie.id) }
                    )
                }
            }

            // 2. Genres Filter Chips
            item {
                GenresScrollRow(
                    genres = genres,
                    selectedGenreId = selectedGenreId,
                    onGenreSelected = onGenreSelected,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            // New Releases Row
            if (newReleaseMovies.isNotEmpty()) {
                item {
                    MediaSectionRow(
                        title = "New Releases",
                        icon = Icons.Default.Movie,
                        iconTint = NetflixRed,
                        items = newReleaseMovies,
                        isSaved = isSaved,
                        showRank = false,
                        onItemClick = { onMovieClick(it.id) },
                        onPlayClick = { onPlayMovie(it) },
                        onBookmarkToggle = { onToggleWatchlist(it) }
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            // 3. Trending Now (Horizontal Row with Top 10 badges)
            if (trendingMovies.isNotEmpty()) {
                item {
                    MediaSectionRow(
                        title = "Trending Now",
                        icon = Icons.Default.TrendingUp,
                        iconTint = NetflixRed,
                        items = trendingMovies,
                        isSaved = isSaved,
                        showRank = true,
                        onItemClick = { onMovieClick(it.id) },
                        onPlayClick = { onPlayMovie(it) },
                        onBookmarkToggle = { onToggleWatchlist(it) }
                    )
                }
            }

            // 4. Popular on Zvid
            if (popularMovies.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    MediaSectionRow(
                        title = "Popular on Zvid",
                        icon = Icons.Default.LocalFireDepartment,
                        iconTint = NetflixGold,
                        items = popularMovies,
                        isSaved = isSaved,
                        onItemClick = { onMovieClick(it.id) },
                        onPlayClick = { onPlayMovie(it) },
                        onBookmarkToggle = { onToggleWatchlist(it) }
                    )
                }
            }

            // 5. Top Rated Classics & Blockbusters
            if (topRatedMovies.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    MediaSectionRow(
                        title = "Critically Acclaimed Movies",
                        icon = Icons.Default.Star,
                        iconTint = NetflixGold,
                        items = topRatedMovies,
                        isSaved = isSaved,
                        onItemClick = { onMovieClick(it.id) },
                        onPlayClick = { onPlayMovie(it) },
                        onBookmarkToggle = { onToggleWatchlist(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun MediaSectionRow(
    title: String,
    icon: ImageVector? = null,
    iconTint: Color = NetflixRed,
    items: List<TmdbMovie>,
    isSaved: (Int) -> Boolean,
    showRank: Boolean = false,
    onItemClick: (TmdbMovie) -> Unit,
    onPlayClick: (TmdbMovie) -> Unit,
    onBookmarkToggle: (TmdbMovie) -> Unit,
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
            itemsIndexed(items, key = { _, item -> item.id }) { index, movie ->
                MediaPosterCard(
                    title = movie.displayTitle,
                    secondaryTitle = movie.originalTitle,
                    posterUrl = movie.fullPosterUrl,
                    rating = movie.voteAverage,
                    year = movie.year,
                    isSaved = isSaved(movie.id),
                    rank = if (showRank) index + 1 else null,
                    onClick = { onItemClick(movie) },
                    onPlayClick = { onPlayClick(movie) },
                    onBookmarkToggle = { onBookmarkToggle(movie) },
                    modifier = Modifier.width(115.dp)
                )
            }
        }
    }
}

@Composable
fun GenresScrollRow(
    genres: List<GenreItem>,
    selectedGenreId: Int?,
    onGenreSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        item {
            GenreChip(
                name = "All Genres",
                isSelected = selectedGenreId == null,
                onClick = { onGenreSelected(null) }
            )
        }

        items(genres) { genre ->
            GenreChip(
                name = genre.name,
                isSelected = selectedGenreId == genre.id,
                onClick = { onGenreSelected(if (selectedGenreId == genre.id) null else genre.id) }
            )
        }
    }
}

@Composable
fun GenreChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) NetflixRed else NetflixCardElevated,
        modifier = Modifier.testTag("genre_chip_$name")
    ) {
        Text(
            text = name,
            color = if (isSelected) Color.White else NetflixTextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun GenreFilteredMoviesView(
    genreName: String,
    movies: List<TmdbMovie>,
    genres: List<GenreItem>,
    selectedGenreId: Int?,
    isSaved: (Int) -> Boolean,
    onGenreSelected: (Int?) -> Unit,
    onMovieClick: (Int) -> Unit,
    onPlayMovie: (TmdbMovie) -> Unit,
    onToggleWatchlist: (TmdbMovie) -> Unit
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
            text = "$genreName Movies",
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
            items(movies, key = { it.id }) { movie ->
                MediaPosterCard(
                    title = movie.displayTitle,
                    secondaryTitle = movie.originalTitle,
                    posterUrl = movie.fullPosterUrl,
                    rating = movie.voteAverage,
                    year = movie.year,
                    isSaved = isSaved(movie.id),
                    onClick = { onMovieClick(movie.id) },
                    onPlayClick = { onPlayMovie(movie) },
                    onBookmarkToggle = { onToggleWatchlist(movie) }
                )
            }
        }
    }
}
