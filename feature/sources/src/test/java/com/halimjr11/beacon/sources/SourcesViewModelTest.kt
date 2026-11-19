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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
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

    @ParameterizedTest
    @MethodSource("queryCases")
    fun `queries are trimmed before they reach the repository`(typed: String, expected: String) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val vm = SourcesViewModel(GetSourcesUseCase(repository))
        val job = backgroundScope.launch { vm.sources.collect { } }
        runCurrent()

        vm.onQueryChange(typed)
        runCurrent()

        verify { repository.sources(NewsCategory.General, expected) }
        assertEquals(typed, vm.query.value)
        job.cancel()
    }

    @ParameterizedTest
    @EnumSource(NewsCategory::class)
    fun `every category chip requests its own list`(category: NewsCategory) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val vm = SourcesViewModel(GetSourcesUseCase(repository))
        val job = backgroundScope.launch { vm.sources.collect { } }
        runCurrent()

        vm.onCategoryChange(category)
        runCurrent()

        verify { repository.sources(category, "") }
        job.cancel()
    }

    @ParameterizedTest
    @EnumSource(NewsCategory::class)
    fun `a genre screen starts on its own category`(category: NewsCategory) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val vm = SourcesViewModel(GetSourcesUseCase(repository), initialCategory = category)
        val job = backgroundScope.launch { vm.sources.collect { } }
        runCurrent()

        verify(exactly = 1) { repository.sources(category, "") }
        assertEquals(category, vm.category.value)
        job.cancel()
    }

    companion object {
        @JvmStatic
        fun queryCases(): Stream<Arguments> = Stream.of(
            argumentSet("plain", "bbc", "bbc"),
            argumentSet("padded", "  bbc ", "bbc"),
            argumentSet("only spaces", "   ", ""),
            argumentSet("inner space kept", " bbc news ", "bbc news"),
        )
    }
}
