package com.halimjr11.beacon.database

import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import androidx.room3.withWriteTransaction
import com.halimjr11.beacon.database.dao.ArticleDao
import com.halimjr11.beacon.database.dao.RemoteKeyDao
import com.halimjr11.beacon.database.dao.SourceDao
import com.halimjr11.beacon.database.entity.ArticleEntity
import com.halimjr11.beacon.database.entity.RemoteKeyEntity
import com.halimjr11.beacon.database.entity.SourceEntity

@Database(
    entities = [SourceEntity::class, ArticleEntity::class, RemoteKeyEntity::class],
    version = 1,
    exportSchema = true,
)
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
abstract class BeaconDatabase : RoomDatabase() {
    abstract fun sourceDao(): SourceDao
    abstract fun articleDao(): ArticleDao
    abstract fun remoteKeyDao(): RemoteKeyDao
}

/** Runs [block] in one write transaction so a refresh is atomic (AC 8.4). */
suspend fun <R> BeaconDatabase.inTransaction(block: suspend () -> R): R =
    withWriteTransaction { block() }
