package com.halimjr11.beacon.ui

import androidx.annotation.StringRes
import com.halimjr11.beacon.common.NewsError

/** User-facing message per error type (PRD FR-07). Never includes the API key or request URL. */
@StringRes
fun NewsError.messageRes(): Int = when (this) {
    NewsError.Offline -> R.string.error_offline
    NewsError.InvalidApiKey -> R.string.error_api_key
    NewsError.RateLimited -> R.string.error_rate_limited
    NewsError.SourceNotFound -> R.string.error_source_not_found
    is NewsError.BadRequest -> R.string.error_bad_request
    NewsError.Server -> R.string.error_server
    NewsError.Parse -> R.string.error_parse
    is NewsError.Unknown, NewsError.EndOfResults -> R.string.error_unknown
}

/** Retrying cannot fix a bad key, an exhausted quota, or a source that no longer exists. */
fun NewsError.canRetry(): Boolean = when (this) {
    NewsError.InvalidApiKey, NewsError.RateLimited, NewsError.SourceNotFound -> false
    else -> true
}
