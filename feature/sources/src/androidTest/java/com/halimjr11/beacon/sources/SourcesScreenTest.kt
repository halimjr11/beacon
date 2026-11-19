package com.halimjr11.beacon.sources

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.swipe
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.halimjr11.beacon.domain.model.NewsCategory
import com.halimjr11.beacon.domain.usecase.GetSourcesUseCase
import com.halimjr11.beacon.ui.theme.BeaconTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SourcesScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val repo = FakeNewsRepository(
        sources = mapOf(
            NewsCategory.General to listOf(source("bbc-news", "BBC News"), source("cnn", "CNN")),
            NewsCategory.Sports to listOf(source("espn", "ESPN", NewsCategory.Sports)),
        ),
    )
    private val clicked = mutableListOf<Pair<String, String>>()

    private fun show() {
        // Built outside the composable: a ViewModel must not be constructed during composition.
        val viewModel = SourcesViewModel(GetSourcesUseCase(repo))
        rule.setContent {
            BeaconTheme {
                SourcesScreen(
                    onSourceClick = { id, name -> clicked += id to name },
                    viewModel = viewModel,
                )
            }
        }
    }

    @Test
    fun startsOnGeneralWithAllCategoryChips() {
        show()
        rule.onNodeWithText("BBC News").assertIsDisplayed()
        rule.onNodeWithText("CNN").assertIsDisplayed()
        rule.onNodeWithText("General").assertIsDisplayed()
        rule.onNodeWithText("Business").assertIsDisplayed()
        rule.onNodeWithText("ESPN").assertDoesNotExist()
    }

    @Test
    fun choosingAChipShowsThatCategory() {
        show()
        // Sports is past the visible chips: swipe the chip row until it is composed.
        val rowY = rule.onNodeWithText("Business").fetchSemanticsNode().boundsInRoot.center.y
        repeat(2) {
            rule.onRoot().performTouchInput { swipe(Offset(width * 0.9f, rowY), Offset(width * 0.05f, rowY), 300) }
            rule.waitForIdle()
        }
        rule.waitUntil(3_000) { rule.onAllNodes(hasText("Sports")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Sports").performClick()
        rule.waitUntil(3_000) { repo.sourceRequests.contains(NewsCategory.Sports to "") }
        rule.onNodeWithText("ESPN").assertIsDisplayed()
        rule.onNodeWithText("BBC News").assertDoesNotExist()
    }

    @Test
    fun typingFiltersTheListAndSendsTheTrimmedQuery() {
        show()
        rule.onNodeWithText(context.getString(R.string.sources_search_hint)).performTextInput(" bbc ")
        rule.waitUntil(3_000) { repo.sourceRequests.contains(NewsCategory.General to "bbc") }

        rule.onNodeWithText("BBC News").assertIsDisplayed()
        rule.onNodeWithText("CNN").assertDoesNotExist()
    }

    @Test
    fun noMatchShowsTheQueryInTheMessage() {
        show()
        rule.onNodeWithText(context.getString(R.string.sources_search_hint)).performTextInput("zzz")
        rule.waitUntil(3_000) { repo.sourceRequests.contains(NewsCategory.General to "zzz") }
        rule.waitUntil(3_000) {
            rule.onAllNodesWithTextCount(context.getString(R.string.sources_no_match, "zzz")) == 1
        }
    }

    @Test
    fun tappingASourceReportsItsIdAndName() {
        show()
        rule.onNodeWithText("CNN").performClick()
        assertEquals(listOf("cnn" to "CNN"), clicked)
    }

    @Test
    fun emptyCategoryShowsTheEmptyMessage() {
        show()
        rule.onNodeWithText("Health").performClick()
        rule.waitUntil(3_000) {
            rule.onAllNodesWithTextCount(context.getString(R.string.sources_empty)) == 1
        }
    }
}

private fun androidx.compose.ui.test.junit4.ComposeTestRule.onAllNodesWithTextCount(text: String): Int =
    onAllNodes(androidx.compose.ui.test.hasText(text)).fetchSemanticsNodes().size
