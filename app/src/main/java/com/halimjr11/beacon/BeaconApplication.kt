package com.halimjr11.beacon

import android.app.Application
import android.content.pm.ApplicationInfo
import com.halimjr11.beacon.data.cache.CacheRetention
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class BeaconApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val debuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        val koin = startKoin {
            // Koin logs only in debug builds; release stays silent.
            androidLogger(if (debuggable) Level.ERROR else Level.NONE)
            androidContext(this@BeaconApplication)
            // Two modules defining the same type is a mistake here, not an override.
            allowOverride(false)
            modules(appModules)
        }.koin
        // Drop cache older than 7 days off the main thread (ADR-013 retention).
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { koin.get<CacheRetention>().purge() }
    }
}
