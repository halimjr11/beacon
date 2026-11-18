package com.halimjr11.beacon.network

import com.halimjr11.beacon.network.dto.ArticlesResponseDto
import com.halimjr11.beacon.network.dto.SourcesResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    @GET("top-headlines/sources")
    suspend fun getSources(
        @Query("category") category: String?,
        @Query("language") language: String? = "en",
    ): SourcesResponseDto

    @GET("top-headlines")
    suspend fun getTopHeadlines(
        // All optional: null makes Retrofit omit the parameter. NewsAPI forbids mixing
        // `sources` with `country`/`category`; callers pass one group only.
        @Query("sources") sources: String?,
        @Query("category") category: String?,
        @Query("country") country: String?,
        @Query("q") query: String?,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
    ): ArticlesResponseDto
}
