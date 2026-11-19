package com.halimjr11.beacon.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.halimjr11.beacon.common.NewsError
import com.halimjr11.beacon.ui.component.CacheBanner
import com.halimjr11.beacon.ui.theme.BeaconTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** AC 8.2 and 8.5: saved data stays visible; the banner says why the refresh failed. */
@RunWith(Parameterized::class)
class CacheBannerTest(
    @Suppress("unused") private val caseName: String,
    private val error: NewsError,
    private val message: Int,
) {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun showsTheMatchingMessage() {
        rule.setContent { BeaconTheme { CacheBanner(error) } }
        rule.onNodeWithText(text(message)).assertIsDisplayed()
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases() = listOf(
            arrayOf<Any>("offline", NewsError.Offline, R.string.banner_offline),
            arrayOf<Any>("server", NewsError.Server, R.string.banner_offline),
            arrayOf<Any>("rate limited", NewsError.RateLimited, R.string.banner_rate_limited),
        )
    }
}
