package com.halimjr11.beacon.database.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/** `rowId` keeps API order and is the stable Compose item key (urls can repeat across pages). */
@Entity(
    tableName = "articles",
    indices = [Index(value = ["sourceId", "queryKey", "url"], unique = true)],
)
data class ArticleEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val sourceId: String,
    val queryKey: String,
    val url: String,
    val sourceName: String,
    val author: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val publishedAt: String,
    val content: String,
)
