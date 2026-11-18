package com.halimjr11.beacon.database.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.halimjr11.beacon.database.entity.SourceEntity

@Dao
interface SourceDao {

    /** [pattern] is a LIKE pattern such as `%bbc%`; `\` is the escape character. */
    @Query(
        """
        SELECT * FROM sources
        WHERE category = :category
          AND (name LIKE :pattern ESCAPE '\' OR description LIKE :pattern ESCAPE '\')
        ORDER BY name COLLATE NOCASE
        """,
    )
    fun pagingSource(category: String, pattern: String): PagingSource<Int, SourceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SourceEntity>)

    @Query("DELETE FROM sources WHERE category = :category")
    suspend fun deleteByCategory(category: String)
}
