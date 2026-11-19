package com.halimjr11.beacon.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.halimjr11.beacon.domain.model.Article
import com.halimjr11.beacon.domain.usecase.GetArticlesUseCase
import com.halimjr11.beacon.ui.theme.BeaconTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** AC 9.1 / 9.4: highlights always, "Latest" only when more than 5 headlines exist. */
@RunWith(Parameterized::class)
class HomeScreenTest(
    @Suppress("unused")
    private val caseName: String,
    private val count: Int,
    private val latestShown: Boolean,
) {
    @get:Rule
    val rule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val clicked = mutableListOf<Article>()

    private fun show(count: Int) {
        val repo = FakeNewsRepository(articles = (1..count).map { article(it) })
        val viewModel = HomeViewModel(GetArticlesUseCase(repo))
        rule.mainClock.autoAdvance = false // the carousel auto-scrolls; the test drives the clock
        rule.setContent {
            BeaconTheme {
                HomeScreen(onArticleClick = { clicked += it }, viewModel = viewModel)
            }
        }
        rule.mainClock.advanceTimeBy(500)
    }

    @Test
    fun highlightsAlwaysLatestOnlyWithMoreThanFive() {
        show(count)
        rule.onNodeWithText(context.getString(R.string.home_highlights)).assertIsDisplayed()
        rule.onNodeWithText("Headline 1").assertIsDisplayed()

        if (latestShown) rule.onNodeWithText(context.getString(R.string.home_latest)).assertExists()
        else rule.onNodeWithText(context.getString(R.string.home_latest)).assertDoesNotExist()
    }

    @Test
    fun tappingTheHighlightOpensThatArticle() {
        show(count)
        rule.onNodeWithText("Headline 1").performClick()
        assertEquals(listOf("https://a/1"), clicked.map { it.url })
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases() = listOf(
            arrayOf<Any>("three headlines", 3, false),
            arrayOf<Any>("exactly five", 5, false),
            arrayOf<Any>("six headlines", 6, true),
            arrayOf<Any>("twenty headlines", 20, true),
        )
    }
}
