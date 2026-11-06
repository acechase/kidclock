package com.acechase.kidclock.looks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class LookCatalogTest {
    @Test
    fun eighteenDistinctLooksAndStorybookStaysOut() {
        assertEquals(18, LookCatalog.count)
        assertEquals(LookCatalog.names.size, LookCatalog.names.toSet().size)
        assertEquals("Classic", LookCatalog.names[LookCatalog.CLASSIC])
        assertEquals("Analog", LookCatalog.names[LookCatalog.ANALOG])
        assertFalse("dropped from the build on 2026-10-30", LookCatalog.names.contains("Storybook"))
    }

    @Test
    fun indexWrapsBothWays() {
        assertEquals(0, LookCatalog.wrap(18))
        assertEquals(17, LookCatalog.wrap(-1))
        assertEquals(3, LookCatalog.wrap(21))
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

    @Test
    fun eachDrawnForBackgroundNamesItsPartnerLookAndTheSkiesHaveNone() {
        // Background indices follow faces/ClockFace.create; look indices follow names.
        assertEquals("Tiara", LookCatalog.names[LookCatalog.partnerLook(7)!!])          // Doodle Strings
        assertEquals("Marker Stacked", LookCatalog.names[LookCatalog.partnerLook(8)!!]) // Notebook
        assertEquals("Jersey", LookCatalog.names[LookCatalog.partnerLook(9)!!])         // Soccer Board
        assertEquals("Scoreboard", LookCatalog.names[LookCatalog.partnerLook(10)!!])    // Stadium
        assertEquals("Beach", LookCatalog.names[LookCatalog.partnerLook(11)!!])         // Crab Tank
        assertEquals("Bubble", LookCatalog.names[LookCatalog.partnerLook(12)!!])        // Night Reef
        assertEquals("Plush", LookCatalog.names[LookCatalog.partnerLook(13)!!])         // Stuffies
        assertEquals("Marker Blue", LookCatalog.names[LookCatalog.partnerLook(14)!!])   // Doodle Notebook
        assertEquals("Stencil", LookCatalog.names[LookCatalog.partnerLook(15)!!])       // Blueprint
        for (sky in 0..6) assertNull("painted skies suit any look", LookCatalog.partnerLook(sky))
    }

    @Test
    fun theSuggestionIsThePartnerUnlessItIsAlreadyShowing() {
        assertEquals(15, LookCatalog.suggestedLook(faceIndex = 13, lookIndex = LookCatalog.CLASSIC))
        assertNull("Plush is already on", LookCatalog.suggestedLook(faceIndex = 13, lookIndex = 15))
        assertNull("Sky has no partner", LookCatalog.suggestedLook(faceIndex = 0, lookIndex = LookCatalog.CLASSIC))
    }
}
