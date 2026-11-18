package com.halimjr11.beacon.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NewsExceptionTest {
    @Test
    fun `NewsException unwraps to its error`() {
        assertEquals(NewsError.RateLimited, NewsException(NewsError.RateLimited).toNewsError())
    }

    @Test
    fun `other throwable becomes Unknown`() {
        assertEquals(NewsError.Unknown("boom"), IllegalStateException("boom").toNewsError())
    }
}
