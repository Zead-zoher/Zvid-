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
import com.example.util.ArabicSearchHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Blocklist(
    val movies: Set<Int> = emptySet(),
    val tv: Set<Int> = emptySet(),
    val companies: Set<Int> = emptySet(),
    val people: Set<Int> = emptySet(),
    val keywords: Set<Int> = emptySet(),
    val words: Set<String> = emptySet()
)

data class DiscoverFilterParams(
    val includeAdult: Boolean = false,
    val withoutKeywords: String? = null,
    val withoutCompanies: String? = null
)

object ContentFilter {

    // Comma-separated TMDB keyword IDs for sexual keywords only - easily editable
    const val BLOCKED_KEYWORD_IDS: String = "596,190370,9951,191630,155477,9799,235777,222243,158718,33494,4217,368159,333072,353663,336330,264386,363345,378893,33451"

    // Built-in sets matching official blocklist
    val BUILT_IN_MOVIES: Set<Int> = setOf(941071)
    val BUILT_IN_TV: Set<Int> = setOf(95897, 325655)
    val BUILT_IN_COMPANIES: Set<Int> = setOf(80856)
    val BUILT_IN_PEOPLE: Set<Int> = setOf(3961517, 3164807, 166081, 1721615, 240615, 1134569, 553597)
    val BUILT_IN_KEYWORDS: Set<Int> = setOf(
        596, 190370, 9951, 191630, 155477, 9799, 235777, 222243, 158718, 33494, 4217,
        368159, 333072, 353663, 336330, 264386, 363345, 378893, 33451
    )
    val BUILT_IN_WORDS: Set<String> = setOf(
        "sex", "sexy", "sexual", "sexuality", "hentai", "xxx", "porn", "porno",
        "pornography", "erotic", "erotica", "ecchi", "softcore", "explicit sex",
        "incest", "سكس", "إباحي", "اباحي", "إباحية", "اباحية", "بورنو", "هينتاي",
        "جنسي", "جنسيه", "جنسية"
    )

    private val initialBlocklist = Blocklist(
        movies = BUILT_IN_MOVIES,
        tv = BUILT_IN_TV,
        companies = BUILT_IN_COMPANIES,
        people = BUILT_IN_PEOPLE,
        keywords = BUILT_IN_KEYWORDS,
        words = BUILT_IN_WORDS
    )

    private val _blocklist = MutableStateFlow(initialBlocklist)
    val blocklist: StateFlow<Blocklist> = _blocklist.asStateFlow()

    // Sets of works (movies and TV shows) derived from blocked actors, blocked companies, and blocked keywords
    private val _derivedBlockedMovieIds = MutableStateFlow<Set<Int>>(emptySet())
    val derivedBlockedMovieIds: StateFlow<Set<Int>> = _derivedBlockedMovieIds.asStateFlow()

    private val _derivedBlockedTvIds = MutableStateFlow<Set<Int>>(emptySet())
    val derivedBlockedTvIds: StateFlow<Set<Int>> = _derivedBlockedTvIds.asStateFlow()

    // Regex matching sexual / hentai / erotic / sex terms.
    private val SEXUAL_WORD_REGEX = Regex(
        """(?i)(^|[^a-zA-Z0-9])(sex|sexy|sexual|sexuality|hentai|xxx|porn|porno|pornography|erotic|erotica|ecchi|softcore|hardcore|nudity|nude)($|[^a-zA-Z0-9])"""
    )

    private val ARABIC_SEXUAL_REGEX = Regex(
        """(سكس|إباحي|اباحي|إباحية|اباحية|بورنو|هينتاي|جنسي|جنسيه|جنسية|مثير|شذوذ|لواط|عري|عاري)"""
    )

    fun updateBlocklist(remoteBlocklist: Blocklist) {
        _blocklist.value = Blocklist(
            movies = BUILT_IN_MOVIES + remoteBlocklist.movies,
            tv = BUILT_IN_TV + remoteBlocklist.tv,
            companies = BUILT_IN_COMPANIES + remoteBlocklist.companies,
            people = BUILT_IN_PEOPLE + remoteBlocklist.people,
            keywords = BUILT_IN_KEYWORDS + remoteBlocklist.keywords,
            words = BUILT_IN_WORDS + remoteBlocklist.words
        )
    }

    fun getAllBlockedPeopleIds(): Set<Int> {
        return _blocklist.value.people
    }

    fun getAllBlockedCompanyIds(): Set<Int> {
        return _blocklist.value.companies
    }

    fun getAllBlockedKeywordIds(): Set<Int> {
        val defaultKeywordList = BLOCKED_KEYWORD_IDS.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
        return defaultKeywordList + _blocklist.value.keywords
    }

    fun getAllBlockedWords(): Set<String> {
        return _blocklist.value.words
    }

    fun isBlockedPersonId(id: Int): Boolean {
        return getAllBlockedPeopleIds().contains(id)
    }

    fun isBlockedCompanyId(id: Int): Boolean {
        return getAllBlockedCompanyIds().contains(id)
    }

    fun isBlockedKeywordId(id: Int): Boolean {
        return getAllBlockedKeywordIds().contains(id)
    }

    fun addDerivedBlockedWorks(movieIds: Collection<Int>, tvIds: Collection<Int>) {
        if (movieIds.isNotEmpty()) {
            _derivedBlockedMovieIds.value = _derivedBlockedMovieIds.value + movieIds
        }
        if (tvIds.isNotEmpty()) {
            _derivedBlockedTvIds.value = _derivedBlockedTvIds.value + tvIds
        }
    }

    fun addDerivedBlockedMovie(movieId: Int) {
        _derivedBlockedMovieIds.value = _derivedBlockedMovieIds.value + movieId
    }

    fun addDerivedBlockedTv(tvId: Int) {
        _derivedBlockedTvIds.value = _derivedBlockedTvIds.value + tvId
    }

    private fun normalizeArabic(text: String): String {
        return text.replace(Regex("[\\u064B-\\u065F\\u0670\\u0640]"), "") // Tashkeel and tatweel
            .replace(Regex("[أإآٱ]"), "ا")
            .replace("ة", "ه")
            .replace("ى", "ي")
            .trim()
    }

    fun containsBlockedWord(text: String, word: String): Boolean {
        val trimmedText = text.trim()
        val trimmedWord = word.trim()
        if (trimmedText.isBlank() || trimmedWord.isBlank()) return false

        // 1. Arabic matching with normalization
        if (ArabicSearchHelper.isArabic(trimmedWord) || ArabicSearchHelper.isArabic(trimmedText)) {
            val normText = normalizeArabic(trimmedText)
            val normWord = normalizeArabic(trimmedWord)
            if (normWord.isNotBlank() && normText.contains(normWord)) {
                return true
            }
        }

        // 2. English / Latin matching
        if (trimmedWord.contains(" ")) {
            if (trimmedText.contains(trimmedWord, ignoreCase = true)) {
                return true
            }
        } else {
            val pattern = Regex("""(?i)(^|[^a-zA-Z0-9])${Regex.escape(trimmedWord)}($|[^a-zA-Z0-9])""")
            if (pattern.containsMatchIn(trimmedText) || trimmedText.equals(trimmedWord, ignoreCase = true)) {
                return true
            }
        }

        return false
    }

    fun matchesBlockedText(vararg texts: String?): Boolean {
        val customWords = _blocklist.value.words
        for (text in texts) {
            if (!text.isNullOrBlank()) {
                val trimmed = text.trim()
                if (SEXUAL_WORD_REGEX.containsMatchIn(trimmed) || ARABIC_SEXUAL_REGEX.containsMatchIn(trimmed)) {
                    return true
                }
                for (w in customWords) {
                    if (w.isNotBlank() && containsBlockedWord(trimmed, w)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    // Keep alias for backwards compatibility
    fun matchesSexualText(vararg texts: String?): Boolean = matchesBlockedText(*texts)

    fun isBlockedSearchQuery(query: String): Boolean {
        if (query.isBlank()) return false
        val trimmed = query.trim()
        if (matchesBlockedText(trimmed)) return true
        val customWords = _blocklist.value.words
        for (w in customWords) {
            if (w.isNotBlank() && containsBlockedWord(trimmed, w)) {
                return true
            }
        }
        return false
    }

    // === MOVIE CHECKS ===
    fun isBlockedMovie(movie: TmdbMovie): Boolean {
        if (movie.adult) return true
        val current = _blocklist.value
        if (current.movies.contains(movie.id)) return true
        if (_derivedBlockedMovieIds.value.contains(movie.id)) return true
        if (matchesBlockedText(movie.title, movie.originalTitle, movie.overview)) return true
        return false
    }

    fun isBlockedMovie(movieDetail: TmdbMovieDetail): Boolean {
        if (movieDetail.adult) return true
        val current = _blocklist.value
        if (current.movies.contains(movieDetail.id)) return true
        if (_derivedBlockedMovieIds.value.contains(movieDetail.id)) return true
        if (matchesBlockedText(movieDetail.title, movieDetail.originalTitle, movieDetail.overview, movieDetail.tagline)) return true

        // Check if any keyword in this movie is blocked
        val kwList = movieDetail.keywordsContainer?.keywords
        if (kwList != null && kwList.any { isBlockedKeywordId(it.id) || matchesBlockedText(it.name) }) {
            addDerivedBlockedMovie(movieDetail.id)
            return true
        }

        // Check if any company producing this movie is blocked
        val companies = movieDetail.productionCompanies
        if (companies != null && companies.any { isBlockedCompanyId(it.id) }) {
            addDerivedBlockedMovie(movieDetail.id)
            return true
        }

        // Check if any actor/person starring in this movie is blocked
        val cast = movieDetail.credits?.cast
        if (cast != null && cast.any { isBlockedPersonId(it.id) }) {
            addDerivedBlockedMovie(movieDetail.id)
            return true
        }

        return false
    }

    fun isBlockedMovie(
        id: Int,
        title: String? = null,
        originalTitle: String? = null,
        adult: Boolean = false,
        companyIds: List<Int> = emptyList(),
        castIds: List<Int> = emptyList(),
        keywordIds: List<Int> = emptyList()
    ): Boolean {
        if (adult) return true
        val current = _blocklist.value
        if (current.movies.contains(id)) return true
        if (_derivedBlockedMovieIds.value.contains(id)) return true
        if (matchesBlockedText(title, originalTitle)) return true
        if (keywordIds.any { isBlockedKeywordId(it) }) {
            addDerivedBlockedMovie(id)
            return true
        }
        if (companyIds.any { isBlockedCompanyId(it) }) {
            addDerivedBlockedMovie(id)
            return true
        }
        if (castIds.any { isBlockedPersonId(it) }) {
            addDerivedBlockedMovie(id)
            return true
        }
        return false
    }

    // === TV CHECKS ===
    fun isBlockedTv(tv: TmdbTv): Boolean {
        if (tv.adult) return true
        val current = _blocklist.value
        if (current.tv.contains(tv.id)) return true
        if (_derivedBlockedTvIds.value.contains(tv.id)) return true
        if (matchesBlockedText(tv.name, tv.originalName, tv.overview)) return true
        return false
    }

    fun isBlockedTv(tvDetail: TmdbTvDetail): Boolean {
        if (tvDetail.adult) return true
        val current = _blocklist.value
        if (current.tv.contains(tvDetail.id)) return true
        if (_derivedBlockedTvIds.value.contains(tvDetail.id)) return true
        if (matchesBlockedText(tvDetail.name, tvDetail.originalName, tvDetail.overview, tvDetail.tagline)) return true

        // Check if any keyword in this TV show is blocked
        val kwList = tvDetail.keywordsContainer?.results
        if (kwList != null && kwList.any { isBlockedKeywordId(it.id) || matchesBlockedText(it.name) }) {
            addDerivedBlockedTv(tvDetail.id)
            return true
        }

        // Check if any company producing this TV show is blocked
        val companies = tvDetail.productionCompanies
        if (companies != null && companies.any { isBlockedCompanyId(it.id) }) {
            addDerivedBlockedTv(tvDetail.id)
            return true
        }

        // Check if any actor/person starring in this TV show is blocked
        val cast = tvDetail.credits?.cast
        if (cast != null && cast.any { isBlockedPersonId(it.id) }) {
            addDerivedBlockedTv(tvDetail.id)
            return true
        }

        return false
    }

    fun isBlockedTv(
        id: Int,
        name: String? = null,
        originalName: String? = null,
        adult: Boolean = false,
        companyIds: List<Int> = emptyList(),
        castIds: List<Int> = emptyList(),
        keywordIds: List<Int> = emptyList()
    ): Boolean {
        if (adult) return true
        val current = _blocklist.value
        if (current.tv.contains(id)) return true
        if (_derivedBlockedTvIds.value.contains(id)) return true
        if (matchesBlockedText(name, originalName)) return true
        if (keywordIds.any { isBlockedKeywordId(it) }) {
            addDerivedBlockedTv(id)
            return true
        }
        if (companyIds.any { isBlockedCompanyId(it) }) {
            addDerivedBlockedTv(id)
            return true
        }
        if (castIds.any { isBlockedPersonId(it) }) {
            addDerivedBlockedTv(id)
            return true
        }
        return false
    }

    // === PERSON CHECKS ===
    fun isBlockedPerson(person: TmdbPerson): Boolean {
        if (person.adult) return true
        if (isBlockedPersonId(person.id)) return true
        if (matchesBlockedText(person.name, person.originalName)) return true
        return false
    }

    fun isBlockedPerson(personDetail: TmdbPersonDetail): Boolean {
        if (personDetail.adult) return true
        if (isBlockedPersonId(personDetail.id)) return true
        if (matchesBlockedText(personDetail.name)) return true
        return false
    }

    // === COMPANY CHECKS ===
    fun isBlockedCompany(company: TmdbProductionCompany): Boolean {
        if (company.adult) return true
        if (isBlockedCompanyId(company.id)) return true
        if (matchesBlockedText(company.name)) return true
        return false
    }

    fun isBlockedCompany(company: ProductionCompanyInfo): Boolean {
        if (company.adult) return true
        if (isBlockedCompanyId(company.id)) return true
        if (matchesBlockedText(company.name, company.arabicName)) return true
        return false
    }

    fun isBlockedCompany(companyId: Int, companyName: String = "", adult: Boolean = false): Boolean {
        if (adult) return true
        if (isBlockedCompanyId(companyId)) return true
        if (matchesBlockedText(companyName)) return true
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
        val derivedMovies = _derivedBlockedMovieIds.value
        val derivedTv = _derivedBlockedTvIds.value
        return list.filter { item ->
            if (item.adult) return@filter false
            if (matchesBlockedText(item.title, item.name)) return@filter false
            val isTv = item.mediaType == "tv"
            if (isTv) {
                !current.tv.contains(item.id) && !derivedTv.contains(item.id)
            } else {
                !current.movies.contains(item.id) && !derivedMovies.contains(item.id)
            }
        }
    }

    // === DISCOVER PARAMS HELPER ===
    fun discoverParams(): DiscoverFilterParams {
        val allKeywords = getAllBlockedKeywordIds()
        val withoutKeywordsStr = if (allKeywords.isNotEmpty()) {
            allKeywords.joinToString(",")
        } else null

        val allCompanyIds = getAllBlockedCompanyIds()
        val withoutCompaniesStr = if (allCompanyIds.isNotEmpty()) {
            allCompanyIds.joinToString(",")
        } else null

        return DiscoverFilterParams(
            includeAdult = false,
            withoutKeywords = withoutKeywordsStr,
            withoutCompanies = withoutCompaniesStr
        )
    }

    // Debug helper to resolve and print TMDB keyword IDs for sexual keywords
    suspend fun debugResolveKeywordIds(keywordSearchFn: suspend (query: String) -> List<Pair<Int, String>>) {
        val queries = listOf("hentai", "ecchi", "erotic", "softcore", "pornography", "explicit sex", "nudity", "sex scene", "sex")
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
