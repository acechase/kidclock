package com.acechase.kidclock.alarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/** Every alarm sound must be a short, normalised phrase that ends in a rest so looping is clean. */
class AlarmSoundTest {
    private val rate = AlarmSoundPlayer.SAMPLE_RATE

    @Test
    fun everySoundIsAShortNormalisedLoopableClip() {
        for (s in AlarmSound.values()) {
            val pcm = s.synthesize()
            val seconds = pcm.size.toDouble() / rate
            assertTrue("${s.title} length $seconds s", seconds in 1.5..8.0)
            val peak = pcm.maxOf { abs(it.toInt()) }
            assertTrue("${s.title} peak $peak", peak in (0.70 * Short.MAX_VALUE).toInt()..Short.MAX_VALUE.toInt())
            val tail = pcm.takeLast(rate / 5).maxOf { abs(it.toInt()) }
            assertTrue("${s.title} tail $tail should be a rest so loops are seamless", tail < 0.03 * Short.MAX_VALUE)
            val head = pcm.take(rate / 4).maxOf { abs(it.toInt()) }
            assertTrue("${s.title} should sound within a quarter second", head > 0.2 * Short.MAX_VALUE)
        }
    }

    @Test
    fun idsRoundTripAndUnknownFallsBackToChime() {
        for (s in AlarmSound.values()) assertEquals(s, AlarmSound.byId(s.id))
        assertEquals(AlarmSound.CHIME, AlarmSound.byId("nope"))
        assertEquals(AlarmSound.CHIME, AlarmSound.byId(null))
        assertEquals(5, AlarmSound.values().size)
    }
}
