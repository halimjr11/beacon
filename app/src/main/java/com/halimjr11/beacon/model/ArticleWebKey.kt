package com.halimjr11.beacon.model

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Article detail in the WebView. */
@Serializable
data class ArticleWebKey(val url: String, val title: String) : NavKey
