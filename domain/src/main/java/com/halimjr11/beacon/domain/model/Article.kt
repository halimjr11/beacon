package com.halimjr11.beacon.domain.model

/** [id] is the article url, unique within one list. */
data class Article(
    val id: String = "",
    val sourceName: String = "",
    val author: String = "",
    val title: String = "",
    val description: String = "",
    val url: String = "",
    val imageUrl: String = "",
    val publishedAt: String = "",
    val content: String = "",
)
