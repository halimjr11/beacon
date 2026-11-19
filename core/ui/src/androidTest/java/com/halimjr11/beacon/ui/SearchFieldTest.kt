package com.halimjr11.beacon.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.halimjr11.beacon.ui.component.SearchField
import com.halimjr11.beacon.ui.theme.BeaconTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SearchFieldTest {
    @get:Rule
    val rule = createComposeRule()

    private var query by mutableStateOf("")

    private fun show() = rule.setContent {
        BeaconTheme { SearchField(query = query, onQueryChange = { query = it }, placeholder = "Search here") }
    }

    @Test
    fun placeholderShowsWhenEmptyAndClearIsHidden() {
        show()
        rule.onNodeWithText("Search here").assertExists()
        rule.onNodeWithContentDescription(text(R.string.search_clear)).assertDoesNotExist()
    }

    @Test
    fun typingReportsTheTextAndShowsClear() {
        show()
        rule.onNodeWithText("Search here").performTextInput("bbc")

        rule.runOnIdle { assertEquals("bbc", query) }
        rule.onNodeWithContentDescription(text(R.string.search_clear)).assertExists()
    }

    @Test
    fun clearEmptiesTheQuery() {
        show()
        rule.onNodeWithText("Search here").performTextInput("bbc")
        rule.onNodeWithContentDescription(text(R.string.search_clear)).performClick()

        rule.runOnIdle { assertEquals("", query) }
        rule.onNodeWithContentDescription(text(R.string.search_clear)).assertDoesNotExist()
    }
}
