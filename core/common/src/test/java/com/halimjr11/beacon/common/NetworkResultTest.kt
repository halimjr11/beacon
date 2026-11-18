package com.halimjr11.beacon.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NetworkResultTest {
    @Test
    fun `map transforms success`() {
        assertEquals(NetworkResult.Success(4), NetworkResult.Success(2).map { it * 2 })
    }

    @Test
    fun `map keeps error`() {
        val error = NetworkResult.Error(NewsError.Offline)
        assertEquals(error, (error as NetworkResult<Int>).map { it * 2 })
    }
}
