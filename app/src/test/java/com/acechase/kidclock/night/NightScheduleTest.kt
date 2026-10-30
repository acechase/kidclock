package com.acechase.kidclock.night

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NightScheduleTest {
    private val eightThirtyPm = 20 * 60 + 30
    private val sixThirtyAm = 6 * 60 + 30

    @Test
    fun windowCrossingMidnightCoversEveningAndEarlyMorningOnly() {
        assertTrue(NightSchedule.isNight(23 * 60, eightThirtyPm, sixThirtyAm, alwaysNight = false))
        assertTrue(NightSchedule.isNight(2 * 60, eightThirtyPm, sixThirtyAm, alwaysNight = false))
        assertTrue("the first minute of night counts", NightSchedule.isNight(eightThirtyPm, eightThirtyPm, sixThirtyAm, alwaysNight = false))
        assertFalse("the end minute is already day", NightSchedule.isNight(sixThirtyAm, eightThirtyPm, sixThirtyAm, alwaysNight = false))
        assertFalse(NightSchedule.isNight(13 * 60, eightThirtyPm, sixThirtyAm, alwaysNight = false))
    }

    @Test
    fun windowWithinOneDayWorksToo() {
        assertTrue(NightSchedule.isNight(14 * 60, 13 * 60, 15 * 60, alwaysNight = false))
        assertFalse(NightSchedule.isNight(16 * 60, 13 * 60, 15 * 60, alwaysNight = false))
    }

    @Test
    fun alwaysNightIsNightAtAnyHourWhateverTheWindow() {
        for (m in 0 until 24 * 60 step 15) {
            assertTrue("minute $m", NightSchedule.isNight(m, eightThirtyPm, sixThirtyAm, alwaysNight = true))
            assertTrue("minute $m, degenerate window", NightSchedule.isNight(m, 0, 0, alwaysNight = true))
        }
    }
}
