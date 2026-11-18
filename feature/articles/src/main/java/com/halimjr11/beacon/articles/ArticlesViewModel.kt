package com.halimjr11.beacon.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.halimjr11.beacon.domain.model.Article
import com.halimjr11.beacon.domain.model.NewsFeed
import com.halimjr11.beacon.domain.usecase.GetArticlesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal const val SEARCH_DEBOUNCE_MS = 500L
internal const val MIN_QUERY_LENGTH = 2

/** Article list of one [feed] (genre or source) with API search: debounce, min length, cancel stale. */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ArticlesViewModel(
    private val feed: NewsFeed,
    getArticles: GetArticlesUseCase,
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val articles: Flow<PagingData<Article>> = _query
        // Typing waits 500 ms; the initial and cleared (empty) text apply immediately.
        .debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MS }
        // Below 2 characters means the default list, also when the text shrinks (AC 5.8).
        .map { it.trim().let { q -> if (q.length < MIN_QUERY_LENGTH) "" else q } }
        .distinctUntilChanged()
        .flatMapLatest { getArticles(feed, it) }
        .cachedIn(viewModelScope)

    fun onQueryChange(query: String) { _query.value = query }
}
