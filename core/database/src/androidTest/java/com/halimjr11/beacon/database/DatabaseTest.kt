package com.halimjr11.beacon.database

import android.content.Context
import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import com.halimjr11.beacon.database.entity.ArticleEntity
import com.halimjr11.beacon.database.entity.SourceEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before

/** In-memory Room per test; DAO queries run against real SQLite on the device. */
abstract class DatabaseTest {
    protected lateinit var db: BeaconDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BeaconDatabase::class.java).build()
    }

    @After
    fun closeDb() = db.close()

    protected fun source(id: String, name: String = id, description: String = "", category: String = "general") =
        SourceEntity(id = id, name = name, description = description, category = category, language = "en", country = "us")

    protected fun article(url: String, feedId: String = "top", queryKey: String = "") = ArticleEntity(
        feedId = feedId, queryKey = queryKey, url = url, sourceName = "S", author = "", title = "T $url",
        description = "", imageUrl = "", publishedAt = "", content = "",
    )

    /** First page of a PagingSource, as the UI would see it. */
    protected fun <V : Any> PagingSource<Int, V>.firstPage(size: Int = 50): List<V> = runBlocking {
        val result = load(PagingSource.LoadParams.Refresh(key = null, loadSize = size, placeholdersEnabled = false))
        (result as PagingSource.LoadResult.Page).data
    }
}
