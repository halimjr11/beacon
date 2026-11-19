package com.halimjr11.beacon.articles

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.halimjr11.beacon.domain.model.Article
import com.halimjr11.beacon.domain.model.NewsFeed
import com.halimjr11.beacon.domain.usecase.GetArticlesUseCase
import com.halimjr11.beacon.ui.theme.BeaconTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ArticlesScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val repo = FakeNewsRepository(articles = (1..3).map { article(it) })
    private val clicked = mutableListOf<Article>()
    private var backs = 0

    private fun show(feed: NewsFeed = NewsFeed.Source("bbc-news"), title: String = "BBC News") = rule.setContent {
        BeaconTheme {
            ArticlesScreen(
                feed = feed,
                title = title,
                onBack = { backs++ },
                onArticleClick = { clicked += it },
                viewModel = ArticlesViewModel(feed, GetArticlesUseCase(repo)),
            )
        }
    }

    @Test
    fun showsTitleSearchFieldAndCards() {
        show()
        rule.onNodeWithText("BBC News").assertIsDisplayed()
        rule.onNodeWithText(context.getString(R.string.articles_search_hint)).assertIsDisplayed()
        rule.onNodeWithText("Headline 1").assertIsDisplayed()
        rule.onNodeWithText("Headline 2").assertIsDisplayed()
    }

    @Test
    fun backButtonAndCardClicksAreReported() {
        show()
        rule.onNodeWithContentDescription(context.getString(R.string.articles_back)).performClick()
        rule.onNodeWithText("Headline 2").performClick()

        assertEquals(1, backs)
        assertEquals(listOf("https://a/2"), clicked.map { it.url })
    }

    @Test
    fun genreFeedUsesTheGenreNameAsTitle() {
        show(feed = NewsFeed.Category(com.halimjr11.beacon.domain.model.NewsCategory.Technology), title = "Technology")
        rule.onNodeWithText("Technology").assertIsDisplayed()
        rule.waitUntil(3_000) { repo.articleQueries.isNotEmpty() }
    }

    @Test
    fun noArticlesShowsTheEmptyMessage() {
        repo.articles = emptyList()
        show()
        rule.waitUntil(5_000) {
            rule.onAllNodes(hasText(context.getString(R.string.articles_empty))).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
