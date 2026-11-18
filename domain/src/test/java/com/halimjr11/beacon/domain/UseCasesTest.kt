package com.halimjr11.beacon.domain

import androidx.paging.PagingData
import app.cash.turbine.test
import com.halimjr11.beacon.domain.model.NewsCategory
import com.halimjr11.beacon.domain.model.NewsFeed
import com.halimjr11.beacon.domain.repository.NewsRepository
import com.halimjr11.beacon.domain.usecase.GetArticlesUseCase
import com.halimjr11.beacon.domain.usecase.GetSourcesUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UseCasesTest {
    private val repository = mockk<NewsRepository>()

    @Test
    fun `sources forwards arguments and flow`() = runTest {
        val data = PagingData.empty<com.halimjr11.beacon.domain.model.NewsSource>()
        every { repository.sources(NewsCategory.Sports, "bbc") } returns flowOf(data)

        GetSourcesUseCase(repository)(NewsCategory.Sports, "bbc").test {
            assertEquals(data, awaitItem())
            awaitComplete()
        }
        verify(exactly = 1) { repository.sources(NewsCategory.Sports, "bbc") }
    }

    @Test
    fun `articles forwards arguments and flow`() = runTest {
        val data = PagingData.empty<com.halimjr11.beacon.domain.model.Article>()
        every { repository.articles(NewsFeed.Source("bbc-news"), "") } returns flowOf(data)

        GetArticlesUseCase(repository)(NewsFeed.Source("bbc-news"), "").test {
            assertEquals(data, awaitItem())
            awaitComplete()
        }
    }
}
