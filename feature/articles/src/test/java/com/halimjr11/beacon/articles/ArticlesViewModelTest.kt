package com.halimjr11.beacon.articles

import androidx.paging.PagingData
import com.halimjr11.beacon.domain.model.Article
import com.halimjr11.beacon.domain.model.NewsFeed
import com.halimjr11.beacon.domain.repository.NewsRepository
import com.halimjr11.beacon.domain.usecase.GetArticlesUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArticlesViewModelTest {
    private val repository = mockk<NewsRepository>()
    private val feed = NewsFeed.Source("bbc-news")

    @BeforeEach fun setUp() { every { repository.articles(any(), any()) } returns flowOf(PagingData.empty<Article>()) }
    @AfterEach fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `search waits 500 ms and only the last query is requested`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val vm = ArticlesViewModel(feed, GetArticlesUseCase(repository))
        val job = backgroundScope.launch { vm.articles.collect { } }
        runCurrent()
        verify(exactly = 1) { repository.articles(feed, "") }

        vm.onQueryChange("el"); advanceTimeBy(200)
        vm.onQueryChange("ele"); advanceTimeBy(200)
        vm.onQueryChange("elect")
        advanceTimeBy(499); runCurrent()
        verify(exactly = 0) { repository.articles(feed, "elect") }

        advanceTimeBy(2); runCurrent()
        verify(exactly = 1) { repository.articles(feed, "elect") }
        verify(exactly = 0) { repository.articles(feed, "el") }
        job.cancel()
    }

    @Test
    fun `one character or shrinking back goes to the default list`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val vm = ArticlesViewModel(feed, GetArticlesUseCase(repository))
        val job = backgroundScope.launch { vm.articles.collect { } }
        runCurrent()

        vm.onQueryChange("election"); advanceTimeBy(600); runCurrent()
        verify(exactly = 1) { repository.articles(feed, "election") }

        vm.onQueryChange("e"); advanceTimeBy(600); runCurrent()
        // back to "" (AC 5.8): a second request for the default list, none for "e"
        verify(exactly = 2) { repository.articles(feed, "") }
        verify(exactly = 0) { repository.articles(feed, "e") }
        job.cancel()
    }
}
