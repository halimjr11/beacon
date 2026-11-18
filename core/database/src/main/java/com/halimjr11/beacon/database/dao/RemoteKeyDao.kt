package com.halimjr11.beacon.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.halimjr11.beacon.database.entity.RemoteKeyEntity

@Dao
interface RemoteKeyDao {

    @Query("SELECT * FROM remote_keys WHERE `key` = :key")
    suspend fun get(key: String): RemoteKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: RemoteKeyEntity)

    @Query("DELETE FROM remote_keys WHERE `key` = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM remote_keys WHERE fetchedAt < :olderThan")
    suspend fun deleteOlderThan(olderThan: Long)
}
