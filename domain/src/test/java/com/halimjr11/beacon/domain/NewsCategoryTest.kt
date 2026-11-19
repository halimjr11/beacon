package com.halimjr11.beacon.domain

import com.halimjr11.beacon.domain.model.NewsCategory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

class NewsCategoryTest {

    @ParameterizedTest
    @EnumSource(NewsCategory::class)
    fun `every category is found again by its api value`(category: NewsCategory) {
        assertEquals(category, NewsCategory.fromApiValue(category.apiValue))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "Technology", "TECHNOLOGY", "tech", " sports", "unknown"])
    fun `anything else is null (the api value is exact)`(value: String) {
        assertNull(NewsCategory.fromApiValue(value))
    }

    @org.junit.jupiter.api.Test
    fun `the seven NewsAPI categories keep their order`() {
        assertEquals(
            listOf("business", "entertainment", "general", "health", "science", "sports", "technology"),
            NewsCategory.entries.map { it.apiValue },
        )
    }
}
