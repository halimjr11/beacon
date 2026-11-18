package com.halimjr11.beacon.data

import com.halimjr11.beacon.data.cache.articlesKey
import com.halimjr11.beacon.data.cache.likePattern
import com.halimjr11.beacon.data.cache.queryKey
import com.halimjr11.beacon.data.cache.sourcesKey
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CacheKeysTest {
    @Test
    fun keys() {
        assertEquals("sources:general:en", sourcesKey("general"))
        assertEquals("articles:bbc-news:election", articlesKey("bbc-news", "election"))
    }

    @Test
    fun `query key is trimmed and lower case`() {
        assertEquals("election", queryKey("  ElEction "))
        assertEquals("", queryKey("   "))
    }

    @Test
    fun `like pattern escapes wildcards`() {
        assertEquals("%bbc%", likePattern(" bbc "))
        assertEquals("%%", likePattern(""))
        assertEquals("%100\\%\\_a\\\\b%", likePattern("100%_a\\b"))
    }
}
