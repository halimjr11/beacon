package com.halimjr11.beacon.articleweb

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class WebUrlPolicyTest {

    @ParameterizedTest
    @MethodSource("resolveCases")
    fun resolve(input: String, expected: String?) {
        assertEquals(expected, WebUrlPolicy.resolve(input))
    }

    @Test
    fun `only http and https schemes are allowed`() {
        assertTrue(WebUrlPolicy.isAllowedScheme("https://a.com"))
        assertTrue(WebUrlPolicy.isAllowedScheme("HTTP://a.com"))
        assertFalse(WebUrlPolicy.isAllowedScheme("intent://x#Intent;end"))
        assertFalse(WebUrlPolicy.isAllowedScheme("file:///sdcard/a.html"))
        assertFalse(WebUrlPolicy.isAllowedScheme("javascript:alert(1)"))
        assertFalse(WebUrlPolicy.isAllowedScheme(""))
    }

    @Test
    fun `external keeps the original url`() {
        assertEquals("http://a.com/x", WebUrlPolicy.external(" http://a.com/x "))
        assertEquals(null, WebUrlPolicy.external("file:///a"))
    }

    companion object {
        @JvmStatic
        fun resolveCases(): Stream<Arguments> = Stream.of(
            argumentSet("https stays", "https://a.com/x?y=1", "https://a.com/x?y=1"),
            argumentSet("http is upgraded", "http://a.com/x", "https://a.com/x"),
            argumentSet("blank", "", null),
            argumentSet("file", "file:///a.html", null),
            argumentSet("intent", "intent://scan/#Intent;scheme=zxing;end", null),
            argumentSet("no host", "https://", null),
            argumentSet("not a url", "not a url", null),
        )
    }
}
