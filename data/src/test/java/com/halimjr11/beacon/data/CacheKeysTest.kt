package com.halimjr11.beacon.data

import com.halimjr11.beacon.data.cache.articlesKey
import com.halimjr11.beacon.data.cache.likePattern
import com.halimjr11.beacon.data.cache.queryKey
import com.halimjr11.beacon.data.cache.sourcesKey
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class CacheKeysTest {
    @Test
    fun keys() {
        assertEquals("sources:general:en", sourcesKey("general"))
        assertEquals("articles:source:bbc-news:election", articlesKey("source:bbc-news", "election"))
    }

    @ParameterizedTest
    @MethodSource("queryKeys")
    fun `query key is trimmed and lower case`(input: String, expected: String) = assertEquals(expected, queryKey(input))

    @ParameterizedTest
    @MethodSource("patterns")
    fun `like pattern escapes wildcards`(input: String, expected: String) = assertEquals(expected, likePattern(input))

    companion object {
        @JvmStatic
        fun queryKeys(): Stream<Arguments> = Stream.of(
            argumentSet("mixed case with spaces", "  ElEction ", "election"),
            argumentSet("blank", "   ", ""),
            argumentSet("empty", "", ""),
            argumentSet("already clean", "bbc", "bbc"),
        )

        @JvmStatic
        fun patterns(): Stream<Arguments> = Stream.of(
            argumentSet("trimmed", " bbc ", "%bbc%"),
            argumentSet("empty matches all", "", "%%"),
            argumentSet("percent", "100%", "%100\\%%"),
            argumentSet("underscore", "a_b", "%a\\_b%"),
            argumentSet("backslash", "a\\b", "%a\\\\b%"),
        )
    }
}
