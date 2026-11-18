package com.halimjr11.beacon.data.cache

import com.halimjr11.beacon.domain.model.NewsFeed

/** Stable id of a feed, used in Room (`articles.feedId`) and in remote keys. */
fun NewsFeed.feedId(): String = when (this) {
    NewsFeed.Headlines -> "top"
    is NewsFeed.Category -> "category:${category.apiValue}"
    is NewsFeed.Source -> "source:$id"
}

/** Query parameters for `top-headlines`. NewsAPI forbids `sources` together with country/category. */
data class FeedParams(val sources: String? = null, val category: String? = null, val country: String? = null)

fun NewsFeed.toParams(): FeedParams = when (this) {
    NewsFeed.Headlines -> FeedParams(country = CacheConfig.HEADLINES_COUNTRY)
    is NewsFeed.Category -> FeedParams(category = category.apiValue)
    is NewsFeed.Source -> FeedParams(sources = id)
}
