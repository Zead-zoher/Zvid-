package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TmdbMovie
import com.example.data.model.TmdbTv
import com.example.ui.theme.*
import com.example.viewmodel.AppTab
import com.example.viewmodel.ContentRegion

@Composable
fun AppTopBar(
    currentTab: AppTab,
    hasCustomApiKey: Boolean,
    isExternalStreamingActive: Boolean = false,
    selectedRegion: com.example.viewmodel.ContentRegion = com.example.viewmodel.ContentRegion.GLOBAL,
    onSelectRegion: (com.example.viewmodel.ContentRegion) -> Unit = {},
    searchQuery: String,
    searchHistory: List<String> = emptyList(),
    isSearchSubmitted: Boolean = false,
    isSearching: Boolean = false,
    movieSuggestions: List<TmdbMovie> = emptyList(),
    tvSuggestions: List<TmdbTv> = emptyList(),
    companySuggestions: List<com.example.data.model.ProductionCompanyInfo> = emptyList(),
    onSearchQueryChanged: (String) -> Unit,
    onSubmitSearch: () -> Unit = {},
    onClearSearch: () -> Unit,
    onSelectMovieSuggestion: (Int) -> Unit = {},
    onSelectTvSuggestion: (Int) -> Unit = {},
    onSelectCompanySuggestion: (com.example.data.model.ProductionCompanyInfo) -> Unit = {},
    onRemoveSearchHistoryItem: (String) -> Unit = {},
    onClearAllSearchHistory: () -> Unit = {},
    onOpenPeopleDialog: () -> Unit = {},
    onOpenRemoteControl: () -> Unit = {},
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    var isRegionMenuOpen by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(NetflixBlack)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo & Tab Title & Region Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            if (isSearchExpanded) {
                                isSearchExpanded = false
                                onClearSearch()
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(NetflixRed, Color(0xFF900C12))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Z",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.SansSerif
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = "Zvid",
                                color = NetflixTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = when (currentTab) {
                                    AppTab.MOVIES -> "Movies"
                                    AppTab.SERIES -> "TV Series"
                                    AppTab.COMPANIES -> "Companies"
                                    AppTab.SAVED -> "My Watchlist"
                                    AppTab.RECENT -> "Watch History"
                                },
                                color = NetflixTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Region Selector Button next to Zvid (Movies, Series, Companies tabs)
                    if (currentTab == AppTab.MOVIES || currentTab == AppTab.SERIES || currentTab == AppTab.COMPANIES) {
                        Spacer(modifier = Modifier.width(8.dp))

                        Box {
                            Surface(
                                onClick = { isRegionMenuOpen = true },
                                shape = RoundedCornerShape(14.dp),
                                color = if (selectedRegion != com.example.viewmodel.ContentRegion.GLOBAL) NetflixRed.copy(alpha = 0.2f) else NetflixCardElevated,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (selectedRegion != com.example.viewmodel.ContentRegion.GLOBAL) NetflixRed else NetflixBorder
                                ),
                                modifier = Modifier.testTag("region_selector_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        tint = if (selectedRegion != com.example.viewmodel.ContentRegion.GLOBAL) NetflixRed else NetflixTextSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = selectedRegion.label,
                                        color = if (selectedRegion != com.example.viewmodel.ContentRegion.GLOBAL) Color.White else NetflixTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = NetflixTextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = isRegionMenuOpen,
                                onDismissRequest = { isRegionMenuOpen = false },
                                modifier = Modifier.background(NetflixCardElevated)
                            ) {
                                com.example.viewmodel.ContentRegion.entries.forEach { region ->
                                    val isSelected = region == selectedRegion
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = region.label,
                                                    color = if (isSelected) NetflixRed else NetflixTextPrimary,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 13.sp
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = NetflixRed,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            isRegionMenuOpen = false
                                            onSelectRegion(region)
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // People Small Circular Button (Next to region preference)
                        Surface(
                            onClick = onOpenPeopleDialog,
                            shape = CircleShape,
                            color = NetflixCardElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, NetflixBorder),
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("top_people_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "People / Actors",
                                    tint = NetflixRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Action buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Search toggle button for Movies, Series, and Companies tabs
                    if (currentTab == AppTab.MOVIES || currentTab == AppTab.SERIES || currentTab == AppTab.COMPANIES) {
                        IconButton(
                            onClick = {
                                isSearchExpanded = !isSearchExpanded
                                if (!isSearchExpanded) {
                                    onClearSearch()
                                }
                            },
                            modifier = Modifier.size(38.dp).testTag("top_search_toggle")
                        ) {
                            Icon(
                                imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (isSearchExpanded) NetflixRed else NetflixTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // TV Remote Button
                    if (isExternalStreamingActive) {
                        Surface(
                            onClick = onOpenRemoteControl,
                            shape = CircleShape,
                            color = NetflixRed.copy(alpha = 0.2f),
                            modifier = Modifier.size(38.dp).testTag("remote_control_shortcut_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Tv,
                                    contentDescription = "TV Remote",
                                    tint = NetflixRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .align(Alignment.TopEnd)
                                        .padding(top = 2.dp, end = 2.dp)
                                        .clip(CircleShape)
                                        .background(NetflixGreen)
                                )
                            }
                        }
                    }

                    // TMDB Key Button (Icon only)
                    Surface(
                        onClick = onOpenSettings,
                        shape = CircleShape,
                        color = NetflixCardElevated,
                        modifier = Modifier.size(38.dp).testTag("settings_key_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "TMDB Key",
                                tint = if (hasCustomApiKey) NetflixGreen else NetflixTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .align(Alignment.TopEnd)
                                    .padding(top = 4.dp, end = 4.dp)
                                    .clip(CircleShape)
                                    .background(if (hasCustomApiKey) NetflixGreen else Color(0xFFFFB300))
                            )
                        }
                    }
                }
            }

            // Expanded Search Bar
            AnimatedVisibility(
                visible = isSearchExpanded && (currentTab == AppTab.MOVIES || currentTab == AppTab.SERIES || currentTab == AppTab.COMPANIES),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChanged,
                        placeholder = {
                            Text(
                                text = when (currentTab) {
                                    AppTab.MOVIES -> "Search movies by title, genre, actor..."
                                    AppTab.SERIES -> "Search TV shows, anime, dramas..."
                                    AppTab.COMPANIES -> "Search production companies (Marvel, Warner, Netflix, Shahid...)"
                                    else -> "Search..."
                                },
                                color = NetflixTextMuted,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = NetflixTextSecondary
                            )
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            focusManager.clearFocus()
                                            isSearchExpanded = false
                                            onSubmitSearch()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search Now",
                                            tint = NetflixRed
                                        )
                                    }
                                    IconButton(onClick = onClearSearch) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = NetflixTextSecondary
                                        )
                                    }
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            focusManager.clearFocus()
                            isSearchExpanded = false
                            onSubmitSearch()
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NetflixCardElevated,
                            unfocusedContainerColor = NetflixCardElevated,
                            focusedBorderColor = NetflixRed,
                            unfocusedBorderColor = Color(0xFF333333),
                            focusedTextColor = NetflixTextPrimary,
                            unfocusedTextColor = NetflixTextPrimary,
                            cursorColor = NetflixRed
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .testTag("search_text_field")
                    )

                    // ==================== STATE A: LIVE AUTOCOMPLETE SUGGESTIONS OVERLAY ====================
                    if (!isSearchSubmitted && searchQuery.isNotBlank()) {
                        if (currentTab == AppTab.MOVIES) {
                            LiveMovieSuggestionsOverlay(
                                suggestions = movieSuggestions,
                                isLoading = isSearching,
                                onSelectMovie = { id ->
                                    focusManager.clearFocus()
                                    onSelectMovieSuggestion(id)
                                }
                            )
                        } else if (currentTab == AppTab.SERIES) {
                            LiveTvSuggestionsOverlay(
                                suggestions = tvSuggestions,
                                isLoading = isSearching,
                                onSelectTv = { id ->
                                    focusManager.clearFocus()
                                    onSelectTvSuggestion(id)
                                }
                            )
                        } else if (currentTab == AppTab.COMPANIES) {
                            LiveCompanySuggestionsOverlay(
                                suggestions = companySuggestions,
                                isLoading = isSearching,
                                isArabicRegion = selectedRegion == ContentRegion.ARABIC,
                                onSelectCompany = { company ->
                                    focusManager.clearFocus()
                                    onSelectCompanySuggestion(company)
                                }
                            )
                        }
                    }

                    // ==================== RECENT SEARCHES HISTORY (When input is empty) ====================
                    if (searchHistory.isNotEmpty() && searchQuery.isEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent:",
                                color = NetflixTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            searchHistory.forEach { item ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = NetflixCardElevated,
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 10.dp, end = 4.dp)
                                    ) {
                                        Text(
                                            text = item,
                                            color = NetflixTextPrimary,
                                            fontSize = 11.sp,
                                            modifier = Modifier.clickable {
                                                onSearchQueryChanged(item)
                                                isSearchExpanded = false
                                                onSubmitSearch()
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { onRemoveSearchHistoryItem(item) },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Delete $item",
                                                tint = NetflixTextMuted,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            TextButton(
                                onClick = onClearAllSearchHistory,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text(
                                    text = "Clear All",
                                    color = NetflixRed,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
