package com.halimjr11.beacon.home

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
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `home asks for the default headlines feed`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = mockk<NewsRepository>()
        every { repository.articles(any(), any()) } returns flowOf(PagingData.empty<Article>())
        val vm = HomeViewModel(GetArticlesUseCase(repository))

        val job = backgroundScope.launch { vm.headlines.collect { } }
        runCurrent()

        verify(exactly = 1) { repository.articles(NewsFeed.Headlines, "") }
        job.cancel()
    }
}
