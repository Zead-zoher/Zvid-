package com.example.data.filter

import com.example.data.model.ProductionCompanyInfo
import com.example.data.model.TmdbMovie
import com.example.data.model.TmdbMovieDetail
import com.example.data.model.TmdbPerson
import com.example.data.model.TmdbPersonCreditItem
import com.example.data.model.TmdbPersonDetail
import com.example.data.model.TmdbProductionCompany
import com.example.data.model.TmdbTv
import com.example.data.model.TmdbTvDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Blocklist(
    val movies: Set<Int> = emptySet(),
    val tv: Set<Int> = emptySet(),
    val companies: Set<Int> = emptySet(),
    val people: Set<Int> = emptySet(),
    val keywords: Set<Int> = emptySet()
)

data class DiscoverFilterParams(
    val includeAdult: Boolean = false,
    val withoutKeywords: String? = null,
    val withoutCompanies: String? = null
)

object ContentFilter {

    // Comma-separated TMDB keyword IDs for sexual keywords only - easily editable
    // 9951 = hentai, 191630 = ecchi, 155477 = erotic, 9799 = romantic / sexual themes,
    // 235777 = pornography, 222243 = softcore, 158718 = explicit sex
    const val BLOCKED_KEYWORD_IDS: String = "9951,191630,155477,9799,235777,222243,158718"

    // Built-in sets (empty with TODO comments as specified)
    // TODO: Add hardcoded blocked movie IDs here if needed
    val BUILT_IN_MOVIES: Set<Int> = emptySet()

    // TODO: Add hardcoded blocked TV show IDs here if needed
    val BUILT_IN_TV: Set<Int> = emptySet()

    // TODO: Add hardcoded blocked production company IDs here if needed
    val BUILT_IN_COMPANIES: Set<Int> = emptySet()

    // TODO: Add hardcoded blocked people IDs here if needed
    val BUILT_IN_PEOPLE: Set<Int> = emptySet()

    // TODO: Add hardcoded blocked keyword IDs here if needed
    val BUILT_IN_KEYWORDS: Set<Int> = emptySet()

    private val initialBlocklist = Blocklist(
        movies = BUILT_IN_MOVIES,
        tv = BUILT_IN_TV,
        companies = BUILT_IN_COMPANIES,
        people = BUILT_IN_PEOPLE,
        keywords = BUILT_IN_KEYWORDS
    )

    private val _blocklist = MutableStateFlow(initialBlocklist)
    val blocklist: StateFlow<Blocklist> = _blocklist.asStateFlow()

    // Regex matching sexual / hentai / erotic terms only.
    // Preserves action, crime, war, violence, thriller, horror, blood, etc.
    private val SEXUAL_TITLE_REGEX = Regex(
        """(?i)(^|[^a-zA-Z0-9])(hentai|xxx|porn|porno|pornography|erotic|ecchi)($|[^a-zA-Z0-9])|hentai|ecchi"""
    )

    fun updateBlocklist(remoteBlocklist: Blocklist) {
        _blocklist.value = Blocklist(
            movies = BUILT_IN_MOVIES + remoteBlocklist.movies,
            tv = BUILT_IN_TV + remoteBlocklist.tv,
            companies = BUILT_IN_COMPANIES + remoteBlocklist.companies,
            people = BUILT_IN_PEOPLE + remoteBlocklist.people,
            keywords = BUILT_IN_KEYWORDS + remoteBlocklist.keywords
        )
    }

    private fun matchesSexualTitle(vararg titles: String?): Boolean {
        for (title in titles) {
            if (!title.isNullOrBlank()) {
                val trimmed = title.trim()
                if (SEXUAL_TITLE_REGEX.containsMatchIn(trimmed)) {
                    return true
                }
            }
        }
        return false
    }

    // === MOVIE CHECKS ===
    fun isBlockedMovie(movie: TmdbMovie): Boolean {
        if (movie.adult) return true
        val current = _blocklist.value
        if (current.movies.contains(movie.id)) return true
        if (matchesSexualTitle(movie.title, movie.originalTitle)) return true
        return false
    }

    fun isBlockedMovie(movieDetail: TmdbMovieDetail): Boolean {
        if (movieDetail.adult) return true
        val current = _blocklist.value
        if (current.movies.contains(movieDetail.id)) return true
        if (matchesSexualTitle(movieDetail.title, movieDetail.originalTitle)) return true
        val companies = movieDetail.productionCompanies
        if (companies != null && companies.any { current.companies.contains(it.id) }) {
            return true
        }
        return false
    }

    fun isBlockedMovie(id: Int, title: String? = null, originalTitle: String? = null, adult: Boolean = false, companyIds: List<Int> = emptyList()): Boolean {
        if (adult) return true
        val current = _blocklist.value
        if (current.movies.contains(id)) return true
        if (matchesSexualTitle(title, originalTitle)) return true
        if (companyIds.any { current.companies.contains(it) }) return true
        return false
    }

    // === TV CHECKS ===
    fun isBlockedTv(tv: TmdbTv): Boolean {
        if (tv.adult) return true
        val current = _blocklist.value
        if (current.tv.contains(tv.id)) return true
        if (matchesSexualTitle(tv.name, tv.originalName)) return true
        return false
    }

    fun isBlockedTv(tvDetail: TmdbTvDetail): Boolean {
        if (tvDetail.adult) return true
        val current = _blocklist.value
        if (current.tv.contains(tvDetail.id)) return true
        if (matchesSexualTitle(tvDetail.name, tvDetail.originalName)) return true
        val companies = tvDetail.productionCompanies
        if (companies != null && companies.any { current.companies.contains(it.id) }) {
            return true
        }
        return false
    }

    fun isBlockedTv(id: Int, name: String? = null, originalName: String? = null, adult: Boolean = false, companyIds: List<Int> = emptyList()): Boolean {
        if (adult) return true
        val current = _blocklist.value
        if (current.tv.contains(id)) return true
        if (matchesSexualTitle(name, originalName)) return true
        if (companyIds.any { current.companies.contains(it) }) return true
        return false
    }

    // === PERSON CHECKS ===
    fun isBlockedPerson(person: TmdbPerson): Boolean {
        if (person.adult) return true
        val current = _blocklist.value
        if (current.people.contains(person.id)) return true
        if (matchesSexualTitle(person.name, person.originalName)) return true
        return false
    }

    fun isBlockedPerson(personDetail: TmdbPersonDetail): Boolean {
        if (personDetail.adult) return true
        val current = _blocklist.value
        if (current.people.contains(personDetail.id)) return true
        if (matchesSexualTitle(personDetail.name)) return true
        return false
    }

    // === COMPANY CHECKS ===
    fun isBlockedCompany(company: TmdbProductionCompany): Boolean {
        if (company.adult) return true
        val current = _blocklist.value
        if (current.companies.contains(company.id)) return true
        if (matchesSexualTitle(company.name)) return true
        return false
    }

    fun isBlockedCompany(company: ProductionCompanyInfo): Boolean {
        if (company.adult) return true
        val current = _blocklist.value
        if (current.companies.contains(company.id)) return true
        if (matchesSexualTitle(company.name, company.arabicName)) return true
        return false
    }

    fun isBlockedCompany(companyId: Int, companyName: String = "", adult: Boolean = false): Boolean {
        if (adult) return true
        val current = _blocklist.value
        if (current.companies.contains(companyId)) return true
        if (matchesSexualTitle(companyName)) return true
        return false
    }

    // === LIST FILTERING HELPERS ===
    fun filterMovies(list: List<TmdbMovie>): List<TmdbMovie> {
        return list.filter { !isBlockedMovie(it) }
    }

    fun filterTv(list: List<TmdbTv>): List<TmdbTv> {
        return list.filter { !isBlockedTv(it) }
    }

    fun filterPeople(list: List<TmdbPerson>): List<TmdbPerson> {
        return list.filter { !isBlockedPerson(it) }
    }

    fun filterCompanies(list: List<ProductionCompanyInfo>): List<ProductionCompanyInfo> {
        return list.filter { !isBlockedCompany(it) }
    }

    fun filterTmdbCompanies(list: List<TmdbProductionCompany>): List<TmdbProductionCompany> {
        return list.filter { !isBlockedCompany(it) }
    }

    fun filterPersonCreditItems(list: List<TmdbPersonCreditItem>): List<TmdbPersonCreditItem> {
        val current = _blocklist.value
        return list.filter { item ->
            if (item.adult) return@filter false
            if (matchesSexualTitle(item.title, item.name)) return@filter false
            val isTv = item.mediaType == "tv"
            if (isTv) {
                !current.tv.contains(item.id)
            } else {
                !current.movies.contains(item.id)
            }
        }
    }

    // === DISCOVER PARAMS HELPER ===
    fun discoverParams(): DiscoverFilterParams {
        val current = _blocklist.value
        val defaultKeywordList = BLOCKED_KEYWORD_IDS.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
        val allKeywords = defaultKeywordList + current.keywords
        val withoutKeywordsStr = if (allKeywords.isNotEmpty()) {
            allKeywords.joinToString(",")
        } else null

        val withoutCompaniesStr = if (current.companies.isNotEmpty()) {
            current.companies.joinToString(",")
        } else null

        return DiscoverFilterParams(
            includeAdult = false,
            withoutKeywords = withoutKeywordsStr,
            withoutCompanies = withoutCompaniesStr
        )
    }

    // Debug helper to resolve and print TMDB keyword IDs for sexual keywords
    suspend fun debugResolveKeywordIds(keywordSearchFn: suspend (query: String) -> List<Pair<Int, String>>) {
        val queries = listOf("hentai", "ecchi", "erotic", "softcore", "pornography", "explicit sex", "nudity", "sex scene")
        for (q in queries) {
            try {
                val results = keywordSearchFn(q)
                println("TMDB Keyword query '$q' -> results: $results")
            } catch (e: Exception) {
                println("Failed to resolve keyword '$q': ${e.message}")
            }
        }
    }
}
