package com.halimjr11.beacon.network

import com.google.gson.Gson
import com.halimjr11.beacon.common.NewsError
import com.halimjr11.beacon.network.dto.ErrorBodyDto

/**
 * Maps a NewsAPI error to [NewsError]. The `code` in the body wins; the HTTP status is
 * only a fallback (the docs do not map codes to statuses). Messages never carry the
 * request URL or API key (AC 7.3).
 */
object ErrorParser {
    private val gson = Gson()

    fun parse(httpStatus: Int?, body: String?): NewsError {
        val dto = runCatching { gson.fromJson(body, ErrorBodyDto::class.java) }.getOrNull()
        return when (dto?.code) {
            "apiKeyMissing", "apiKeyInvalid", "apiKeyDisabled" -> NewsError.InvalidApiKey
            "apiKeyExhausted", "rateLimited" -> NewsError.RateLimited
            "sourceDoesNotExist" -> NewsError.SourceNotFound
            "sourcesTooMany", "parameterInvalid", "parametersMissing", "parametersIncompatible" ->
                NewsError.BadRequest(dto.code)
            "unexpectedError" -> NewsError.Server
            "maximumResultsReached" -> NewsError.EndOfResults
            null -> fromStatus(httpStatus)
            else -> NewsError.Unknown(dto.code)
        }
    }

    private fun fromStatus(status: Int?): NewsError = when (status) {
        401, 403 -> NewsError.InvalidApiKey
        429 -> NewsError.RateLimited
        400 -> NewsError.BadRequest("HTTP 400")
        in 500..599 -> NewsError.Server
        else -> NewsError.Unknown("HTTP ${status ?: "?"}")
    }
}
