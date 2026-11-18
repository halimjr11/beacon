package com.halimjr11.beacon.data.mapper

import com.halimjr11.beacon.database.entity.SourceEntity
import com.halimjr11.beacon.domain.model.NewsSource
import com.halimjr11.beacon.network.dto.SourceDto

/** Drops items without id or name. [category] is the requested one, so the cache query matches. */
fun SourceDto.toEntity(category: String): SourceEntity? {
    val id = id?.takeIf { it.isNotBlank() } ?: return null
    val name = name?.takeIf { it.isNotBlank() } ?: return null
    return SourceEntity(
        id = id,
        name = name,
        description = description.orEmpty(),
        category = category,
        language = language.orEmpty(),
        country = country.orEmpty(),
    )
}

fun SourceEntity.toDomain() = NewsSource(
    id = id,
    name = name,
    description = description,
    category = category,
    language = language.uppercase(),
    country = country.uppercase(),
)
