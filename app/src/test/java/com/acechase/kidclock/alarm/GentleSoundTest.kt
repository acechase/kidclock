package com.acechase.kidclock.alarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/** Every gentle sound must be a sane, loopable clip: a few seconds long, normalised, and ending in near-silence. */
class GentleSoundTest {
    private val rate = AlarmSoundPlayer.SAMPLE_RATE

    @Test
    fun everySoundIsAShortNormalisedClipThatEndsQuietly() {
        for (s in GentleSound.values()) {
            val pcm = s.synthesize()
            val seconds = pcm.size.toDouble() / rate
            assertTrue("${s.title} length $seconds s", seconds in 6.0..16.0)
            val peak = pcm.maxOf { abs(it.toInt()) }
            assertTrue("${s.title} peak $peak", peak in (0.80 * Short.MAX_VALUE).toInt()..Short.MAX_VALUE.toInt())
            val tail = pcm.takeLast(rate / 4).maxOf { abs(it.toInt()) }
            assertTrue("${s.title} tail $tail should be near silent so loops are seamless", tail < 0.03 * Short.MAX_VALUE)
            val head = pcm.take(rate / 2).maxOf { abs(it.toInt()) }
            assertTrue("${s.title} should start making sound within half a second", head > 0.05 * Short.MAX_VALUE)
        }
    }

    @Test
    fun idsRoundTripAndUnknownFallsBackToHarp() {
        for (s in GentleSound.values()) assertEquals(s, GentleSound.byId(s.id))
        assertEquals(GentleSound.HARP, GentleSound.byId("nope"))
        assertEquals(GentleSound.HARP, GentleSound.byId(null))
        assertEquals(5, GentleSound.values().size)
    }
}
