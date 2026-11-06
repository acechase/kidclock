package com.acechase.kidclock.looks

import com.acechase.kidclock.looks.LookPickerState.Event
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The hold-to-pick gesture: a second still on the time opens the picker, anything less does not. */
class LookPickerStateTest {
    private val s = LookPickerState(holdMs = 1000, idleMs = 8000, slopPx = 40f)

    private fun holdUntilOpen(t0: Long = 10_000L): Long {
        assertTrue(s.down(600f, 400f, onTime = true, now = t0))
        assertEquals(Event.ENTERED, s.advance(t0 + 1000))
        return t0 + 1000
    }

    @Test
    fun holdingTheTimeForASecondOpensThePicker() {
        assertTrue(s.down(600f, 400f, onTime = true, now = 1000))
        assertTrue(s.holding)
        assertNull("not yet", s.advance(1999))
        assertFalse(s.picking)
        assertEquals(Event.ENTERED, s.advance(2000))
        assertTrue(s.picking)
        assertFalse("the hold is over once the picker opens", s.holding)
    }

    @Test
    fun releasingEarlyCancelsTheHold() {
        s.down(600f, 400f, onTime = true, now = 1000)
        s.up()
        assertFalse(s.holding)
        assertNull(s.advance(5000))
        assertFalse(s.picking)
    }

    @Test
    fun driftingFingerCancelsButSmallWobbleDoesNot() {
        s.down(600f, 400f, onTime = true, now = 1000)
        assertFalse("a 20 px wobble is fine", s.move(615f, 412f))
        assertTrue(s.holding)
        assertTrue("60 px is a drag, not a hold", s.move(660f, 400f))
        assertFalse(s.holding)
        assertNull(s.advance(5000))
    }

    @Test
    fun touchesOffTheTimeNeverStartAHold() {
        assertFalse(s.down(50f, 50f, onTime = false, now = 1000))
        assertFalse(s.holding)
        assertNull(s.advance(9000))
        assertFalse(s.picking)
    }

    @Test
    fun holdProgressFillsFromZeroToOne() {
        assertEquals(0f, s.holdProgress(500), 0f)
        s.down(600f, 400f, onTime = true, now = 1000)
        assertEquals(0f, s.holdProgress(1000), 0.001f)
        assertEquals(0.5f, s.holdProgress(1500), 0.001f)
        assertEquals(1f, s.holdProgress(9000), 0.001f)
    }

    @Test
    fun swipesStepThroughLooksAndATapKeepsTheCurrentOne() {
        assertNull("swipes change the background when not picking", s.swipe(-300f, 500))
        val t = holdUntilOpen()
        assertEquals(1, s.swipe(-300f, t + 100))
        assertEquals(-1, s.swipe(300f, t + 200))
        assertTrue(s.tap())
        assertFalse(s.picking)
        assertFalse("a second tap is an ordinary tap again", s.tap())
    }

    @Test
    fun pickerClosesByItselfAfterEightIdleSeconds() {
        val t = holdUntilOpen()
        assertNull(s.advance(t + 7999))
        assertTrue(s.picking)
        assertEquals(Event.EXITED, s.advance(t + 8000))
        assertFalse(s.picking)
    }

    @Test
    fun anyTouchWhilePickingRestartsTheIdleClock() {
        val t = holdUntilOpen()
        s.swipe(-300f, t + 7000)
        assertNull("swipe at 7 s means no exit at 8 s", s.advance(t + 8000))
        s.down(100f, 100f, onTime = false, now = t + 14_000)
        assertNull(s.advance(t + 15_000))
        assertEquals(Event.EXITED, s.advance(t + 22_000))
    }

    @Test
    fun noNewHoldStartsWhilePicking() {
        val t = holdUntilOpen()
        assertFalse(s.down(600f, 400f, onTime = true, now = t + 100))
        assertFalse(s.holding)
        assertTrue(s.picking)
    }

    @Test
    fun resetForgetsEverything() {
        holdUntilOpen()
        s.reset()
        assertFalse(s.picking)
        assertFalse(s.holding)
    }

    @Test
    fun aSuggestionTakesTheFirstSwipeEitherWayThenSwipesStepFromThere() {
        val t = holdUntilOpen()
        s.suggest(15)
        assertEquals(15, s.suggestion)
        assertEquals("the first swipe lands on the suggestion", 15, s.nextLook(current = 0, step = s.swipe(300f, t + 100)!!))
        assertNull(s.suggestion)
        assertEquals("then it steps as usual", 16, s.nextLook(current = 15, step = s.swipe(-300f, t + 200)!!))
        assertEquals(15, s.nextLook(current = 16, step = -1))
    }

    @Test
    fun withoutASuggestionSwipesStepAndWrap() {
        holdUntilOpen()
        assertEquals(1, s.nextLook(0, 1))
        assertEquals(17, s.nextLook(0, -1))
        assertEquals(0, s.nextLook(17, 1))
    }

    @Test
    fun aSuggestionOnlySticksWhilePickingAndIsForgottenWhenThePickerCloses() {
        s.suggest(15)
        assertNull("not picking yet", s.suggestion)
        val t = holdUntilOpen()
        s.suggest(15)
        assertTrue(s.tap())
        assertNull("a tap keeps the current look and drops the hint", s.suggestion)

        holdUntilOpen(t + 1000)
        s.suggest(15)
        assertEquals(Event.EXITED, s.advance(t + 1000 + 2000 + 8000))
        assertNull("so does the idle timeout", s.suggestion)

        holdUntilOpen(t + 20_000)
        s.suggest(15)
        s.reset()
        assertNull(s.suggestion)
    }
}
