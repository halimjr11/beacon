package com.halimjr11.beacon.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.halimjr11.beacon.R
import com.halimjr11.beacon.articles.ArticlesScreen
import com.halimjr11.beacon.articleweb.ArticleWebScreen
import com.halimjr11.beacon.categories.GenresScreen
import com.halimjr11.beacon.domain.model.NewsCategory
import com.halimjr11.beacon.domain.model.NewsFeed
import com.halimjr11.beacon.home.HomeScreen
import com.halimjr11.beacon.sources.CategorySourcesScreen
import com.halimjr11.beacon.sources.SourcesScreen

/**
 * Navigation 3 host. Feature screens only receive callbacks; the keys live here.
 * Tabs: Home | Genres | Sources. Detail screens hide the bottom bar.
 */
@Composable
fun BeaconNavHost() {
    val backStack = rememberNavBackStack(HomeKey)
    val current = backStack.lastOrNull()

    fun selectTab(key: NavKey) {
        if (current == key) return
        backStack.clear()
        backStack.add(HomeKey) // Back from any tab returns to Home, then exits.
        if (key != HomeKey) backStack.add(key)
    }

    Scaffold(
        // Screens own their top bars and insets; only the bottom bar's height is applied here.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (current in TopLevelKeys) {
                NavigationBar {
                    TopLevelKeys.forEach { key ->
                        NavigationBarItem(
                            selected = current == key,
                            onClick = { selectTab(key) },
                            icon = { Icon(key.icon(), contentDescription = null) },
                            label = { Text(stringResource(key.label())) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(PaddingValues(bottom = padding.calculateBottomPadding())),
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<HomeKey> {
                    HomeScreen(onArticleClick = { backStack.add(ArticleWebKey(it.url, it.title)) })
                }
                entry<GenresKey> {
                    GenresScreen(onCategoryClick = { backStack.add(CategorySourcesKey(it.apiValue)) })
                }
                entry<SourcesKey> {
                    SourcesScreen(onSourceClick = { id, name -> backStack.add(SourceArticlesKey(id, name)) })
                }
                entry<CategorySourcesKey> { key ->
                    val category = NewsCategory.entries.first { it.apiValue == key.category }
                    CategorySourcesScreen(
                        category = category,
                        onBack = { backStack.removeLastOrNull() },
                        onSourceClick = { id, name -> backStack.add(SourceArticlesKey(id, name)) },
                        onTopHeadlinesClick = { backStack.add(CategoryArticlesKey(category.apiValue)) },
                    )
                }
                entry<CategoryArticlesKey> { key ->
                    val category = NewsCategory.entries.first { it.apiValue == key.category }
                    ArticlesScreen(
                        feed = NewsFeed.Category(category),
                        title = category.displayName,
                        onBack = { backStack.removeLastOrNull() },
                        onArticleClick = { backStack.add(ArticleWebKey(it.url, it.title)) },
                    )
                }
                entry<SourceArticlesKey> { key ->
                    ArticlesScreen(
                        feed = NewsFeed.Source(key.sourceId),
                        title = key.sourceName,
                        onBack = { backStack.removeLastOrNull() },
                        onArticleClick = { backStack.add(ArticleWebKey(it.url, it.title)) },
                    )
                }
                entry<ArticleWebKey> { key ->
                    ArticleWebScreen(url = key.url, title = key.title, onBack = { backStack.removeLastOrNull() })
                }
            },
        )
    }
}

private fun NavKey.icon(): ImageVector = when (this) {
    GenresKey -> Icons.Outlined.Category
    SourcesKey -> Icons.Outlined.Newspaper
    else -> Icons.Outlined.Home
}

private fun NavKey.label(): Int = when (this) {
    GenresKey -> R.string.nav_genres
    SourcesKey -> R.string.nav_sources
    else -> R.string.nav_home
}
