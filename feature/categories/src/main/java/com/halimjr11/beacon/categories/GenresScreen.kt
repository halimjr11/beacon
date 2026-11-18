package com.halimjr11.beacon.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.halimjr11.beacon.domain.model.NewsCategory

/** Genres tab: the seven fixed NewsAPI categories. Static, so it is always available offline (AC 1.2). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenresScreen(onCategoryClick: (NewsCategory) -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.genres_title)) }) },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(NewsCategory.entries, key = { it.apiValue }) { category ->
                GenreCard(category, onClick = { onCategoryClick(category) })
            }
        }
    }
}

@Composable
private fun GenreCard(category: NewsCategory, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1.4f).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            Icon(category.icon(), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(category.displayName, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun NewsCategory.icon(): ImageVector = when (this) {
    NewsCategory.Business -> Icons.Outlined.Business
    NewsCategory.Entertainment -> Icons.Outlined.Movie
    NewsCategory.General -> Icons.Outlined.Public
    NewsCategory.Health -> Icons.Outlined.HealthAndSafety
    NewsCategory.Science -> Icons.Outlined.Science
    NewsCategory.Sports -> Icons.Outlined.SportsSoccer
    NewsCategory.Technology -> Icons.Outlined.Memory
}
