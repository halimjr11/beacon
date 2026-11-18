package com.halimjr11.beacon.network.dto

import com.google.gson.annotations.SerializedName

data class ArticleSourceDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
)
