package com.halimjr11.beacon.network.dto

import com.google.gson.annotations.SerializedName

data class SourcesResponseDto(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("sources")
    val sources: List<SourceDto>? = null,
)
