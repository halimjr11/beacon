package com.halimjr11.beacon.data

import androidx.paging.PagingConfig
import androidx.paging.PagingState
import com.halimjr11.beacon.network.dto.ArticleDto
import com.halimjr11.beacon.network.dto.ArticleSourceDto
import com.halimjr11.beacon.network.dto.ArticlesResponseDto
import com.halimjr11.beacon.network.dto.SourceDto
import com.halimjr11.beacon.network.dto.SourcesResponseDto

fun <V : Any> emptyState() = PagingState<Int, V>(emptyList(), null, PagingConfig(20), 0)

fun articleDto(n: Int, title: String? = "Title $n", url: String? = "https://a/$n") = ArticleDto(
    source = ArticleSourceDto(id = "bbc-news", name = "BBC"),
    title = title,
    url = url,
    publishedAt = "2026-07-24T10:15:30Z",
)

fun articlesResponse(count: Int, total: Int, from: Int = 1) = ArticlesResponseDto(
    status = "ok",
    totalResults = total,
    articles = (from until from + count).map { articleDto(it) },
)

fun sourcesResponse(vararg ids: String) = SourcesResponseDto(
    status = "ok",
    sources = ids.map { SourceDto(id = it, name = "Name $it", category = "general", language = "en", country = "gb") },
)
