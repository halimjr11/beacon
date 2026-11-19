package com.halimjr11.beacon.database

import android.content.Context
import androidx.room3.Room
import com.halimjr11.beacon.database.dao.ArticleDao
import com.halimjr11.beacon.database.dao.RemoteKeyDao
import com.halimjr11.beacon.database.dao.SourceDao
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class DatabaseModule {

    /** The cache can always be re-fetched, so a schema change just rebuilds it. */
    @Single
    fun database(context: Context): BeaconDatabase =
        Room.databaseBuilder(context, BeaconDatabase::class.java, "beacon.db")
            .fallbackToDestructiveMigration(true)
            .build()

    @Single
    fun sourceDao(database: BeaconDatabase): SourceDao = database.sourceDao()

    @Single
    fun articleDao(database: BeaconDatabase): ArticleDao = database.articleDao()

    @Single
    fun remoteKeyDao(database: BeaconDatabase): RemoteKeyDao = database.remoteKeyDao()
}
