package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.TmdbMovie
import com.example.data.model.TmdbTv
import com.example.ui.theme.*
import java.util.Locale

// ==================== STATE A: LIVE AUTOCOMPLETE SUGGESTIONS OVERLAY ====================
@Composable
fun LiveMovieSuggestionsOverlay(
    suggestions: List<TmdbMovie>,
    isLoading: Boolean,
    onSelectMovie: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (suggestions.isEmpty() && !isLoading) return

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = NetflixDarkSurface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp)
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .testTag("live_movie_suggestions_overlay")
    ) {
        if (isLoading && suggestions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = NetflixRed,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(suggestions.take(6), key = { it.id }) { movie ->
                    SuggestionMovieRow(
                        movie = movie,
                        onClick = { onSelectMovie(movie.id) }
                    )
                    HorizontalDivider(
                        color = Color(0xFF2B2B2B),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
fun LiveTvSuggestionsOverlay(
    suggestions: List<TmdbTv>,
    isLoading: Boolean,
    onSelectTv: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (suggestions.isEmpty() && !isLoading) return

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = NetflixDarkSurface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp)
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .testTag("live_tv_suggestions_overlay")
    ) {
        if (isLoading && suggestions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = NetflixRed,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(suggestions.take(6), key = { it.id }) { tv ->
                    SuggestionTvRow(
                        tv = tv,
                        onClick = { onSelectTv(tv.id) }
                    )
                    HorizontalDivider(
                        color = Color(0xFF2B2B2B),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionMovieRow(
    movie: TmdbMovie,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail Poster
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(NetflixSurfaceVariant)
        ) {
            if (!movie.posterPath.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(movie.fullPosterUrl)
                        .crossfade(100)
                        .size(100, 150)
                        .build(),
                    contentDescription = movie.displayTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = movie.displayTitle,
                color = NetflixTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF2A2A2A)
                ) {
                    Text(
                        text = "Movie",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                if (movie.year.isNotBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = movie.year,
                        color = NetflixTextSecondary,
                        fontSize = 11.sp
                    )
                }

                if (movie.voteAverage > 0.0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", movie.voteAverage),
                        color = Color(0xFFFFB300),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionTvRow(
    tv: TmdbTv,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail Poster
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(NetflixSurfaceVariant)
        ) {
            if (!tv.posterPath.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(tv.fullPosterUrl)
                        .crossfade(100)
                        .size(100, 150)
                        .build(),
                    contentDescription = tv.displayTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tv.displayTitle,
                color = NetflixTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = NetflixRed.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "TV Series",
                        color = NetflixRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                if (tv.year.isNotBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tv.year,
                        color = NetflixTextSecondary,
                        fontSize = 11.sp
                    )
                }

                if (tv.voteAverage > 0.0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", tv.voteAverage),
                        color = Color(0xFFFFB300),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==================== STATE B: FULL-SCREEN SUBMITTED SEARCH RESULTS GRID ====================
@Composable
fun SubmittedMovieResultsGrid(
    searchQuery: String,
    results: List<TmdbMovie>,
    isSearching: Boolean,
    isSaved: (Int) -> Boolean,
    onMovieClick: (Int) -> Unit,
    onPlayMovie: (TmdbMovie) -> Unit,
    onToggleWatchlist: (TmdbMovie) -> Unit,
    onReportSearch: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .padding(horizontal = 16.dp)
            .padding(top = 10.dp, bottom = 16.dp)
    ) {
        // Heading
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Search \"$searchQuery\"",
                    color = NetflixTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isSearching) {
                    Spacer(modifier = Modifier.width(10.dp))
                    CircularProgressIndicator(
                        color = NetflixRed,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = { onReportSearch(searchQuery) },
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = NetflixRed.copy(alpha = 0.15f),
                    contentColor = NetflixRed
                ),
                border = BorderStroke(1.dp, NetflixRed.copy(alpha = 0.6f)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .testTag("report_movie_search_button")
                    .height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = "Report Search",
                    tint = NetflixRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Report",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        if (!isSearching && results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = NetflixTextMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No movies found for \"$searchQuery\"",
                        color = NetflixTextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Try searching for another movie title, actor, or genre",
                        color = NetflixTextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(110.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(results, key = { it.id }) { movie ->
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
}

@Composable
fun SubmittedTvResultsGrid(
    searchQuery: String,
    results: List<TmdbTv>,
    isSearching: Boolean,
    isSaved: (Int) -> Boolean,
    onTvClick: (Int) -> Unit,
    onToggleWatchlist: (TmdbTv) -> Unit,
    onReportSearch: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .padding(horizontal = 16.dp)
            .padding(top = 10.dp, bottom = 16.dp)
    ) {
        // Heading
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Search \"$searchQuery\"",
                    color = NetflixTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isSearching) {
                    Spacer(modifier = Modifier.width(10.dp))
                    CircularProgressIndicator(
                        color = NetflixRed,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = { onReportSearch(searchQuery) },
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = NetflixRed.copy(alpha = 0.15f),
                    contentColor = NetflixRed
                ),
                border = BorderStroke(1.dp, NetflixRed.copy(alpha = 0.6f)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .testTag("report_tv_search_button")
                    .height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = "Report Search",
                    tint = NetflixRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Report",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        if (!isSearching && results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = NetflixTextMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No TV series found for \"$searchQuery\"",
                        color = NetflixTextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Try searching for another TV show, anime, or drama",
                        color = NetflixTextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(110.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(results, key = { it.id }) { tv ->
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
}

// ==================== STATE A (COMPANIES): LIVE AUTOCOMPLETE SUGGESTIONS ====================
@Composable
fun LiveCompanySuggestionsOverlay(
    suggestions: List<com.example.data.model.ProductionCompanyInfo>,
    isLoading: Boolean,
    isArabicRegion: Boolean = false,
    onSelectCompany: (com.example.data.model.ProductionCompanyInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    if (suggestions.isEmpty() && !isLoading) return

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = NetflixDarkSurface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp)
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .testTag("live_company_suggestions_overlay")
    ) {
        if (isLoading && suggestions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = NetflixRed,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(suggestions.take(6), key = { it.id }) { company ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectCompany(company) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(42.dp)
                                .height(42.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (company.fullLogoUrl != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(company.fullLogoUrl)
                                        .crossfade(100)
                                        .size(100, 100)
                                        .build(),
                                    contentDescription = company.name,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = NetflixTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabicRegion && company.arabicName.isNotBlank()) company.arabicName else company.name,
                                color = NetflixTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (isArabicRegion && company.arabicName.isNotBlank() && company.arabicName != company.name) {
                                Text(
                                    text = company.name,
                                    color = NetflixTextSecondary,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = NetflixRed.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Production Company",
                                    color = NetflixRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    HorizontalDivider(
                        color = Color(0xFF2B2B2B),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}
