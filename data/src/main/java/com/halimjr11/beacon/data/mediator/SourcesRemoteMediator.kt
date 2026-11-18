package com.halimjr11.beacon.data.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.halimjr11.beacon.common.NetworkResult
import com.halimjr11.beacon.common.NewsException
import com.halimjr11.beacon.data.cache.CacheConfig
import com.halimjr11.beacon.data.cache.Clock
import com.halimjr11.beacon.data.cache.TransactionRunner
import com.halimjr11.beacon.data.cache.sourcesKey
import com.halimjr11.beacon.data.mapper.toEntity
import com.halimjr11.beacon.database.dao.RemoteKeyDao
import com.halimjr11.beacon.database.dao.SourceDao
import com.halimjr11.beacon.database.entity.RemoteKeyEntity
import com.halimjr11.beacon.database.entity.SourceEntity
import com.halimjr11.beacon.network.NewsApiService
import com.halimjr11.beacon.network.safeApiCall

/**
 * Fills Room with every source of [category] in one call (the endpoint has no pages).
 * Skips the network while the cache is younger than 24 h; a failure never touches saved data.
 */
@OptIn(ExperimentalPagingApi::class)
class SourcesRemoteMediator(
    private val category: String,
    private val api: NewsApiService,
    private val sourceDao: SourceDao,
    private val keyDao: RemoteKeyDao,
    private val transaction: TransactionRunner,
    private val clock: Clock,
) : RemoteMediator<Int, SourceEntity>() {

    private val key = sourcesKey(category)

    override suspend fun initialize(): InitializeAction {
        val saved = keyDao.get(key)
        val fresh = saved != null && clock.now() - saved.fetchedAt < CacheConfig.SOURCES_TTL_MS
        return if (fresh) InitializeAction.SKIP_INITIAL_REFRESH else InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, SourceEntity>): MediatorResult {
        // Everything arrives in the REFRESH call; Room paginates locally.
        if (loadType != LoadType.REFRESH) return MediatorResult.Success(endOfPaginationReached = true)

        return when (val result = safeApiCall { api.getSources(category, CacheConfig.LANGUAGE) }) {
            is NetworkResult.Error -> MediatorResult.Error(NewsException(result.error))
            is NetworkResult.Success -> {
                val items = result.data.sources.orEmpty().mapNotNull { it.toEntity(category) }
                transaction.run {
                    sourceDao.deleteByCategory(category)
                    sourceDao.insertAll(items)
                    keyDao.upsert(RemoteKeyEntity(key, nextPage = null, fetchedAt = clock.now()))
                }
                MediatorResult.Success(endOfPaginationReached = true)
            }
        }
    }
}
