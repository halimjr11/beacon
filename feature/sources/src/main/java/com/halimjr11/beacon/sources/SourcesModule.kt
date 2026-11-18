package com.halimjr11.beacon.sources

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sourcesModule = module {
    viewModelOf(::SourcesViewModel)
}
