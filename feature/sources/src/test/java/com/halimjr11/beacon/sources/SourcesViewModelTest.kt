package com.halimjr11.beacon.sources

import androidx.paging.PagingData
import com.halimjr11.beacon.domain.model.NewsCategory
import com.halimjr11.beacon.domain.model.NewsSource
import com.halimjr11.beacon.domain.repository.NewsRepository
import com.halimjr11.beacon.domain.usecase.GetSourcesUseCase
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SourcesViewModelTest {
    private val repository = mockk<NewsRepository>()

    @BeforeEach
    fun setUp() {
        every { repository.sources(any(), any()) } returns flowOf(PagingData.empty<NewsSource>())
    }

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `starts on General with an empty query`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val vm = SourcesViewModel(GetSourcesUseCase(repository))
        val job = backgroundScope.launch { vm.sources.collect { } }
        runCurrent()

        verify(exactly = 1) { repository.sources(NewsCategory.General, "") }
        assertEquals(NewsCategory.General, vm.category.value)
        job.cancel()
    }

    @Test
    fun `query is trimmed, field keeps raw text, category change requests a new list`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val vm = SourcesViewModel(GetSourcesUseCase(repository))
        val job = backgroundScope.launch { vm.sources.collect { } }
        runCurrent()

        vm.onQueryChange("  bbc ")
        vm.onCategoryChange(NewsCategory.Sports)
        runCurrent()

        verify { repository.sources(NewsCategory.Sports, "bbc") }
        assertEquals("  bbc ", vm.query.value)
        job.cancel()
    }
}
