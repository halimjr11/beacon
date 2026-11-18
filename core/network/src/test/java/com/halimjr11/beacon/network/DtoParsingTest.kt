package com.halimjr11.beacon.network

import com.google.gson.Gson
import com.halimjr11.beacon.network.dto.ArticlesResponseDto
import com.halimjr11.beacon.network.dto.SourcesResponseDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class DtoParsingTest {
    private val gson = Gson()

    @Test
    fun `articles keep fields and tolerate null and unknown keys`() {
        val json = """{"status":"ok","totalResults":1,"extra":1,"articles":[
            {"source":{"id":null,"name":"BBC"},"title":"T","url":"https://a","urlToImage":null,"unknown":"x"}]}"""
        val dto = gson.fromJson(json, ArticlesResponseDto::class.java)

        assertEquals(1, dto.totalResults)
        val article = dto.articles!!.single()
        assertEquals("BBC", article.source?.name)
        assertEquals("T", article.title)
        assertNull(article.urlToImage)
        assertNull(article.author)
    }

    @Test
    fun `empty object parses to all null`() {
        val dto = gson.fromJson("{}", SourcesResponseDto::class.java)
        assertNull(dto.sources)
        assertNull(dto.status)
    }

    @Test
    fun `sources parse`() {
        val dto = gson.fromJson("""{"sources":[{"id":"bbc-news","name":"BBC News","country":"gb"}]}""", SourcesResponseDto::class.java)
        assertEquals("bbc-news", dto.sources!!.single().id)
        assertNull(dto.sources!!.single().description)
    }
}
