package com.halimjr11.beacon.articles

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.halimjr11.beacon.domain.model.NewsFeed
import com.halimjr11.beacon.domain.usecase.GetArticlesUseCase
import com.halimjr11.beacon.ui.theme.BeaconTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * US5, article search: the API is asked only for 2+ characters (after trim), 500 ms after typing stops;
 * anything shorter keeps the default list.
 */
@RunWith(Parameterized::class)
class ArticlesSearchTest(
    @Suppress("unused") private val caseName: String,
    private val typed: String,
    private val expectedQueries: List<String>,
) {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun requestedQueriesMatchTheRules() {
        val repo = FakeNewsRepository(articles = (1..3).map { article(it) })
        val feed = NewsFeed.Source("bbc-news")
        rule.setContent {
            BeaconTheme {
                ArticlesScreen(feed, "BBC", onBack = {}, onArticleClick = {}, viewModel = ArticlesViewModel(feed, GetArticlesUseCase(repo)))
            }
        }
        rule.waitUntil(3_000) { repo.articleQueries.isNotEmpty() }

        val hint = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.articles_search_hint)
        rule.onNodeWithText(hint).performTextInput(typed)
        Thread.sleep(1_200) // debounce is 500 ms of real time (viewModelScope)
        rule.waitForIdle()

        assertEquals(expectedQueries, repo.articleQueries.toList())
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases() = listOf(
            arrayOf<Any>("one character stays on the default list", "a", listOf("")),
            arrayOf<Any>("two characters search", "ab", listOf("", "ab")),
            arrayOf<Any>("text is trimmed", "  election  ", listOf("", "election")),
            arrayOf<Any>("only spaces stay on the default list", "    ", listOf("")),
            arrayOf<Any>("a full word", "election", listOf("", "election")),
        )
    }
}
