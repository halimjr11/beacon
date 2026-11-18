package com.halimjr11.beacon.common

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

class NewsFormattersTest {

    @ParameterizedTest
    @MethodSource("cases")
    fun formatPublishedAt(input: String?, expected: String) {
        assertEquals(expected, formatPublishedAt(input))
    }

    companion object {
        @JvmStatic
        fun cases(): Stream<Arguments> = Stream.of(
            argumentSet("iso utc", "2026-07-24T10:15:30Z", "24 Jul 2026"),
            argumentSet("date only", "2026-01-05", "05 Jan 2026"),
            argumentSet("leap day", "2024-02-29T00:00:00Z", "29 Feb 2024"),
            argumentSet("null", null, ""),
            argumentSet("blank", "  ", ""),
            argumentSet("garbage", "yesterday", "yesterday"),
            argumentSet("month 13", "2026-13-01T00:00:00Z", "2026-13-01T00:00:00Z"),
            argumentSet("feb 30", "2026-02-30T00:00:00Z", "2026-02-30T00:00:00Z"),
            argumentSet("non leap feb 29", "2025-02-29", "2025-02-29"),
        )
    }
}
