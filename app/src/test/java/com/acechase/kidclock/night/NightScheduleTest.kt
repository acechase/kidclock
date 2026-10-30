package com.acechase.kidclock.night

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NightScheduleTest {
    private val eightThirtyPm = 20 * 60 + 30
    private val sixThirtyAm = 6 * 60 + 30

    @Test
    fun windowCrossingMidnightCoversEveningAndEarlyMorningOnly() {
        assertTrue(NightSchedule.inWindow(23 * 60, eightThirtyPm, sixThirtyAm))
        assertTrue(NightSchedule.inWindow(2 * 60, eightThirtyPm, sixThirtyAm))
        assertTrue("the first minute of night counts", NightSchedule.inWindow(eightThirtyPm, eightThirtyPm, sixThirtyAm))
        assertFalse("the end minute is already day", NightSchedule.inWindow(sixThirtyAm, eightThirtyPm, sixThirtyAm))
        assertFalse(NightSchedule.inWindow(13 * 60, eightThirtyPm, sixThirtyAm))
    }

    @Test
    fun windowWithinOneDayWorksToo() {
        assertTrue(NightSchedule.inWindow(14 * 60, 13 * 60, 15 * 60))
        assertFalse(NightSchedule.inWindow(16 * 60, 13 * 60, 15 * 60))
    }

    @Test
    fun alwaysNightGivesNightColoursAtAnyHourWhateverTheWindow() {
        for (m in 0 until 24 * 60 step 15) {
            assertTrue("minute $m", NightSchedule.isNight(m, eightThirtyPm, sixThirtyAm, alwaysNight = true))
            assertTrue("minute $m, degenerate window", NightSchedule.isNight(m, 0, 0, alwaysNight = true))
        }
        assertFalse(NightSchedule.isNight(13 * 60, eightThirtyPm, sixThirtyAm, alwaysNight = false))
    }

    @Test
    fun alwaysNightLeavesTheWindowAloneSoDaytimeStaysBright() {
        // Brightness follows inWindow only: at 1 pm with Always night on, the colours are amber but
        // the screen is not dimmed.
        assertTrue(NightSchedule.isNight(13 * 60, eightThirtyPm, sixThirtyAm, alwaysNight = true))
        assertFalse(NightSchedule.inWindow(13 * 60, eightThirtyPm, sixThirtyAm))
        // And in the real window it dims as usual.
        assertTrue(NightSchedule.inWindow(23 * 60, eightThirtyPm, sixThirtyAm))
    }
}
