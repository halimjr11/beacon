package com.halimjr11.beacon.data

import com.halimjr11.beacon.data.cache.Clock
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * Repository, retention and transaction runner are picked up as @Single classes under
 * com.halimjr11.beacon.data; only what cannot be annotated (an interface constant) is provided here.
 */
@Module
@ComponentScan
class DataModule {
    @Single
    fun clock(): Clock = Clock.System
}
