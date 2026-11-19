package com.halimjr11.beacon

import com.halimjr11.beacon.domain.model.NewsCategory
import com.halimjr11.beacon.domain.model.NewsFeed
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.test.verify.verify

class KoinModulesTest {
    @Test
    fun `dependency graph is complete`() {
        module { includes(appModules) }.verify(extraTypes = listOf(NewsFeed::class, NewsCategory::class))
    }

    @Test
    fun `no two modules define the same type (startKoin runs with allowOverride false)`() {
        val app = koinApplication {
            allowOverride(false)
            modules(appModules)
        }
        // 8 Gradle modules contribute a Koin module each.
        assertEquals(8, appModules.size)
        app.close()
    }
}
