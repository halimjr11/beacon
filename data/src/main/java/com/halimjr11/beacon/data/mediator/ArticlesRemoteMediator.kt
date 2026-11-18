package com.halimjr11.beacon.data.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.halimjr11.beacon.common.NetworkResult
import com.halimjr11.beacon.common.NewsError
import com.halimjr11.beacon.common.NewsException
import com.halimjr11.beacon.data.cache.CacheConfig
import com.halimjr11.beacon.data.cache.Clock
import com.halimjr11.beacon.data.cache.TransactionRunner
import com.halimjr11.beacon.data.cache.articlesKey
import com.halimjr11.beacon.data.mapper.toEntity
import com.halimjr11.beacon.database.dao.ArticleDao
import com.halimjr11.beacon.database.dao.RemoteKeyDao
import com.halimjr11.beacon.database.entity.ArticleEntity
import com.halimjr11.beacon.database.entity.RemoteKeyEntity
import com.halimjr11.beacon.network.NewsApiService
import com.halimjr11.beacon.network.safeApiCall

/**
 * Pages `top-headlines` into Room per `(sourceId, queryKey)`. [queryKey] is already
 * trimmed and lower-cased; "" means the default list (no `q`).
 */
@OptIn(ExperimentalPagingApi::class)
class ArticlesRemoteMediator(
    private val sourceId: String,
    private val queryKey: String,
    private val api: NewsApiService,
    private val articleDao: ArticleDao,
    private val keyDao: RemoteKeyDao,
    private val transaction: TransactionRunner,
    private val clock: Clock,
) : RemoteMediator<Int, ArticleEntity>() {

    private val key = articlesKey(sourceId, queryKey)

    override suspend fun initialize(): InitializeAction {
        val saved = keyDao.get(key)
        val fresh = saved != null && clock.now() - saved.fetchedAt < CacheConfig.ARTICLES_TTL_MS
        return if (fresh) InitializeAction.SKIP_INITIAL_REFRESH else InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, ArticleEntity>): MediatorResult {
        if (loadType == LoadType.PREPEND) return MediatorResult.Success(endOfPaginationReached = true)

        val saved = keyDao.get(key)
        val page = if (loadType == LoadType.REFRESH) {
            1
        } else {
            saved?.nextPage ?: return MediatorResult.Success(endOfPaginationReached = true)
        }
        // TTL stays anchored at the last REFRESH, not at the last scroll.
        val fetchedAt = if (loadType == LoadType.REFRESH) clock.now() else saved?.fetchedAt ?: clock.now()

        val result = safeApiCall {
            api.getTopHeadlines(sourceId, queryKey.ifEmpty { null }, page, CacheConfig.PAGE_SIZE)
        }
        return when (result) {
            is NetworkResult.Error -> when {
                // Plan limit: the list simply ends here (not verified against the real API).
                result.error == NewsError.EndOfResults && loadType == LoadType.APPEND -> {
                    keyDao.upsert(RemoteKeyEntity(key, nextPage = null, fetchedAt = fetchedAt))
                    MediatorResult.Success(endOfPaginationReached = true)
                }
                else -> MediatorResult.Error(NewsException(result.error))
            }
            is NetworkResult.Success -> {
                val dto = result.data
                val raw = dto.articles.orEmpty()
                val items = raw.mapNotNull { it.toEntity(sourceId, queryKey) }
                val total = dto.totalResults ?: Int.MAX_VALUE
                val nextPage = if (raw.isEmpty() || page * CacheConfig.PAGE_SIZE >= total) null else page + 1
                transaction.run {
                    if (loadType == LoadType.REFRESH) articleDao.deleteByKey(sourceId, queryKey)
                    articleDao.insertAll(items)
                    keyDao.upsert(RemoteKeyEntity(key, nextPage, fetchedAt))
                }
                MediatorResult.Success(endOfPaginationReached = nextPage == null)
            }
        }
    }
}
