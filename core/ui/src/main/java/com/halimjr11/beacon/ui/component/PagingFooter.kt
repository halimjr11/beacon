package com.halimjr11.beacon.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import com.halimjr11.beacon.common.toNewsError
import com.halimjr11.beacon.ui.R
import com.halimjr11.beacon.ui.canRetry

/** Append footer: spinner while loading, "Failed to load more - Retry" on error (AC 3.4, 6.2). */
@Composable
fun PagingFooter(appendState: LoadState, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    when (appendState) {
        is LoadState.Loading -> Box(modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is LoadState.Error -> Row(
            modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.paging_footer_error), style = MaterialTheme.typography.bodyMedium)
            if (appendState.error.toNewsError().canRetry()) {
                TextButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
            }
        }
        is LoadState.NotLoading -> Unit
    }
}
