package com.example.ui.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.data.model.TmdbEpisode
import com.example.data.model.TmdbMovieDetail
import com.example.data.model.TmdbSeasonDetail
import com.example.data.model.TmdbTvDetail
import com.example.ui.theme.NetflixBlack
import com.example.ui.theme.NetflixBorder
import com.example.ui.theme.NetflixCardElevated
import com.example.ui.theme.NetflixDarkSurface
import com.example.ui.theme.NetflixGold
import com.example.ui.theme.NetflixRed
import com.example.ui.theme.NetflixSurfaceVariant
import com.example.ui.theme.NetflixTextMuted
import com.example.ui.theme.NetflixTextPrimary
import com.example.ui.theme.NetflixTextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailModal(
    movieDetail: TmdbMovieDetail?,
    tvDetail: TmdbTvDetail?,
    seasonDetail: TmdbSeasonDetail?,
    selectedSeasonNumber: Int,
    isLoadingDetail: Boolean,
    isLoadingSeason: Boolean,
    isSaved: Boolean,
    onSeasonSelected: (tvId: Int, seasonNumber: Int) -> Unit,
    onPlayMovie: (TmdbMovieDetail) -> Unit,
    onPlayEpisode: (tvDetail: TmdbTvDetail, seasonNumber: Int, episode: TmdbEpisode) -> Unit,
    onToggleWatchlistMovie: (TmdbMovieDetail) -> Unit,
    onToggleWatchlistTv: (TmdbTvDetail) -> Unit,
    onReportMovie: (TmdbMovieDetail) -> Unit = {},
    onReportTv: (TmdbTvDetail) -> Unit = {},
    onCompanyClick: (companyId: Int, companyName: String) -> Unit = { _, _ -> },
    onPersonClick: (personId: Int) -> Unit = {},
    onDismiss: () -> Unit
) {
    if (movieDetail == null && tvDetail == null && !isLoadingDetail) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showFullCastList by remember { mutableStateOf<List<com.example.data.model.TmdbCastMember>?>(null) }

    if (showFullCastList != null) {
        FullCastModal(
            cast = showFullCastList!!,
            onSelectPerson = { personId ->
                showFullCastList = null
                onDismiss()
                onPersonClick(personId)
            },
            onDismiss = { showFullCastList = null }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NetflixDarkSurface,
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NetflixBlack)
                .testTag("media_detail_modal")
        ) {
            if (isLoadingDetail) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NetflixRed)
                }
            } else if (movieDetail != null) {
                MovieDetailContent(
                    movie = movieDetail,
                    isSaved = isSaved,
                    onPlayClick = { onPlayMovie(movieDetail) },
                    onWatchlistToggle = { onToggleWatchlistMovie(movieDetail) },
                    onReportClick = { onReportMovie(movieDetail) },
                    onCompanyClick = { id, name ->
                        onDismiss()
                        onCompanyClick(id, name)
                    },
                    onPersonClick = { personId ->
                        onDismiss()
                        onPersonClick(personId)
                    },
                    onShowMoreCast = { fullCast ->
                        showFullCastList = fullCast
                    },
                    onClose = onDismiss
                )
            } else if (tvDetail != null) {
                TvDetailContent(
                    tv = tvDetail,
                    seasonDetail = seasonDetail,
                    selectedSeasonNumber = selectedSeasonNumber,
                    isLoadingSeason = isLoadingSeason,
                    isSaved = isSaved,
                    onSeasonSelected = { num -> onSeasonSelected(tvDetail.id, num) },
                    onPlayEpisode = { ep -> onPlayEpisode(tvDetail, selectedSeasonNumber, ep) },
                    onToggleWatchlist = { onToggleWatchlistTv(tvDetail) },
                    onReportClick = { onReportTv(tvDetail) },
                    onCompanyClick = { id, name ->
                        onDismiss()
                        onCompanyClick(id, name)
                    },
                    onPersonClick = { personId ->
                        onDismiss()
                        onPersonClick(personId)
                    },
                    onShowMoreCast = { fullCast ->
                        showFullCastList = fullCast
                    },
                    onClose = onDismiss
                )
            }
        }
    }
}

@Composable
fun MovieDetailContent(
    movie: TmdbMovieDetail,
    isSaved: Boolean,
    onPlayClick: () -> Unit,
    onWatchlistToggle: () -> Unit,
    onReportClick: () -> Unit = {},
    onCompanyClick: (Int, String) -> Unit = { _, _ -> },
    onPersonClick: (Int) -> Unit = {},
    onShowMoreCast: (List<com.example.data.model.TmdbCastMember>) -> Unit = {},
    onClose: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // Hero Backdrop with Close Button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                if (!movie.fullBackdropUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(movie.fullBackdropUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = movie.displayTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    NetflixBlack.copy(alpha = 0.5f),
                                    Color.Transparent,
                                    NetflixBlack
                                )
                            )
                        )
                )

                // Actions top-right (Report & Close)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onReportClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .testTag("report_movie_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Report",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Title and Metadata Header
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = movie.displayTitle,
                    color = NetflixTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )

                if (!movie.tagline.isNullOrBlank()) {
                    Text(
                        text = "\"${movie.tagline}\"",
                        color = NetflixTextSecondary,
                        fontSize = 13.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Metadata row (Year, Rating, Runtime, 4K)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (movie.voteAverage > 0.0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = NetflixGold,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f", movie.voteAverage),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(text = movie.year, color = NetflixTextSecondary, fontSize = 13.sp)
                    Text(text = movie.formattedRuntime, color = NetflixTextSecondary, fontSize = 13.sp)

                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = NetflixCardElevated
                    ) {
                        Text(
                            text = "4K ULTRA HD",
                            color = NetflixRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Prominent Big Red Play Button
                Button(
                    onClick = onPlayClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NetflixRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("detail_play_movie_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Play Movie",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Add to Watchlist Button
                OutlinedButton(
                    onClick = onWatchlistToggle,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NetflixTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("detail_watchlist_movie_btn")
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        tint = if (isSaved) NetflixRed else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSaved) "Saved in Watchlist" else "Add to My Watchlist",
                        color = if (isSaved) NetflixRed else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Long Report Button below Watchlist
                OutlinedButton(
                    onClick = onReportClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("detail_report_movie_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = NetflixRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Report Content",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Genres tags
                if (!movie.genres.isNullOrEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        items(movie.genres) { genre ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = NetflixCardElevated
                            ) {
                                Text(
                                    text = genre.name,
                                    color = NetflixTextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Production Companies (Clickable to company page)
                if (!movie.productionCompanies.isNullOrEmpty()) {
                    Text(
                        text = "Production Companies",
                        color = NetflixTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 14.dp)
                    ) {
                        items(movie.productionCompanies) { company ->
                            Surface(
                                onClick = { onCompanyClick(company.id, company.name) },
                                shape = RoundedCornerShape(8.dp),
                                color = NetflixCardElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, NetflixBorder)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    if (company.fullLogoUrl != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(company.fullLogoUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = company.name,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .padding(end = 6.dp)
                                        )
                                    }
                                    Text(
                                        text = company.name,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // Overview
                Text(
                    text = "Storyline",
                    color = NetflixTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = movie.overview ?: "No overview available for this title.",
                    color = NetflixTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }

        // Cast rail with character name and More button
        if (movie.credits?.cast?.isNotEmpty() == true) {
            item {
                val castList = movie.credits.cast
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Top Cast",
                        color = NetflixTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (castList.size > 8) {
                        TextButton(
                            onClick = { onShowMoreCast(castList) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "More (${castList.size})",
                                color = NetflixRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(castList.take(12)) { actor ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(78.dp)
                                .clickable { onPersonClick(actor.id) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(NetflixCardElevated)
                            ) {
                                if (actor.fullProfileUrl != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(actor.fullProfileUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = actor.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            if (!actor.character.isNullOrBlank()) {
                                Text(
                                    text = actor.character,
                                    color = NetflixTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = actor.name,
                                    color = NetflixTextSecondary,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    text = actor.name,
                                    color = NetflixTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TvDetailContent(
    tv: TmdbTvDetail,
    seasonDetail: TmdbSeasonDetail?,
    selectedSeasonNumber: Int,
    isLoadingSeason: Boolean,
    isSaved: Boolean,
    onSeasonSelected: (Int) -> Unit,
    onPlayEpisode: (TmdbEpisode) -> Unit,
    onToggleWatchlist: () -> Unit,
    onReportClick: () -> Unit = {},
    onCompanyClick: (Int, String) -> Unit = { _, _ -> },
    onPersonClick: (Int) -> Unit = {},
    onShowMoreCast: (List<com.example.data.model.TmdbCastMember>) -> Unit = {},
    onClose: () -> Unit
) {
    var seasonDropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // Hero Backdrop
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                if (!tv.fullBackdropUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(tv.fullBackdropUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = tv.displayTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    NetflixBlack.copy(alpha = 0.5f),
                                    Color.Transparent,
                                    NetflixBlack
                                )
                            )
                        )
                )

                // Actions top-right (Report & Close)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onReportClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .testTag("report_tv_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Report",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Title and Metadata
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = tv.displayTitle,
                    color = NetflixTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (tv.voteAverage > 0.0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = NetflixGold,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f", tv.voteAverage),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(text = tv.year, color = NetflixTextSecondary, fontSize = 13.sp)
                    Text(text = "${tv.numberOfSeasons} Seasons", color = NetflixTextSecondary, fontSize = 13.sp)

                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = NetflixCardElevated
                    ) {
                        Text(
                            text = "TV-MA",
                            color = NetflixRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Watchlist toggle button
                OutlinedButton(
                    onClick = onToggleWatchlist,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NetflixTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("detail_watchlist_tv_btn")
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        tint = if (isSaved) NetflixRed else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSaved) "Saved in Watchlist" else "Add Series to Watchlist",
                        color = if (isSaved) NetflixRed else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Long Report Button below Watchlist
                OutlinedButton(
                    onClick = onReportClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("detail_report_tv_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = NetflixRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Report Content",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Production Companies
                if (!tv.productionCompanies.isNullOrEmpty()) {
                    Text(
                        text = "Production Companies",
                        color = NetflixTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 14.dp)
                    ) {
                        items(tv.productionCompanies) { company ->
                            Surface(
                                onClick = { onCompanyClick(company.id, company.name) },
                                shape = RoundedCornerShape(8.dp),
                                color = NetflixCardElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, NetflixBorder)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    if (company.fullLogoUrl != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(company.fullLogoUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = company.name,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .padding(end = 6.dp)
                                        )
                                    }
                                    Text(
                                        text = company.name,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // Overview
                Text(
                    text = "Storyline",
                    color = NetflixTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = tv.overview ?: "No overview available.",
                    color = NetflixTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }

        // TV Cast Rail
        if (tv.credits?.cast?.isNotEmpty() == true) {
            item {
                val castList = tv.credits.cast
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Top Cast",
                        color = NetflixTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (castList.size > 8) {
                        TextButton(
                            onClick = { onShowMoreCast(castList) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "More (${castList.size})",
                                color = NetflixRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(castList.take(12)) { actor ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(78.dp)
                                .clickable { onPersonClick(actor.id) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(NetflixCardElevated)
                            ) {
                                if (actor.fullProfileUrl != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(actor.fullProfileUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = actor.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            if (!actor.character.isNullOrBlank()) {
                                Text(
                                    text = actor.character,
                                    color = NetflixTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = actor.name,
                                    color = NetflixTextSecondary,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    text = actor.name,
                                    color = NetflixTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Season Selector Dropdown
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Episodes",
                    color = NetflixTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Box {
                    Surface(
                        onClick = { seasonDropdownExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        color = NetflixCardElevated,
                        modifier = Modifier.testTag("season_selector_dropdown")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Season $selectedSeasonNumber",
                                color = NetflixTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = NetflixTextSecondary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = seasonDropdownExpanded,
                        onDismissRequest = { seasonDropdownExpanded = false },
                        modifier = Modifier.background(NetflixCardElevated)
                    ) {
                        tv.seasons?.filter { it.seasonNumber > 0 }?.forEach { season ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = season.name ?: "Season ${season.seasonNumber}",
                                        color = if (season.seasonNumber == selectedSeasonNumber) NetflixRed else NetflixTextPrimary
                                    )
                                },
                                onClick = {
                                    onSeasonSelected(season.seasonNumber)
                                    seasonDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Episodes List
        if (isLoadingSeason) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NetflixRed)
                }
            }
        } else if (seasonDetail != null && seasonDetail.episodes.isNotEmpty()) {
            items(seasonDetail.episodes) { episode ->
                EpisodeRowItem(
                    episode = episode,
                    onPlay = { onPlayEpisode(episode) }
                )
            }
        }
    }
}

@Composable
fun EpisodeRowItem(
    episode: TmdbEpisode,
    onPlay: () -> Unit
) {
    Surface(
        onClick = onPlay,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("episode_item_${episode.episodeNumber}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Episode Thumbnail with Play icon
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(NetflixCardElevated)
            ) {
                if (episode.fullStillUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(episode.fullStillUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = episode.displayTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Play overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(NetflixRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Episode",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Episode title, runtime & synopsis
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${episode.episodeNumber}. ${episode.displayTitle}",
                        color = NetflixTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = episode.formattedDuration,
                        color = NetflixTextMuted,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = episode.overview?.ifBlank { "No episode description available." } ?: "No episode description.",
                    color = NetflixTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
