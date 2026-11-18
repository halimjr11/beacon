package com.halimjr11.beacon.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * [key] is `sources:<category>:<language>` or `articles:<sourceId>:<queryKey>`.
 * [nextPage] is null at the end of the list; [fetchedAt] is epoch millis (TTL).
 */
@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(
    @PrimaryKey val key: String,
    val nextPage: Int?,
    val fetchedAt: Long,
)
