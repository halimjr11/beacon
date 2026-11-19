package com.halimjr11.beacon.common

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class CommonModule {
    @Single
    fun dispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider
}
