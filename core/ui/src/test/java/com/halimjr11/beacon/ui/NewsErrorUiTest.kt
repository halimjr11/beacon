package com.halimjr11.beacon.ui

import com.halimjr11.beacon.common.NewsError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class NewsErrorUiTest {

    @ParameterizedTest
    @MethodSource("cases")
    fun mapsMessageAndRetry(error: NewsError, message: Int, retry: Boolean) {
        assertEquals(message, error.messageRes())
        assertEquals(retry, error.canRetry())
    }

    companion object {
        @JvmStatic
        fun cases(): Stream<Arguments> = Stream.of(
            argumentSet("offline", NewsError.Offline, R.string.error_offline, true),
            argumentSet("api key", NewsError.InvalidApiKey, R.string.error_api_key, false),
            argumentSet("rate limited", NewsError.RateLimited, R.string.error_rate_limited, false),
            argumentSet("source gone", NewsError.SourceNotFound, R.string.error_source_not_found, false),
            argumentSet("bad request", NewsError.BadRequest("x"), R.string.error_bad_request, true),
            argumentSet("server", NewsError.Server, R.string.error_server, true),
            argumentSet("parse", NewsError.Parse, R.string.error_parse, true),
            argumentSet("unknown", NewsError.Unknown("x"), R.string.error_unknown, true),
        )
    }
}
