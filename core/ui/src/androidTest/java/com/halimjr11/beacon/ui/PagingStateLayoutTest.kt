package com.halimjr11.beacon.ui

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.halimjr11.beacon.common.NewsError
import com.halimjr11.beacon.common.NewsException
import com.halimjr11.beacon.ui.component.PagingStateLayout
import com.halimjr11.beacon.ui.theme.BeaconTheme
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * The offline-first screen states (PRD FR-08): nothing saved -> loading / error / empty;
 * saved data -> always shown, with a banner when the refresh failed.
 */
@RunWith(Parameterized::class)
class PagingStateLayoutTest(
    @Suppress("unused") private val caseName: String,
    private val items: List<String>,
    private val refresh: LoadState,
    private val shown: List<Any>,
    private val hidden: List<Any>,
) {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun rendersTheExpectedState() {
        val states = LoadStates(refresh, LoadState.NotLoading(true), LoadState.NotLoading(true))
        rule.setContent {
            BeaconTheme {
                val paging = flowOf(PagingData.from(items, states)).collectAsLazyPagingItems()
                PagingStateLayout(paging, emptyText = "EMPTY") { Text("CONTENT") }
            }
        }
        // An empty list is only believed after it stays empty briefly (avoids a flash of "empty").
        rule.mainClock.advanceTimeBy(1_000)
        rule.waitForIdle()

        shown.forEach { rule.onNodeWithText(text(it)).assertIsDisplayed() }
        hidden.forEach { rule.onNodeWithText(text(it)).assertDoesNotExist() }
    }

    companion object {
        private val idle = LoadState.NotLoading(endOfPaginationReached = true)
        private fun error(e: NewsError) = LoadState.Error(NewsException(e))

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases() = listOf(
            arrayOf<Any>("empty and loading", emptyList<String>(), LoadState.Loading,
                emptyList<Any>(), listOf("EMPTY", "CONTENT", R.string.action_retry)),
            arrayOf<Any>("empty and idle is the empty text", emptyList<String>(), idle,
                listOf("EMPTY"), listOf("CONTENT")),
            arrayOf<Any>("empty and offline is a full error with retry", emptyList<String>(), error(NewsError.Offline),
                listOf(R.string.error_offline, R.string.action_retry), listOf("EMPTY", "CONTENT")),
            arrayOf<Any>("empty and bad key has no retry", emptyList<String>(), error(NewsError.InvalidApiKey),
                listOf(R.string.error_api_key), listOf(R.string.action_retry, "CONTENT")),
            arrayOf<Any>("data and idle shows only content", listOf("a", "b"), idle,
                listOf("CONTENT"), listOf(R.string.banner_offline, R.string.banner_rate_limited, "EMPTY")),
            arrayOf<Any>("data and offline keeps content with banner", listOf("a"), error(NewsError.Offline),
                listOf("CONTENT", R.string.banner_offline), listOf(R.string.error_offline, "EMPTY")),
            arrayOf<Any>("data and rate limit keeps content with quota banner", listOf("a"), error(NewsError.RateLimited),
                listOf("CONTENT", R.string.banner_rate_limited), listOf(R.string.action_retry)),
        )
    }
}
