package com.halimjr11.beacon.data

import androidx.paging.PagingSource
import com.halimjr11.beacon.data.cache.Clock
import com.halimjr11.beacon.data.cache.TransactionRunner
import com.halimjr11.beacon.database.dao.ArticleDao
import com.halimjr11.beacon.database.dao.RemoteKeyDao
import com.halimjr11.beacon.database.dao.SourceDao
import com.halimjr11.beacon.database.entity.ArticleEntity
import com.halimjr11.beacon.database.entity.RemoteKeyEntity
import com.halimjr11.beacon.database.entity.SourceEntity

class FakeClock(var time: Long = 1_000_000_000L) : Clock {
    override fun now() = time
}

class FakeTransaction : TransactionRunner {
    var count = 0
    override suspend fun <R> run(block: suspend () -> R): R { count++; return block() }
}

class FakeRemoteKeyDao : RemoteKeyDao {
    val keys = mutableMapOf<String, RemoteKeyEntity>()
    override suspend fun get(key: String) = keys[key]
    override suspend fun upsert(item: RemoteKeyEntity) { keys[item.key] = item }
    override suspend fun delete(key: String) { keys.remove(key) }
    override suspend fun deleteOlderThan(olderThan: Long) { keys.values.removeAll { it.fetchedAt < olderThan } }
}

class FakeSourceDao : SourceDao {
    val rows = mutableListOf<SourceEntity>()
    override fun pagingSource(category: String, pattern: String): PagingSource<Int, SourceEntity> =
        error("not used in unit tests")
    override suspend fun insertAll(items: List<SourceEntity>) {
        items.forEach { item -> rows.removeAll { it.id == item.id }; rows += item }
    }
    override suspend fun deleteByCategory(category: String) { rows.removeAll { it.category == category } }
}

class FakeArticleDao : ArticleDao {
    val rows = mutableListOf<ArticleEntity>()
    var keysProvider: () -> Set<String> = { emptySet() }
    private var nextId = 1L
    override fun pagingSource(feedId: String, queryKey: String): PagingSource<Int, ArticleEntity> =
        error("not used in unit tests")
    override suspend fun insertAll(items: List<ArticleEntity>) {
        items.forEach { item ->
            val dup = rows.any { it.feedId == item.feedId && it.queryKey == item.queryKey && it.url == item.url }
            if (!dup) rows += item.copy(rowId = nextId++)
        }
    }
    override suspend fun deleteByKey(feedId: String, queryKey: String) {
        rows.removeAll { it.feedId == feedId && it.queryKey == queryKey }
    }
    override suspend fun deleteOrphans() {
        val keys = keysProvider()
        rows.removeAll { "articles:${it.feedId}:${it.queryKey}" !in keys }
    }
    override suspend fun trimTo(max: Int) {
        val keep = rows.sortedByDescending { it.rowId }.take(max).toSet()
        rows.retainAll(keep)
    }
}
