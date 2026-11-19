package com.halimjr11.beacon.database

import com.halimjr11.beacon.database.entity.RemoteKeyEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

class RemoteKeyDaoTest : DatabaseTest() {
    @Test
    fun upsertReplacesThePreviousKey() = runBlocking {
        val dao = db.remoteKeyDao()
        dao.upsert(RemoteKeyEntity("k", nextPage = 2, fetchedAt = 10L))
        dao.upsert(RemoteKeyEntity("k", nextPage = null, fetchedAt = 20L))
        assertEquals(RemoteKeyEntity("k", null, 20L), dao.get("k"))
    }

    @Test
    fun getAndDelete() = runBlocking {
        val dao = db.remoteKeyDao()
        assertNull(dao.get("missing"))
        dao.upsert(RemoteKeyEntity("k", 2, 1L))
        dao.delete("k")
        assertNull(dao.get("k"))
    }
}

/** Retention: which keys survive `deleteOlderThan(100)`. */
@RunWith(Parameterized::class)
class RemoteKeyRetentionTest(
    @Suppress("unused")
    private val caseName: String,
    private val fetchedAt: Long,
    private val survives: Boolean,
) : DatabaseTest() {
    @Test
    fun deleteOlderThan() = runBlocking {
        db.remoteKeyDao().upsert(RemoteKeyEntity("k", null, fetchedAt))
        db.remoteKeyDao().deleteOlderThan(100L)
        assertEquals(survives, db.remoteKeyDao().get("k") != null)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases() = listOf(
            arrayOf<Any>("older is deleted", 99L, false),
            arrayOf<Any>("equal to the limit is kept", 100L, true),
            arrayOf<Any>("newer is kept", 101L, true),
        )
    }
}
