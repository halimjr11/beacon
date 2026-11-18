package com.halimjr11.beacon.domain.usecase

import androidx.paging.PagingData
import com.halimjr11.beacon.domain.model.Article
import com.halimjr11.beacon.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow

class GetArticlesUseCase(private val repository: NewsRepository) {
    operator fun invoke(sourceId: String, query: String): Flow<PagingData<Article>> =
        repository.articles(sourceId, query)
}
