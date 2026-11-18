package com.halimjr11.beacon.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.halimjr11.beacon.common.NewsError
import com.halimjr11.beacon.ui.R

/** Banner above a non-empty list when a refresh failed: the saved data stays visible (AC 8.2, 8.5). */
@Composable
fun CacheBanner(error: NewsError, modifier: Modifier = Modifier) {
    val text = if (error == NewsError.RateLimited) R.string.banner_rate_limited else R.string.banner_offline
    Surface(modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.secondaryContainer) {
        Text(
            text = stringResource(text),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
