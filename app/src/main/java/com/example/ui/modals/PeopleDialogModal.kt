package com.example.ui.modals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.WatchlistEntity
import com.example.data.model.TmdbMovie
import com.example.data.model.TmdbPerson
import com.example.data.model.TmdbPersonDetail
import com.example.data.model.TmdbTv
import com.example.ui.components.MediaPosterCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleDialogModal(
    searchQuery: String,
    searchResults: List<TmdbPerson>,
    popularPeople: List<TmdbPerson>,
    isSearching: Boolean,
    selectedPerson: TmdbPersonDetail?,
    isLoadingPerson: Boolean,
    watchlist: List<WatchlistEntity>,
    onSearchQueryChanged: (String) -> Unit,
    onSubmitSearch: () -> Unit,
    onSelectPerson: (Int) -> Unit,
    onBackFromPerson: () -> Unit,
    onMovieClick: (Int) -> Unit,
    onTvClick: (Int) -> Unit,
    onPlayMovie: (TmdbMovie) -> Unit,
    onToggleWatchlistMovie: (TmdbMovie) -> Unit,
    onToggleWatchlistTv: (TmdbTv) -> Unit,
    onReportPerson: (TmdbPersonDetail) -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                .statusBarsPadding()
                .testTag("people_dialog_modal")
        ) {
            if (selectedPerson != null) {
                PersonDetailView(
                    person = selectedPerson,
                    isLoading = isLoadingPerson,
                    watchlist = watchlist,
                    onBack = onBackFromPerson,
                    onMovieClick = onMovieClick,
                    onTvClick = onTvClick,
                    onPlayMovie = onPlayMovie,
                    onToggleWatchlistMovie = onToggleWatchlistMovie,
                    onToggleWatchlistTv = onToggleWatchlistTv,
                    onReport = { onReportPerson(selectedPerson) },
                    onClose = onDismiss
                )
            } else {
                PeopleSearchView(
                    searchQuery = searchQuery,
                    searchResults = searchResults,
                    popularPeople = popularPeople,
                    isSearching = isSearching,
                    onSearchQueryChanged = onSearchQueryChanged,
                    onSubmitSearch = onSubmitSearch,
                    onSelectPerson = onSelectPerson,
                    onClose = onDismiss
                )
            }
        }
    }
}

@Composable
fun PeopleSearchView(
    searchQuery: String,
    searchResults: List<TmdbPerson>,
    popularPeople: List<TmdbPerson>,
    isSearching: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onSubmitSearch: () -> Unit,
    onSelectPerson: (Int) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = NetflixRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Actors & Directors (People)",
                    color = NetflixTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NetflixCardElevated)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text("Search actor by name...", color = NetflixTextMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = NetflixTextSecondary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = NetflixTextSecondary)
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSubmitSearch() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NetflixRed,
                unfocusedBorderColor = NetflixBorder,
                focusedContainerColor = NetflixCardElevated,
                unfocusedContainerColor = NetflixCardElevated,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("people_search_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        val listToShow = if (searchQuery.isNotBlank()) searchResults else popularPeople

        if (isSearching && listToShow.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NetflixRed)
            }
        } else if (listToShow.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (searchQuery.isNotBlank()) "No actors found for \"$searchQuery\"" else "No people available.",
                    color = NetflixTextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            Text(
                text = if (searchQuery.isNotBlank()) "Search Results" else "Trending Actors & Celebrities",
                color = NetflixTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(100.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(listToShow, key = { it.id }) { person ->
                    Surface(
                        onClick = { onSelectPerson(person.id) },
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(NetflixCardElevated)
                            ) {
                                if (person.fullProfileUrl != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(person.fullProfileUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = person.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = NetflixTextMuted,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .align(Alignment.Center)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = person.name,
                                color = NetflixTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!person.knownForDepartment.isNullOrBlank()) {
                                Text(
                                    text = person.knownForDepartment,
                                    color = NetflixTextMuted,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
fun PersonDetailView(
    person: TmdbPersonDetail,
    isLoading: Boolean,
    watchlist: List<WatchlistEntity>,
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
    onTvClick: (Int) -> Unit,
    onPlayMovie: (TmdbMovie) -> Unit,
    onToggleWatchlistMovie: (TmdbMovie) -> Unit,
    onToggleWatchlistTv: (TmdbTv) -> Unit,
    onReport: () -> Unit = {},
    onClose: () -> Unit
) {
    var isMoviesTab by remember { mutableStateOf(true) } // Requirement: Switch between movies and series, movies first!
    val isMovieSaved = { id: Int -> watchlist.any { it.id == id } }

    val movies = person.movieCredits?.cast?.sortedByDescending { it.voteAverage } ?: emptyList()
    val tvShows = person.tvCredits?.cast?.sortedByDescending { it.voteAverage } ?: emptyList()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Header with Back and Close
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NetflixCardElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onReport,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NetflixCardElevated)
                            .testTag("report_person_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Report Person",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NetflixCardElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Profile info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(NetflixCardElevated)
                ) {
                    if (person.fullProfileUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(person.fullProfileUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = person.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = person.name,
                        color = NetflixTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!person.knownForDepartment.isNullOrBlank()) {
                        Text(
                            text = person.knownForDepartment,
                            color = NetflixRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (!person.placeOfBirth.isNullOrBlank()) {
                        Text(
                            text = person.placeOfBirth,
                            color = NetflixTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    if (!person.birthday.isNullOrBlank()) {
                        Text(
                            text = "Born: ${person.birthday}",
                            color = NetflixTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (!person.biography.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Biography",
                    color = NetflixTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = person.biography,
                    color = NetflixTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Switcher: Movies / TV Series (Movies first as required!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = { isMoviesTab = true },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isMoviesTab) NetflixRed else NetflixCardElevated,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Movies (${movies.size})",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    onClick = { isMoviesTab = false },
                    shape = RoundedCornerShape(8.dp),
                    color = if (!isMoviesTab) NetflixRed else NetflixCardElevated,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Tv, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TV Shows (${tvShows.size})",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Long Report Person Button below the two buttons (Movies & TV Shows)
            OutlinedButton(
                onClick = onReport,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White.copy(alpha = 0.8f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("detail_report_person_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = NetflixRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Report Person",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Works Grid
        if (isMoviesTab) {
            if (movies.isEmpty()) {
                item {
                    Text(
                        text = "No movies recorded for this person.",
                        color = NetflixTextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    )
                }
            } else {
                items(movies.chunked(3)) { rowMovies ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
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
                    Text(
                        text = "No series recorded for this person.",
                        color = NetflixTextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    )
                }
            } else {
                items(tvShows.chunked(3)) { rowTv ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
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
}
