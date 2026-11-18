package com.halimjr11.beacon.data

import com.halimjr11.beacon.data.cache.CacheConfig
import com.halimjr11.beacon.data.cache.CacheRetention
import com.halimjr11.beacon.data.mapper.toEntity
import com.halimjr11.beacon.database.entity.RemoteKeyEntity
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CacheRetentionTest {
    private val articleDao = FakeArticleDao()
    private val keyDao = FakeRemoteKeyDao()
    private val clock = FakeClock(time = 100 * CacheConfig.RETENTION_MS)
    private val retention = CacheRetention(articleDao, keyDao, FakeTransaction(), clock)

    init {
        articleDao.keysProvider = { keyDao.keys.keys }
    }

    @Test
    fun `purges keys older than 7 days and the articles they owned`() = runTest {
        keyDao.upsert(RemoteKeyEntity("articles:old:", null, clock.time - CacheConfig.RETENTION_MS - 1))
        keyDao.upsert(RemoteKeyEntity("articles:new:", null, clock.time - 1))
        articleDao.insertAll(listOf(articleDto(1).toEntity("old", "")!!, articleDto(2).toEntity("new", "")!!))

        retention.purge()

        assertEquals(setOf("articles:new:"), keyDao.keys.keys)
        assertEquals(listOf("new"), articleDao.rows.map { it.sourceId })
    }

    @Test
    fun `trims to the row cap keeping the newest`() = runTest {
        keyDao.upsert(RemoteKeyEntity("articles:s:", null, clock.time))
        articleDao.insertAll((1..CacheConfig.MAX_ARTICLES + 5).map { articleDto(it).toEntity("s", "")!! })

        retention.purge()

        assertEquals(CacheConfig.MAX_ARTICLES, articleDao.rows.size)
        assertEquals("https://a/${CacheConfig.MAX_ARTICLES + 5}", articleDao.rows.last().url)
        assertEquals("https://a/6", articleDao.rows.first().url)
    }
}
