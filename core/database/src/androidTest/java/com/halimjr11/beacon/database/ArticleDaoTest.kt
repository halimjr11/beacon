package com.halimjr11.beacon.database

import com.halimjr11.beacon.database.entity.RemoteKeyEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleDaoTest : DatabaseTest() {
    private val dao get() = db.articleDao()

    @Test
    fun articlesKeepInsertionOrderByRowId() = runBlocking {
        dao.insertAll(listOf(article("c"), article("a"), article("b")))
        assertEquals(listOf("c", "a", "b"), dao.pagingSource("top", "").firstPage().map { it.url })
    }

    @Test
    fun sameUrlInTheSameFeedAndQueryIsIgnoredInAnotherFeedItIsKeptAC63() = runBlocking {
        dao.insertAll(listOf(article("u1"), article("u1"), article("u1", feedId = "category:sports")))
        assertEquals(listOf("u1"), dao.pagingSource("top", "").firstPage().map { it.url })
        assertEquals(listOf("u1"), dao.pagingSource("category:sports", "").firstPage().map { it.url })
    }

    @Test
    fun feedsAndQueriesAreSeparateLists() = runBlocking {
        dao.insertAll(listOf(article("a"), article("b", queryKey = "election"), article("c", feedId = "source:bbc")))
        assertEquals(listOf("a"), dao.pagingSource("top", "").firstPage().map { it.url })
        assertEquals(listOf("b"), dao.pagingSource("top", "election").firstPage().map { it.url })
        assertEquals(listOf("c"), dao.pagingSource("source:bbc", "").firstPage().map { it.url })
    }

    @Test
    fun deleteByKeyRemovesOnlyThatFeedAndQuery() = runBlocking {
        dao.insertAll(listOf(article("a"), article("b", queryKey = "q"), article("c", feedId = "other")))
        dao.deleteByKey("top", "")
        assertTrue(dao.pagingSource("top", "").firstPage().isEmpty())
        assertEquals(1, dao.pagingSource("top", "q").firstPage().size)
        assertEquals(1, dao.pagingSource("other", "").firstPage().size)
    }

    @Test
    fun deleteOrphansDropsArticlesWhoseRemoteKeyIsGone() = runBlocking {
        dao.insertAll(listOf(article("kept", feedId = "top"), article("orphan", feedId = "category:x", queryKey = "q")))
        db.remoteKeyDao().upsert(RemoteKeyEntity("articles:top:", nextPage = null, fetchedAt = 1L))

        dao.deleteOrphans()

        assertEquals(listOf("kept"), dao.pagingSource("top", "").firstPage().map { it.url })
        assertTrue(dao.pagingSource("category:x", "q").firstPage().isEmpty())
    }

    @Test
    fun trimToKeepsOnlyTheNewestRows() = runBlocking {
        dao.insertAll((1..10).map { article("u$it") })
        dao.trimTo(3)
        assertEquals(listOf("u8", "u9", "u10"), dao.pagingSource("top", "").firstPage().map { it.url })
    }
}
