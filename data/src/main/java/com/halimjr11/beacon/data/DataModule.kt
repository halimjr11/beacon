package com.halimjr11.beacon.data

import com.halimjr11.beacon.data.cache.CacheRetention
import com.halimjr11.beacon.data.cache.Clock
import com.halimjr11.beacon.data.cache.RoomTransactionRunner
import com.halimjr11.beacon.data.cache.TransactionRunner
import com.halimjr11.beacon.data.repository.NewsRepositoryImpl
import com.halimjr11.beacon.domain.repository.NewsRepository
import org.koin.dsl.module

val dataModule = module {
    single<Clock> { Clock.System }
    single<TransactionRunner> { RoomTransactionRunner(get()) }
    single<NewsRepository> { NewsRepositoryImpl(get(), get(), get(), get(), get(), get()) }
    single { CacheRetention(get(), get(), get(), get()) }
}
