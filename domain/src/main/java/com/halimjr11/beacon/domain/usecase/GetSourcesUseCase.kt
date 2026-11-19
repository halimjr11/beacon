package com.halimjr11.beacon.domain.usecase

import androidx.paging.PagingData
import com.halimjr11.beacon.domain.model.NewsCategory
import com.halimjr11.beacon.domain.model.NewsSource
import com.halimjr11.beacon.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetSourcesUseCase(private val repository: NewsRepository) {
    operator fun invoke(category: NewsCategory, query: String): Flow<PagingData<NewsSource>> =
        repository.sources(category, query)
}
