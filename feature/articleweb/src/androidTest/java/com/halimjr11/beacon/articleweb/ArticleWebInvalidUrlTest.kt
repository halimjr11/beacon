package com.halimjr11.beacon.articleweb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.halimjr11.beacon.ui.theme.BeaconTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** AC 4.3 and 4.5: an unusable url shows an error and never creates a WebView; Back still works. */
@RunWith(Parameterized::class)
class ArticleWebInvalidUrlTest(
    @Suppress("unused")
    private val caseName: String,
    private val url: String,
) {
    @get:Rule
    val rule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun showsErrorWithoutRetryOrBrowserAction() {
        var backs = 0
        rule.setContent { BeaconTheme { ArticleWebScreen(url = url, title = "The title", onBack = { backs++ }) } }

        rule.onNodeWithText(context.getString(R.string.web_invalid_url)).assertIsDisplayed()
        rule.onNodeWithText(context.getString(R.string.web_retry)).assertDoesNotExist()
        rule.onNodeWithText(context.getString(R.string.web_open_in_browser)).assertDoesNotExist()
        rule.onNodeWithContentDescription(context.getString(R.string.web_open_in_browser)).assertDoesNotExist()

        rule.onNodeWithContentDescription(context.getString(R.string.web_back)).performClick()
        assertEquals(1, backs)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases() = listOf(
            arrayOf<Any>("empty", ""),
            arrayOf<Any>("blank", "   "),
            arrayOf<Any>("file scheme", "file:///sdcard/a.html"),
            arrayOf<Any>("intent scheme", "intent://scan/#Intent;scheme=zxing;end"),
            arrayOf<Any>("javascript scheme", "javascript:alert(1)"),
            arrayOf<Any>("no host", "https://"),
            arrayOf<Any>("not a url", "not a url"),
        )
    }
}
