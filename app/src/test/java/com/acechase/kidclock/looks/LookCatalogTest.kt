package com.acechase.kidclock.looks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class LookCatalogTest {
    @Test
    fun twentyFourDistinctLooksAndStorybookStaysOut() {
        assertEquals(24, LookCatalog.count)
        assertEquals(LookCatalog.names.size, LookCatalog.names.toSet().size)
        assertEquals("Classic", LookCatalog.names[LookCatalog.CLASSIC])
        assertEquals("Analog", LookCatalog.names[LookCatalog.ANALOG])
        assertFalse("dropped from the build on 2026-10-30", LookCatalog.names.contains("Storybook"))
    }

    @Test
    fun indexWrapsBothWays() {
        assertEquals(0, LookCatalog.wrap(24))
        assertEquals(23, LookCatalog.wrap(-1))
        assertEquals(3, LookCatalog.wrap(27))
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
    fun eachDrawnForBackgroundNamesItsPartnerLookAndOnlySkyHasNone() {
        // Background indices follow faces/ClockFace.create; look indices follow names.
        assertEquals("Tide", LookCatalog.names[LookCatalog.partnerLook(1)!!])           // Ocean
        assertEquals("Rainbow Arch", LookCatalog.names[LookCatalog.partnerLook(2)!!])        // Rainbow
        assertEquals("Nebula", LookCatalog.names[LookCatalog.partnerLook(3)!!])         // Galaxy
        assertEquals("Glow", LookCatalog.names[LookCatalog.partnerLook(4)!!])           // Fireflies
        assertEquals("Borealis", LookCatalog.names[LookCatalog.partnerLook(5)!!])       // Aurora
        assertEquals("Daisy", LookCatalog.names[LookCatalog.partnerLook(6)!!])          // Meadow
        assertEquals("Tiara", LookCatalog.names[LookCatalog.partnerLook(7)!!])          // Doodle Strings
        assertEquals("Marker Stacked", LookCatalog.names[LookCatalog.partnerLook(8)!!]) // Notebook
        assertEquals("Jersey", LookCatalog.names[LookCatalog.partnerLook(9)!!])         // Soccer Board
        assertEquals("Scoreboard", LookCatalog.names[LookCatalog.partnerLook(10)!!])    // Stadium
        assertEquals("Beach", LookCatalog.names[LookCatalog.partnerLook(11)!!])         // Crab Tank
        assertEquals("Bubble", LookCatalog.names[LookCatalog.partnerLook(12)!!])        // Night Reef
        assertEquals("Plush", LookCatalog.names[LookCatalog.partnerLook(13)!!])         // Stuffies
        assertEquals("Marker Blue", LookCatalog.names[LookCatalog.partnerLook(14)!!])   // Doodle Notebook
        assertEquals("Stencil", LookCatalog.names[LookCatalog.partnerLook(15)!!])       // Blueprint
        assertNull("Sky follows the time of day and suits any look", LookCatalog.partnerLook(0))
    }

    @Test
    fun aSwipeToABackgroundBringsItsPartnerLookAndTheSkiesBringBackTheChosenOne() {
        // Tiara chosen on Sky, then swiping through Soccer Board, Stadium and back to Ocean.
        val tiara = 8
        assertEquals("Jersey", LookCatalog.names[LookCatalog.lookForBackground(faceIndex = 9, chosenLook = tiara)])
        assertEquals("Scoreboard", LookCatalog.names[LookCatalog.lookForBackground(faceIndex = 10, chosenLook = tiara)])
        assertEquals("Tiara", LookCatalog.names[LookCatalog.lookForBackground(faceIndex = 0, chosenLook = tiara)])
        assertEquals("Tide", LookCatalog.names[LookCatalog.lookForBackground(faceIndex = 1, chosenLook = tiara)])
        assertEquals(LookCatalog.CLASSIC, LookCatalog.lookForBackground(0, LookCatalog.CLASSIC))
        assertEquals("the partner wins even over a chosen themed look", 15, LookCatalog.lookForBackground(faceIndex = 13, chosenLook = 12))
    }
}
