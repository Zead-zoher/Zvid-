package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.local.SavedCompanyEntity
import com.example.data.local.WatchlistEntity
import com.example.ui.components.MediaPosterCard
import com.example.ui.theme.*
import com.example.viewmodel.AppTab

import com.example.viewmodel.ContentRegion

val WatchlistEntity.fullPosterUrl: String?
    get() = posterPath?.let { if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/w500$it" }

val SavedCompanyEntity.fullLogoUrl: String?
    get() = logoPath?.let { if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/w500$it" }

@Composable
fun SavedScreen(
    watchlist: List<WatchlistEntity>,
    savedCompanies: List<SavedCompanyEntity> = emptyList(),
    selectedRegion: ContentRegion = ContentRegion.GLOBAL,
    onItemClick: (WatchlistEntity) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onOpenCompany: (Int) -> Unit = {},
    onRemoveCompany: (Int) -> Unit = {},
    onNavigateToTab: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("all") } // "all", "movie", "tv", "companies"

    val moviesCount = watchlist.count { it.mediaType == "movie" }
    val tvCount = watchlist.count { it.mediaType == "tv" }
    val companiesCount = savedCompanies.size

    val isCompletelyEmpty = watchlist.isEmpty() && savedCompanies.isEmpty()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .testTag("saved_watchlist_screen")
    ) {
        if (isCompletelyEmpty) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(NetflixCardElevated, RoundedCornerShape(36.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = NetflixRed,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Your Saved Collection is Empty",
                        color = NetflixTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Save movies, series, and production companies to quickly access them here.",
                        color = NetflixTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { onNavigateToTab(AppTab.MOVIES) },
                            colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 40.dp)
                                .testTag("explore_movies_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Movies",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { onNavigateToTab(AppTab.COMPANIES) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NetflixBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = NetflixCardElevated,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 40.dp)
                                .testTag("explore_companies_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Companies",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Filter Tabs: All, Companies, Movies, TV Shows
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WatchlistFilterChip(
                        label = "All (${watchlist.size + companiesCount})",
                        isSelected = selectedFilter == "all",
                        onClick = { selectedFilter = "all" }
                    )
                    WatchlistFilterChip(
                        label = "Companies ($companiesCount)",
                        isSelected = selectedFilter == "companies",
                        onClick = { selectedFilter = "companies" }
                    )
                    WatchlistFilterChip(
                        label = "Movies ($moviesCount)",
                        isSelected = selectedFilter == "movie",
                        onClick = { selectedFilter = "movie" }
                    )
                    WatchlistFilterChip(
                        label = "TV Shows ($tvCount)",
                        isSelected = selectedFilter == "tv",
                        onClick = { selectedFilter = "tv" }
                    )
                }

                if (selectedFilter == "companies") {
                    // Companies List Only
                    if (savedCompanies.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No saved companies yet. Save a company from the Companies tab.",
                                color = NetflixTextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(150.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(savedCompanies, key = { it.companyId }) { comp ->
                                SavedCompanyCard(
                                    company = comp,
                                    selectedRegion = selectedRegion,
                                    onClick = { onOpenCompany(comp.companyId) },
                                    onRemove = { onRemoveCompany(comp.companyId) }
                                )
                            }
                        }
                    }
                } else {
                    // Movies / TV / Mixed List
                    val filteredMedia = when (selectedFilter) {
                        "movie" -> watchlist.filter { it.mediaType == "movie" }
                        "tv" -> watchlist.filter { it.mediaType == "tv" }
                        else -> watchlist
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // If "all" and there are saved companies, show a horizontal strip for them at top!
                        if (selectedFilter == "all" && savedCompanies.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Saved Companies (${savedCompanies.size})",
                                        color = NetflixTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "View All",
                                        color = NetflixRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable { selectedFilter = "companies" }
                                    )
                                }

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                ) {
                                    items(savedCompanies, key = { it.companyId }) { comp ->
                                        SavedCompanyMiniCard(
                                            company = comp,
                                            onClick = { onOpenCompany(comp.companyId) },
                                            onRemove = { onRemoveCompany(comp.companyId) }
                                        )
                                    }
                                }
                            }

                            item {
                                Text(
                                    text = "Saved Titles (${filteredMedia.size})",
                                    color = NetflixTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )
                            }
                        }

                        if (filteredMedia.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (selectedFilter == "movie") "No saved movies yet."
                                        else if (selectedFilter == "tv") "No saved series yet."
                                        else "No saved titles found.",
                                        color = NetflixTextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        } else {
                            // Grid of media cards
                            items(filteredMedia.chunked(3), key = { chunk -> chunk.joinToString { it.id.toString() } }) { rowItems ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { item ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            SavedMediaItemCard(
                                                item = item,
                                                onClick = { onItemClick(item) },
                                                onRemove = { onRemoveItem(item.id) }
                                            )
                                        }
                                    }
                                    // Filler boxes for incomplete rows
                                    repeat(3 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedMediaItemCard(
    item: WatchlistEntity,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = NetflixCardElevated),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("saved_media_card_${item.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .background(NetflixSurfaceVariant)
            ) {
                if (!item.posterPath.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.fullPosterUrl)
                            .crossfade(150)
                            .size(240, 360)
                            .build(),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Media type badge (top-left)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                ) {
                    Text(
                        text = if (item.mediaType == "movie") "MOVIE" else "TV",
                        color = if (item.mediaType == "movie") NetflixRed else Color(0xFF00C6FF),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                // Bookmark Remove Button (top-right)
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                        .testTag("remove_saved_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Remove",
                        tint = NetflixRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Play icon overlay at bottom-right
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(6.dp)) {
                Text(
                    text = item.title,
                    color = NetflixTextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.voteAverage > 0.0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = NetflixGold,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format(java.util.Locale.US, "%.1f", item.voteAverage),
                            color = NetflixGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavedCompanyCard(
    company: SavedCompanyEntity,
    selectedRegion: ContentRegion = ContentRegion.GLOBAL,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = NetflixCardElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, NetflixBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("saved_company_card_${company.companyId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.07f))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                if (company.fullLogoUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(company.fullLogoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = company.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = NetflixTextMuted,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Remove",
                        tint = NetflixRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (selectedRegion == ContentRegion.ARABIC && company.arabicName.isNotBlank()) company.arabicName else company.name,
                color = NetflixTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (selectedRegion == ContentRegion.ARABIC && company.arabicName.isNotBlank() && company.arabicName != company.name) {
                Text(
                    text = company.name,
                    color = NetflixTextSecondary,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SavedCompanyMiniCard(
    company: SavedCompanyEntity,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = NetflixCardElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, NetflixBorder),
        modifier = Modifier
            .width(130.dp)
            .height(72.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (company.fullLogoUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(company.fullLogoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = company.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = NetflixTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = NetflixRed,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onRemove() }
                )
            }

            Text(
                text = company.name,
                color = NetflixTextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WatchlistFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) NetflixRed else NetflixCardElevated,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) NetflixRed else NetflixBorder
        ),
        modifier = Modifier
            .height(34.dp)
            .testTag("watchlist_filter_${label.take(3)}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) Color.White else NetflixTextSecondary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
