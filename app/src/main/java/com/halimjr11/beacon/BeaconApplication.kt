package com.halimjr11.beacon

import android.app.Application
import com.halimjr11.beacon.data.cache.CacheRetention
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BeaconApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val koin = startKoin {
            androidContext(this@BeaconApplication)
            modules(appModules)
        }.koin
        // Drop cache older than 7 days off the main thread (ADR-013 retention).
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { koin.get<CacheRetention>().purge() }
    }
}
