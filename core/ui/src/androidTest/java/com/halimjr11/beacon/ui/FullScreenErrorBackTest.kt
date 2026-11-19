package com.halimjr11.beacon.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.halimjr11.beacon.common.NewsError
import com.halimjr11.beacon.ui.component.FullScreenError
import com.halimjr11.beacon.ui.theme.BeaconTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FullScreenErrorBackTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun missingSourceOffersBackInsteadOfRetry() {
        var backs = 0
        rule.setContent { BeaconTheme { FullScreenError(NewsError.SourceNotFound, onRetry = {}, onBack = { backs++ }) } }

        rule.onNodeWithText(text(R.string.action_retry)).assertDoesNotExist()
        rule.onNodeWithText(text(R.string.action_back)).performClick()
        assertEquals(1, backs)
    }
}
