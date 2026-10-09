package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.filter.ContentFilter
import com.example.data.filter.RemoteBlocklist
import com.example.data.local.ApiKeyStore
import com.example.data.local.RecentHistoryEntity
import com.example.data.local.SavedCompanyEntity
import com.example.data.local.WatchlistEntity
import com.example.data.local.ZvidDatabase
import com.example.data.model.CompaniesCatalog
import com.example.data.model.ProductionCompanyInfo
import com.example.data.model.TmdbEpisode
import com.example.data.model.TmdbMovie
import com.example.data.model.TmdbMovieDetail
import com.example.data.model.TmdbPerson
import com.example.data.model.TmdbPersonDetail
import com.example.data.model.TmdbSeasonDetail
import com.example.data.model.TmdbTv
import com.example.data.model.TmdbTvDetail
import com.example.data.report.ReportStore
import com.example.data.repository.MediaRepository
import com.example.ui.screens.CompanySortOption
import com.example.ui.screens.SortDirection
import com.example.player.DetectedStreamMedia
import com.example.player.EmbedProvider
import com.example.player.EmbedStreamResolver
import com.example.player.EmbeddedHttpServer
import com.example.player.ResolvedStream
import com.example.player.StreamResolver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    MOVIES,
    SERIES,
    COMPANIES,
    SAVED,
    RECENT
}

data class GenreItem(val id: Int, val name: String)

data class ResolvingTargetInfo(
    val mediaId: Int,
    val mediaType: String,
    val title: String,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val episodeTitle: String? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val initialPositionSeconds: Long = 0L,
    val totalDurationSeconds: Long = 7200L
)

enum class PlaybackDestination {
    NONE,
    LOCAL_DEVICE,
    TV_REMOTE
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val apiKeyStore = ApiKeyStore(application)
    val reportStore = ReportStore(application)
    val remoteBlocklist = RemoteBlocklist(application)
    val searchHistoryStore = com.example.data.local.SearchHistoryStore(application)
    private val database = ZvidDatabase.getDatabase(application)
    val repository = MediaRepository(
        apiKeyStore = apiKeyStore,
        watchlistDao = database.watchlistDao(),
        recentHistoryDao = database.recentHistoryDao(),
        savedCompanyDao = database.savedCompanyDao(),
        reportStore = reportStore
    )

    private val _isDetailUnavailable = MutableStateFlow(false)
    val isDetailUnavailable: StateFlow<Boolean> = _isDetailUnavailable.asStateFlow()

    fun dismissUnavailable() {
        _isDetailUnavailable.value = false
    }

    fun onItemReported(id: Int) {
        if (_activeMovieDetail.value?.id == id) {
            closeDetailModal()
        }
        if (_activeTvDetail.value?.id == id) {
            closeDetailModal()
        }
        if (_selectedCompany.value?.id == id) {
            closeCompanyDetail()
        }
        if (_selectedPerson.value?.id == id) {
            closePersonDetail()
        }
        loadMovies()
        loadSeries()
        loadCompaniesForRegion()
        loadPopularPeople()
    }

    private val _playbackDestination = MutableStateFlow(PlaybackDestination.NONE)
    val playbackDestination: StateFlow<PlaybackDestination> = _playbackDestination.asStateFlow()

    // Active Navigation Tab & Selected Content Origin Region (Global, Arabic, Hollywood, Asian, Euro-Latin)
    private val _currentTab = MutableStateFlow(AppTab.MOVIES)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _selectedContentRegion = MutableStateFlow(ContentRegion.GLOBAL)
    val selectedContentRegion: StateFlow<ContentRegion> = _selectedContentRegion.asStateFlow()

    fun selectContentRegion(region: ContentRegion) {
        _selectedContentRegion.value = region
        // Clear active genre filters to reload fresh feed for newly selected region
        _selectedMovieGenre.value = null
        _selectedTvGenre.value = null
        loadMovies()
        loadSeries()
        loadCompaniesForRegion()
        loadPopularPeople()
        val currentCompany = _selectedCompany.value
        if (currentCompany != null) {
            loadCompanyContent(currentCompany.id, _selectedCompanySortOption.value, _selectedCompanySortDirection.value)
        }
        viewModelScope.launch {
            _userMessage.emit("Switched to ${region.label}")
        }
    }

    // Settings / BYOK Drawer State
    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _apiKeyInput = MutableStateFlow(apiKeyStore.getApiKey())
    val apiKeyInput: StateFlow<String> = _apiKeyInput.asStateFlow()

    private val _isKeyTesting = MutableStateFlow(false)
    val isKeyTesting: StateFlow<Boolean> = _isKeyTesting.asStateFlow()

    private val _keyValidationStatus = MutableStateFlow<String?>(null)
    val keyValidationStatus: StateFlow<String?> = _keyValidationStatus.asStateFlow()

    // Snackbars / Toast events
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // === MOVIES TAB STATE ===
    private val _heroMovie = MutableStateFlow<TmdbMovie?>(null)
    val heroMovie: StateFlow<TmdbMovie?> = _heroMovie.asStateFlow()

    private val _newReleaseMovies = MutableStateFlow<List<TmdbMovie>>(emptyList())
    val newReleaseMovies: StateFlow<List<TmdbMovie>> = _newReleaseMovies.asStateFlow()

    private val _trendingMovies = MutableStateFlow<List<TmdbMovie>>(emptyList())
    val trendingMovies: StateFlow<List<TmdbMovie>> = _trendingMovies.asStateFlow()

    private val _popularMovies = MutableStateFlow<List<TmdbMovie>>(emptyList())
    val popularMovies: StateFlow<List<TmdbMovie>> = _popularMovies.asStateFlow()

    private val _topRatedMovies = MutableStateFlow<List<TmdbMovie>>(emptyList())
    val topRatedMovies: StateFlow<List<TmdbMovie>> = _topRatedMovies.asStateFlow()

    private val _selectedMovieGenre = MutableStateFlow<Int?>(null)
    val selectedMovieGenre: StateFlow<Int?> = _selectedMovieGenre.asStateFlow()

    private val _genreMovies = MutableStateFlow<List<TmdbMovie>>(emptyList())
    val genreMovies: StateFlow<List<TmdbMovie>> = _genreMovies.asStateFlow()

    private val _movieSearchQuery = MutableStateFlow("")
    val movieSearchQuery: StateFlow<String> = _movieSearchQuery.asStateFlow()

    private val _movieSuggestions = MutableStateFlow<List<TmdbMovie>>(emptyList())
    val movieSuggestions: StateFlow<List<TmdbMovie>> = _movieSuggestions.asStateFlow()

    private val _movieSearchResults = MutableStateFlow<List<TmdbMovie>>(emptyList())
    val movieSearchResults: StateFlow<List<TmdbMovie>> = _movieSearchResults.asStateFlow()

    private val _isMovieSearchSubmitted = MutableStateFlow(false)
    val isMovieSearchSubmitted: StateFlow<Boolean> = _isMovieSearchSubmitted.asStateFlow()

    private val _isSearchingMovies = MutableStateFlow(false)
    val isSearchingMovies: StateFlow<Boolean> = _isSearchingMovies.asStateFlow()

    private val _isMoviesLoading = MutableStateFlow(false)
    val isMoviesLoading: StateFlow<Boolean> = _isMoviesLoading.asStateFlow()

    // === SERIES TAB STATE ===
    private val _heroTv = MutableStateFlow<TmdbTv?>(null)
    val heroTv: StateFlow<TmdbTv?> = _heroTv.asStateFlow()

    private val _newReleaseTv = MutableStateFlow<List<TmdbTv>>(emptyList())
    val newReleaseTv: StateFlow<List<TmdbTv>> = _newReleaseTv.asStateFlow()

    private val _trendingTv = MutableStateFlow<List<TmdbTv>>(emptyList())
    val trendingTv: StateFlow<List<TmdbTv>> = _trendingTv.asStateFlow()

    private val _popularTv = MutableStateFlow<List<TmdbTv>>(emptyList())
    val popularTv: StateFlow<List<TmdbTv>> = _popularTv.asStateFlow()

    private val _topRatedTv = MutableStateFlow<List<TmdbTv>>(emptyList())
    val topRatedTv: StateFlow<List<TmdbTv>> = _topRatedTv.asStateFlow()

    private val _selectedTvGenre = MutableStateFlow<Int?>(null)
    val selectedTvGenre: StateFlow<Int?> = _selectedTvGenre.asStateFlow()

    private val _genreTv = MutableStateFlow<List<TmdbTv>>(emptyList())
    val genreTv: StateFlow<List<TmdbTv>> = _genreTv.asStateFlow()

    private val _tvSearchQuery = MutableStateFlow("")
    val tvSearchQuery: StateFlow<String> = _tvSearchQuery.asStateFlow()

    private val _tvSuggestions = MutableStateFlow<List<TmdbTv>>(emptyList())
    val tvSuggestions: StateFlow<List<TmdbTv>> = _tvSuggestions.asStateFlow()

    private val _tvSearchResults = MutableStateFlow<List<TmdbTv>>(emptyList())
    val tvSearchResults: StateFlow<List<TmdbTv>> = _tvSearchResults.asStateFlow()

    private val _isTvSearchSubmitted = MutableStateFlow(false)
    val isTvSearchSubmitted: StateFlow<Boolean> = _isTvSearchSubmitted.asStateFlow()

    private val _isSearchingTv = MutableStateFlow(false)
    val isSearchingTv: StateFlow<Boolean> = _isSearchingTv.asStateFlow()

    private val _isSeriesLoading = MutableStateFlow(false)
    val isSeriesLoading: StateFlow<Boolean> = _isSeriesLoading.asStateFlow()

    // === DETAILS VIEW / MODAL STATE ===
    private val _activeMovieDetail = MutableStateFlow<TmdbMovieDetail?>(null)
    val activeMovieDetail: StateFlow<TmdbMovieDetail?> = _activeMovieDetail.asStateFlow()

    private val _activeTvDetail = MutableStateFlow<TmdbTvDetail?>(null)
    val activeTvDetail: StateFlow<TmdbTvDetail?> = _activeTvDetail.asStateFlow()

    private val _selectedSeasonNumber = MutableStateFlow(1)
    val selectedSeasonNumber: StateFlow<Int> = _selectedSeasonNumber.asStateFlow()

    private val _seasonDetail = MutableStateFlow<TmdbSeasonDetail?>(null)
    val seasonDetail: StateFlow<TmdbSeasonDetail?> = _seasonDetail.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    private val _isLoadingSeason = MutableStateFlow(false)
    val isLoadingSeason: StateFlow<Boolean> = _isLoadingSeason.asStateFlow()

    // === PHASE 2 WEBVIEW RESOLUTION OVERLAY & DETECTOR STATE ===
    private val _isResolvingEmbed = MutableStateFlow(false)
    val isResolvingEmbed: StateFlow<Boolean> = _isResolvingEmbed.asStateFlow()

    private val _selectedEmbedProvider = MutableStateFlow(EmbedStreamResolver.providers.first())
    val selectedEmbedProvider: StateFlow<EmbedProvider> = _selectedEmbedProvider.asStateFlow()

    private val _resolvingTargetInfo = MutableStateFlow<ResolvingTargetInfo?>(null)
    val resolvingTargetInfo: StateFlow<ResolvingTargetInfo?> = _resolvingTargetInfo.asStateFlow()

    private val _resolutionLogs = MutableStateFlow<List<String>>(emptyList())
    val resolutionLogs: StateFlow<List<String>> = _resolutionLogs.asStateFlow()

    private val _detectedStreamMedia = MutableStateFlow<DetectedStreamMedia?>(null)
    val detectedStreamMedia: StateFlow<DetectedStreamMedia?> = _detectedStreamMedia.asStateFlow()

    // === PHASE 4 PHONE REMOTE CONTROL STATE ===
    private val _isPhoneRemoteActive = MutableStateFlow(false)
    val isPhoneRemoteActive: StateFlow<Boolean> = _isPhoneRemoteActive.asStateFlow()

    // === PLAYER STATE ===
    private val _activeStream = MutableStateFlow<ResolvedStream?>(null)
    val activeStream: StateFlow<ResolvedStream?> = _activeStream.asStateFlow()

    private val _isResolvingStream = MutableStateFlow(false)
    val isResolvingStream: StateFlow<Boolean> = _isResolvingStream.asStateFlow()

    // === WATCHLIST & RECENT HISTORY (ROOM) ===
    val watchlistItems: StateFlow<List<WatchlistEntity>> = repository.getAllWatchlist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentHistoryItems: StateFlow<List<RecentHistoryEntity>> = repository.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // === COMPANIES TAB STATE ===
    private val _companies = MutableStateFlow<List<ProductionCompanyInfo>>(emptyList())
    val companies: StateFlow<List<ProductionCompanyInfo>> = _companies.asStateFlow()

    private val _companySearchQuery = MutableStateFlow("")
    val companySearchQuery: StateFlow<String> = _companySearchQuery.asStateFlow()

    private val _companySuggestions = MutableStateFlow<List<ProductionCompanyInfo>>(emptyList())
    val companySuggestions: StateFlow<List<ProductionCompanyInfo>> = _companySuggestions.asStateFlow()

    private val _companySearchResults = MutableStateFlow<List<ProductionCompanyInfo>>(emptyList())
    val companySearchResults: StateFlow<List<ProductionCompanyInfo>> = _companySearchResults.asStateFlow()

    private val _isCompanySearchSubmitted = MutableStateFlow(false)
    val isCompanySearchSubmitted: StateFlow<Boolean> = _isCompanySearchSubmitted.asStateFlow()

    private val _isSearchingCompanies = MutableStateFlow(false)
    val isSearchingCompanies: StateFlow<Boolean> = _isSearchingCompanies.asStateFlow()

    private var companySearchJob: Job? = null

    private val _selectedCompany = MutableStateFlow<ProductionCompanyInfo?>(null)
    val selectedCompany: StateFlow<ProductionCompanyInfo?> = _selectedCompany.asStateFlow()

    private val _companyMovies = MutableStateFlow<List<TmdbMovie>>(emptyList())
    val companyMovies: StateFlow<List<TmdbMovie>> = _companyMovies.asStateFlow()

    private val _companyTv = MutableStateFlow<List<TmdbTv>>(emptyList())
    val companyTv: StateFlow<List<TmdbTv>> = _companyTv.asStateFlow()

    private val _isCompanyLoading = MutableStateFlow(false)
    val isCompanyLoading: StateFlow<Boolean> = _isCompanyLoading.asStateFlow()

    private val _selectedCompanySortOption = MutableStateFlow(CompanySortOption.POPULARITY)
    val selectedCompanySortOption: StateFlow<CompanySortOption> = _selectedCompanySortOption.asStateFlow()

    private val _selectedCompanySortDirection = MutableStateFlow(SortDirection.ASC)
    val selectedCompanySortDirection: StateFlow<SortDirection> = _selectedCompanySortDirection.asStateFlow()

    val savedCompanies: StateFlow<List<SavedCompanyEntity>> = repository.getAllSavedCompanies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadCompaniesForRegion() {
        _companies.value = CompaniesCatalog.getCompaniesForRegion(_selectedContentRegion.value)
    }

    fun onCompanySearchQueryChanged(query: String) {
        _companySearchQuery.value = query
        _isCompanySearchSubmitted.value = false
        companySearchJob?.cancel()
        if (query.isBlank()) {
            _companySuggestions.value = emptyList()
            _companySearchResults.value = emptyList()
            _isSearchingCompanies.value = false
            return
        }

        companySearchJob = viewModelScope.launch {
            _isSearchingCompanies.value = true
            delay(300)
            val res = repository.searchCompanies(query, _selectedContentRegion.value)
            _companySuggestions.value = res.getOrDefault(emptyList())
            _isSearchingCompanies.value = false
        }
    }

    fun submitCompanySearch() {
        val query = _companySearchQuery.value.trim()
        if (query.isNotBlank()) {
            _isCompanySearchSubmitted.value = true
            viewModelScope.launch {
                _isSearchingCompanies.value = true
                val res = repository.searchCompanies(query, _selectedContentRegion.value)
                _companySearchResults.value = res.getOrDefault(emptyList())
                _isSearchingCompanies.value = false
            }
        }
    }

    fun clearCompanySearch() {
        _companySearchQuery.value = ""
        _companySuggestions.value = emptyList()
        _companySearchResults.value = emptyList()
        _isCompanySearchSubmitted.value = false
        _isSearchingCompanies.value = false
    }

    fun selectCompany(company: ProductionCompanyInfo) {
        closeDetailModal()
        closePeopleDialog()
        if (ContentFilter.isBlockedCompany(company) || reportStore.isReported(company.id)) {
            _selectedCompany.value = null
            _isDetailUnavailable.value = true
            return
        }
        _selectedCompany.value = company
        _selectedCompanySortOption.value = CompanySortOption.POPULARITY
        _selectedCompanySortDirection.value = SortDirection.ASC
        loadCompanyContent(company.id, CompanySortOption.POPULARITY, SortDirection.ASC)
    }

    fun openCompanyById(companyId: Int, companyName: String = "") {
        closeDetailModal()
        closePeopleDialog()
        if (ContentFilter.isBlockedCompany(companyId, companyName) || reportStore.isReported(companyId)) {
            _selectedCompany.value = null
            _isDetailUnavailable.value = true
            return
        }
        _currentTab.value = AppTab.COMPANIES
        viewModelScope.launch {
            _isCompanyLoading.value = true
            val detailsRes = repository.getCompanyDetails(companyId)
            val info = detailsRes.getOrNull() ?: ProductionCompanyInfo(
                id = companyId,
                name = companyName.ifBlank { "Production Company" }
            )
            if (ContentFilter.isBlockedCompany(info) || reportStore.isReported(info.id)) {
                _selectedCompany.value = null
                _isDetailUnavailable.value = true
            } else {
                _selectedCompany.value = info
                loadCompanyContent(companyId, CompanySortOption.POPULARITY, SortDirection.ASC)
            }
        }
    }

    fun closeCompanyDetail() {
        _selectedCompany.value = null
        _companyMovies.value = emptyList()
        _companyTv.value = emptyList()
    }

    fun updateCompanySort(option: CompanySortOption, direction: SortDirection) {
        _selectedCompanySortOption.value = option
        _selectedCompanySortDirection.value = direction
        val company = _selectedCompany.value ?: return
        loadCompanyContent(company.id, option, direction)
    }

    fun loadCompanyContent(companyId: Int, sortOption: CompanySortOption, direction: SortDirection) {
        viewModelScope.launch {
            _isCompanyLoading.value = true
            val movieSort = "${sortOption.apiMovieSort}.${direction.apiSuffix}"
            val tvSort = "${sortOption.apiTvSort}.${direction.apiSuffix}"

            val moviesRes = repository.getCompanyMovies(companyId, movieSort, page = 1, region = _selectedContentRegion.value)
            val tvRes = repository.getCompanyTv(companyId, tvSort, page = 1, region = _selectedContentRegion.value)

            _companyMovies.value = moviesRes.getOrDefault(emptyList())
            _companyTv.value = tvRes.getOrDefault(emptyList())
            _isCompanyLoading.value = false
        }
    }

    fun toggleSaveCompany(company: ProductionCompanyInfo) {
        viewModelScope.launch {
            val saved = repository.toggleSaveCompany(company)
            _userMessage.emit(if (saved) "Saved ${company.name} to Companies" else "Removed ${company.name}")
        }
    }

    fun removeSavedCompany(companyId: Int) {
        viewModelScope.launch {
            repository.removeSavedCompany(companyId)
            _userMessage.emit("Removed company")
        }
    }

    // === PEOPLE & CAST DIALOG STATE ===
    private val _isPeopleDialogOpen = MutableStateFlow(false)
    val isPeopleDialogOpen: StateFlow<Boolean> = _isPeopleDialogOpen.asStateFlow()

    private val _peopleSearchQuery = MutableStateFlow("")
    val peopleSearchQuery: StateFlow<String> = _peopleSearchQuery.asStateFlow()

    private val _peopleSearchResults = MutableStateFlow<List<TmdbPerson>>(emptyList())
    val peopleSearchResults: StateFlow<List<TmdbPerson>> = _peopleSearchResults.asStateFlow()

    private val _popularPeople = MutableStateFlow<List<TmdbPerson>>(emptyList())
    val popularPeople: StateFlow<List<TmdbPerson>> = _popularPeople.asStateFlow()

    private val _isSearchingPeople = MutableStateFlow(false)
    val isSearchingPeople: StateFlow<Boolean> = _isSearchingPeople.asStateFlow()

    private val _selectedPerson = MutableStateFlow<TmdbPersonDetail?>(null)
    val selectedPerson: StateFlow<TmdbPersonDetail?> = _selectedPerson.asStateFlow()

    private val _isLoadingPerson = MutableStateFlow(false)
    val isLoadingPerson: StateFlow<Boolean> = _isLoadingPerson.asStateFlow()

    private var peopleSearchJob: Job? = null

    fun openPeopleDialog() {
        _isPeopleDialogOpen.value = true
        if (_popularPeople.value.isEmpty()) {
            loadPopularPeople()
        }
    }

    fun closePeopleDialog() {
        _isPeopleDialogOpen.value = false
        _selectedPerson.value = null
        _peopleSearchQuery.value = ""
        _peopleSearchResults.value = emptyList()
    }

    fun loadPopularPeople() {
        viewModelScope.launch {
            val res = repository.getPopularPeople(_selectedContentRegion.value)
            _popularPeople.value = res.getOrDefault(emptyList())
        }
    }

    fun onPeopleSearchQueryChanged(query: String) {
        _peopleSearchQuery.value = query
        peopleSearchJob?.cancel()
        if (query.isBlank()) {
            _peopleSearchResults.value = emptyList()
            _isSearchingPeople.value = false
            return
        }
        peopleSearchJob = viewModelScope.launch {
            _isSearchingPeople.value = true
            delay(300)
            val res = repository.searchPeople(query, _selectedContentRegion.value)
            _peopleSearchResults.value = res.getOrDefault(emptyList())
            _isSearchingPeople.value = false
        }
    }

    fun submitPeopleSearch() {
        val query = _peopleSearchQuery.value.trim()
        if (query.isNotBlank()) {
            viewModelScope.launch {
                _isSearchingPeople.value = true
                val res = repository.searchPeople(query, _selectedContentRegion.value)
                _peopleSearchResults.value = res.getOrDefault(emptyList())
                _isSearchingPeople.value = false
            }
        }
    }

    fun selectPerson(personId: Int) {
        if (reportStore.isReported(personId)) {
            _selectedPerson.value = null
            _isDetailUnavailable.value = true
            return
        }
        _isPeopleDialogOpen.value = true
        viewModelScope.launch {
            _isLoadingPerson.value = true
            val res = repository.getPersonDetails(personId, _selectedContentRegion.value)
            val detail = res.getOrNull()
            if (detail != null && (ContentFilter.isBlockedPerson(detail) || reportStore.isReported(detail.id))) {
                _selectedPerson.value = null
                _isPeopleDialogOpen.value = false
                _isDetailUnavailable.value = true
            } else {
                _selectedPerson.value = detail
            }
            _isLoadingPerson.value = false
        }
    }

    fun closePersonDetail() {
        _selectedPerson.value = null
    }

    // Search debounce jobs
    private var movieSearchJob: Job? = null
    private var tvSearchJob: Job? = null

    // Genres lists
    val movieGenres = listOf(
        GenreItem(28, "Action"),
        GenreItem(12, "Adventure"),
        GenreItem(16, "Animation"),
        GenreItem(35, "Comedy"),
        GenreItem(80, "Crime"),
        GenreItem(99, "Documentary"),
        GenreItem(18, "Drama"),
        GenreItem(27, "Horror"),
        GenreItem(878, "Sci-Fi"),
        GenreItem(53, "Thriller")
    )

    val tvGenres = listOf(
        GenreItem(10759, "Action & Adventure"),
        GenreItem(16, "Animation"),
        GenreItem(35, "Comedy"),
        GenreItem(80, "Crime"),
        GenreItem(18, "Drama"),
        GenreItem(10765, "Sci-Fi & Fantasy"),
        GenreItem(9648, "Mystery"),
        GenreItem(10768, "War & Politics")
    )

    init {
        viewModelScope.launch {
            remoteBlocklist.initialize()
        }
        viewModelScope.launch {
            ContentFilter.blocklist.collect {
                val currentMovie = _activeMovieDetail.value
                if (currentMovie != null && ContentFilter.isBlockedMovie(currentMovie)) {
                    _activeMovieDetail.value = null
                    _isDetailUnavailable.value = true
                }
                val currentTv = _activeTvDetail.value
                if (currentTv != null && ContentFilter.isBlockedTv(currentTv)) {
                    _activeTvDetail.value = null
                    _isDetailUnavailable.value = true
                }
                val currentCompany = _selectedCompany.value
                if (currentCompany != null && ContentFilter.isBlockedCompany(currentCompany)) {
                    _selectedCompany.value = null
                    _isDetailUnavailable.value = true
                }
                val currentPerson = _selectedPerson.value
                if (currentPerson != null && ContentFilter.isBlockedPerson(currentPerson)) {
                    _selectedPerson.value = null
                    _isDetailUnavailable.value = true
                }
            }
        }
        loadMovies()
        loadSeries()
        loadCompaniesForRegion()
        loadPopularPeople()
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun openSettings() {
        _apiKeyInput.value = apiKeyStore.getApiKey()
        _keyValidationStatus.value = null
        _isSettingsOpen.value = true
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
    }

    fun updateApiKeyInput(key: String) {
        _apiKeyInput.value = key
    }

    fun saveAndValidateApiKey(newKey: String) {
        viewModelScope.launch {
            _isKeyTesting.value = true
            val trimmed = newKey.trim()
            if (trimmed.isEmpty()) {
                _keyValidationStatus.value = "Error: Key cannot be empty"
                _isKeyTesting.value = false
                return@launch
            }

            apiKeyStore.saveApiKey(trimmed)
            val validation = repository.validateApiKey()
            _isKeyTesting.value = false
            if (validation.isSuccess) {
                _keyValidationStatus.value = "Success: TMDB API Key verified!"
                _userMessage.emit("TMDB Key activated successfully")
                loadMovies()
                loadSeries()
            } else {
                _keyValidationStatus.value = "Warning: Could not verify key. Check connection or key correctness."
            }
        }
    }

    fun useDemoKey() {
        apiKeyStore.setDemoKey()
        _apiKeyInput.value = ApiKeyStore.DEFAULT_DEMO_KEY
        _keyValidationStatus.value = "Using default TMDB demo access."
        viewModelScope.launch {
            _userMessage.emit("Switched to TMDB Demo Key")
            loadMovies()
            loadSeries()
        }
    }

    // === MOVIES ACTIONS ===
    fun loadMovies() {
        viewModelScope.launch {
            _isMoviesLoading.value = true
            try {
                val region = _selectedContentRegion.value
                val trendingRes = repository.getTrendingMovies("day", 1, region)
                val trending = trendingRes.getOrDefault(emptyList())
                _trendingMovies.value = trending
                _heroMovie.value = trending.firstOrNull { it.backdropPath != null } ?: trending.firstOrNull()

                val popularRes = repository.getPopularMovies(1, region)
                _popularMovies.value = popularRes.getOrDefault(emptyList())

                val newRelRes = repository.getNewReleaseMovies(1, region)
                _newReleaseMovies.value = newRelRes.getOrDefault(emptyList())

                val topRatedRes = repository.getTopRatedMovies(1, region)
                _topRatedMovies.value = topRatedRes.getOrDefault(emptyList())
            } catch (e: Exception) {
                _userMessage.emit("Failed to load movies: ${e.message}")
            } finally {
                _isMoviesLoading.value = false
            }
        }
    }

    fun selectMovieGenre(genreId: Int?) {
        _selectedMovieGenre.value = genreId
        if (genreId == null) {
            _genreMovies.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isMoviesLoading.value = true
            val res = repository.getMoviesByGenre(genreId, 1, _selectedContentRegion.value)
            _genreMovies.value = res.getOrDefault(emptyList())
            _isMoviesLoading.value = false
        }
    }

    // === SEPARATE REAL SEARCH HISTORIES (MOVIES & SERIES) ===
    private val _movieSearchHistory = MutableStateFlow<List<String>>(searchHistoryStore.getMovieHistory())
    val movieSearchHistory: StateFlow<List<String>> = _movieSearchHistory.asStateFlow()

    private val _tvSearchHistory = MutableStateFlow<List<String>>(searchHistoryStore.getTvHistory())
    val tvSearchHistory: StateFlow<List<String>> = _tvSearchHistory.asStateFlow()

    fun addMovieSearchQueryToHistory(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotBlank() && trimmed.length > 1) {
            val updated = _movieSearchHistory.value.toMutableList()
            updated.remove(trimmed)
            updated.add(0, trimmed)
            val trimmedList = updated.take(15)
            _movieSearchHistory.value = trimmedList
            searchHistoryStore.saveMovieHistory(trimmedList)
        }
    }

    fun removeMovieSearchHistoryItem(query: String) {
        val updated = _movieSearchHistory.value.toMutableList()
        updated.remove(query)
        _movieSearchHistory.value = updated
        searchHistoryStore.saveMovieHistory(updated)
    }

    fun clearMovieSearchHistory() {
        _movieSearchHistory.value = emptyList()
        searchHistoryStore.saveMovieHistory(emptyList())
    }

    fun addTvSearchQueryToHistory(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotBlank() && trimmed.length > 1) {
            val updated = _tvSearchHistory.value.toMutableList()
            updated.remove(trimmed)
            updated.add(0, trimmed)
            val trimmedList = updated.take(15)
            _tvSearchHistory.value = trimmedList
            searchHistoryStore.saveTvHistory(trimmedList)
        }
    }

    fun removeTvSearchHistoryItem(query: String) {
        val updated = _tvSearchHistory.value.toMutableList()
        updated.remove(query)
        _tvSearchHistory.value = updated
        searchHistoryStore.saveTvHistory(updated)
    }

    fun clearTvSearchHistory() {
        _tvSearchHistory.value = emptyList()
        searchHistoryStore.saveTvHistory(emptyList())
    }

    fun onMovieSearchQueryChanged(query: String) {
        _movieSearchQuery.value = query
        _isMovieSearchSubmitted.value = false
        movieSearchJob?.cancel()
        if (query.isBlank()) {
            _movieSuggestions.value = emptyList()
            _movieSearchResults.value = emptyList()
            _isSearchingMovies.value = false
            return
        }

        movieSearchJob = viewModelScope.launch {
            _isSearchingMovies.value = true
            delay(300) // 300ms debounce for live suggestions dropdown only
            val results = repository.searchMovies(query, 1, _selectedContentRegion.value)
            val list = results.getOrDefault(emptyList())
            _movieSuggestions.value = list
            // NOTE: Do not set _movieSearchResults here.
            // Search results grid only appears after user explicitly presses Search.
            _isSearchingMovies.value = false
        }
    }

    fun submitMovieSearch() {
        val query = _movieSearchQuery.value.trim()
        if (query.isNotBlank()) {
            _isMovieSearchSubmitted.value = true
            addMovieSearchQueryToHistory(query)
            viewModelScope.launch {
                _isSearchingMovies.value = true
                val results = repository.searchMovies(query, 1, _selectedContentRegion.value)
                _movieSearchResults.value = results.getOrDefault(emptyList())
                _isSearchingMovies.value = false
            }
        }
    }

    fun clearMovieSearch() {
        _movieSearchQuery.value = ""
        _movieSuggestions.value = emptyList()
        _movieSearchResults.value = emptyList()
        _isMovieSearchSubmitted.value = false
        _isSearchingMovies.value = false
    }

    // === SERIES ACTIONS ===
    fun loadSeries() {
        viewModelScope.launch {
            _isSeriesLoading.value = true
            try {
                val region = _selectedContentRegion.value
                val trendingRes = repository.getTrendingTv("day", 1, region)
                val trending = trendingRes.getOrDefault(emptyList())
                _trendingTv.value = trending
                _heroTv.value = trending.firstOrNull { it.backdropPath != null } ?: trending.firstOrNull()

                val popularRes = repository.getPopularTv(1, region)
                _popularTv.value = popularRes.getOrDefault(emptyList())

                val newRelRes = repository.getNewReleaseTv(1, region)
                _newReleaseTv.value = newRelRes.getOrDefault(emptyList())

                val topRatedRes = repository.getTopRatedTv(1, region)
                _topRatedTv.value = topRatedRes.getOrDefault(emptyList())
            } catch (e: Exception) {
                _userMessage.emit("Failed to load series: ${e.message}")
            } finally {
                _isSeriesLoading.value = false
            }
        }
    }

    fun selectTvGenre(genreId: Int?) {
        _selectedTvGenre.value = genreId
        if (genreId == null) {
            _genreTv.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSeriesLoading.value = true
            val res = repository.getTvByGenre(genreId, 1, _selectedContentRegion.value)
            _genreTv.value = res.getOrDefault(emptyList())
            _isSeriesLoading.value = false
        }
    }

    fun onTvSearchQueryChanged(query: String) {
        _tvSearchQuery.value = query
        _isTvSearchSubmitted.value = false
        tvSearchJob?.cancel()
        if (query.isBlank()) {
            _tvSuggestions.value = emptyList()
            _tvSearchResults.value = emptyList()
            _isSearchingTv.value = false
            return
        }

        tvSearchJob = viewModelScope.launch {
            _isSearchingTv.value = true
            delay(300) // 300ms debounce for live suggestions dropdown only
            val results = repository.searchTv(query, 1, _selectedContentRegion.value)
            val list = results.getOrDefault(emptyList())
            _tvSuggestions.value = list
            // NOTE: Do not set _tvSearchResults here.
            _isSearchingTv.value = false
        }
    }

    fun submitTvSearch() {
        val query = _tvSearchQuery.value.trim()
        if (query.isNotBlank()) {
            _isTvSearchSubmitted.value = true
            addTvSearchQueryToHistory(query)
            viewModelScope.launch {
                _isSearchingTv.value = true
                val results = repository.searchTv(query, 1, _selectedContentRegion.value)
                _tvSearchResults.value = results.getOrDefault(emptyList())
                _isSearchingTv.value = false
            }
        }
    }

    fun clearTvSearch() {
        _tvSearchQuery.value = ""
        _tvSuggestions.value = emptyList()
        _tvSearchResults.value = emptyList()
        _isTvSearchSubmitted.value = false
        _isSearchingTv.value = false
    }

    // === DETAILS MODAL ACTIONS ===
    fun openMovieDetail(movieId: Int) {
        if (reportStore.isReported(movieId)) {
            _activeMovieDetail.value = null
            _isDetailUnavailable.value = true
            return
        }
        viewModelScope.launch {
            _isLoadingDetail.value = true
            _activeTvDetail.value = null
            val res = repository.getMovieDetails(movieId, _selectedContentRegion.value)
            if (res.isSuccess) {
                val detail = res.getOrNull()
                if (detail != null && (ContentFilter.isBlockedMovie(detail) || reportStore.isReported(detail.id))) {
                    _activeMovieDetail.value = null
                    _isDetailUnavailable.value = true
                } else {
                    _activeMovieDetail.value = detail
                }
            } else {
                _userMessage.emit("Could not fetch movie details")
            }
            _isLoadingDetail.value = false
        }
    }

    fun openTvDetail(tvId: Int) {
        if (reportStore.isReported(tvId)) {
            _activeTvDetail.value = null
            _isDetailUnavailable.value = true
            return
        }
        viewModelScope.launch {
            _isLoadingDetail.value = true
            _activeMovieDetail.value = null
            _selectedSeasonNumber.value = 1
            _seasonDetail.value = null
            val res = repository.getTvDetails(tvId, _selectedContentRegion.value)
            if (res.isSuccess) {
                val tvDetail = res.getOrNull()
                if (tvDetail != null && (ContentFilter.isBlockedTv(tvDetail) || reportStore.isReported(tvDetail.id))) {
                    _activeTvDetail.value = null
                    _isDetailUnavailable.value = true
                } else {
                    _activeTvDetail.value = tvDetail
                    if (tvDetail != null && !tvDetail.seasons.isNullOrEmpty()) {
                        val firstSeason = tvDetail.seasons.firstOrNull { it.seasonNumber > 0 } ?: tvDetail.seasons.first()
                        selectSeason(tvId, firstSeason.seasonNumber)
                    }
                }
            } else {
                _userMessage.emit("Could not fetch series details")
            }
            _isLoadingDetail.value = false
        }
    }

    fun selectSeason(tvId: Int, seasonNumber: Int) {
        _selectedSeasonNumber.value = seasonNumber
        viewModelScope.launch {
            _isLoadingSeason.value = true
            val res = repository.getTvSeasonDetails(tvId, seasonNumber, _selectedContentRegion.value)
            if (res.isSuccess) {
                _seasonDetail.value = res.getOrNull()
            }
            _isLoadingSeason.value = false
        }
    }

    fun closeDetailModal() {
        _activeMovieDetail.value = null
        _activeTvDetail.value = null
        _seasonDetail.value = null
    }

    // === DIRECT EMBED & CAST STREAM FLOW ===
    fun startStreamResolution(
        mediaId: Int,
        mediaType: String,
        title: String,
        imdbId: String? = null,
        seasonNumber: Int? = null,
        episodeNumber: Int? = null,
        episodeTitle: String? = null,
        posterPath: String? = null,
        backdropPath: String? = null,
        initialPositionSeconds: Long = 0L,
        totalDurationSeconds: Long = 7200L
    ) {
        closeDetailModal()
        closePeopleDialog()
        val embedUrl = EmbedStreamResolver.getEmbedUrl(
            provider = _selectedEmbedProvider.value,
            mediaId = mediaId,
            mediaType = mediaType,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber
        )

        val detected = DetectedStreamMedia(
            mediaId = mediaId,
            mediaType = mediaType,
            title = title,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            episodeTitle = episodeTitle,
            posterPath = posterPath,
            backdropPath = backdropPath,
            streamUrl = embedUrl,
            embedUrl = embedUrl,
            providerName = _selectedEmbedProvider.value.name,
            headers = mapOf("Referer" to _selectedEmbedProvider.value.defaultReferer),
            initialPositionSeconds = initialPositionSeconds,
            totalDurationSeconds = totalDurationSeconds
        )

        _detectedStreamMedia.value = detected
        _isResolvingEmbed.value = false

        // Automatically record to Watch History immediately on play
        viewModelScope.launch {
            repository.savePlaybackProgress(
                mediaId = mediaId,
                mediaType = mediaType,
                title = title,
                seasonNumber = seasonNumber,
                episodeNumber = episodeNumber,
                episodeTitle = episodeTitle,
                posterPath = posterPath,
                backdropPath = backdropPath,
                currentPositionSeconds = initialPositionSeconds,
                totalDurationSeconds = totalDurationSeconds
            )
        }

        // Direct instant opening of in-app player screen
        val resolved = ResolvedStream(
            mediaId = mediaId,
            mediaType = mediaType,
            title = title,
            url = embedUrl,
            imdbId = imdbId ?: if (mediaType == "movie") _activeMovieDetail.value?.effectiveImdbId else _activeTvDetail.value?.effectiveImdbId,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            episodeTitle = episodeTitle,
            posterPath = posterPath,
            backdropPath = backdropPath,
            initialPositionSeconds = initialPositionSeconds,
            totalDurationSeconds = totalDurationSeconds
        )
        _activeStream.value = resolved
        _playbackDestination.value = PlaybackDestination.LOCAL_DEVICE
        closeDetailModal()
    }

    fun playNextEpisode() {
        val current = _activeStream.value ?: return
        if (current.mediaType != "tv") return
        val currentEp = current.episodeNumber ?: 1
        val nextEp = currentEp + 1
        val seasonNum = current.seasonNumber ?: 1

        startStreamResolution(
            mediaId = current.mediaId,
            mediaType = "tv",
            title = current.title,
            imdbId = current.imdbId,
            seasonNumber = seasonNum,
            episodeNumber = nextEp,
            episodeTitle = "Episode $nextEp",
            posterPath = current.posterPath,
            backdropPath = current.backdropPath
        )
    }

    fun playPreviousEpisode() {
        val current = _activeStream.value ?: return
        if (current.mediaType != "tv") return
        val currentEp = current.episodeNumber ?: 1
        if (currentEp <= 1) return
        val prevEp = currentEp - 1
        val seasonNum = current.seasonNumber ?: 1

        startStreamResolution(
            mediaId = current.mediaId,
            mediaType = "tv",
            title = current.title,
            imdbId = current.imdbId,
            seasonNumber = seasonNum,
            episodeNumber = prevEp,
            episodeTitle = "Episode $prevEp",
            posterPath = current.posterPath,
            backdropPath = current.backdropPath
        )
    }

    fun selectEmbedProvider(provider: EmbedProvider) {
        _selectedEmbedProvider.value = provider
        addResolutionLog("Switched embed provider to ${provider.name}")
    }

    fun addResolutionLog(log: String) {
        val current = _resolutionLogs.value.toMutableList()
        current.add(log)
        _resolutionLogs.value = current
    }

    fun onStreamDetected(streamUrl: String, headers: Map<String, String>) {
        val target = _resolvingTargetInfo.value ?: return
        addResolutionLog("Detected high-speed video stream playlist!")

        val detected = DetectedStreamMedia(
            mediaId = target.mediaId,
            mediaType = target.mediaType,
            title = target.title,
            seasonNumber = target.seasonNumber,
            episodeNumber = target.episodeNumber,
            episodeTitle = target.episodeTitle,
            posterPath = target.posterPath,
            backdropPath = target.backdropPath,
            streamUrl = streamUrl,
            providerName = _selectedEmbedProvider.value.name,
            headers = headers,
            initialPositionSeconds = target.initialPositionSeconds,
            totalDurationSeconds = target.totalDurationSeconds
        )

        _detectedStreamMedia.value = detected
        _isResolvingEmbed.value = false

        // Automatically continue in current mode when user clicks Re-sync/Refresh
        when (_playbackDestination.value) {
            PlaybackDestination.LOCAL_DEVICE -> {
                playDetectedLocalStream()
            }
            PlaybackDestination.TV_REMOTE -> {
                launchPhoneRemote()
            }
            PlaybackDestination.NONE -> {
                // Keep detectedStreamMedia non-null to display StreamOptionDialog
            }
        }
    }

    fun cancelStreamResolution() {
        _isResolvingEmbed.value = false
        _resolvingTargetInfo.value = null
        _detectedStreamMedia.value = null
        _playbackDestination.value = PlaybackDestination.NONE
    }

    fun refreshStreamSession() {
        val stream = _activeStream.value
        val remoteMedia = EmbeddedHttpServer.remoteState.value

        val mediaId = stream?.mediaId ?: _resolvingTargetInfo.value?.mediaId ?: 0
        val mediaType = stream?.mediaType ?: _resolvingTargetInfo.value?.mediaType ?: "movie"
        val title = stream?.title ?: remoteMedia.title
        val seasonNum = stream?.seasonNumber ?: remoteMedia.seasonNumber
        val episodeNum = stream?.episodeNumber ?: remoteMedia.episodeNumber
        val episodeTitle = stream?.episodeTitle ?: remoteMedia.episodeTitle
        val posterPath = stream?.posterPath ?: remoteMedia.posterUrl
        val backdropPath = stream?.backdropPath
        val pos = stream?.initialPositionSeconds ?: remoteMedia.currentTimeSeconds
        val dur = stream?.totalDurationSeconds ?: remoteMedia.totalDurationSeconds

        _userMessage.tryEmit("Re-syncing Stream...")
        startStreamResolution(
            mediaId = mediaId,
            mediaType = mediaType,
            title = title,
            seasonNumber = seasonNum,
            episodeNumber = episodeNum,
            episodeTitle = episodeTitle,
            posterPath = posterPath,
            backdropPath = backdropPath,
            initialPositionSeconds = pos,
            totalDurationSeconds = dur
        )
    }

    fun dismissStreamOptionDialog() {
        _detectedStreamMedia.value = null
        _playbackDestination.value = PlaybackDestination.NONE
    }

    fun launchPhoneRemote() {
        val detected = _detectedStreamMedia.value
        val context = getApplication<Application>()
        EmbeddedHttpServer.startServer(context)
        _playbackDestination.value = PlaybackDestination.TV_REMOTE

        if (detected != null) {
            EmbeddedHttpServer.updateMediaInfo(
                title = detected.title,
                mediaType = detected.mediaType,
                seasonNumber = detected.seasonNumber,
                episodeNumber = detected.episodeNumber,
                episodeTitle = detected.episodeTitle,
                posterUrl = detected.posterPath,
                streamUrl = detected.streamUrl,
                referer = detected.headers["Referer"] ?: _selectedEmbedProvider.value.defaultReferer,
                initialPositionSeconds = detected.initialPositionSeconds,
                totalDurationSeconds = detected.totalDurationSeconds
            )
        }

        _isPhoneRemoteActive.value = true
        _detectedStreamMedia.value = null
    }

    fun closePhoneRemote() {
        _isPhoneRemoteActive.value = false
        _playbackDestination.value = PlaybackDestination.NONE
    }

    fun openPhoneRemoteDirect() {
        _isPhoneRemoteActive.value = true
    }

    fun playDetectedLocalStream() {
        val detected = _detectedStreamMedia.value ?: return
        _playbackDestination.value = PlaybackDestination.LOCAL_DEVICE
        viewModelScope.launch {
            _isResolvingStream.value = true
            val stream = StreamResolver.resolveStream(
                mediaId = detected.mediaId,
                mediaType = detected.mediaType,
                title = detected.title,
                seasonNumber = detected.seasonNumber,
                episodeNumber = detected.episodeNumber,
                episodeTitle = detected.episodeTitle,
                posterPath = detected.posterPath,
                backdropPath = detected.backdropPath,
                initialPositionSeconds = detected.initialPositionSeconds,
                customDurationMinutes = (detected.totalDurationSeconds / 60).toInt()
            )
            _activeStream.value = stream.copy(url = detected.streamUrl)
            _isResolvingStream.value = false
            _isResolvingEmbed.value = false
            _detectedStreamMedia.value = null
        }
    }

    // === PLAYBACK WRAPPERS (WIRED TO RESOLVER & AUTO-DISMISS MODALS) ===
    fun playMovie(movie: TmdbMovieDetail, startPositionSeconds: Long = 0L) {
        closeDetailModal()
        startStreamResolution(
            mediaId = movie.id,
            mediaType = "movie",
            title = movie.displayTitle,
            posterPath = movie.posterPath,
            backdropPath = movie.backdropPath,
            initialPositionSeconds = startPositionSeconds,
            totalDurationSeconds = (movie.runtime ?: 118) * 60L
        )
    }

    fun playMovieSimple(movie: TmdbMovie, startPositionSeconds: Long = 0L) {
        closeDetailModal()
        startStreamResolution(
            mediaId = movie.id,
            mediaType = "movie",
            title = movie.displayTitle,
            posterPath = movie.posterPath,
            backdropPath = movie.backdropPath,
            initialPositionSeconds = startPositionSeconds
        )
    }

    fun playTvEpisode(
        tvDetail: TmdbTvDetail,
        seasonNumber: Int,
        episode: TmdbEpisode,
        startPositionSeconds: Long = 0L
    ) {
        closeDetailModal()
        startStreamResolution(
            mediaId = tvDetail.id,
            mediaType = "tv",
            title = tvDetail.displayTitle,
            seasonNumber = seasonNumber,
            episodeNumber = episode.episodeNumber,
            episodeTitle = episode.displayTitle,
            posterPath = episode.stillPath ?: tvDetail.posterPath,
            backdropPath = tvDetail.backdropPath,
            initialPositionSeconds = startPositionSeconds,
            totalDurationSeconds = (episode.runtime ?: 45) * 60L
        )
    }

    fun playFromHistory(history: RecentHistoryEntity) {
        closeDetailModal()
        startStreamResolution(
            mediaId = history.mediaId,
            mediaType = history.mediaType,
            title = history.title,
            seasonNumber = history.seasonNumber,
            episodeNumber = history.episodeNumber,
            episodeTitle = history.episodeTitle,
            posterPath = history.posterPath,
            backdropPath = history.backdropPath,
            initialPositionSeconds = history.lastPositionSeconds,
            totalDurationSeconds = history.totalDurationSeconds
        )
    }

    fun playFromWatchlist(watchlist: WatchlistEntity) {
        if (watchlist.mediaType == "tv") {
            openTvDetail(watchlist.id)
        } else {
            openMovieDetail(watchlist.id)
        }
    }

    fun updatePlaybackProgress(currentSeconds: Long, totalSeconds: Long) {
        val stream = _activeStream.value ?: return
        viewModelScope.launch {
            repository.savePlaybackProgress(
                mediaId = stream.mediaId,
                mediaType = stream.mediaType,
                title = stream.title,
                seasonNumber = stream.seasonNumber,
                episodeNumber = stream.episodeNumber,
                episodeTitle = stream.episodeTitle,
                posterPath = stream.posterPath,
                backdropPath = stream.backdropPath,
                currentPositionSeconds = currentSeconds,
                totalDurationSeconds = totalSeconds
            )
        }
    }

    fun closePlayer() {
        _activeStream.value = null
        _isResolvingStream.value = false
        _isResolvingEmbed.value = false
        _playbackDestination.value = PlaybackDestination.NONE
    }

    // === WATCHLIST ACTIONS ===
    fun toggleWatchlistMovie(movie: TmdbMovie) {
        viewModelScope.launch {
            val saved = repository.toggleWatchlist(
                id = movie.id,
                mediaType = "movie",
                title = movie.displayTitle,
                overview = movie.overview ?: "",
                posterPath = movie.posterPath,
                backdropPath = movie.backdropPath,
                voteAverage = movie.voteAverage,
                releaseDate = movie.releaseDate
            )
            _userMessage.emit(if (saved) "Added to Watchlist" else "Removed from Watchlist")
        }
    }

    fun toggleWatchlistMovieDetail(movie: TmdbMovieDetail) {
        viewModelScope.launch {
            val saved = repository.toggleWatchlist(
                id = movie.id,
                mediaType = "movie",
                title = movie.displayTitle,
                overview = movie.overview ?: "",
                posterPath = movie.posterPath,
                backdropPath = movie.backdropPath,
                voteAverage = movie.voteAverage,
                releaseDate = movie.releaseDate
            )
            _userMessage.emit(if (saved) "Added to Watchlist" else "Removed from Watchlist")
        }
    }

    fun toggleWatchlistTv(tv: TmdbTv) {
        viewModelScope.launch {
            val saved = repository.toggleWatchlist(
                id = tv.id,
                mediaType = "tv",
                title = tv.displayTitle,
                overview = tv.overview ?: "",
                posterPath = tv.posterPath,
                backdropPath = tv.backdropPath,
                voteAverage = tv.voteAverage,
                releaseDate = tv.firstAirDate
            )
            _userMessage.emit(if (saved) "Added to Watchlist" else "Removed from Watchlist")
        }
    }

    fun toggleWatchlistTvDetail(tv: TmdbTvDetail) {
        viewModelScope.launch {
            val saved = repository.toggleWatchlist(
                id = tv.id,
                mediaType = "tv",
                title = tv.displayTitle,
                overview = tv.overview ?: "",
                posterPath = tv.posterPath,
                backdropPath = tv.backdropPath,
                voteAverage = tv.voteAverage,
                releaseDate = tv.firstAirDate
            )
            _userMessage.emit(if (saved) "Added to Watchlist" else "Removed from Watchlist")
        }
    }

    fun removeFromWatchlist(id: Int) {
        viewModelScope.launch {
            repository.removeFromWatchlist(id)
            _userMessage.emit("Removed from Watchlist")
        }
    }

    // === RECENT HISTORY ACTIONS ===
    fun deleteHistoryItem(historyId: String) {
        viewModelScope.launch {
            repository.deleteHistoryItem(historyId)
            _userMessage.emit("Removed from History")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            _userMessage.emit("Watch history cleared")
        }
    }
}
