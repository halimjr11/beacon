package com.halimjr11.beacon.data

import com.halimjr11.beacon.data.mapper.toDomain
import com.halimjr11.beacon.data.mapper.toEntity
import com.halimjr11.beacon.database.entity.ArticleEntity
import com.halimjr11.beacon.database.entity.SourceEntity
import com.halimjr11.beacon.network.dto.ArticleDto
import com.halimjr11.beacon.network.dto.SourceDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class MappersTest {
    @Test
    fun `source null fields become empty and category is the requested one`() {
        val entity = SourceDto(id = "a", name = "A").toEntity("sports")!!
        assertEquals(SourceEntity("a", "A", "", "sports", "", ""), entity)
    }

    @Test
    fun `source without id or name is dropped`() {
        assertNull(SourceDto(id = null, name = "A").toEntity("x"))
        assertNull(SourceDto(id = "", name = "A").toEntity("x"))
        assertNull(SourceDto(id = "a", name = " ").toEntity("x"))
    }

    @Test
    fun `source shows language and country in upper case`() {
        val domain = SourceEntity("a", "A", "d", "general", "en", "gb").toDomain()
        assertEquals("EN", domain.language)
        assertEquals("GB", domain.country)
    }

    @Test
    fun `article maps fields and null becomes empty`() {
        val entity = ArticleDto(title = "T", url = "https://a").toEntity("bbc", "q")!!
        assertEquals("bbc", entity.feedId)
        assertEquals("q", entity.queryKey)
        assertEquals("", entity.author)
        assertEquals("", entity.imageUrl)
        assertEquals("", entity.sourceName)
    }

    @Test
    fun `article without url, blank title or removed title is dropped`() {
        assertNull(ArticleDto(title = "T", url = null).toEntity("s", ""))
        assertNull(ArticleDto(title = "T", url = " ").toEntity("s", ""))
        assertNull(ArticleDto(title = "", url = "https://a").toEntity("s", ""))
        assertNull(ArticleDto(title = null, url = "https://a").toEntity("s", ""))
        assertNull(ArticleDto(title = "[Removed]", url = "https://a").toEntity("s", ""))
    }

    @Test
    fun `article domain uses url as id and formats the date`() {
        val domain = ArticleEntity(
            rowId = 5, feedId = "s", queryKey = "", url = "https://a", sourceName = "BBC", author = "",
            title = "T", description = "", imageUrl = "", publishedAt = "2026-07-24T10:15:30Z", content = "",
        ).toDomain()
        assertEquals("https://a", domain.id)
        assertEquals("24 Jul 2026", domain.publishedAt)
    }
}
