package com.halimjr11.beacon.articleweb

import java.net.URI

/** Pure URL rules for the in-app WebView (PRD FR-04). */
object WebUrlPolicy {

    /** Only http and https may be navigated to (blocks intent://, file://, javascript: ...). */
    fun isAllowedScheme(url: String): Boolean = scheme(url) in setOf("http", "https")

    /** Url to load: https as is, http upgraded to https (cleartext stays blocked), anything else null. */
    fun resolve(url: String): String? {
        val trimmed = url.trim()
        return when (scheme(trimmed)) {
            "https" -> trimmed.takeIf { hasHost(it) }
            "http" -> ("https" + trimmed.substring(4)).takeIf { hasHost(it) }
            else -> null
        }
    }

    /** Url for the external browser: the original, if it is a valid http(s) link. */
    fun external(url: String): String? = url.trim().takeIf { isAllowedScheme(it) && hasHost(it) }

    private fun scheme(url: String): String? =
        runCatching { URI(url.trim()).scheme?.lowercase() }.getOrNull()

    private fun hasHost(url: String): Boolean =
        runCatching { !URI(url).host.isNullOrBlank() }.getOrDefault(false)
}
