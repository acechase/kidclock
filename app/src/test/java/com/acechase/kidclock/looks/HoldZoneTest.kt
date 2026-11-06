package com.acechase.kidclock.looks

import com.acechase.kidclock.looks.HoldZone.Box
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The look-picker hold starts anywhere in the time/date section, which is the whole middle minus other targets. */
class HoldZoneTest {
    private val w = 1280f
    private val h = 800f
    private val time = Box(300f, 250f, 980f, 480f)
    private val weather = Box(896f, 0f, 1280f, 192f)
    private val alarm = Box(0f, 704f, 922f, 800f)
    private val quote = Box(80f, 560f, 800f, 680f)
    private val none = Box(0f, 0f, 0f, 0f)
    private val corner = 180f

    private fun hit(x: Float, y: Float, blocked: List<Box> = listOf(weather, alarm, quote)) =
        HoldZone.contains(x, y, w, h, time, blocked, corner)

    @Test
    fun theTimeItselfAndTheDateUnderItBothCount() {
        assertTrue(hit(640f, 400f))
        assertTrue("the date line, below the time box", hit(640f, 520f))
        assertTrue("well to the side of the digits", hit(60f, 400f))
    }

    @Test
    fun theOtherTapTargetsAreLeftAlone() {
        assertFalse("settings corner", hit(100f, 100f))
        assertFalse("weather", hit(1200f, 100f))
        assertFalse("next-alarm line", hit(400f, 750f))
        assertFalse("quote", hit(400f, 600f))
    }

    @Test
    fun theTopChromeAndBottomStripAreOut() {
        assertFalse(hit(640f, 40f))
        assertFalse(hit(640f, 790f))
        assertFalse("off the screen", hit(-5f, 400f))
    }

    @Test
    fun aTimeBoxOutsideTheBandStillCounts() {
        val tall = Box(100f, 40f, 600f, 760f)
        assertTrue(HoldZone.contains(300f, 60f, w, h, tall, listOf(none, none, none), corner))
    }

    @Test
    fun emptyBoxesBlockNothing() {
        assertTrue(hit(1200f, 100f, blocked = listOf(none, none, none)))
        assertTrue("an inverted box is empty too", hit(400f, 600f, blocked = listOf(none, none, Box(500f, 500f, 100f, 100f))))
    }
}
