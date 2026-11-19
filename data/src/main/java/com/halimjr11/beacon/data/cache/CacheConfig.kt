package com.halimjr11.beacon.data.cache

import java.util.concurrent.TimeUnit

object CacheConfig {
    const val PAGE_SIZE = 20
    const val PREFETCH_DISTANCE = 5
    const val LANGUAGE = "en"
    const val HEADLINES_COUNTRY = "us"
    const val MAX_ARTICLES = 1000
    val SOURCES_TTL_MS = TimeUnit.HOURS.toMillis(24)
    val ARTICLES_TTL_MS = TimeUnit.HOURS.toMillis(1)
    val RETENTION_MS = TimeUnit.DAYS.toMillis(7)
}
