package com.halimjr11.beacon.sources

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.halimjr11.beacon.domain.model.NewsCategory
import com.halimjr11.beacon.domain.usecase.GetSourcesUseCase
import com.halimjr11.beacon.ui.theme.BeaconTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** US2: tapping a genre lists the sources of that genre; "Top headlines" is the other way in. */
@RunWith(Parameterized::class)
class CategorySourcesScreenTest(private val category: NewsCategory) {
    @get:Rule
    val rule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val repo = FakeNewsRepository(
        sources = NewsCategory.entries.associateWith { c ->
            listOf(source("${c.apiValue}-a", "${c.displayName} Daily", c), source("${c.apiValue}-b", "${c.displayName} Times", c))
        },
    )
    private val sourceClicks = mutableListOf<Pair<String, String>>()
    private var backs = 0
    private var headlines = 0

    private fun show() {
        val viewModel = SourcesViewModel(GetSourcesUseCase(repo), initialCategory = category)
        rule.setContent {
            BeaconTheme {
                CategorySourcesScreen(
                    category = category,
                    onBack = { backs++ },
                    onSourceClick = { id, name -> sourceClicks += id to name },
                    onTopHeadlinesClick = { headlines++ },
                    viewModel = viewModel,
                )
            }
        }
    }

    @Test
    fun showsOnlyThisGenresSourcesWithoutCategoryChips() {
        show()
        rule.onNodeWithText("${category.displayName} Daily").assertIsDisplayed()
        rule.onNodeWithText("${category.displayName} Times").assertIsDisplayed()
        NewsCategory.entries.filter { it != category }.forEach {
            rule.onNodeWithText("${it.displayName} Daily").assertDoesNotExist()
        }
        // The genre is fixed here: the title carries it and there is no chip row.
        rule.onNodeWithText(context.getString(R.string.sources_search_hint)).assertIsDisplayed()
        assertEquals(listOf(category to ""), repo.sourceRequests.distinct())
    }

    @Test
    fun tappingASourceReportsIt() {
        show()
        rule.onNodeWithText("${category.displayName} Times").performClick()
        assertEquals(listOf("${category.apiValue}-b" to "${category.displayName} Times"), sourceClicks)
    }

    @Test
    fun backAndTopHeadlinesAreReported() {
        show()
        rule.onNodeWithContentDescription(context.getString(R.string.sources_back)).performClick()
        rule.onNodeWithText(context.getString(R.string.sources_top_headlines)).performClick()
        assertEquals(1, backs)
        assertEquals(1, headlines)
    }

    @Test
    fun searchStaysInsideTheGenre() {
        show()
        rule.onNodeWithText(context.getString(R.string.sources_search_hint)).performTextInput("times")
        rule.waitUntil(3_000) { repo.sourceRequests.contains(category to "times") }
        rule.onNodeWithText("${category.displayName} Times").assertIsDisplayed()
        rule.onNodeWithText("${category.displayName} Daily").assertDoesNotExist()
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun categories() = NewsCategory.entries.toList()
    }
}
