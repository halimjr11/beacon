package com.halimjr11.beacon.categories

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.halimjr11.beacon.domain.model.NewsCategory
import com.halimjr11.beacon.ui.theme.BeaconTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** US1: every one of the seven genres is shown and opens its own list. */
@RunWith(Parameterized::class)
class GenresScreenTest(private val category: NewsCategory) {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun genreIsShownAndReportsItsCategory() {
        val clicked = mutableListOf<NewsCategory>()
        rule.setContent { BeaconTheme { GenresScreen(onCategoryClick = { clicked += it }) } }

        rule.onNodeWithText(category.displayName).performScrollTo().assertIsDisplayed()
        rule.onNodeWithText(category.displayName).performClick()

        assertEquals(listOf(category), clicked)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun categories() = NewsCategory.entries.toList()
    }
}
