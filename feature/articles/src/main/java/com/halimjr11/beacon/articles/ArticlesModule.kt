package com.halimjr11.beacon.articles

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val articlesModule = module {
    // Created with parametersOf(feed); the screen keys the ViewModel per feed.
    viewModel { params -> ArticlesViewModel(feed = params.get(), getArticles = get()) }
}
