package com.halimjr11.beacon.data.cache

import com.halimjr11.beacon.database.dao.ArticleDao
import com.halimjr11.beacon.database.dao.RemoteKeyDao

/** Drops keys older than 7 days, the articles they owned, and rows beyond the cap. */
class CacheRetention(
    private val articleDao: ArticleDao,
    private val keyDao: RemoteKeyDao,
    private val transaction: TransactionRunner,
    private val clock: Clock,
) {
    suspend fun purge() = transaction.run {
        keyDao.deleteOlderThan(clock.now() - CacheConfig.RETENTION_MS)
        articleDao.deleteOrphans()
        articleDao.trimTo(CacheConfig.MAX_ARTICLES)
    }
}
