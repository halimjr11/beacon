package com.halimjr11.beacon.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Top-level tabs. */
@Serializable data object HomeKey : NavKey
@Serializable data object GenresKey : NavKey
@Serializable data object SourcesKey : NavKey

/** Articles of one genre (`category` is the API value). */
@Serializable data class CategoryArticlesKey(val category: String) : NavKey

/** Articles of one source. */
@Serializable data class SourceArticlesKey(val sourceId: String, val sourceName: String) : NavKey

@Serializable data class ArticleWebKey(val url: String, val title: String) : NavKey

internal val TopLevelKeys: List<NavKey> = listOf(HomeKey, GenresKey, SourcesKey)
