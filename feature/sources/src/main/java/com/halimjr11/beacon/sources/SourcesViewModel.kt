package com.halimjr11.beacon.sources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.halimjr11.beacon.domain.model.NewsCategory
import com.halimjr11.beacon.domain.model.NewsSource
import com.halimjr11.beacon.domain.usecase.GetSourcesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

/** Category chip and search text live here so they survive rotation (AC 5.3). Search is local (Room LIKE), so no debounce. */
@OptIn(ExperimentalCoroutinesApi::class)
/** Koin ignores @InjectedParam on a defaulted parameter, so the category is required; the tab passes General. */
@KoinViewModel
class SourcesViewModel(
    getSources: GetSourcesUseCase,
    @InjectedParam
    initialCategory: NewsCategory,
) : ViewModel() {
    constructor(getSources: GetSourcesUseCase) : this(getSources, NewsCategory.General)

    private val _category = MutableStateFlow(initialCategory)
    private val _query = MutableStateFlow("")

    val category: StateFlow<NewsCategory> = _category
    val query: StateFlow<String> = _query

    val sources: Flow<PagingData<NewsSource>> =
        combine(_category, _query) { category, query -> category to query.trim() }
            .flatMapLatest { (category, query) -> getSources(category, query) }
            .cachedIn(viewModelScope)

    fun onCategoryChange(category: NewsCategory) { _category.value = category }
    fun onQueryChange(query: String) { _query.value = query }
}
