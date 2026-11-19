package com.halimjr11.beacon.ui.theme

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class BeaconIconsTest {

    @ParameterizedTest
    @ValueSource(strings = ["business", "entertainment", "health", "science", "sports", "technology"])
    fun `each specific genre has its own icon, different from the general one`(apiValue: String) {
        assertNotEquals(BeaconIcons.category("general").name, BeaconIcons.category(apiValue).name)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "unknown", "TECHNOLOGY"])
    fun `an unknown genre falls back to the general icon`(apiValue: String) {
        assertSame(BeaconIcons.category("general").name, BeaconIcons.category(apiValue).name)
    }

    @org.junit.jupiter.api.Test
    fun `tab icons are three different glyphs`() {
        val names = listOf(BeaconIcons.Home, BeaconIcons.Genres, BeaconIcons.Sources).map { it.name }
        assertEquals(3, names.toSet().size)
    }
}
