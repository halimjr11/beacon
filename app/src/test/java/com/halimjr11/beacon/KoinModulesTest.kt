package com.halimjr11.beacon

import com.halimjr11.beacon.domain.model.NewsCategory
import com.halimjr11.beacon.domain.model.NewsFeed
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.test.verify.verify

class KoinModulesTest {
    @Test
    fun `dependency graph is complete`() {
        module { includes(appModules) }.verify(extraTypes = listOf(NewsFeed::class, NewsCategory::class))
    }
}
