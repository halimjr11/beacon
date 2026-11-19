package com.halimjr11.beacon.data

import com.halimjr11.beacon.data.cache.FeedParams
import com.halimjr11.beacon.data.cache.feedId
import com.halimjr11.beacon.data.cache.toParams
import com.halimjr11.beacon.domain.model.NewsCategory
import com.halimjr11.beacon.domain.model.NewsFeed
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class FeedParamsTest {

    @ParameterizedTest
    @MethodSource("feeds")
    fun `feed maps to an id and the right query parameters`(feed: NewsFeed, id: String, params: FeedParams) {
        assertEquals(id, feed.feedId())
        assertEquals(params, feed.toParams())
    }

    @ParameterizedTest
    @MethodSource("allFeeds")
    fun `sources is never mixed with country or category (NewsAPI rejects it)`(feed: NewsFeed) {
        val p = feed.toParams()
        assertTrue(p.sources == null || (p.category == null && p.country == null))
    }

    companion object {
        @JvmStatic
        fun feeds(): Stream<Arguments> = Stream.of(
            argumentSet("home headlines", NewsFeed.Headlines, "top", FeedParams(country = "us")),
            argumentSet("genre", NewsFeed.Category(NewsCategory.Technology), "category:technology", FeedParams(category = "technology")),
            argumentSet("source", NewsFeed.Source("bbc-news"), "source:bbc-news", FeedParams(sources = "bbc-news")),
        )

        @JvmStatic
        fun allFeeds(): Stream<Arguments> = Stream.concat(
            Stream.of(argumentSet("headlines", NewsFeed.Headlines), argumentSet("source", NewsFeed.Source("x"))),
            NewsCategory.entries.stream().map { argumentSet(it.apiValue, NewsFeed.Category(it)) },
        )
    }
}
