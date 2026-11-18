package com.halimjr11.beacon.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.halimjr11.beacon.data.cache.CacheConfig
import com.halimjr11.beacon.data.cache.Clock
import com.halimjr11.beacon.data.cache.TransactionRunner
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

    private val config = PagingConfig(pageSize = CacheConfig.PAGE_SIZE, enablePlaceholders = false)

    override fun sources(category: NewsCategory, query: String): Flow<PagingData<NewsSource>> = Pager(
        config = config,
        remoteMediator = SourcesRemoteMediator(category.apiValue, api, sourceDao, keyDao, transaction, clock),
        pagingSourceFactory = { sourceDao.pagingSource(category.apiValue, likePattern(query)) },
    ).flow.map { page -> page.map { it.toDomain() } }

    override fun articles(sourceId: String, query: String): Flow<PagingData<Article>> {
        val key = queryKey(query)
        return Pager(
            config = config,
            remoteMediator = ArticlesRemoteMediator(sourceId, key, api, articleDao, keyDao, transaction, clock),
            pagingSourceFactory = { articleDao.pagingSource(sourceId, key) },
        ).flow.map { page -> page.map { it.toDomain() } }
    }
}
