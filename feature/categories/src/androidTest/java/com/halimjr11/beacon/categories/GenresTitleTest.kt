package com.halimjr11.beacon.categories

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.halimjr11.beacon.ui.theme.BeaconTheme
import org.junit.Rule
import org.junit.Test

class GenresTitleTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun titleIsGenres() {
        rule.setContent { BeaconTheme { GenresScreen(onCategoryClick = {}) } }
        rule.onNodeWithText("Genres").assertIsDisplayed()
    }
}
