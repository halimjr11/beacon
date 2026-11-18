package com.halimjr11.beacon.domain

import com.halimjr11.beacon.domain.usecase.GetArticlesUseCase
import com.halimjr11.beacon.domain.usecase.GetSourcesUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::GetSourcesUseCase)
    factoryOf(::GetArticlesUseCase)
}
