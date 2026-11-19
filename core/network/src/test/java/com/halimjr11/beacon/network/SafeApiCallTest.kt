package com.halimjr11.beacon.network

import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.halimjr11.beacon.common.NetworkResult
import com.halimjr11.beacon.common.NewsError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.stream.Stream

class SafeApiCallTest {

    @Test
    fun `success is wrapped`() = runTest {
        assertEquals(NetworkResult.Success(42), safeApiCall { 42 })
    }

    @ParameterizedTest
    @MethodSource("failures")
    fun `every failure maps to a NewsError`(failure: Throwable, expected: NewsError) = runTest {
        assertEquals(NetworkResult.Error(expected), safeApiCall<Int> { throw failure })
    }

    @Test
    fun `cancellation is rethrown, never turned into an error (AC 7_2)`() = runTest {
        assertThrows(CancellationException::class.java) {
            kotlinx.coroutines.runBlocking { safeApiCall<Int> { throw CancellationException("left the screen") } }
        }
    }

    companion object {
        private fun http(status: Int, body: String) =
            HttpException(Response.error<Any>(status, body.toResponseBody("application/json".toMediaType())))

        @JvmStatic
        fun failures(): Stream<Arguments> = Stream.of(
            argumentSet("offline", IOException("no network"), NewsError.Offline),
            argumentSet("timeout", SocketTimeoutException(), NewsError.Offline),
            argumentSet("dns", UnknownHostException("newsapi.org"), NewsError.Offline),
            argumentSet("gson parse", JsonParseException("bad"), NewsError.Parse),
            argumentSet("gson syntax", JsonSyntaxException("bad"), NewsError.Parse),
            argumentSet("http rate limit body", http(429, """{"status":"error","code":"rateLimited","message":"m"}"""), NewsError.RateLimited),
            argumentSet("http bad key body", http(401, """{"status":"error","code":"apiKeyInvalid","message":"m"}"""), NewsError.InvalidApiKey),
            argumentSet("http source missing", http(400, """{"status":"error","code":"sourceDoesNotExist","message":"m"}"""), NewsError.SourceNotFound),
            argumentSet("http 500 without json", http(500, "<html>oops</html>"), NewsError.Server),
            argumentSet("http 429 empty body", http(429, ""), NewsError.RateLimited),
            argumentSet("unexpected exception hides its message", IllegalStateException("https://secret/url?apiKey=abc"), NewsError.Unknown("IllegalStateException")),
        )
    }
}
