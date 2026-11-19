package com.halimjr11.beacon.model

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Sources of one genre (`category` is the API value): Genres -> Sources -> Articles. */
@Serializable
data class CategorySourcesKey(val category: String) : NavKey
