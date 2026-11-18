package com.halimjr11.beacon.domain.repository

import androidx.paging.PagingData
import com.halimjr11.beacon.domain.model.Article
import com.halimjr11.beacon.domain.model.NewsCategory
import com.halimjr11.beacon.domain.model.NewsSource
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun sources(category: NewsCategory, query: String): Flow<PagingData<NewsSource>>
    fun articles(sourceId: String, query: String): Flow<PagingData<Article>>
}
