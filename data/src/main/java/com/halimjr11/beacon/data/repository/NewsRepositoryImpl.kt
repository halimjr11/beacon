package com.halimjr11.beacon.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.halimjr11.beacon.data.cache.CacheConfig
import com.halimjr11.beacon.data.cache.Clock
import com.halimjr11.beacon.data.cache.TransactionRunner
import com.halimjr11.beacon.data.cache.feedId
import com.halimjr11.beacon.data.cache.likePattern
import com.halimjr11.beacon.data.cache.queryKey
import com.halimjr11.beacon.data.mapper.toDomain
import com.halimjr11.beacon.data.mediator.ArticlesRemoteMediator
import com.halimjr11.beacon.data.mediator.SourcesRemoteMediator
import com.halimjr11.beacon.database.dao.ArticleDao
import com.halimjr11.beacon.database.dao.RemoteKeyDao
import com.halimjr11.beacon.database.dao.SourceDao
import com.halimjr11.beacon.domain.model.Article
import com.halimjr11.beacon.domain.model.NewsCategory
import com.halimjr11.beacon.domain.model.NewsFeed
import com.halimjr11.beacon.domain.model.NewsSource
import com.halimjr11.beacon.domain.repository.NewsRepository
import com.halimjr11.beacon.network.NewsApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Room is the source of truth; the mediators only refresh it from the API. */
@OptIn(ExperimentalPagingApi::class)
class NewsRepositoryImpl(
    private val api: NewsApiService,
    private val sourceDao: SourceDao,
    private val articleDao: ArticleDao,
    private val keyDao: RemoteKeyDao,
    private val transaction: TransactionRunner,
    private val clock: Clock,
) : NewsRepository {

    // initialLoadSize defaults to 3 pages, which made the first open fetch 3 API pages (3 requests of a
    // 100/day quota). One page up front, and fetch the next one only when the user nears the end.
    private val config = PagingConfig(
        pageSize = CacheConfig.PAGE_SIZE,
        initialLoadSize = CacheConfig.PAGE_SIZE,
        prefetchDistance = CacheConfig.PREFETCH_DISTANCE,
        enablePlaceholders = false,
    )

    override fun sources(category: NewsCategory, query: String): Flow<PagingData<NewsSource>> = Pager(
        config = config,
        remoteMediator = SourcesRemoteMediator(category.apiValue, api, sourceDao, keyDao, transaction, clock),
        pagingSourceFactory = { sourceDao.pagingSource(category.apiValue, likePattern(query)) },
    ).flow.map { page -> page.map { it.toDomain() } }

    override fun articles(feed: NewsFeed, query: String): Flow<PagingData<Article>> {
        val key = queryKey(query)
        return Pager(
            config = config,
            remoteMediator = ArticlesRemoteMediator(feed, key, api, articleDao, keyDao, transaction, clock),
            pagingSourceFactory = { articleDao.pagingSource(feed.feedId(), key) },
        ).flow.map { page -> page.map { it.toDomain() } }
    }
}
