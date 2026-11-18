package com.halimjr11.beacon.data.mapper

import com.halimjr11.beacon.common.formatPublishedAt
import com.halimjr11.beacon.database.entity.ArticleEntity
import com.halimjr11.beacon.domain.model.Article
import com.halimjr11.beacon.network.dto.ArticleDto

private const val REMOVED_TITLE = "[Removed]"

/** Drops articles without url, or with a blank/"[Removed]" title (AC 3.7). */
fun ArticleDto.toEntity(sourceId: String, queryKey: String): ArticleEntity? {
    val url = url?.takeIf { it.isNotBlank() } ?: return null
    val title = title?.takeIf { it.isNotBlank() && it != REMOVED_TITLE } ?: return null
    return ArticleEntity(
        sourceId = sourceId,
        queryKey = queryKey,
        url = url,
        sourceName = source?.name.orEmpty(),
        author = author.orEmpty(),
        title = title,
        description = description.orEmpty(),
        imageUrl = urlToImage.orEmpty(),
        publishedAt = publishedAt.orEmpty(),
        content = content.orEmpty(),
    )
}

fun ArticleEntity.toDomain() = Article(
    id = url,
    sourceName = sourceName,
    author = author,
    title = title,
    description = description,
    url = url,
    imageUrl = imageUrl,
    publishedAt = formatPublishedAt(publishedAt),
    content = content,
)
