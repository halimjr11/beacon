package com.halimjr11.beacon.database.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.halimjr11.beacon.database.entity.ArticleEntity

@Dao
interface ArticleDao {

    @Query("SELECT * FROM articles WHERE sourceId = :sourceId AND queryKey = :queryKey ORDER BY rowId")
    fun pagingSource(sourceId: String, queryKey: String): PagingSource<Int, ArticleEntity>

    /** Duplicates of `(sourceId, queryKey, url)` are skipped by the unique index. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<ArticleEntity>)

    @Query("DELETE FROM articles WHERE sourceId = :sourceId AND queryKey = :queryKey")
    suspend fun deleteByKey(sourceId: String, queryKey: String)

    /** Retention: drop articles whose remote key was already purged. */
    @Query("DELETE FROM articles WHERE 'articles:' || sourceId || ':' || queryKey NOT IN (SELECT `key` FROM remote_keys)")
    suspend fun deleteOrphans()

    /** Retention: keep only the newest [max] rows. */
    @Query("DELETE FROM articles WHERE rowId NOT IN (SELECT rowId FROM articles ORDER BY rowId DESC LIMIT :max)")
    suspend fun trimTo(max: Int)
}
