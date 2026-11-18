package com.halimjr11.beacon.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.halimjr11.beacon.common.NewsError
import com.halimjr11.beacon.ui.R
import com.halimjr11.beacon.ui.canRetry
import com.halimjr11.beacon.ui.messageRes

/** Shown only when there is nothing to display (itemCount == 0). */
@Composable
fun FullScreenError(
    error: NewsError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    Column(
        modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(error.messageRes()),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        if (error.canRetry()) {
            Button(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
        } else if (error == NewsError.SourceNotFound && onBack != null) {
            OutlinedButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
        }
    }
}
