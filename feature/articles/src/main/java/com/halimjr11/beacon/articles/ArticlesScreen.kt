package com.halimjr11.beacon.articles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.halimjr11.beacon.domain.model.Article
import com.halimjr11.beacon.domain.model.NewsFeed
import com.halimjr11.beacon.ui.component.ArticleCard
import com.halimjr11.beacon.ui.component.PagingFooter
import com.halimjr11.beacon.ui.component.PagingStateLayout
import com.halimjr11.beacon.ui.component.SearchField
import com.halimjr11.beacon.ui.theme.BeaconIcons
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesScreen(
    feed: NewsFeed,
    title: String,
    onBack: () -> Unit,
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArticlesViewModel = koinViewModel(key = "articles_$feed", parameters = { parametersOf(feed) }),
) {
    val query by viewModel.query.collectAsState()
    val items = viewModel.articles.collectAsLazyPagingItems()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(BeaconIcons.Back, contentDescription = stringResource(R.string.articles_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            SearchField(query, viewModel::onQueryChange, stringResource(R.string.articles_search_hint))
            val empty = if (query.isBlank()) stringResource(R.string.articles_empty)
            else stringResource(R.string.articles_no_match, query.trim())
            PagingStateLayout(items, emptyText = empty, onBack = onBack) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(count = items.itemCount, key = items.itemKey { it.id }) { index ->
                        items[index]?.let { article ->
                            ArticleCard(
                                title = article.title,
                                sourceName = article.sourceName,
                                date = article.publishedAt,
                                description = article.description,
                                imageUrl = article.imageUrl,
                                onClick = { onArticleClick(article) },
                            )
                        }
                    }
                    item { PagingFooter(items.loadState.append, onRetry = { items.retry() }) }
                }
            }
        }
    }
}
