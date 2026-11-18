package com.halimjr11.beacon.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.halimjr11.beacon.domain.model.Article
import com.halimjr11.beacon.ui.component.ArticleCard
import com.halimjr11.beacon.ui.component.HeroArticleCard
import com.halimjr11.beacon.ui.component.PagingFooter
import com.halimjr11.beacon.ui.component.PagingStateLayout
import org.koin.androidx.compose.koinViewModel

private const val HERO_COUNT = 5

/** Home: a highlight carousel (top headlines) followed by the latest headlines. Offline-first. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val items = viewModel.headlines.collectAsLazyPagingItems()
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.home_title)) }) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = items.loadState.refresh is LoadState.Loading && items.itemCount > 0,
            onRefresh = { items.refresh() },
            modifier = Modifier.padding(padding),
        ) {
            PagingStateLayout(items, emptyText = stringResource(R.string.home_empty)) {
                HomeContent(items, onArticleClick)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeContent(items: LazyPagingItems<Article>, onArticleClick: (Article) -> Unit) {
    val heroes = items.itemSnapshotList.items.take(HERO_COUNT)
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "highlights") {
            Text(stringResource(R.string.home_highlights), style = MaterialTheme.typography.titleLarge)
        }
        item(key = "carousel") {
            val pager = rememberPagerState { heroes.size }
            Box {
                HorizontalPager(pager, pageSpacing = 12.dp) { page ->
                    val article = heroes[page]
                    HeroArticleCard(
                        title = article.title,
                        sourceName = article.sourceName,
                        imageUrl = article.imageUrl,
                        onClick = { onArticleClick(article) },
                    )
                }
                PagerDots(count = heroes.size, current = pager.currentPage, modifier = Modifier.align(Alignment.TopEnd))
            }
        }
        if (items.itemCount > HERO_COUNT) {
            item(key = "latest") {
                Text(stringResource(R.string.home_latest), style = MaterialTheme.typography.titleLarge)
            }
        }
        // Index offset: the first HERO_COUNT items live in the carousel.
        items(
            count = (items.itemCount - HERO_COUNT).coerceAtLeast(0),
            key = { index -> items.peek(index + HERO_COUNT)?.id ?: index },
        ) { index ->
            items[index + HERO_COUNT]?.let { article ->
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
        item(key = "footer") {
            PagingFooter(items.loadState.append, onRetry = { items.retry() })
        }
    }
}

@Composable
private fun PagerDots(count: Int, current: Int, modifier: Modifier = Modifier) {
    Row(modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(count) { index ->
            Box(
                Modifier
                    .size(if (index == current) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == current) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    ),
            )
        }
    }
}
