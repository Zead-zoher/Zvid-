package com.example

import com.example.data.filter.Blocklist
import com.example.data.filter.ContentFilter
import com.example.data.model.TmdbKeyword
import com.example.data.model.TmdbMovieDetail
import com.example.data.model.TmdbMovieKeywordsContainer
import com.example.data.model.TmdbTvDetail
import com.example.data.model.TmdbTvKeywordsContainer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun blocklistKeywords_areBlockedInContentFilter() {
        // Load keywords matching https://github.com/Zead-zoher/Zvid-/blob/main/blocklist.json
        val remoteBlocklist = Blocklist(
            movies = emptySet(),
            tv = setOf(95897),
            companies = emptySet(),
            people = setOf(3961517, 3164807, 166081),
            keywords = setOf(596, 190370, 9951, 191630, 155477, 9799, 235777, 222243, 158718, 33494, 4217)
        )
        ContentFilter.updateBlocklist(remoteBlocklist)

        // Check keyword IDs are recognized
        assertTrue("33494 should be blocked", ContentFilter.isBlockedKeywordId(33494))
        assertTrue("4217 should be blocked", ContentFilter.isBlockedKeywordId(4217))
        assertTrue("596 should be blocked", ContentFilter.isBlockedKeywordId(596))
        assertFalse("12345 should not be blocked", ContentFilter.isBlockedKeywordId(12345))

        // Check movie with blocked keyword is blocked
        val movieWithBlockedKeyword = TmdbMovieDetail(
            id = 1001,
            title = "A Regular Romance",
            keywordsContainer = TmdbMovieKeywordsContainer(
                keywords = listOf(
                    TmdbKeyword(id = 33494, name = "couple"),
                    TmdbKeyword(id = 888, name = "normal")
                )
            )
        )
        assertTrue("Movie containing blocked keyword 33494 must be blocked", ContentFilter.isBlockedMovie(movieWithBlockedKeyword))

        // Check TV show with blocked keyword is blocked
        val tvWithBlockedKeyword = TmdbTvDetail(
            id = 2002,
            name = "A Drama Series",
            keywordsContainer = TmdbTvKeywordsContainer(
                results = listOf(
                    TmdbKeyword(id = 4217, name = "keyword4217")
                )
            )
        )
        assertTrue("TV show containing blocked keyword 4217 must be blocked", ContentFilter.isBlockedTv(tvWithBlockedKeyword))

        // Check search query blocking
        assertTrue(ContentFilter.isBlockedSearchQuery("hentai"))
        assertTrue(ContentFilter.isBlockedSearchQuery("سكس"))
        assertFalse(ContentFilter.isBlockedSearchQuery("Spider-Man"))
    }
}

