package com.halimjr11.beacon.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.halimjr11.beacon.common.toNewsError
import kotlinx.coroutines.delay

private const val EMPTY_CONFIRM_MS = 400L

/**
 * Offline-first screen states (PRD FR-08): nothing saved -> full-screen loading/error/empty;
 * saved data present -> always show it, with a banner when the last refresh failed.
 */
@Composable
fun <T : Any> PagingStateLayout(
    items: LazyPagingItems<T>,
    emptyText: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val refresh = items.loadState.refresh
    val emptyAndIdle = items.itemCount == 0 && refresh is LoadState.NotLoading
    // Right after a refresh the mediator reports "done" a moment before Room re-emits the rows, so
    // an empty list is only believed after it stays empty briefly (seen on the real API as a flash
    // of "No headlines found").
    var emptyConfirmed by remember { mutableStateOf(false) }
    LaunchedEffect(emptyAndIdle) {
        if (emptyAndIdle) { delay(EMPTY_CONFIRM_MS); emptyConfirmed = true } else emptyConfirmed = false
    }
    when {
        emptyAndIdle && !emptyConfirmed -> FullScreenLoading(modifier)
        items.itemCount == 0 && refresh is LoadState.Loading -> FullScreenLoading(modifier)
        items.itemCount == 0 && refresh is LoadState.Error ->
            FullScreenError(refresh.error.toNewsError(), onRetry = { items.retry() }, modifier = modifier, onBack = onBack)
        items.itemCount == 0 -> EmptyState(emptyText, modifier)
        else -> Column(modifier.fillMaxSize()) {
            if (refresh is LoadState.Error) CacheBanner(refresh.error.toNewsError())
            Box(Modifier.weight(1f)) { content() }
        }
    }
}
