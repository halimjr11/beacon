package com.halimjr11.beacon.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.halimjr11.beacon.common.NewsError
import com.halimjr11.beacon.ui.component.FullScreenError
import com.halimjr11.beacon.ui.theme.BeaconTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** PRD FR-07: message per error type, and Retry only where retrying can help. */
@RunWith(Parameterized::class)
class FullScreenErrorTest(
    @Suppress("unused") private val caseName: String,
    private val error: NewsError,
    private val message: Int,
    private val retryShown: Boolean,
) {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun showsMessageAndRetryOnlyWhenUseful() {
        var retries = 0
        rule.setContent { BeaconTheme { FullScreenError(error, onRetry = { retries++ }) } }

        rule.onNodeWithText(text(message)).assertIsDisplayed()
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
            arrayOf<Any>("offline", NewsError.Offline, R.string.error_offline, true),
            arrayOf<Any>("api key", NewsError.InvalidApiKey, R.string.error_api_key, false),
            arrayOf<Any>("rate limited", NewsError.RateLimited, R.string.error_rate_limited, false),
            arrayOf<Any>("source gone", NewsError.SourceNotFound, R.string.error_source_not_found, false),
            arrayOf<Any>("bad request", NewsError.BadRequest("x"), R.string.error_bad_request, true),
            arrayOf<Any>("server", NewsError.Server, R.string.error_server, true),
            arrayOf<Any>("parse", NewsError.Parse, R.string.error_parse, true),
            arrayOf<Any>("unknown", NewsError.Unknown("x"), R.string.error_unknown, true),
        )
    }
}
