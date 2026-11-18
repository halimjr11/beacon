package com.halimjr11.beacon.network.dto

import com.google.gson.annotations.SerializedName

data class SourceDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("language")
    val language: String? = null,
    @SerializedName("country")
    val country: String? = null,
)
