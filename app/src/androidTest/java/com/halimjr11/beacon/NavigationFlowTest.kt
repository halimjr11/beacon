package com.halimjr11.beacon

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso
import com.halimjr11.beacon.domain.model.NewsCategory
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** The real activity, real navigation graph, with Espresso's system Back: Genres -> Sources -> (Top headlines). */
@RunWith(Parameterized::class)
class GenreNavigationTest(private val category: NewsCategory) {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun genreOpensItsSourcesHidesTheBottomBarAndBackReturns() {
        rule.onNodeWithText("Genres").performClick()
        rule.onNodeWithText(category.displayName).performScrollTo().performClick()

        // Sources of the genre: title, search, the headlines shortcut, and no bottom bar.
        rule.onNodeWithText(category.displayName).assertIsDisplayed()
        rule.onNodeWithText("Search sources").assertIsDisplayed()
        rule.onNodeWithText("Top headlines").assertIsDisplayed()
        rule.onNodeWithText("Sources").assertDoesNotExist()

        Espresso.pressBack()

        rule.onNodeWithText(category.displayName).assertIsDisplayed()
        rule.onNodeWithText("Sources").assertIsDisplayed()
    }

    @Test
    fun topHeadlinesChipOpensTheGenreArticlesAndBackWalksBack() {
        rule.onNodeWithText("Genres").performClick()
        rule.onNodeWithText(category.displayName).performScrollTo().performClick()
        rule.onNodeWithText("Top headlines").performClick()

        rule.onNodeWithText("Search articles").assertIsDisplayed()

        Espresso.pressBack()
        rule.onNodeWithText("Search sources").assertIsDisplayed()

        Espresso.pressBack()
        rule.onNodeWithText("Sources").assertIsDisplayed()
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun categories() = NewsCategory.entries.toList()
    }
}

class TabsTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun startsOnHomeWithThreeTabs() {
        rule.onNodeWithText("Beacon").assertIsDisplayed()
        rule.onNodeWithText("Home").assertIsDisplayed()
        rule.onNodeWithText("Genres").assertIsDisplayed()
        rule.onNodeWithText("Sources").assertIsDisplayed()
    }

    @Test
    fun sourcesTabShowsSearchAndCategoryChips() {
        rule.onNodeWithText("Sources").performClick()
        rule.onAllNodesWithText("Sources").assertCountEquals(2) // top bar + tab label
        rule.onNodeWithText("Search sources").assertIsDisplayed()
        rule.onNodeWithText("General").assertIsDisplayed()
    }

    @Test
    fun backFromAnotherTabReturnsToHome() {
        rule.onNodeWithText("Genres").performClick()
        rule.onAllNodesWithText("Genres").assertCountEquals(2)

        Espresso.pressBack()

        rule.onNodeWithText("Beacon").assertIsDisplayed()
    }
}
