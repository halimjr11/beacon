package com.halimjr11.beacon

import com.halimjr11.beacon.articles.articlesModule
import com.halimjr11.beacon.data.dataModule
import com.halimjr11.beacon.database.databaseModule
import com.halimjr11.beacon.domain.domainModule
import com.halimjr11.beacon.home.homeModule
import com.halimjr11.beacon.network.networkModule
import com.halimjr11.beacon.sources.sourcesModule

/** Every Koin module of the app; features register only their own ViewModels. */
val appModules = listOf(
    networkModule,
    databaseModule,
    dataModule,
    domainModule,
    homeModule,
    sourcesModule,
    articlesModule,
)
