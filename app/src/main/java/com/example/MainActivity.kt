package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.report.ReportTargetType
import com.example.ui.components.AppTopBar
import com.example.ui.components.BottomNavBar
import com.example.ui.components.SettingsDrawer
import com.example.ui.modals.MediaDetailModal
import com.example.ui.modals.PeopleDialogModal
import com.example.ui.modals.ReportDialog
import com.example.ui.modals.ResolutionOverlayModal
import com.example.ui.modals.StreamOptionDialog
import com.example.ui.modals.UnavailableContentDialog
import com.example.ui.player.PlayerScreen
import com.example.ui.screens.CompaniesScreen
import com.example.ui.screens.MoviesScreen
import com.example.ui.screens.RecentScreen
import com.example.ui.screens.SavedScreen
import com.example.ui.screens.SeriesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NetflixBlack
import com.example.viewmodel.AppTab
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.ReportViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedContentRegion by viewModel.selectedContentRegion.collectAsStateWithLifecycle()

    val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()
    val apiKeyInput by viewModel.apiKeyInput.collectAsStateWithLifecycle()
    val isKeyTesting by viewModel.isKeyTesting.collectAsStateWithLifecycle()
    val keyValidationStatus by viewModel.keyValidationStatus.collectAsStateWithLifecycle()

    // Movies tab state
    val heroMovie by viewModel.heroMovie.collectAsStateWithLifecycle()
    val newReleaseMovies by viewModel.newReleaseMovies.collectAsStateWithLifecycle()
    val trendingMovies by viewModel.trendingMovies.collectAsStateWithLifecycle()
    val popularMovies by viewModel.popularMovies.collectAsStateWithLifecycle()
    val topRatedMovies by viewModel.topRatedMovies.collectAsStateWithLifecycle()
    val genreMovies by viewModel.genreMovies.collectAsStateWithLifecycle()
    val selectedMovieGenre by viewModel.selectedMovieGenre.collectAsStateWithLifecycle()
    val movieSearchQuery by viewModel.movieSearchQuery.collectAsStateWithLifecycle()
    val movieSuggestions by viewModel.movieSuggestions.collectAsStateWithLifecycle()
    val movieSearchResults by viewModel.movieSearchResults.collectAsStateWithLifecycle()
    val isMovieSearchSubmitted by viewModel.isMovieSearchSubmitted.collectAsStateWithLifecycle()
    val isSearchingMovies by viewModel.isSearchingMovies.collectAsStateWithLifecycle()
    val isMoviesLoading by viewModel.isMoviesLoading.collectAsStateWithLifecycle()

    // Series tab state
    val heroTv by viewModel.heroTv.collectAsStateWithLifecycle()
    val newReleaseTv by viewModel.newReleaseTv.collectAsStateWithLifecycle()
    val trendingTv by viewModel.trendingTv.collectAsStateWithLifecycle()
    val popularTv by viewModel.popularTv.collectAsStateWithLifecycle()
    val topRatedTv by viewModel.topRatedTv.collectAsStateWithLifecycle()
    val genreTv by viewModel.genreTv.collectAsStateWithLifecycle()
    val selectedTvGenre by viewModel.selectedTvGenre.collectAsStateWithLifecycle()
    val tvSearchQuery by viewModel.tvSearchQuery.collectAsStateWithLifecycle()
    val tvSuggestions by viewModel.tvSuggestions.collectAsStateWithLifecycle()
    val tvSearchResults by viewModel.tvSearchResults.collectAsStateWithLifecycle()
    val isTvSearchSubmitted by viewModel.isTvSearchSubmitted.collectAsStateWithLifecycle()
    val isSearchingTv by viewModel.isSearchingTv.collectAsStateWithLifecycle()
    val isSeriesLoading by viewModel.isSeriesLoading.collectAsStateWithLifecycle()

    // Details Modal State
    val activeMovieDetail by viewModel.activeMovieDetail.collectAsStateWithLifecycle()
    val activeTvDetail by viewModel.activeTvDetail.collectAsStateWithLifecycle()
    val selectedSeasonNumber by viewModel.selectedSeasonNumber.collectAsStateWithLifecycle()
    val seasonDetail by viewModel.seasonDetail.collectAsStateWithLifecycle()
    val isLoadingDetail by viewModel.isLoadingDetail.collectAsStateWithLifecycle()
    val isLoadingSeason by viewModel.isLoadingSeason.collectAsStateWithLifecycle()

    // Phase 2 Web & Direct Resolution Overlay State
    val isResolvingEmbed by viewModel.isResolvingEmbed.collectAsStateWithLifecycle()
    val selectedEmbedProvider by viewModel.selectedEmbedProvider.collectAsStateWithLifecycle()
    val resolvingTargetInfo by viewModel.resolvingTargetInfo.collectAsStateWithLifecycle()
    val resolutionLogs by viewModel.resolutionLogs.collectAsStateWithLifecycle()
    val detectedStreamMedia by viewModel.detectedStreamMedia.collectAsStateWithLifecycle()

    // Phase 4 Phone Remote State
    val isPhoneRemoteActive by viewModel.isPhoneRemoteActive.collectAsStateWithLifecycle()
    val playbackDestination by viewModel.playbackDestination.collectAsStateWithLifecycle()

    // Player state
    val activeStream by viewModel.activeStream.collectAsStateWithLifecycle()
    val isResolvingStream by viewModel.isResolvingStream.collectAsStateWithLifecycle()

    // Room Watchlist & Recent History & Real Search Histories
    val watchlist by viewModel.watchlistItems.collectAsStateWithLifecycle()
    val recentHistory by viewModel.recentHistoryItems.collectAsStateWithLifecycle()
    val movieSearchHistory by viewModel.movieSearchHistory.collectAsStateWithLifecycle()
    val tvSearchHistory by viewModel.tvSearchHistory.collectAsStateWithLifecycle()

    // Companies tab state
    val companies by viewModel.companies.collectAsStateWithLifecycle()
    val companySearchQuery by viewModel.companySearchQuery.collectAsStateWithLifecycle()
    val companySuggestions by viewModel.companySuggestions.collectAsStateWithLifecycle()
    val companySearchResults by viewModel.companySearchResults.collectAsStateWithLifecycle()
    val isCompanySearchSubmitted by viewModel.isCompanySearchSubmitted.collectAsStateWithLifecycle()
    val isSearchingCompanies by viewModel.isSearchingCompanies.collectAsStateWithLifecycle()
    val selectedCompany by viewModel.selectedCompany.collectAsStateWithLifecycle()
    val companyMovies by viewModel.companyMovies.collectAsStateWithLifecycle()
    val companyTv by viewModel.companyTv.collectAsStateWithLifecycle()
    val isCompanyLoading by viewModel.isCompanyLoading.collectAsStateWithLifecycle()
    val savedCompanies by viewModel.savedCompanies.collectAsStateWithLifecycle()

    // People dialog state
    val isPeopleDialogOpen by viewModel.isPeopleDialogOpen.collectAsStateWithLifecycle()
    val peopleSearchQuery by viewModel.peopleSearchQuery.collectAsStateWithLifecycle()
    val peopleSearchResults by viewModel.peopleSearchResults.collectAsStateWithLifecycle()
    val popularPeople by viewModel.popularPeople.collectAsStateWithLifecycle()
    val isSearchingPeople by viewModel.isSearchingPeople.collectAsStateWithLifecycle()
    val selectedPerson by viewModel.selectedPerson.collectAsStateWithLifecycle()
    val isLoadingPerson by viewModel.isLoadingPerson.collectAsStateWithLifecycle()

    // Smart BackHandler navigation: return to suggestions / close search / dismiss modals before exiting
    val isMovieSearching = currentTab == AppTab.MOVIES && (isMovieSearchSubmitted || movieSearchQuery.isNotEmpty())
    val isTvSearching = currentTab == AppTab.SERIES && (isTvSearchSubmitted || tvSearchQuery.isNotEmpty())
    val isCompanySearching = currentTab == AppTab.COMPANIES && (isCompanySearchSubmitted || companySearchQuery.isNotEmpty())
    val isCompanyDetailActive = currentTab == AppTab.COMPANIES && selectedCompany != null
    val isAnyGenreActive = (currentTab == AppTab.MOVIES && selectedMovieGenre != null) ||
            (currentTab == AppTab.SERIES && selectedTvGenre != null)
    val isSubTabActive = currentTab != AppTab.MOVIES

    BackHandler(
        enabled = activeStream != null || isResolvingEmbed || isPhoneRemoteActive || isSettingsOpen ||
                selectedPerson != null || isPeopleDialogOpen ||
                activeMovieDetail != null || activeTvDetail != null ||
                isCompanyDetailActive || isCompanySearching ||
                isMovieSearching || isTvSearching || isAnyGenreActive || isSubTabActive
    ) {
        when {
            activeStream != null || isResolvingEmbed -> {
                viewModel.cancelStreamResolution()
                viewModel.closePlayer()
            }
            isPhoneRemoteActive -> {
                viewModel.closePhoneRemote()
            }
            isSettingsOpen -> {
                viewModel.closeSettings()
            }
            activeMovieDetail != null || activeTvDetail != null -> {
                viewModel.closeDetailModal()
            }
            selectedPerson != null -> {
                viewModel.closePersonDetail()
            }
            isPeopleDialogOpen -> {
                viewModel.closePeopleDialog()
            }
            isCompanyDetailActive -> {
                viewModel.closeCompanyDetail()
            }
            isCompanySearching -> {
                viewModel.clearCompanySearch()
            }
            isMovieSearching -> {
                viewModel.clearMovieSearch()
            }
            isTvSearching -> {
                viewModel.clearTvSearch()
            }
            currentTab == AppTab.MOVIES && selectedMovieGenre != null -> {
                viewModel.selectMovieGenre(null)
            }
            currentTab == AppTab.SERIES && selectedTvGenre != null -> {
                viewModel.selectTvGenre(null)
            }
            isSubTabActive -> {
                viewModel.setTab(AppTab.MOVIES)
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val isDetailMovieSaved = activeMovieDetail?.let { movie ->
        watchlist.any { it.id == movie.id }
    } ?: false

    val isDetailTvSaved = activeTvDetail?.let { tv ->
        watchlist.any { it.id == tv.id }
    } ?: false

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixBlack)
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = NetflixBlack,
            topBar = {
                if (activeStream == null && !isResolvingEmbed) {
                    AppTopBar(
                        currentTab = currentTab,
                        hasCustomApiKey = viewModel.apiKeyStore.hasCustomKey(),
                        isExternalStreamingActive = isPhoneRemoteActive,
                        selectedRegion = selectedContentRegion,
                        onSelectRegion = { region -> viewModel.selectContentRegion(region) },
                        searchQuery = when (currentTab) {
                            AppTab.MOVIES -> movieSearchQuery
                            AppTab.SERIES -> tvSearchQuery
                            AppTab.COMPANIES -> companySearchQuery
                            else -> ""
                        },
                        searchHistory = when (currentTab) {
                            AppTab.MOVIES -> movieSearchHistory
                            AppTab.SERIES -> tvSearchHistory
                            else -> emptyList()
                        },
                        isSearchSubmitted = when (currentTab) {
                            AppTab.MOVIES -> isMovieSearchSubmitted
                            AppTab.SERIES -> isTvSearchSubmitted
                            AppTab.COMPANIES -> isCompanySearchSubmitted
                            else -> false
                        },
                        isSearching = when (currentTab) {
                            AppTab.MOVIES -> isSearchingMovies
                            AppTab.SERIES -> isSearchingTv
                            AppTab.COMPANIES -> isSearchingCompanies
                            else -> false
                        },
                        movieSuggestions = movieSuggestions,
                        tvSuggestions = tvSuggestions,
                        companySuggestions = companySuggestions,
                        onSearchQueryChanged = { q ->
                            when (currentTab) {
                                AppTab.MOVIES -> viewModel.onMovieSearchQueryChanged(q)
                                AppTab.SERIES -> viewModel.onTvSearchQueryChanged(q)
                                AppTab.COMPANIES -> viewModel.onCompanySearchQueryChanged(q)
                                else -> {}
                            }
                        },
                        onSubmitSearch = {
                            when (currentTab) {
                                AppTab.MOVIES -> viewModel.submitMovieSearch()
                                AppTab.SERIES -> viewModel.submitTvSearch()
                                AppTab.COMPANIES -> viewModel.submitCompanySearch()
                                else -> {}
                            }
                        },
                        onClearSearch = {
                            when (currentTab) {
                                AppTab.MOVIES -> viewModel.clearMovieSearch()
                                AppTab.SERIES -> viewModel.clearTvSearch()
                                AppTab.COMPANIES -> viewModel.clearCompanySearch()
                                else -> {}
                            }
                        },
                        onSelectMovieSuggestion = { id -> viewModel.openMovieDetail(id) },
                        onSelectTvSuggestion = { id -> viewModel.openTvDetail(id) },
                        onSelectCompanySuggestion = { company -> viewModel.selectCompany(company) },
                        onRemoveSearchHistoryItem = { term ->
                            when (currentTab) {
                                AppTab.MOVIES -> viewModel.removeMovieSearchHistoryItem(term)
                                AppTab.SERIES -> viewModel.removeTvSearchHistoryItem(term)
                                else -> {}
                            }
                        },
                        onClearAllSearchHistory = {
                            when (currentTab) {
                                AppTab.MOVIES -> viewModel.clearMovieSearchHistory()
                                AppTab.SERIES -> viewModel.clearTvSearchHistory()
                                else -> {}
                            }
                        },
                        onOpenPeopleDialog = { viewModel.openPeopleDialog() },
                        onOpenRemoteControl = { viewModel.openPhoneRemoteDirect() },
                        onOpenSettings = { viewModel.openSettings() }
                    )
                }
            },
            bottomBar = {
                if (activeStream == null && !isResolvingEmbed) {
                    BottomNavBar(
                        currentTab = currentTab,
                        onTabSelected = { tab -> viewModel.setTab(tab) },
                        savedCount = watchlist.size,
                        recentCount = recentHistory.size
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Main Content depending on selected Tab
                when (currentTab) {
                    AppTab.MOVIES -> {
                        MoviesScreen(
                            heroMovie = heroMovie,
                            newReleaseMovies = newReleaseMovies,
                            trendingMovies = trendingMovies,
                            popularMovies = popularMovies,
                            topRatedMovies = topRatedMovies,
                            genreMovies = genreMovies,
                            selectedGenreId = selectedMovieGenre,
                            genres = viewModel.movieGenres,
                            searchQuery = movieSearchQuery,
                            searchResults = movieSearchResults,
                            isSearchSubmitted = isMovieSearchSubmitted,
                            isSearching = isSearchingMovies,
                            isLoading = isMoviesLoading,
                            watchlist = watchlist,
                            onGenreSelected = { id -> viewModel.selectMovieGenre(id) },
                            onMovieClick = { id -> viewModel.openMovieDetail(id) },
                            onPlayMovie = { movie -> viewModel.playMovieSimple(movie) },
                            onToggleWatchlist = { movie -> viewModel.toggleWatchlistMovie(movie) },
                            onRefresh = { viewModel.loadMovies() }
                        )
                    }

                    AppTab.SERIES -> {
                        SeriesScreen(
                            heroTv = heroTv,
                            newReleaseTv = newReleaseTv,
                            trendingTv = trendingTv,
                            popularTv = popularTv,
                            topRatedTv = topRatedTv,
                            genreTv = genreTv,
                            selectedGenreId = selectedTvGenre,
                            genres = viewModel.tvGenres,
                            searchQuery = tvSearchQuery,
                            searchResults = tvSearchResults,
                            isSearchSubmitted = isTvSearchSubmitted,
                            isSearching = isSearchingTv,
                            isLoading = isSeriesLoading,
                            watchlist = watchlist,
                            onGenreSelected = { id -> viewModel.selectTvGenre(id) },
                            onTvClick = { id -> viewModel.openTvDetail(id) },
                            onToggleWatchlist = { tv -> viewModel.toggleWatchlistTv(tv) },
                            onRefresh = { viewModel.loadSeries() }
                        )
                    }

                    AppTab.COMPANIES -> {
                        CompaniesScreen(
                            companies = companies,
                            searchQuery = companySearchQuery,
                            isSearchSubmitted = isCompanySearchSubmitted,
                            isSearching = isSearchingCompanies,
                            searchResults = companySearchResults,
                            selectedCompany = selectedCompany,
                            companyMovies = companyMovies,
                            companyTv = companyTv,
                            isLoading = isCompanyLoading,
                            savedCompanies = savedCompanies,
                            watchlist = watchlist,
                            selectedRegion = selectedContentRegion,
                            onSelectCompany = { company -> viewModel.selectCompany(company) },
                            onCloseCompany = { viewModel.closeCompanyDetail() },
                            onToggleSaveCompany = { company -> viewModel.toggleSaveCompany(company) },
                            onMovieClick = { id -> viewModel.openMovieDetail(id) },
                            onTvClick = { id -> viewModel.openTvDetail(id) },
                            onPlayMovie = { movie -> viewModel.playMovieSimple(movie) },
                            onToggleWatchlistMovie = { movie -> viewModel.toggleWatchlistMovie(movie) },
                            onToggleWatchlistTv = { tv -> viewModel.toggleWatchlistTv(tv) },
                            onSortChanged = { option, dir -> viewModel.updateCompanySort(option, dir) }
                        )
                    }

                    AppTab.SAVED -> {
                        SavedScreen(
                            watchlist = watchlist,
                            savedCompanies = savedCompanies,
                            selectedRegion = selectedContentRegion,
                            onItemClick = { item -> viewModel.playFromWatchlist(item) },
                            onRemoveItem = { id -> viewModel.removeFromWatchlist(id) },
                            onOpenCompany = { companyId -> viewModel.openCompanyById(companyId) },
                            onRemoveCompany = { companyId -> viewModel.removeSavedCompany(companyId) },
                            onNavigateToTab = { tab -> viewModel.setTab(tab) }
                        )
                    }

                    AppTab.RECENT -> {
                        RecentScreen(
                            historyList = recentHistory,
                            onResumePlayback = { history -> viewModel.playFromHistory(history) },
                            onDeleteItem = { id -> viewModel.deleteHistoryItem(id) },
                            onClearAllHistory = { viewModel.clearAllHistory() },
                            onNavigateToTab = { tab -> viewModel.setTab(tab) }
                        )
                    }
                }
            }
        }

        // People Dialog Modal
        if (isPeopleDialogOpen && !isResolvingEmbed && activeStream == null) {
            PeopleDialogModal(
                searchQuery = peopleSearchQuery,
                searchResults = peopleSearchResults,
                popularPeople = popularPeople,
                isSearching = isSearchingPeople,
                selectedPerson = selectedPerson,
                isLoadingPerson = isLoadingPerson,
                watchlist = watchlist,
                onSearchQueryChanged = { q -> viewModel.onPeopleSearchQueryChanged(q) },
                onSubmitSearch = { viewModel.submitPeopleSearch() },
                onSelectPerson = { id -> viewModel.selectPerson(id) },
                onBackFromPerson = { viewModel.closePersonDetail() },
                onMovieClick = { id -> viewModel.openMovieDetail(id) },
                onTvClick = { id -> viewModel.openTvDetail(id) },
                onPlayMovie = { movie -> viewModel.playMovieSimple(movie) },
                onToggleWatchlistMovie = { movie -> viewModel.toggleWatchlistMovie(movie) },
                onToggleWatchlistTv = { tv -> viewModel.toggleWatchlistTv(tv) },
                onDismiss = { viewModel.closePeopleDialog() }
            )
        }

        // Details Modal (Movie or TV)
        if (!isResolvingEmbed && activeStream == null) {
            MediaDetailModal(
                movieDetail = activeMovieDetail,
                tvDetail = activeTvDetail,
                seasonDetail = seasonDetail,
                selectedSeasonNumber = selectedSeasonNumber,
                isLoadingDetail = isLoadingDetail,
                isLoadingSeason = isLoadingSeason,
                isSaved = if (activeMovieDetail != null) isDetailMovieSaved else isDetailTvSaved,
                onSeasonSelected = { tvId, seasonNum -> viewModel.selectSeason(tvId, seasonNum) },
                onPlayMovie = { movie -> viewModel.playMovie(movie) },
                onPlayEpisode = { tvDetail, sNum, ep -> viewModel.playTvEpisode(tvDetail, sNum, ep) },
                onToggleWatchlistMovie = { movie -> viewModel.toggleWatchlistMovieDetail(movie) },
                onToggleWatchlistTv = { tv -> viewModel.toggleWatchlistTvDetail(tv) },
                onCompanyClick = { companyId, name -> viewModel.openCompanyById(companyId, name) },
                onPersonClick = { personId -> viewModel.selectPerson(personId) },
                onDismiss = { viewModel.closeDetailModal() }
            )
        }

        // Settings / BYOK Modal Drawer
        SettingsDrawer(
            isOpen = isSettingsOpen,
            currentKeyInput = apiKeyInput,
            isTesting = isKeyTesting,
            validationStatus = keyValidationStatus,
            hasCustomKey = viewModel.apiKeyStore.hasCustomKey(),
            onKeyInputChanged = { newKey -> viewModel.updateApiKeyInput(newKey) },
            onSaveKey = { key -> viewModel.saveAndValidateApiKey(key) },
            onUseDemoKey = { viewModel.useDemoKey() },
            onDismiss = { viewModel.closeSettings() }
        )

        // Phase 2 Stream Option Dialog ([ Option 1: Play Local ] vs [ Option 2: Stream via LAN ])
        if (detectedStreamMedia != null && playbackDestination == com.example.viewmodel.PlaybackDestination.NONE) {
            StreamOptionDialog(
                streamMedia = detectedStreamMedia,
                onPlayLocal = { viewModel.playDetectedLocalStream() },
                onStreamViaLan = { viewModel.launchPhoneRemote() },
                onDismiss = { viewModel.dismissStreamOptionDialog() }
            )
        }

        // Phase 4 Phone Remote Control View
        if (isPhoneRemoteActive) {
            com.example.ui.remote.PhoneRemoteControlScreen(
                onResyncLink = { viewModel.refreshStreamSession() },
                onClose = { viewModel.closePhoneRemote() }
            )
        }

        // Fullscreen Player & Loading Screen (Landscape)
        if (isResolvingEmbed || activeStream != null) {
            val resolvingEmbedUrl = if (resolvingTargetInfo != null) {
                com.example.player.EmbedStreamResolver.getEmbedUrl(
                    provider = selectedEmbedProvider,
                    mediaId = resolvingTargetInfo!!.mediaId,
                    mediaType = resolvingTargetInfo!!.mediaType,
                    seasonNumber = resolvingTargetInfo!!.seasonNumber,
                    episodeNumber = resolvingTargetInfo!!.episodeNumber
                )
            } else null

            PlayerScreen(
                stream = activeStream,
                isResolving = isResolvingEmbed,
                resolvingTitle = resolvingTargetInfo?.title ?: activeStream?.title ?: "Zvid Media",
                embedUrl = resolvingEmbedUrl,
                providerReferer = selectedEmbedProvider.defaultReferer,
                logs = resolutionLogs,
                selectedProvider = selectedEmbedProvider,
                onProviderChange = { provider -> viewModel.selectEmbedProvider(provider) },
                onNextEpisode = { viewModel.playNextEpisode() },
                onPreviousEpisode = { viewModel.playPreviousEpisode() },
                onUpdateProgress = { cur, total -> viewModel.updatePlaybackProgress(cur, total) },
                onRefreshSession = { viewModel.refreshStreamSession() },
                onClose = {
                    viewModel.cancelStreamResolution()
                    viewModel.closePlayer()
                }
            )
        }
    }
}
