package com.example.data.repository

import com.example.data.filter.ContentFilter
import com.example.data.local.ApiKeyStore
import com.example.data.local.RecentHistoryDao
import com.example.data.local.RecentHistoryEntity
import com.example.data.local.SavedCompanyDao
import com.example.data.local.SavedCompanyEntity
import com.example.data.local.WatchlistDao
import com.example.data.local.WatchlistEntity
import com.example.data.model.CompaniesCatalog
import com.example.data.model.ProductionCompanyInfo
import com.example.data.model.TmdbMovie
import com.example.data.model.TmdbMovieDetail
import com.example.data.model.TmdbPagedResponse
import com.example.data.model.TmdbPerson
import com.example.data.model.TmdbPersonDetail
import com.example.data.model.TmdbProductionCompany
import com.example.data.model.TmdbSeasonDetail
import com.example.data.model.TmdbTv
import com.example.data.model.TmdbTvDetail
import com.example.data.remote.TmdbApiClient
import com.example.data.report.ReportStore
import com.example.util.ArabicSearchHelper
import com.example.viewmodel.ContentRegion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class MediaRepository(
    private val apiKeyStore: ApiKeyStore,
    private val watchlistDao: WatchlistDao,
    private val recentHistoryDao: RecentHistoryDao,
    private val savedCompanyDao: SavedCompanyDao,
    private val reportStore: ReportStore
) {
    private val api get() = TmdbApiClient.getApi(apiKeyStore)

    // Helper to filter movies with content filter
    fun filterMoviesWithReports(list: List<TmdbMovie>): List<TmdbMovie> {
        return ContentFilter.filterMovies(list)
    }

    // Helper to filter TV with content filter
    fun filterTvWithReports(list: List<TmdbTv>): List<TmdbTv> {
        return ContentFilter.filterTv(list)
    }

    // Helper to filter people with content filter
    fun filterPeopleWithReports(list: List<TmdbPerson>): List<TmdbPerson> {
        return ContentFilter.filterPeople(list)
    }

    // Helper to filter companies with content filter
    fun filterCompaniesWithReports(list: List<ProductionCompanyInfo>): List<ProductionCompanyInfo> {
        return ContentFilter.filterCompanies(list)
    }

    // Pagination helper: if filtered list has fewer than 10 items and more pages exist,
    // automatically fetch next pages (max 3 extra pages) so screens never look empty.
    private suspend fun <T> fetchWithFilter(
        initialPage: Int,
        fetchPage: suspend (page: Int) -> TmdbPagedResponse<T>?,
        filterFn: (List<T>) -> List<T>
    ): List<T> {
        val accumulated = mutableListOf<T>()
        var currentPage = initialPage
        var extraPagesRemaining = 3

        while (true) {
            val response = try {
                fetchPage(currentPage)
            } catch (_: Exception) {
                null
            } ?: break

            val filtered = filterFn(response.results)
            accumulated.addAll(filtered)

            if (accumulated.size >= 10 || currentPage >= response.totalPages || extraPagesRemaining <= 0) {
                break
            }
            currentPage++
            extraPagesRemaining--
        }
        return accumulated
    }

    // === TMDB Key Validation ===
    suspend fun validateApiKey(keyToTest: String? = null): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (keyToTest != null) {
                val original = apiKeyStore.getApiKey()
                apiKeyStore.saveApiKey(keyToTest)
                try {
                    api.getPopularMovies(1)
                    Result.success(true)
                } finally {
                    apiKeyStore.saveApiKey(original)
                }
            } else {
                api.getPopularMovies(1)
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // === MOVIES ===
    suspend fun getTrendingMovies(
        timeWindow: String = "day",
        page: Int = 1,
        region: ContentRegion = ContentRegion.GLOBAL
    ): Result<List<TmdbMovie>> = withContext(Dispatchers.IO) {
        try {
            val filterParams = ContentFilter.discoverParams()
            val list = fetchWithFilter(
                initialPage = page,
                fetchPage = { p ->
                    if (region == ContentRegion.GLOBAL) {
                        api.getTrendingMovies(timeWindow, p, language = region.languageCode)
                    } else if (region.originalLanguages != null) {
                        api.discoverMovies(
                            withOriginalLanguage = region.originalLanguages,
                            language = region.languageCode,
                            page = p,
                            sortBy = "popularity.desc",
                            withoutKeywords = filterParams.withoutKeywords,
                            withoutCompanies = filterParams.withoutCompanies,
                            includeAdult = false
                        )
                    } else {
                        api.getTrendingMovies(timeWindow, p, language = region.languageCode)
                    }
                },
                filterFn = { filterMoviesWithReports(it) }
            )
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPopularMovies(
        page: Int = 1,
        region: ContentRegion = ContentRegion.GLOBAL
    ): Result<List<TmdbMovie>> = withContext(Dispatchers.IO) {
        try {
            val filterParams = ContentFilter.discoverParams()
            val list = fetchWithFilter(
                initialPage = page,
                fetchPage = { p ->
                    api.discoverMovies(
                        withOriginalLanguage = if (region == ContentRegion.GLOBAL) null else region.originalLanguages,
                        language = region.languageCode,
                        page = p,
                        sortBy = "popularity.desc",
                        withoutKeywords = filterParams.withoutKeywords,
                        withoutCompanies = filterParams.withoutCompanies,
                        includeAdult = false
                    )
                },
                filterFn = { filterMoviesWithReports(it) }
            )
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTopRatedMovies(
        page: Int = 1,
        region: ContentRegion = ContentRegion.GLOBAL
    ): Result<List<TmdbMovie>> = withContext(Dispatchers.IO) {
        try {
            val filterParams = ContentFilter.discoverParams()
            val list = fetchWithFilter(
                initialPage = page,
                fetchPage = { p ->
                    api.discoverMovies(
                        withOriginalLanguage = if (region == ContentRegion.GLOBAL) null else region.originalLanguages,
                        language = region.languageCode,
                        page = p,
                        sortBy = "vote_average.desc",
                        withoutKeywords = filterParams.withoutKeywords,
                        withoutCompanies = filterParams.withoutCompanies,
                        voteCountGte = 200,
                        includeAdult = false
                    )
                },
                filterFn = { filterMoviesWithReports(it) }
            )
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMoviesByGenre(
        genreId: Int,
        page: Int = 1,
        region: ContentRegion = ContentRegion.GLOBAL
    ): Result<List<TmdbMovie>> = withContext(Dispatchers.IO) {
        try {
            val filterParams = ContentFilter.discoverParams()
            val list = fetchWithFilter(
                initialPage = page,
                fetchPage = { p ->
                    if (region.originalLanguages != null) {
                        api.discoverMovies(
                            withGenres = genreId.toString(),
                            withOriginalLanguage = region.originalLanguages,
                            language = region.languageCode,
                            page = p,
                            withoutKeywords = filterParams.withoutKeywords,
                            withoutCompanies = filterParams.withoutCompanies,
                            includeAdult = false
                        )
                    } else {
                        api.discoverMoviesByGenre(
                            genreId = genreId,
                            page = p,
                            language = region.languageCode,
                            withoutKeywords = filterParams.withoutKeywords,
                            withoutCompanies = filterParams.withoutCompanies,
                            includeAdult = false
                        )
                    }
                },
                filterFn = { filterMoviesWithReports(it) }
            )
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchMovies(
        query: String,
        page: Int = 1,
        region: ContentRegion = ContentRegion.GLOBAL
    ): Result<List<TmdbMovie>> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext Result.success(emptyList())
        if (ContentFilter.isBlockedSearchQuery(trimmed)) return@withContext Result.success(emptyList())

        try {
            coroutineScope {
                val variants = ArabicSearchHelper.generateSearchVariants(trimmed)
                val searchLanguage = if (ArabicSearchHelper.isArabic(trimmed) || region == ContentRegion.ARABIC) "ar-SA" else region.languageCode

                val deferreds = variants.map { variant ->
                    async {
                        try {
                            api.searchMovies(variant, page, language = searchLanguage, includeAdult = false).results
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }
                }

                val allResults = deferreds.flatMap { it.await() }
                // Deduplicate by ID
                val uniqueMap = linkedMapOf<Int, TmdbMovie>()
                allResults.forEach { movie ->
                    if (!uniqueMap.containsKey(movie.id)) {
                        uniqueMap[movie.id] = movie
                    }
                }

                var finalResults = uniqueMap.values.toList()

                // Filter by region languages if region is not Global
                if (region != ContentRegion.GLOBAL && region.originalLanguages != null) {
                    val allowed = region.originalLanguages.split("|").toSet()
                    val filtered = finalResults.filter { it.originalLanguage in allowed }
                    if (filtered.isNotEmpty()) {
                        finalResults = filtered
                    }
                }

                Result.success(filterMoviesWithReports(finalResults))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMovieDetails(
        movieId: Int,
        region: ContentRegion = ContentRegion.GLOBAL
    ): Result<TmdbMovieDetail> = withContext(Dispatchers.IO) {
        try {
            val detail = api.getMovieDetails(movieId, language = region.languageCode)
            if (ContentFilter.isBlockedMovie(detail)) {
                ContentFilter.addDerivedBlockedMovie(detail.id)
                Result.failure(Exception("Movie is blocked"))
            } else {
                val filteredSimilar = detail.similar?.let { sim ->
                    sim.copy(results = filterMoviesWithReports(sim.results))
                }
                Result.success(detail.copy(similar = filteredSimilar))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // === TV SERIES ===
    suspend fun getTrendingTv(
        timeWindow: String = "day",
        page: Int = 1,
        region: ContentRegion = ContentRegion.GLOBAL
    ): Result<List<TmdbTv>> = withContext(Dispatchers.IO) {
        try {
            val filterParams = ContentFilter.discoverParams()
            val list = fetchWithFilter(
                initialPage = page,
                fetchPage = { p ->
                    if (region == ContentRegion.GLOBAL) {
                        api.getTrendingTv(timeWindow, p, language = region.languageCode)
                    } else if (region.originalLanguages != null) {
                        api.discoverTv(
                            withOriginalLanguage = region.originalLanguages,
                            language = region.languageCode,
                            page = p,
                            sortBy = "popularity.desc",
                            withoutKeywords = filterParams.withoutKeywords,
                            withoutCompanies = filterParams.withoutCompanies,
                            includeAdult = false
                        )
                    } else {
                        api.getTrendingTv(timeWindow, p, language = region.languageCode)
                    }
                },
                filterFn = { filterTvWithReports(it) }
            )
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPopularTv(
        page: Int = 1,
        region: ContentRegion = ContentRegion.GLOBAL
    ): Result<List<TmdbTv>> = withContext(Dispatchers.IO) {
        try {
            val filterParams = ContentFilter.discoverParams()
            val list = fetchWithFilter(
                initialPage = page,
                fetchPage = { p ->
                    api.discoverTv(
                        withOriginalLanguage = if (region == ContentRegion.GLOBAL) null else region.originalLanguages,
                        language = region.languageCode,
                        page = p,
                        sortBy = "popularity.desc",
                        withoutKeywords = filterParams.withoutKeywords,
                        withoutCompanies = filterParams.withoutCompanies,
                        includeAdult = false
                    )
                },
                filterFn = { filterTvWithReports(it) }
            )
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTopRatedTv(
        page: Int = 1,
        region: ContentRegion = ContentRegion.GLOBAL
    ): Result<List<TmdbTv>> = withContext(Dispatchers.IO) {
        try {
            val filterParams = ContentFilter.discoverParams()
            val list = fetchWithFilter(
                initialPage = page,
                fetchPage = { p ->
                    api.discoverTv(
                        withOriginalLanguage = if (region == ContentRegion.GLOBAL) null else region.originalLanguages,
                        language = region.languageCode,
                        page = p,
                        sortBy = "vote_average.desc",
                        withoutKeywords = filterParams.withoutKeywords,
                        withoutCompanies = filterParams.withoutCompanies,
                        voteCountGte = 100,
                        includeAdult = false
                    )
                },
                filterFn = { filterTvWithReports(it) }
            )
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTvByGenre(
        genreId: Int,
        page: Int = 1,
        region: ContentRegion = ContentRegion.GLOBAL
    ): Result<List<TmdbTv>> = withContext(Dispatchers.IO) {
        try {
            val filterParams = ContentFilter.discoverParams()
            val list = fetchWithFilter(
                initialPage = page,
                fetchPage = { p ->
                    if (region.originalLanguages != null) {
                        api.discoverTv(
                            withGenres = genreId.toString(),
                            withOriginalLanguage = region.originalLanguages,
                            language = region.languageCode,
                            page = p,
                            withoutKeywords = filterParams.withoutKeywords,
                            withoutCompanies = filterParams.withoutCompanies,
                            includeAdult = false
                        )
                    } else {
                        api.discoverTvByGenre(
                            genreId = genreId,
                            page = p,
                            language = region.languageCode,
                            withoutKeywords = filterParams.withoutKeywords,
                            withoutCompanies = filterParams.withoutCompanies,
                            includeAdult = false
                        )
                    }
                },
                filterFn = { filterTvWithReports(it) }
            )
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchTv(
        query: String,
        page: Int = 1,
        region: ContentRegion = ContentRegion.GLOBAL
    ): Result<List<TmdbTv>> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext Result.success(emptyList())
        if (ContentFilter.isBlockedSearchQuery(trimmed)) return@withContext Result.success(emptyList())

        try {
            coroutineScope {
                val variants = ArabicSearchHelper.generateSearchVariants(trimmed)
                val searchLanguage = if (ArabicSearchHelper.isArabic(trimmed) || region == ContentRegion.ARABIC) "ar-SA" else region.languageCode

                val deferreds = variants.map { variant ->
                    async {
                        try {
                            api.searchTv(variant, page, language = searchLanguage, includeAdult = false).results
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }
                }

                val allResults = deferreds.flatMap { it.await() }
                val uniqueMap = linkedMapOf<Int, TmdbTv>()
                allResults.forEach { tv ->
                    if (!uniqueMap.containsKey(tv.id)) {
                        uniqueMap[tv.id] = tv
                    }
                }

                var finalResults = uniqueMap.values.toList()

                if (region != ContentRegion.GLOBAL && region.originalLanguages != null) {
                    val allowed = region.originalLanguages.split("|").toSet()
                    val filtered = finalResults.filter { it.originalLanguage in allowed }
                    if (filtered.isNotEmpty()) {
                        finalResults = filtered
                    }
                }

                Result.success(filterTvWithReports(finalResults))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTvDetails(
        tvId: Int,
        region: ContentRegion = ContentRegion.GLOBAL
    ): Result<TmdbTvDetail> = withContext(Dispatchers.IO) {
        try {
            val detail = api.getTvDetails(tvId, language = region.languageCode)
            if (ContentFilter.isBlockedTv(detail)) {
                ContentFilter.addDerivedBlockedTv(detail.id)
                Result.failure(Exception("Series is blocked"))
            } else {
                val filteredSimilar = detail.similar?.let { sim ->
                    sim.copy(results = filterTvWithReports(sim.results))
                }
                Result.success(detail.copy(similar = filteredSimilar))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTvSeasonDetails(
        tvId: Int,
        seasonNumber: Int,
        region: ContentRegion = ContentRegion.GLOBAL
    ): Result<TmdbSeasonDetail> = withContext(Dispatchers.IO) {
        try {
            val detail = api.getTvSeasonDetails(tvId, seasonNumber, language = region.languageCode)
            Result.success(detail)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // === ROOM WATCHLIST OPERATIONS ===
    fun getAllWatchlist(): Flow<List<WatchlistEntity>> = combine(
        watchlistDao.getAllWatchlist(),
        ContentFilter.blocklist,
        reportStore.reportedIdsFlow
    ) { list, _, reportedIds ->
        list.filter { entity ->
            if (reportedIds.contains(entity.id)) return@filter false
            if (entity.mediaType == "tv") {
                !ContentFilter.isBlockedTv(id = entity.id, name = entity.title)
            } else {
                !ContentFilter.isBlockedMovie(id = entity.id, title = entity.title)
            }
        }
    }

    suspend fun addToWatchlist(item: WatchlistEntity) = withContext(Dispatchers.IO) {
        watchlistDao.addToWatchlist(item)
    }

    suspend fun removeFromWatchlist(id: Int) = withContext(Dispatchers.IO) {
        watchlistDao.removeFromWatchlist(id)
    }

    suspend fun isItemInWatchlist(id: Int): Boolean = withContext(Dispatchers.IO) {
        watchlistDao.isSavedSync(id)
    }

    suspend fun toggleWatchlist(
        id: Int,
        mediaType: String,
        title: String,
        overview: String,
        posterPath: String?,
        backdropPath: String?,
        voteAverage: Double,
        releaseDate: String?
    ): Boolean = withContext(Dispatchers.IO) {
        val isCurrentlySaved = watchlistDao.isSavedSync(id)
        if (isCurrentlySaved) {
            watchlistDao.removeFromWatchlist(id)
            false
        } else {
            val entity = WatchlistEntity(
                id = id,
                mediaType = mediaType,
                title = title,
                overview = overview,
                posterPath = posterPath,
                backdropPath = backdropPath,
                voteAverage = voteAverage,
                releaseDate = releaseDate,
                addedAt = System.currentTimeMillis()
            )
            watchlistDao.addToWatchlist(entity)
            true
        }
    }

    // === ROOM RECENT HISTORY OPERATIONS ===
    fun getAllHistory(): Flow<List<RecentHistoryEntity>> = combine(
        recentHistoryDao.getAllHistory(),
        ContentFilter.blocklist,
        reportStore.reportedIdsFlow
    ) { list, _, reportedIds ->
        list.filter { entity ->
            if (reportedIds.contains(entity.mediaId)) return@filter false
            if (entity.mediaType == "tv") {
                !ContentFilter.isBlockedTv(id = entity.mediaId, name = entity.title)
            } else {
                !ContentFilter.isBlockedMovie(id = entity.mediaId, title = entity.title)
            }
        }
    }

    suspend fun savePlaybackProgress(
        mediaId: Int,
        mediaType: String,
        title: String,
        seasonNumber: Int?,
        episodeNumber: Int?,
        episodeTitle: String?,
        posterPath: String?,
        backdropPath: String?,
        currentPositionSeconds: Long,
        totalDurationSeconds: Long
    ) = withContext(Dispatchers.IO) {
        val historyId = if (mediaType == "tv" && seasonNumber != null && episodeNumber != null) {
            "tv_${mediaId}_s${seasonNumber}_e${episodeNumber}"
        } else {
            "movie_${mediaId}"
        }
        val entity = RecentHistoryEntity(
            historyId = historyId,
            mediaId = mediaId,
            mediaType = mediaType,
            title = title,
            posterPath = posterPath,
            backdropPath = backdropPath,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            episodeTitle = episodeTitle,
            lastPositionSeconds = currentPositionSeconds,
            totalDurationSeconds = totalDurationSeconds,
            updatedAt = System.currentTimeMillis()
        )
        recentHistoryDao.upsertHistory(entity)
    }

    suspend fun deleteHistoryItem(historyId: String) = withContext(Dispatchers.IO) {
        recentHistoryDao.deleteHistoryItem(historyId)
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        recentHistoryDao.clearAllHistory()
    }

    // === NEW RELEASES (MOVIES & SERIES) ===
    suspend fun getNewReleaseMovies(
        page: Int = 1,
        region: ContentRegion = ContentRegion.GLOBAL
    ): Result<List<TmdbMovie>> = withContext(Dispatchers.IO) {
        try {
            val filterParams = ContentFilter.discoverParams()
            val response = api.discoverMovies(
                withOriginalLanguage = if (region != ContentRegion.GLOBAL) region.originalLanguages else null,
                language = region.languageCode,
                page = page,
                sortBy = "primary_release_date.desc",
                withoutKeywords = filterParams.withoutKeywords,
                withoutCompanies = filterParams.withoutCompanies,
                includeAdult = false
            )
            val valid = response.results.filter { !it.releaseDate.isNullOrBlank() }
            Result.success(filterMoviesWithReports(valid))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNewReleaseTv(
        page: Int = 1,
        region: ContentRegion = ContentRegion.GLOBAL
    ): Result<List<TmdbTv>> = withContext(Dispatchers.IO) {
        try {
            val filterParams = ContentFilter.discoverParams()
            val response = api.discoverTv(
                withOriginalLanguage = if (region != ContentRegion.GLOBAL) region.originalLanguages else null,
                language = region.languageCode,
                page = page,
                sortBy = "first_air_date.desc",
                withoutKeywords = filterParams.withoutKeywords,
                withoutCompanies = filterParams.withoutCompanies,
                includeAdult = false
            )
            Result.success(filterTvWithReports(response.results))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // === COMPANIES OPERATIONS ===
    suspend fun searchCompanies(
        query: String,
        region: ContentRegion = ContentRegion.GLOBAL
    ): Result<List<ProductionCompanyInfo>> = withContext(Dispatchers.IO) {
        try {
            val q = query.trim().lowercase(java.util.Locale.ROOT)
            val matchedCurated = CompaniesCatalog.allCompanies.filter { c ->
                c.name.lowercase(java.util.Locale.ROOT).contains(q) ||
                        c.arabicName.contains(query.trim())
            }

            val apiResults = mutableListOf<ProductionCompanyInfo>()
            try {
                val res = api.searchCompanies(query = query.trim(), page = 1)
                res.results.forEach { item ->
                    if (matchedCurated.none { it.id == item.id }) {
                        apiResults.add(
                            ProductionCompanyInfo(
                                id = item.id,
                                name = item.name,
                                arabicName = item.name,
                                description = "Production Company",
                                logoPath = item.logoPath,
                                originCountry = item.originCountry ?: "",
                                regions = listOf(region)
                            )
                        )
                    }
                }
            } catch (_: Exception) {}

            val combined = (matchedCurated + apiResults).distinctBy { it.id }
            val filtered = if (region != ContentRegion.GLOBAL) {
                val arabicCountries = setOf("EG", "SA", "AE", "SY", "LB", "MA", "TN", "JO", "KW", "QA", "OM", "BH", "IQ", "LY", "SD", "DZ")
                val asianCountries = setOf("JP", "KR", "CN", "HK", "TW", "IN", "TH", "PH", "SG", "ID")
                val euroLatinCountries = setOf("GB", "FR", "ES", "IT", "DE", "MX", "BR", "AR", "CO", "CL", "TR")

                combined.filter { c ->
                    when (region) {
                        ContentRegion.ARABIC -> c.originCountry.uppercase() in arabicCountries || c.regions.contains(ContentRegion.ARABIC)
                        ContentRegion.HOLLYWOOD -> c.originCountry.uppercase() == "US" || c.regions.contains(ContentRegion.HOLLYWOOD)
                        ContentRegion.ASIAN -> c.originCountry.uppercase() in asianCountries || c.regions.contains(ContentRegion.ASIAN)
                        ContentRegion.EURO_LATIN -> c.originCountry.uppercase() in euroLatinCountries || c.regions.contains(ContentRegion.EURO_LATIN)
                        ContentRegion.GLOBAL -> true
                    }
                }
            } else {
                combined
            }
            Result.success(filterCompaniesWithReports(filtered))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCompanyDetails(companyId: Int): Result<ProductionCompanyInfo> = withContext(Dispatchers.IO) {
        try {
            val curated = CompaniesCatalog.allCompanies.firstOrNull { it.id == companyId }
            if (curated != null) return@withContext Result.success(curated)

            val detail = api.getCompanyDetails(companyId)
            Result.success(
                ProductionCompanyInfo(
                    id = detail.id,
                    name = detail.name,
                    arabicName = detail.name,
                    description = "Production Company",
                    logoPath = detail.logoPath,
                    originCountry = detail.originCountry ?: ""
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCompanyMovies(
        companyId: Int,
        sortBy: String = "popularity.desc",
        page: Int = 1,
        region: ContentRegion = ContentRegion.GLOBAL
    ): Result<List<TmdbMovie>> = withContext(Dispatchers.IO) {
        try {
            val filterParams = ContentFilter.discoverParams()
            val allMovies = mutableListOf<TmdbMovie>()
            var currentPage = 1
            var totalPages = 1
            val maxPages = 50

            while (currentPage <= totalPages && currentPage <= maxPages) {
                val res = api.discoverMovies(
                    withCompanies = companyId.toString(),
                    language = region.languageCode,
                    page = currentPage,
                    sortBy = sortBy,
                    withoutKeywords = filterParams.withoutKeywords,
                    withoutCompanies = filterParams.withoutCompanies,
                    includeAdult = false
                )
                allMovies.addAll(res.results)
                totalPages = res.totalPages
                currentPage++
            }
            val filtered = filterMoviesWithReports(allMovies.distinctBy { it.id })
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCompanyTv(
        companyId: Int,
        sortBy: String = "popularity.desc",
        page: Int = 1,
        region: ContentRegion = ContentRegion.GLOBAL
    ): Result<List<TmdbTv>> = withContext(Dispatchers.IO) {
        try {
            val filterParams = ContentFilter.discoverParams()
            val allTv = mutableListOf<TmdbTv>()
            var currentPage = 1
            var totalPages = 1
            val maxPages = 50

            while (currentPage <= totalPages && currentPage <= maxPages) {
                val res = api.discoverTv(
                    withCompanies = companyId.toString(),
                    language = region.languageCode,
                    page = currentPage,
                    sortBy = sortBy,
                    withoutKeywords = filterParams.withoutKeywords,
                    withoutCompanies = filterParams.withoutCompanies,
                    includeAdult = false
                )
                allTv.addAll(res.results)
                totalPages = res.totalPages
                currentPage++
            }
            val filtered = filterTvWithReports(allTv.distinctBy { it.id })
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllSavedCompanies(): Flow<List<SavedCompanyEntity>> = combine(
        savedCompanyDao.getAllSavedCompanies(),
        ContentFilter.blocklist,
        reportStore.reportedIdsFlow
    ) { list, _, reportedIds ->
        list.filter { entity ->
            if (reportedIds.contains(entity.companyId)) return@filter false
            !ContentFilter.isBlockedCompany(companyId = entity.companyId, companyName = entity.name)
        }
    }

    suspend fun isCompanySaved(companyId: Int): Boolean = withContext(Dispatchers.IO) {
        savedCompanyDao.isCompanySavedSync(companyId)
    }

    suspend fun toggleSaveCompany(company: ProductionCompanyInfo): Boolean = withContext(Dispatchers.IO) {
        val isSaved = savedCompanyDao.isCompanySavedSync(company.id)
        if (isSaved) {
            savedCompanyDao.removeCompany(company.id)
            false
        } else {
            val entity = SavedCompanyEntity(
                companyId = company.id,
                name = company.name,
                arabicName = company.arabicName,
                description = company.description,
                logoPath = company.logoPath,
                originCountry = company.originCountry,
                savedAt = System.currentTimeMillis()
            )
            savedCompanyDao.saveCompany(entity)
            true
        }
    }

    suspend fun removeSavedCompany(companyId: Int) = withContext(Dispatchers.IO) {
        savedCompanyDao.removeCompany(companyId)
    }

    // === PEOPLE OPERATIONS ===
    suspend fun searchPeople(
        query: String,
        region: ContentRegion = ContentRegion.GLOBAL
    ): Result<List<TmdbPerson>> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext Result.success(emptyList())
        try {
            val lang = if (ArabicSearchHelper.isArabic(trimmed) || region == ContentRegion.ARABIC) "ar-SA" else region.languageCode
            val res = api.searchPeople(query = trimmed, page = 1, language = lang, includeAdult = false)
            val filtered = filterPeopleWithReports(res.results.sortedByDescending { it.popularity })
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPopularPeople(
        region: ContentRegion = ContentRegion.GLOBAL
    ): Result<List<TmdbPerson>> = withContext(Dispatchers.IO) {
        try {
            val lang = if (region == ContentRegion.ARABIC) "ar-SA" else region.languageCode
            val res = api.getPopularPeople(page = 1, language = lang)
            val filtered = filterPeopleWithReports(res.results)
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPersonDetails(
        personId: Int,
        region: ContentRegion = ContentRegion.GLOBAL
    ): Result<TmdbPersonDetail> = withContext(Dispatchers.IO) {
        try {
            val lang = if (region == ContentRegion.ARABIC) "ar-SA" else region.languageCode
            val detail = api.getPersonDetails(personId, language = lang)
            if (ContentFilter.isBlockedPerson(detail)) {
                // If person is blocked, all their works are blocked as well
                val movieIds = detail.movieCredits?.cast?.map { it.id } ?: emptyList()
                val tvIds = detail.tvCredits?.cast?.map { it.id } ?: emptyList()
                ContentFilter.addDerivedBlockedWorks(movieIds, tvIds)
                Result.failure(Exception("Person is blocked"))
            } else {
                val filteredMovieCredits = detail.movieCredits?.let { mc ->
                    mc.copy(cast = filterMoviesWithReports(mc.cast))
                }
                val filteredTvCredits = detail.tvCredits?.let { tc ->
                    tc.copy(cast = filterTvWithReports(tc.cast))
                }
                Result.success(detail.copy(movieCredits = filteredMovieCredits, tvCredits = filteredTvCredits))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // === RESOLVE AND BLOCK WORKS FOR PEOPLE & COMPANIES ===
    suspend fun resolveAndBlockWorksForPerson(personId: Int) = withContext(Dispatchers.IO) {
        try {
            val detail = api.getPersonDetails(personId, appendToResponse = "movie_credits,tv_credits")
            val movieIds = detail.movieCredits?.cast?.map { it.id } ?: emptyList()
            val tvIds = detail.tvCredits?.cast?.map { it.id } ?: emptyList()
            ContentFilter.addDerivedBlockedWorks(movieIds, tvIds)
        } catch (_: Exception) {}
    }

    suspend fun resolveAndBlockWorksForCompany(companyId: Int) = withContext(Dispatchers.IO) {
        try {
            val movieIds = mutableListOf<Int>()
            val tvIds = mutableListOf<Int>()
            for (p in 1..5) {
                val moviesRes = try {
                    api.discoverMovies(withCompanies = companyId.toString(), page = p)
                } catch (_: Exception) { null }
                if (moviesRes != null) {
                    movieIds.addAll(moviesRes.results.map { it.id })
                    if (p >= moviesRes.totalPages) break
                } else break
            }
            for (p in 1..5) {
                val tvRes = try {
                    api.discoverTv(withCompanies = companyId.toString(), page = p)
                } catch (_: Exception) { null }
                if (tvRes != null) {
                    tvIds.addAll(tvRes.results.map { it.id })
                    if (p >= tvRes.totalPages) break
                } else break
            }
            ContentFilter.addDerivedBlockedWorks(movieIds, tvIds)
        } catch (_: Exception) {}
    }

    suspend fun resolveAndBlockWorksForKeyword(keywordId: Int) = withContext(Dispatchers.IO) {
        try {
            val movieIds = mutableListOf<Int>()
            val tvIds = mutableListOf<Int>()
            for (p in 1..2) {
                val moviesRes = try {
                    api.discoverMovies(withKeywords = keywordId.toString(), page = p)
                } catch (_: Exception) { null }
                if (moviesRes != null) {
                    movieIds.addAll(moviesRes.results.map { it.id })
                    if (p >= moviesRes.totalPages) break
                } else break
            }
            for (p in 1..2) {
                val tvRes = try {
                    api.discoverTv(withKeywords = keywordId.toString(), page = p)
                } catch (_: Exception) { null }
                if (tvRes != null) {
                    tvIds.addAll(tvRes.results.map { it.id })
                    if (p >= tvRes.totalPages) break
                } else break
            }
            ContentFilter.addDerivedBlockedWorks(movieIds, tvIds)
        } catch (_: Exception) {}
    }

    suspend fun syncAllBlockedWorks() = withContext(Dispatchers.IO) {
        val people = ContentFilter.getAllBlockedPeopleIds()
        for (personId in people) {
            resolveAndBlockWorksForPerson(personId)
        }
        val companies = ContentFilter.getAllBlockedCompanyIds()
        for (companyId in companies) {
            resolveAndBlockWorksForCompany(companyId)
        }
        val keywords = ContentFilter.getAllBlockedKeywordIds()
        for (keywordId in keywords) {
            resolveAndBlockWorksForKeyword(keywordId)
        }
    }

    suspend fun searchKeywords(query: String): List<com.example.data.model.TmdbKeyword> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext emptyList()
        try {
            api.searchKeywords(trimmed, page = 1).results
        } catch (_: Exception) {
            emptyList()
        }
    }
}
