package com.halimjr11.beacon.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.RemoteMediator
import com.halimjr11.beacon.common.NewsError
import com.halimjr11.beacon.common.NewsException
import com.halimjr11.beacon.data.cache.CacheConfig
import com.halimjr11.beacon.data.mediator.ArticlesRemoteMediator
import com.halimjr11.beacon.data.mapper.toEntity
import com.halimjr11.beacon.database.entity.RemoteKeyEntity
import com.halimjr11.beacon.network.dto.ArticleDto
import com.halimjr11.beacon.network.NewsApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody

@OptIn(ExperimentalPagingApi::class)
class ArticlesRemoteMediatorTest {
    private val api = mockk<NewsApiService>()
    private val articleDao = FakeArticleDao()
    private val keyDao = FakeRemoteKeyDao()
    private val tx = FakeTransaction()
    private val clock = FakeClock()
    private val key = "articles:bbc-news:"

    private fun mediator(query: String = "") =
        ArticlesRemoteMediator("bbc-news", query, api, articleDao, keyDao, tx, clock)

    private fun success(r: RemoteMediator.MediatorResult) =
        (r as RemoteMediator.MediatorResult.Success).endOfPaginationReached

    @Test
    fun `ttl is one hour`() = runTest {
        assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, mediator().initialize())
        keyDao.upsert(RemoteKeyEntity(key, 2, clock.time))
        clock.time += CacheConfig.ARTICLES_TTL_MS - 1
        assertEquals(RemoteMediator.InitializeAction.SKIP_INITIAL_REFRESH, mediator().initialize())
        clock.time += 1
        assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, mediator().initialize())
    }

    @Test
    fun `refresh loads page 1, replaces old rows and stores nextPage`() = runTest {
        articleDao.insertAll(listOf(articleDto(99).toEntityForTest()))
        coEvery { api.getTopHeadlines("bbc-news", null, 1, 20) } returns articlesResponse(20, total = 45)

        val result = mediator().load(LoadType.REFRESH, emptyState())

        assertEquals(false, success(result))
        assertEquals(20, articleDao.rows.size)
        assertEquals(2, keyDao.keys.getValue(key).nextPage)
        assertEquals(clock.time, keyDao.keys.getValue(key).fetchedAt)
    }

    @Test
    fun `append uses nextPage and keeps refresh time`() = runTest {
        keyDao.upsert(RemoteKeyEntity(key, 2, fetchedAt = 123L))
        coEvery { api.getTopHeadlines("bbc-news", null, 2, 20) } returns articlesResponse(20, total = 45, from = 21)

        val result = mediator().load(LoadType.APPEND, emptyState())

        assertEquals(false, success(result))
        assertEquals(3, keyDao.keys.getValue(key).nextPage)
        assertEquals(123L, keyDao.keys.getValue(key).fetchedAt)
    }

    @Test
    fun `last page ends paging`() = runTest {
        keyDao.upsert(RemoteKeyEntity(key, 3, 1L))
        coEvery { api.getTopHeadlines(any(), any(), 3, any()) } returns articlesResponse(5, total = 45, from = 41)

        assertEquals(true, success(mediator().load(LoadType.APPEND, emptyState())))
        assertEquals(null, keyDao.keys.getValue(key).nextPage)
    }

    @Test
    fun `empty page ends paging`() = runTest {
        coEvery { api.getTopHeadlines(any(), any(), any(), any()) } returns articlesResponse(0, total = 100)
        assertEquals(true, success(mediator().load(LoadType.REFRESH, emptyState())))
    }

    @Test
    fun `append without next page does not call the api`() = runTest {
        keyDao.upsert(RemoteKeyEntity(key, null, 1L))
        assertEquals(true, success(mediator().load(LoadType.APPEND, emptyState())))
        assertEquals(true, success(mediator().load(LoadType.PREPEND, emptyState())))
        coVerify(exactly = 0) { api.getTopHeadlines(any(), any(), any(), any()) }
    }

    @Test
    fun `search query is sent as q and stored under its own key`() = runTest {
        coEvery { api.getTopHeadlines("bbc-news", "election", 1, 20) } returns articlesResponse(3, total = 3)

        mediator("election").load(LoadType.REFRESH, emptyState())

        assertEquals(setOf("articles:bbc-news:election"), keyDao.keys.keys)
        assertEquals("election", articleDao.rows.first().queryKey)
    }

    @Test
    fun `unusable articles are filtered but still count for paging`() = runTest {
        val response = articlesResponse(2, total = 100).copy(
            articles = listOf(articleDto(1), articleDto(2, title = "[Removed]"), articleDto(3, url = null)),
        )
        coEvery { api.getTopHeadlines(any(), any(), any(), any()) } returns response

        val result = mediator().load(LoadType.REFRESH, emptyState())

        assertEquals(false, success(result))
        assertEquals(listOf("https://a/1"), articleDao.rows.map { it.url })
    }

    @Test
    fun `duplicate url in later page is ignored`() = runTest {
        keyDao.upsert(RemoteKeyEntity(key, 2, 1L))
        articleDao.insertAll(listOf(articleDto(1).toEntityForTest()))
        coEvery { api.getTopHeadlines(any(), any(), 2, any()) } returns articlesResponse(2, total = 100, from = 1)

        mediator().load(LoadType.APPEND, emptyState())

        assertEquals(listOf("https://a/1", "https://a/2"), articleDao.rows.map { it.url })
    }

    @Test
    fun `network failure keeps rows and key`() = runTest {
        articleDao.insertAll(listOf(articleDto(1).toEntityForTest()))
        keyDao.upsert(RemoteKeyEntity(key, 2, 1L))
        coEvery { api.getTopHeadlines(any(), any(), any(), any()) } throws IOException()

        val result = mediator().load(LoadType.APPEND, emptyState())

        val error = assertInstanceOf(RemoteMediator.MediatorResult.Error::class.java, result)
        assertEquals(NewsError.Offline, (error.throwable as NewsException).error)
        assertEquals(1, articleDao.rows.size)
        assertEquals(2, keyDao.keys.getValue(key).nextPage)
    }

    @Test
    fun `rate limit surfaces as RateLimited`() = runTest {
        val body = """{"status":"error","code":"rateLimited","message":"m"}""".toResponseBody("application/json".toMediaType())
        coEvery { api.getTopHeadlines(any(), any(), any(), any()) } throws HttpException(Response.error<Any>(429, body))

        val result = mediator().load(LoadType.REFRESH, emptyState())

        val error = assertInstanceOf(RemoteMediator.MediatorResult.Error::class.java, result)
        assertEquals(NewsError.RateLimited, (error.throwable as NewsException).error)
    }

    @Test
    fun `max results on append is the end of the list, not an error`() = runTest {
        keyDao.upsert(RemoteKeyEntity(key, 6, 1L))
        val body = """{"status":"error","code":"maximumResultsReached","message":"m"}""".toResponseBody("application/json".toMediaType())
        coEvery { api.getTopHeadlines(any(), any(), 6, any()) } throws HttpException(Response.error<Any>(426, body))

        assertEquals(true, success(mediator().load(LoadType.APPEND, emptyState())))
        assertEquals(null, keyDao.keys.getValue(key).nextPage)
    }

    @Test
    fun `cancellation is rethrown`() = runTest {
        coEvery { api.getTopHeadlines(any(), any(), any(), any()) } throws CancellationException("left screen")
        var thrown = false
        try { mediator().load(LoadType.REFRESH, emptyState()) } catch (e: CancellationException) { thrown = true }
        assertEquals(true, thrown)
    }

    private fun ArticleDto.toEntityForTest() = toEntity("bbc-news", "")!!
}
