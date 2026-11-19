package com.halimjr11.beacon.sources

import com.halimjr11.beacon.domain.model.NewsCategory
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val sourcesModule = module {
    // The Sources tab starts the ViewModel with no parameter (General); the screen of one genre
    // passes parametersOf(category).
    viewModel { params ->
        SourcesViewModel(getSources = get(), initialCategory = params.getOrNull<NewsCategory>() ?: NewsCategory.General)
    }
}
