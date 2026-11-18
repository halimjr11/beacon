package com.halimjr11.beacon.network.dto

import com.google.gson.annotations.SerializedName

data class ArticlesResponseDto(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("totalResults")
    val totalResults: Int? = null,
    @SerializedName("articles")
    val articles: List<ArticleDto>? = null,
)
