package com.acechase.kidclock.looks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class LookCatalogTest {
    @Test
    fun elevenDistinctLooksAndStorybookStaysOut() {
        assertEquals(11, LookCatalog.count)
        assertEquals(LookCatalog.names.size, LookCatalog.names.toSet().size)
        assertEquals("Classic", LookCatalog.names[LookCatalog.CLASSIC])
        assertEquals("Analog", LookCatalog.names[LookCatalog.ANALOG])
        assertFalse("dropped from the build on 2026-10-30", LookCatalog.names.contains("Storybook"))
    }

    @Test
    fun indexWrapsBothWays() {
        assertEquals(0, LookCatalog.wrap(11))
        assertEquals(10, LookCatalog.wrap(-1))
        assertEquals(3, LookCatalog.wrap(14))
    }

    @Test
    fun oldAnalogBackgroundBecomesTheAnalogLook() {
        // There were seven painted backgrounds then; index 7 used to be the Analog face. Later
        // backgrounds (Doodle Strings, Notebook) must not change that, so the count is a constant.
        assertEquals(7, LookCatalog.BACKGROUNDS_BEFORE_LOOKS)
        assertEquals(LookCatalog.ANALOG, LookCatalog.migrateAnalogBackground(7, LookCatalog.BACKGROUNDS_BEFORE_LOOKS))
        assertNull(LookCatalog.migrateAnalogBackground(0, 7))
        assertNull(LookCatalog.migrateAnalogBackground(6, 7))
    }
}
