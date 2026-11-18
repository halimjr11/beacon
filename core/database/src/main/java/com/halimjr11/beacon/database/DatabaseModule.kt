package com.halimjr11.beacon.database

import androidx.room3.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    // The cache can always be re-fetched, so a schema change just rebuilds it.
    single<BeaconDatabase> {
        Room.databaseBuilder(androidContext(), BeaconDatabase::class.java, "beacon.db")
            .fallbackToDestructiveMigration(true)
            .build()
    }
    single { get<BeaconDatabase>().sourceDao() }
    single { get<BeaconDatabase>().articleDao() }
    single { get<BeaconDatabase>().remoteKeyDao() }
}
