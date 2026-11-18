package com.halimjr11.beacon.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.halimjr11.beacon.common.toNewsError

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
    when {
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
