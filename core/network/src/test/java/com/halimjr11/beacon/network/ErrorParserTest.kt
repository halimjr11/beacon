package com.halimjr11.beacon.network

import com.halimjr11.beacon.common.NewsError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ErrorParserTest {

    @ParameterizedTest
    @MethodSource("cases")
    fun parse(status: Int?, body: String?, expected: NewsError) {
        assertEquals(expected, ErrorParser.parse(status, body))
    }

    companion object {
        private fun body(code: String) = """{"status":"error","code":"$code","message":"m"}"""

        @JvmStatic
        fun cases(): Stream<Arguments> = Stream.of(
            argumentSet("apiKeyMissing", 401, body("apiKeyMissing"), NewsError.InvalidApiKey),
            argumentSet("apiKeyInvalid", 401, body("apiKeyInvalid"), NewsError.InvalidApiKey),
            argumentSet("apiKeyDisabled", 401, body("apiKeyDisabled"), NewsError.InvalidApiKey),
            argumentSet("apiKeyExhausted", 429, body("apiKeyExhausted"), NewsError.RateLimited),
            argumentSet("rateLimited", 429, body("rateLimited"), NewsError.RateLimited),
            argumentSet("sourceDoesNotExist", 400, body("sourceDoesNotExist"), NewsError.SourceNotFound),
            argumentSet("sourcesTooMany", 400, body("sourcesTooMany"), NewsError.BadRequest("sourcesTooMany")),
            argumentSet("parameterInvalid", 400, body("parameterInvalid"), NewsError.BadRequest("parameterInvalid")),
            argumentSet("parametersMissing", 400, body("parametersMissing"), NewsError.BadRequest("parametersMissing")),
            argumentSet("unexpectedError", 500, body("unexpectedError"), NewsError.Server),
            argumentSet("max results", 426, body("maximumResultsReached"), NewsError.EndOfResults),
            argumentSet("unknown code", 400, body("zzz"), NewsError.Unknown("zzz")),
            argumentSet("code wins over status", 500, body("rateLimited"), NewsError.RateLimited),
            argumentSet("not json, 503", 503, "<html>", NewsError.Server),
            argumentSet("empty body, 401", 401, "", NewsError.InvalidApiKey),
            argumentSet("null body, 429", 429, null, NewsError.RateLimited),
            argumentSet("json without code, 400", 400, "{}", NewsError.BadRequest("HTTP 400")),
            argumentSet("no info", null, null, NewsError.Unknown("HTTP ?")),
        )
    }
}
