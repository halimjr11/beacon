package com.halimjr11.beacon.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.LoadState
import com.halimjr11.beacon.common.NewsError
import com.halimjr11.beacon.common.NewsException
import com.halimjr11.beacon.ui.component.PagingFooter
import com.halimjr11.beacon.ui.theme.BeaconTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** AC 3.4 and 6.2: footer error with Retry, except where retrying cannot help (rate limit). */
@RunWith(Parameterized::class)
class PagingFooterTest(
    @Suppress("unused")
    private val caseName: String,
    private val state: LoadState,
    private val errorText: Boolean,
    private val retryShown: Boolean,
) {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun footerReflectsTheAppendState() {
        var retries = 0
        rule.setContent { BeaconTheme { PagingFooter(state, onRetry = { retries++ }) } }

        if (errorText) rule.onNodeWithText(text(R.string.paging_footer_error)).assertIsDisplayed()
        else rule.onNodeWithText(text(R.string.paging_footer_error)).assertDoesNotExist()

        if (retryShown) {
            rule.onNodeWithText(text(R.string.action_retry)).performClick()
            assertEquals(1, retries)
        } else {
            rule.onNodeWithText(text(R.string.action_retry)).assertDoesNotExist()
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases() = listOf(
            arrayOf<Any>("loading shows no error", LoadState.Loading, false, false),
            arrayOf<Any>("idle shows nothing", LoadState.NotLoading(endOfPaginationReached = false), false, false),
            arrayOf<Any>("end of list shows nothing", LoadState.NotLoading(endOfPaginationReached = true), false, false),
            arrayOf<Any>("offline error has retry", LoadState.Error(NewsException(NewsError.Offline)), true, true),
            arrayOf<Any>("rate limit has no retry", LoadState.Error(NewsException(NewsError.RateLimited)), true, false),
        )
    }
}
