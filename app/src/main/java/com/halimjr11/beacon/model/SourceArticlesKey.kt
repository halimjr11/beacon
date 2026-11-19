package com.halimjr11.beacon.model

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Articles of one source. */
@Serializable
data class SourceArticlesKey(val sourceId: String, val sourceName: String) : NavKey
