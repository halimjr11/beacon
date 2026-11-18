package com.halimjr11.beacon.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.RemoteMediator
import com.halimjr11.beacon.common.NewsError
import com.halimjr11.beacon.common.NewsException
import com.halimjr11.beacon.data.cache.CacheConfig
import com.halimjr11.beacon.data.mediator.SourcesRemoteMediator
import com.halimjr11.beacon.database.entity.RemoteKeyEntity
import com.halimjr11.beacon.database.entity.SourceEntity
import com.halimjr11.beacon.network.NewsApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class SourcesRemoteMediatorTest {
    private val api = mockk<NewsApiService>()
    private val sourceDao = FakeSourceDao()
    private val keyDao = FakeRemoteKeyDao()
    private val tx = FakeTransaction()
    private val clock = FakeClock()
    private val key = "sources:general:en"

    private fun mediator(category: String = "general") =
        SourcesRemoteMediator(category, api, sourceDao, keyDao, tx, clock)

    @Test
    fun `no saved key launches refresh`() = runTest {
        assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, mediator().initialize())
    }

    @Test
    fun `fresh cache skips refresh, stale cache refreshes`() = runTest {
        keyDao.upsert(RemoteKeyEntity(key, null, clock.time))
        clock.time += CacheConfig.SOURCES_TTL_MS - 1
        assertEquals(RemoteMediator.InitializeAction.SKIP_INITIAL_REFRESH, mediator().initialize())

        clock.time += 1
        assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, mediator().initialize())
    }

    @Test
    fun `refresh replaces the category in one transaction and ends paging`() = runTest {
        sourceDao.insertAll(listOf(SourceEntity("old", "Old", "", "general", "en", "us")))
        sourceDao.insertAll(listOf(SourceEntity("other", "Other", "", "sports", "en", "us")))
        coEvery { api.getSources("general", "en") } returns sourcesResponse("a", "b")

        val result = mediator().load(LoadType.REFRESH, emptyState())

        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals(setOf("a", "b", "other"), sourceDao.rows.map { it.id }.toSet())
        assertEquals(clock.time, keyDao.keys.getValue(key).fetchedAt)
        assertEquals(1, tx.count)
    }

    @Test
    fun `invalid items are dropped`() = runTest {
        val response = sourcesResponse("a").let {
            it.copy(sources = it.sources!! + com.halimjr11.beacon.network.dto.SourceDto(id = null, name = "x"))
        }
        coEvery { api.getSources(any(), any()) } returns response

        mediator().load(LoadType.REFRESH, emptyState())

        assertEquals(listOf("a"), sourceDao.rows.map { it.id })
    }

    @Test
    fun `failure keeps saved data and key`() = runTest {
        sourceDao.insertAll(listOf(SourceEntity("old", "Old", "", "general", "en", "us")))
        keyDao.upsert(RemoteKeyEntity(key, null, 1L))
        coEvery { api.getSources(any(), any()) } throws IOException("offline")

        val result = mediator().load(LoadType.REFRESH, emptyState())

        val error = assertInstanceOf(RemoteMediator.MediatorResult.Error::class.java, result)
        assertEquals(NewsError.Offline, (error.throwable as NewsException).error)
        assertEquals(listOf("old"), sourceDao.rows.map { it.id })
        assertEquals(1L, keyDao.keys.getValue(key).fetchedAt)
        assertEquals(0, tx.count)
    }

    @Test
    fun `append and prepend do nothing`() = runTest {
        val m = mediator()
        assertTrue((m.load(LoadType.APPEND, emptyState()) as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertTrue((m.load(LoadType.PREPEND, emptyState()) as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        coVerify(exactly = 0) { api.getSources(any(), any()) }
    }
}
