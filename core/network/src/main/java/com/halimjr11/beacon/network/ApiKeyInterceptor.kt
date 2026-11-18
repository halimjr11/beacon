package com.halimjr11.beacon.network

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(
        chain.request().newBuilder()
            .header("X-Api-Key", apiKey)
            .header("Accept", "application/json")
            .build(),
    )
}
