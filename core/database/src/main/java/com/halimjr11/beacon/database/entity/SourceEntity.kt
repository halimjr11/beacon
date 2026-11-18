package com.halimjr11.beacon.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "sources")
data class SourceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String,
    val language: String,
    val country: String,
)
