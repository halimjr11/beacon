package com.halimjr11.beacon.data.cache

/** Injectable time source so TTL can be tested. */
fun interface Clock {
    fun now(): Long

    companion object {
        val System = Clock { java.lang.System.currentTimeMillis() }
    }
}
