package com.halimjr11.beacon.model

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Top headlines of one genre (`category` is the API value). */
@Serializable
data class CategoryArticlesKey(val category: String) : NavKey
