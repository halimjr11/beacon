package com.halimjr11.beacon.common

sealed interface NewsError {
    data object Offline : NewsError
    data object InvalidApiKey : NewsError
    data object RateLimited : NewsError
    data object SourceNotFound : NewsError
    data class BadRequest(val message: String) : NewsError
    data object Server : NewsError
    data object Parse : NewsError
    data class Unknown(val message: String) : NewsError

    /** Plan limit reached (`maximumResultsReached`): the end of the list, not a failure. */
    data object EndOfResults : NewsError
}
