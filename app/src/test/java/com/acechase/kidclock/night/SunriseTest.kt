package com.acechase.kidclock.night

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SunriseTest {
    private val start = 1_000_000L
    private val end = start + 20 * 60_000L

    @Test
    fun progressRunsFromTheStartOfTheWakeUpToTheAlarm() {
        assertEquals(0f, Sunrise.progress(start - 5_000L, start, end), 0f)
        assertEquals(0f, Sunrise.progress(start, start, end), 0f)
        assertEquals(0.25f, Sunrise.progress(start + 5 * 60_000L, start, end), 1e-6f)
        assertEquals(1f, Sunrise.progress(end, start, end), 0f)
        assertEquals(1f, Sunrise.progress(end + 60_000L, start, end), 0f)
    }

    @Test
    fun aMissingStartIsTakenAsTwentyMinutesBeforeTheAlarm() {
        val tenBefore = end - 10 * 60_000L
        assertEquals(0.5f, Sunrise.progress(tenBefore, 0L, end), 1e-6f)
        assertEquals(0.5f, Sunrise.progress(tenBefore, end + 1, end), 1e-6f)   // start after end: nonsense
        assertEquals(0.5f, Sunrise.progress(tenBefore, end, end), 1e-6f)
    }

    @Test
    fun brightnessStartsDimAndEndsFullGrowingFasterTowardsTheEnd() {
        assertEquals(Sunrise.MIN_BRIGHTNESS, Sunrise.brightness(0f), 0f)
        assertEquals(1f, Sunrise.brightness(1f), 1e-6f)
        val firstHalf = Sunrise.brightness(0.5f) - Sunrise.brightness(0f)
        val secondHalf = Sunrise.brightness(1f) - Sunrise.brightness(0.5f)
        assertTrue(firstHalf > 0f && secondHalf > firstHalf)
        assertEquals(Sunrise.MIN_BRIGHTNESS, Sunrise.brightness(-1f), 0f)
        assertEquals(1f, Sunrise.brightness(7f), 1e-6f)
    }

    @Test
    fun coloursMixChannelByChannel() {
        assertEquals(0xFF000000.toInt(), Sunrise.lerpColor(0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0f))
        assertEquals(0xFFFFFFFF.toInt(), Sunrise.lerpColor(0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 1f))
        assertEquals(0xFF808080.toInt(), Sunrise.lerpColor(0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0.5f))
        assertEquals(0x80FF0080.toInt(), Sunrise.lerpColor(0x00FF0000, 0xFFFF00FF.toInt(), 0.5f))
        assertEquals(0xFFFFFFFF.toInt(), Sunrise.lerpColor(0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 3f))
    }

    @Test
    fun keyframesHitTheirColoursExactlyAndBlendBetween() {
        for ((i, k) in Sunrise.KEYS.withIndex()) assertEquals(Sunrise.SKY_TOP[i], Sunrise.keyColor(Sunrise.SKY_TOP, k))
        val ramp = intArrayOf(0xFF000000.toInt(), 0xFF000000.toInt(), 0xFF000000.toInt(), 0xFF000000.toInt(), 0xFFFFFFFF.toInt())
        assertColorNear(0xFF808080.toInt(), Sunrise.keyColor(ramp, 0.9f))
        assertEquals(0xFFFFFFFF.toInt(), Sunrise.keyColor(ramp, 2f))
        assertEquals(0.15f, Sunrise.keyValue(Sunrise.GLOW_STRENGTH, 0f), 0f)
        assertEquals(0.625f, Sunrise.keyValue(Sunrise.GLOW_STRENGTH, 0.425f), 1e-6f)
        assertEquals(0.18f, Sunrise.keyValue(Sunrise.GLOW_STRENGTH, 1f), 0f)
    }

    /** Channel by channel within one step: float arithmetic lands a half-way mix on 7F or 80. */
    private fun assertColorNear(expected: Int, actual: Int) {
        for (shift in listOf(24, 16, 8, 0)) {
            val e = (expected ushr shift) and 0xFF; val a = (actual ushr shift) and 0xFF
            assertTrue("channel $shift: expected $e, was $a", Math.abs(e - a) <= 1)
        }
    }

    @Test
    fun everyRampHasOneColourPerKeyframe() {
        for (ramp in listOf(Sunrise.SKY_TOP, Sunrise.SKY_MID, Sunrise.SKY_LOW, Sunrise.GLOW, Sunrise.HILL_FAR, Sunrise.HILL_NEAR, Sunrise.CLOUD, Sunrise.SUN))
            assertEquals(Sunrise.KEYS.size, ramp.size)
        assertEquals(Sunrise.KEYS.size, Sunrise.GLOW_STRENGTH.size)
    }

    @Test
    fun theSunStarsAndBirdsKeepToTheirPartsOfTheMorning() {
        assertEquals(0f, Sunrise.sunHeight(0f), 0f)
        assertEquals(1f, Sunrise.sunHeight(1f), 1e-6f)
        assertTrue(Sunrise.sunHeight(0.3f) < 0.3f)      // slow to show
        assertEquals(1f, Sunrise.starAlpha(0f), 0f)
        assertEquals(0f, Sunrise.starAlpha(0.45f), 0f)
        assertEquals(0f, Sunrise.starAlpha(0.9f), 0f)
        assertEquals(0f, Sunrise.birdAlpha(0.5f), 0f)
        assertEquals(1f, Sunrise.birdAlpha(0.9f), 1e-6f)
    }
}
