package com.halimjr11.beacon.network.dto

import com.google.gson.annotations.SerializedName

data class ErrorBodyDto(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("code")
    val code: String? = null,
    @SerializedName("message")
    val message: String? = null,
)
