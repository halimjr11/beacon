package com.halimjr11.beacon.sources

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.halimjr11.beacon.domain.model.NewsCategory
import com.halimjr11.beacon.ui.component.PagingFooter
import com.halimjr11.beacon.ui.component.PagingStateLayout
import com.halimjr11.beacon.ui.component.SearchField
import org.koin.androidx.compose.koinViewModel

/** Sources tab: category chips + local search over the Room-cached sources, 20 per page. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcesScreen(
    onSourceClick: (id: String, name: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SourcesViewModel = koinViewModel(),
) {
    val category by viewModel.category.collectAsState()
    val query by viewModel.query.collectAsState()
    val items = viewModel.sources.collectAsLazyPagingItems()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.sources_title)) }) },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            SearchField(query, viewModel::onQueryChange, stringResource(R.string.sources_search_hint))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(NewsCategory.entries, key = { it.apiValue }) { entry ->
                    FilterChip(
                        selected = entry == category,
                        onClick = { viewModel.onCategoryChange(entry) },
                        label = { Text(entry.displayName) },
                    )
                }
            }
            val empty = if (query.isBlank()) stringResource(R.string.sources_empty)
            else stringResource(R.string.sources_no_match, query.trim())
            PagingStateLayout(items, emptyText = empty) {
                LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(count = items.itemCount, key = items.itemKey { it.id }) { index ->
                        items[index]?.let { source ->
                            ListItem(
                                headlineContent = { Text(source.name) },
                                supportingContent = {
                                    Text(source.description, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                },
                                overlineContent = {
                                    Text(listOf(source.language, source.country).filter { it.isNotBlank() }.joinToString(" \u2022 "))
                                },
                                modifier = Modifier.clickable { onSourceClick(source.id, source.name) },
                            )
                            HorizontalDivider()
                        }
                    }
                    item { PagingFooter(items.loadState.append, onRetry = { items.retry() }) }
                }
            }
        }
    }
}
