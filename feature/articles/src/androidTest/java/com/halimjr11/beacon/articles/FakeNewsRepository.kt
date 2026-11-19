package com.halimjr11.beacon.articles

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.halimjr11.beacon.domain.model.Article
import com.halimjr11.beacon.domain.model.NewsCategory
import com.halimjr11.beacon.domain.model.NewsFeed
import com.halimjr11.beacon.domain.model.NewsSource
import com.halimjr11.beacon.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

private val idle = LoadStates(
    LoadState.NotLoading(endOfPaginationReached = true),
    LoadState.NotLoading(endOfPaginationReached = true),
    LoadState.NotLoading(endOfPaginationReached = true),
)

/** In-memory repository: whatever the test hands in, filtered like Room would (contains, ignore case). */
class FakeNewsRepository(
    var sources: Map<NewsCategory, List<NewsSource>> = emptyMap(),
    var articles: List<Article> = emptyList(),
) : NewsRepository {
    val articleQueries = mutableListOf<String>()
    val sourceRequests = mutableListOf<Pair<NewsCategory, String>>()

    override fun sources(category: NewsCategory, query: String): Flow<PagingData<NewsSource>> {
        sourceRequests += category to query
        val list = sources[category].orEmpty().filter { query.isBlank() || it.name.contains(query, true) || it.description.contains(query, true) }
        return flowOf(PagingData.from(list, idle))
    }

    override fun articles(feed: NewsFeed, query: String): Flow<PagingData<Article>> {
        articleQueries += query
        return flowOf(PagingData.from(articles, idle))
    }
}

fun article(n: Int) = Article(
    id = "https://a/$n", sourceName = "Source $n", title = "Headline $n", description = "Summary $n",
    url = "https://a/$n", publishedAt = "0$n Jul 2026",
)

fun source(id: String, name: String = id, category: NewsCategory = NewsCategory.General) =
    NewsSource(id = id, name = name, description = "About $name", category = category.apiValue, language = "EN", country = "US")
