package com.halimjr11.beacon.data.cache

fun sourcesKey(category: String, language: String = CacheConfig.LANGUAGE) = "sources:$category:$language"

fun articlesKey(feedId: String, queryKey: String) = "articles:$feedId:$queryKey"

/** Trimmed, lower-cased query; "" is the default list. */
fun queryKey(query: String): String = query.trim().lowercase()

/** SQL LIKE pattern for a contains-search; `%`, `_` and `\` in the query are escaped. */
fun likePattern(query: String): String {
    val escaped = query.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
    return "%$escaped%"
}
