package com.halimjr11.beacon.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.halimjr11.beacon.domain.model.Article
import com.halimjr11.beacon.domain.model.NewsFeed
import com.halimjr11.beacon.domain.usecase.GetArticlesUseCase
import kotlinx.coroutines.flow.Flow
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class HomeViewModel(getArticles: GetArticlesUseCase) : ViewModel() {
    /** One headline list: the first items feed the carousel, the rest the "Latest" list. */
    val headlines: Flow<PagingData<Article>> =
        getArticles(NewsFeed.Headlines, "").cachedIn(viewModelScope)
}
