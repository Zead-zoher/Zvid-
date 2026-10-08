package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.graphics.Brush
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.local.SavedCompanyEntity
import com.example.data.local.WatchlistEntity
import com.example.data.model.ProductionCompanyInfo
import com.example.data.model.TmdbMovie
import com.example.data.model.TmdbTv
import com.example.ui.components.MediaPosterCard
import com.example.ui.theme.*

import com.example.viewmodel.ContentRegion

enum class CompanySortOption(val label: String, val apiMovieSort: String, val apiTvSort: String) {
    POPULARITY("Popularity", "popularity", "popularity"),
    RATING("Rating", "vote_average", "vote_average"),
    RELEASE("Release", "primary_release_date", "first_air_date")
}

enum class SortDirection(val label: String, val apiSuffix: String) {
    ASC("Ascending", "asc"),
    DESC("Descending", "desc")
}

@Composable
fun CompaniesScreen(
    companies: List<ProductionCompanyInfo>,
    searchQuery: String,
    isSearchSubmitted: Boolean = false,
    isSearching: Boolean = false,
    searchResults: List<ProductionCompanyInfo> = emptyList(),
    selectedCompany: ProductionCompanyInfo?,
    companyMovies: List<TmdbMovie>,
    companyTv: List<TmdbTv>,
    isLoading: Boolean,
    savedCompanies: List<SavedCompanyEntity>,
    watchlist: List<WatchlistEntity>,
    selectedRegion: ContentRegion = ContentRegion.GLOBAL,
    onSelectCompany: (ProductionCompanyInfo) -> Unit,
    onCloseCompany: () -> Unit,
    onToggleSaveCompany: (ProductionCompanyInfo) -> Unit,
    onMovieClick: (Int) -> Unit,
    onTvClick: (Int) -> Unit,
    onPlayMovie: (TmdbMovie) -> Unit,
    onToggleWatchlistMovie: (TmdbMovie) -> Unit,
    onToggleWatchlistTv: (TmdbTv) -> Unit,
    onSortChanged: (CompanySortOption, SortDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedCompany != null) {
        BackHandler {
            onCloseCompany()
        }
        CompanyDetailScreen(
            company = selectedCompany,
            movies = companyMovies,
            tvShows = companyTv,
            isLoading = isLoading,
            isSaved = savedCompanies.any { it.companyId == selectedCompany.id },
            watchlist = watchlist,
            selectedRegion = selectedRegion,
            onBack = onCloseCompany,
            onToggleSave = { onToggleSaveCompany(selectedCompany) },
            onMovieClick = onMovieClick,
            onTvClick = onTvClick,
            onPlayMovie = onPlayMovie,
            onToggleWatchlistMovie = onToggleWatchlistMovie,
            onToggleWatchlistTv = onToggleWatchlistTv,
            onSortChanged = onSortChanged
        )
    } else if (isSearchSubmitted && searchQuery.isNotBlank()) {
        SubmittedCompaniesResultsGrid(
            searchQuery = searchQuery,
            results = searchResults,
            isSearching = isSearching,
            selectedRegion = selectedRegion,
            onCompanyClick = onSelectCompany
        )
    } else {
        // Feed of Companies
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = modifier
                .fillMaxSize()
                .background(NetflixBlack)
                .testTag("companies_screen")
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Column(modifier = Modifier.padding(bottom = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = NetflixRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Famous Studios & Production Companies",
                            color = NetflixTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Browse movies & series produced by top international & regional studios",
                        color = NetflixTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            items(companies, key = { it.id }) { company ->
                CompanyCatalogCard(
                    company = company,
                    isSaved = savedCompanies.any { it.companyId == company.id },
                    selectedRegion = selectedRegion,
                    onClick = { onSelectCompany(company) },
                    onToggleSave = { onToggleSaveCompany(company) }
                )
            }
        }
    }
}

@Composable
fun CompanyLogoDisplay(
    company: ProductionCompanyInfo,
    modifier: Modifier = Modifier
) {
    if (!company.fullLogoUrl.isNullOrBlank()) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(company.fullLogoUrl)
                .crossfade(true)
                .build(),
            contentDescription = company.name,
            contentScale = ContentScale.Fit,
            modifier = modifier,
            loading = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(color = NetflixRed, modifier = Modifier.size(16.dp), strokeWidth = 1.5.dp)
                }
            },
            error = {
                CompanyFallbackBadge(company)
            }
        )
    } else {
        CompanyFallbackBadge(company)
    }
}

@Composable
fun CompanyFallbackBadge(company: ProductionCompanyInfo) {
    val displayName = company.name.ifBlank { "STUDIO" }
    val shortName = displayName.split(" ").take(2).joinToString(" ").uppercase()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF2E090C), Color(0xFF141414))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = shortName,
            color = NetflixRed,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun CompanyCatalogCard(
    company: ProductionCompanyInfo,
    isSaved: Boolean,
    selectedRegion: ContentRegion = ContentRegion.GLOBAL,
    onClick: () -> Unit,
    onToggleSave: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NetflixCardElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, NetflixBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("company_card_${company.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                CompanyLogoDisplay(
                    company = company,
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = onToggleSave,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save Company",
                        tint = if (isSaved) NetflixRed else NetflixTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (selectedRegion == ContentRegion.ARABIC && company.arabicName.isNotBlank()) company.arabicName else company.name,
                color = NetflixTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (selectedRegion == ContentRegion.ARABIC && company.arabicName.isNotBlank() && company.arabicName != company.name) {
                Text(
                    text = company.name,
                    color = NetflixTextSecondary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (company.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = company.description,
                    color = NetflixTextMuted,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

@Composable
fun CompanyDetailScreen(
    company: ProductionCompanyInfo,
    movies: List<TmdbMovie>,
    tvShows: List<TmdbTv>,
    isLoading: Boolean,
    isSaved: Boolean,
    watchlist: List<WatchlistEntity>,
    selectedRegion: ContentRegion = ContentRegion.GLOBAL,
    onBack: () -> Unit,
    onToggleSave: () -> Unit,
    onMovieClick: (Int) -> Unit,
    onTvClick: (Int) -> Unit,
    onPlayMovie: (TmdbMovie) -> Unit,
    onToggleWatchlistMovie: (TmdbMovie) -> Unit,
    onToggleWatchlistTv: (TmdbTv) -> Unit,
    onSortChanged: (CompanySortOption, SortDirection) -> Unit
) {
    var isMoviesTab by remember { mutableStateOf(true) } // Default Movies First
    var selectedSortOption by remember { mutableStateOf(CompanySortOption.POPULARITY) } // Default Popularity
    var selectedSortDirection by remember { mutableStateOf(SortDirection.ASC) } // Default Ascending

    var isSortMenuOpen by remember { mutableStateOf(false) }
    var sortDialogStep by remember { mutableStateOf<CompanySortOption?>(null) } // Non-null when choosing asc/dsc

    val isMovieSaved = { id: Int -> watchlist.any { it.id == id } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .statusBarsPadding()
            .testTag("company_detail_screen"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Header with Back, Title & Save
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NetflixCardElevated)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = onToggleSave,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NetflixCardElevated)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save Company",
                            tint = if (isSaved) NetflixRed else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CompanyLogoDisplay(
                            company = company,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = if (selectedRegion == ContentRegion.ARABIC && company.arabicName.isNotBlank()) company.arabicName else company.name,
                            color = NetflixTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (selectedRegion == ContentRegion.ARABIC && company.arabicName.isNotBlank() && company.arabicName != company.name) {
                            Text(
                                text = company.name,
                                color = NetflixTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                        if (company.originCountry.isNotBlank()) {
                            Text(
                                text = "Country: ${company.originCountry}",
                                color = NetflixTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Two Main Controls:
                // 1. Movie vs TV Show Button (Movies First)
                // 2. Sort Button (Popularity, Rating, Release -> then Asc / Dsc)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Switch Button: Movies / TV Series
                    Surface(
                        onClick = { isMoviesTab = !isMoviesTab },
                        shape = RoundedCornerShape(8.dp),
                        color = NetflixCardElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NetflixRed.copy(alpha = 0.6f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isMoviesTab) Icons.Default.Movie else Icons.Default.Tv,
                                contentDescription = null,
                                tint = NetflixRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isMoviesTab) "Movies (${movies.size})" else "TV Shows (${tvShows.size})",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = NetflixTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Sort Button
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            onClick = { isSortMenuOpen = true },
                            shape = RoundedCornerShape(8.dp),
                            color = NetflixCardElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, NetflixBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = null,
                                    tint = NetflixGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${selectedSortOption.label} (${if (selectedSortDirection == SortDirection.ASC) "Asc" else "Desc"})",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = NetflixTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Dropdown menu for Sort Option
                        DropdownMenu(
                            expanded = isSortMenuOpen,
                            onDismissRequest = { isSortMenuOpen = false },
                            modifier = Modifier.background(NetflixDarkSurface)
                        ) {
                            CompanySortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.label,
                                            color = if (selectedSortOption == option) NetflixRed else NetflixTextPrimary,
                                            fontWeight = if (selectedSortOption == option) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        isSortMenuOpen = false
                                        sortDialogStep = option
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Content List (Movies or TV Shows)
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NetflixRed)
                }
            }
        } else if (isMoviesTab) {
            if (movies.isEmpty()) {
                item {
                    EmptyCompanyContentView(text = "No movies found for this studio with selected sort.")
                }
            } else {
                items(movies.chunked(3)) { rowMovies ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowMovies.forEach { movie ->
                            Box(modifier = Modifier.weight(1f)) {
                                MediaPosterCard(
                                    title = movie.displayTitle,
                                    secondaryTitle = movie.originalTitle,
                                    posterUrl = movie.fullPosterUrl,
                                    rating = movie.voteAverage,
                                    year = movie.year,
                                    isSaved = isMovieSaved(movie.id),
                                    onClick = { onMovieClick(movie.id) },
                                    onPlayClick = { onPlayMovie(movie) },
                                    onBookmarkToggle = { onToggleWatchlistMovie(movie) }
                                )
                            }
                        }
                        repeat(3 - rowMovies.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            if (tvShows.isEmpty()) {
                item {
                    EmptyCompanyContentView(text = "No series found for this studio with selected sort.")
                }
            } else {
                items(tvShows.chunked(3)) { rowTv ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowTv.forEach { tv ->
                            Box(modifier = Modifier.weight(1f)) {
                                MediaPosterCard(
                                    title = tv.displayTitle,
                                    secondaryTitle = tv.originalName,
                                    posterUrl = tv.fullPosterUrl,
                                    rating = tv.voteAverage,
                                    year = tv.year,
                                    isSaved = isMovieSaved(tv.id),
                                    onClick = { onTvClick(tv.id) },
                                    onPlayClick = { onTvClick(tv.id) },
                                    onBookmarkToggle = { onToggleWatchlistTv(tv) }
                                )
                            }
                        }
                        repeat(3 - rowTv.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    // Modal / Dialog for choosing Ascending vs Descending after picking sort
    if (sortDialogStep != null) {
        val pickedOption = sortDialogStep!!
        AlertDialog(
            onDismissRequest = { sortDialogStep = null },
            containerColor = NetflixDarkSurface,
            title = {
                Text(
                    text = "Order for ${pickedOption.label}",
                    color = NetflixTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Choose sort direction:",
                        color = NetflixTextSecondary,
                        fontSize = 13.sp
                    )
                    Button(
                        onClick = {
                            selectedSortOption = pickedOption
                            selectedSortDirection = SortDirection.ASC
                            sortDialogStep = null
                            onSortChanged(pickedOption, SortDirection.ASC)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedSortOption == pickedOption && selectedSortDirection == SortDirection.ASC) NetflixRed else NetflixCardElevated
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (selectedRegion == ContentRegion.ARABIC) "تصاعدي (Ascending)" else "Ascending")
                    }
                    Button(
                        onClick = {
                            selectedSortOption = pickedOption
                            selectedSortDirection = SortDirection.DESC
                            sortDialogStep = null
                            onSortChanged(pickedOption, SortDirection.DESC)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedSortOption == pickedOption && selectedSortDirection == SortDirection.DESC) NetflixRed else NetflixCardElevated
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (selectedRegion == ContentRegion.ARABIC) "تنازلي (Descending)" else "Descending")
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun EmptyCompanyContentView(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = NetflixTextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SubmittedCompaniesResultsGrid(
    searchQuery: String,
    results: List<ProductionCompanyInfo>,
    isSearching: Boolean,
    selectedRegion: ContentRegion = ContentRegion.GLOBAL,
    onCompanyClick: (ProductionCompanyInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = "Search \"$searchQuery\"",
                color = NetflixTextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
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
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No production companies found for \"$searchQuery\"",
                        color = NetflixTextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(results, key = { it.id }) { company ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = NetflixCardElevated),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCompanyClick(company) }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
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
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (selectedRegion == ContentRegion.ARABIC && company.arabicName.isNotBlank()) company.arabicName else company.name,
                                color = NetflixTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            if (selectedRegion == ContentRegion.ARABIC && company.arabicName.isNotBlank() && company.arabicName != company.name) {
                                Text(
                                    text = company.name,
                                    color = NetflixTextSecondary,
                                    fontSize = 10.sp,
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
