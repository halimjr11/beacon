package com.halimjr11.beacon.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.halimjr11.beacon.ui.component.ArticleCard
import com.halimjr11.beacon.ui.theme.BeaconTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ArticleCardTest(
    @Suppress("unused")
    private val caseName: String,
    private val source: String,
    private val date: String,
    private val description: String,
    private val meta: String?,
) {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun showsTitleMetaAndOptionalDescription() {
        var clicks = 0
        rule.setContent {
            BeaconTheme {
                ArticleCard("The title", source, date, description, imageUrl = "", onClick = { clicks++ })
            }
        }

        rule.onNodeWithText("The title").assertIsDisplayed()
        if (meta != null) rule.onNodeWithText(meta).assertIsDisplayed()
        if (description.isNotBlank()) rule.onNodeWithText(description).assertIsDisplayed()

        rule.onNodeWithText("The title").performClick()
        assertEquals(1, clicks)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases() = listOf(
            arrayOf<Any?>("full", "BBC News", "07 Jul 2026", "A short summary", "BBC News \u2022 07 Jul 2026"),
            arrayOf<Any?>("no description", "BBC News", "07 Jul 2026", "", "BBC News \u2022 07 Jul 2026"),
            arrayOf<Any?>("no date", "BBC News", "", "Summary", "BBC News"),
            arrayOf<Any?>("no source", "", "07 Jul 2026", "Summary", "07 Jul 2026"),
        )
    }
}
