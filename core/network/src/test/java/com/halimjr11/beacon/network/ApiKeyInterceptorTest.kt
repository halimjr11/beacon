package com.halimjr11.beacon.network

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ApiKeyInterceptorTest {
    @Test
    fun `adds api key and accept headers`() {
        val sent = slot<Request>()
        val response = mockk<Response>()
        val chain = mockk<Interceptor.Chain> {
            every { request() } returns Request.Builder().url("https://newsapi.org/v2/").build()
            every { proceed(capture(sent)) } returns response
        }

        ApiKeyInterceptor("secret").intercept(chain)

        assertEquals("secret", sent.captured.header("X-Api-Key"))
        assertEquals("application/json", sent.captured.header("Accept"))
    }
}
