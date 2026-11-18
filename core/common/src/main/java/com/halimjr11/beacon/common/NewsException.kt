package com.halimjr11.beacon.common

/** Carries a [NewsError] through Paging, which only reports `Throwable`. */
class NewsException(val error: NewsError) : Exception()

fun Throwable.toNewsError(): NewsError =
    (this as? NewsException)?.error ?: NewsError.Unknown(message.orEmpty())
