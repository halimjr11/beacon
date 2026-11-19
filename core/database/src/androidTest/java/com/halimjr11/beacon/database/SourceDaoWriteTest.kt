package com.halimjr11.beacon.database

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SourceDaoWriteTest : DatabaseTest() {

    @Test
    fun deleteByCategoryOnlyRemovesThatCategory() = runBlocking {
        db.sourceDao().insertAll(listOf(source("a", category = "general"), source("b", category = "sports")))

        db.sourceDao().deleteByCategory("general")

        assertEquals(emptyList<String>(), db.sourceDao().pagingSource("general", "%%").firstPage().map { it.id })
        assertEquals(listOf("b"), db.sourceDao().pagingSource("sports", "%%").firstPage().map { it.id })
    }

    @Test
    fun insertReplacesASourceWithTheSameId() = runBlocking {
        db.sourceDao().insertAll(listOf(source("a", name = "Old")))
        db.sourceDao().insertAll(listOf(source("a", name = "New")))

        assertEquals(listOf("New"), db.sourceDao().pagingSource("general", "%%").firstPage().map { it.name })
    }

    @Test
    fun sourcesAreOrderedByNameIgnoringCase() = runBlocking {
        db.sourceDao().insertAll(listOf(source("1", "banana"), source("2", "Apple"), source("3", "cherry")))

        assertEquals(listOf("Apple", "banana", "cherry"), db.sourceDao().pagingSource("general", "%%").firstPage().map { it.name })
    }
}
