package com.halimjr11.beacon.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.halimjr11.beacon.domain.model.Article
import com.halimjr11.beacon.domain.usecase.GetArticlesUseCase
import com.halimjr11.beacon.ui.theme.BeaconTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeLatestAndCarouselTest {
    @get:Rule
    val rule = createComposeRule()

    private val clicked = mutableListOf<Article>()

    private fun show(count: Int) {
        val repo = FakeNewsRepository(articles = (1..count).map { article(it) })
        rule.mainClock.autoAdvance = false
        rule.setContent {
            BeaconTheme {
                HomeScreen(onArticleClick = { clicked += it }, viewModel = HomeViewModel(GetArticlesUseCase(repo)))
            }
        }
        rule.mainClock.advanceTimeBy(500)
    }

    @Test
    fun latestListStartsAfterTheCarouselWithoutRepeatingIt() {
        show(12)
        rule.onNodeWithText("Headline 6").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Headline 6").performClick()
        assertEquals(listOf("https://a/6"), clicked.map { it.url })
    }

    @Test
    fun carouselAdvancesByItselfEveryFourSeconds() {
        show(8)
        rule.onNodeWithText("Headline 1").assertIsDisplayed()

        rule.mainClock.advanceTimeBy(4_500)
        rule.waitForIdle()
        rule.onNodeWithText("Headline 2").assertIsDisplayed()

        rule.mainClock.advanceTimeBy(4_000)
        rule.waitForIdle()
        rule.onNodeWithText("Headline 3").assertIsDisplayed()
    }

    @Test
    fun carouselWrapsAroundToTheFirstItem() {
        show(8)
        repeat(5) { rule.mainClock.advanceTimeBy(4_200); rule.waitForIdle() }
        rule.onNodeWithText("Headline 1").assertIsDisplayed()
    }
}
