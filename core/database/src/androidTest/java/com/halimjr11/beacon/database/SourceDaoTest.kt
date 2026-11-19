package com.halimjr11.beacon.database

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** The LIKE search used by the Sources tab (AC 5.1): case-insensitive, name or description, wildcards escaped. */
@RunWith(Parameterized::class)
class SourceDaoSearchTest(
    @Suppress("unused") private val caseName: String,
    private val pattern: String,
    private val expectedIds: List<String>,
) : DatabaseTest() {

    @Test
    fun search() {
        runBlocking {
            db.sourceDao().insertAll(
                listOf(
                    source("bbc-news", "BBC News", "Use BBC News for breaking news"),
                    source("cnn", "CNN", "Cable news from Atlanta"),
                    source("the-verge", "The Verge", "Technology, 100% coverage"),
                    source("snake", "Snake_Case", "has an underscore"),
                    source("sports-1", "Sports", "bbc sport", category = "sports"),
                ),
            )
        }
        val ids = db.sourceDao().pagingSource("general", pattern).firstPage().map { it.id }
        assertEquals(expectedIds, ids)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases() = listOf(
            arrayOf<Any>("empty pattern matches all in category", "%%", listOf("bbc-news", "cnn", "snake", "the-verge")),
            arrayOf<Any>("by name", "%verge%", listOf("the-verge")),
            arrayOf<Any>("case-insensitive", "%BBC%", listOf("bbc-news")),
            arrayOf<Any>("by description", "%atlanta%", listOf("cnn")),
            arrayOf<Any>("name or description", "%news%", listOf("bbc-news", "cnn")),
            arrayOf<Any>("no match", "%zzz%", emptyList<String>()),
            arrayOf<Any>("other category excluded", "%sport%", emptyList<String>()),
            arrayOf<Any>("escaped percent is literal", "%100\\%%", listOf("the-verge")),
            arrayOf<Any>("escaped underscore is literal", "%e\\_c%", listOf("snake")),
            arrayOf<Any>("unescaped underscore is a wildcard", "%e_c%", listOf("snake")),
        )
    }
}
