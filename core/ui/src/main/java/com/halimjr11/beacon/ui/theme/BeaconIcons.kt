package com.halimjr11.beacon.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The app's icon set, by meaning. Screens ask for `BeaconIcons.Back`, never for a raw
 * `Icons.*`, so changing a glyph (or the whole style) is a one-line change here.
 */
object BeaconIcons {
    // Navigation tabs
    val Home: ImageVector
        get() = Icons.Outlined.Home
    val Genres: ImageVector
        get() = Icons.Outlined.Category
    val Sources: ImageVector
        get() = Icons.Outlined.Newspaper

    // Actions
    val Back: ImageVector
        get() = Icons.AutoMirrored.Filled.ArrowBack
    val OpenInBrowser: ImageVector
        get() = Icons.AutoMirrored.Filled.OpenInNew
    val Search: ImageVector
        get() = Icons.Filled.Search
    val Clear: ImageVector
        get() = Icons.Filled.Clear
    val TopHeadlines: ImageVector
        get() = Icons.Outlined.Newspaper

    // Content states
    val BrokenImage: ImageVector
        get() = Icons.Outlined.BrokenImage

    /**
     * Icon of a news genre, by its NewsAPI value (`"technology"`). :core:ui does not know the domain
     * model, so features pass `category.apiValue`; an unknown value falls back to the general icon.
     */
    fun category(apiValue: String): ImageVector = when (apiValue) {
        "business" -> Icons.Outlined.Business
        "entertainment" -> Icons.Outlined.Movie
        "health" -> Icons.Outlined.HealthAndSafety
        "science" -> Icons.Outlined.Science
        "sports" -> Icons.Outlined.SportsSoccer
        "technology" -> Icons.Outlined.Memory
        else -> Icons.Outlined.Public // general
    }
}
