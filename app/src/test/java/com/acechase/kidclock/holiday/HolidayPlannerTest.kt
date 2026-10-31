package com.acechase.kidclock.holiday

import com.acechase.kidclock.holiday.HolidayPlanner.Action
import com.acechase.kidclock.holiday.HolidayPlanner.State
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HolidayPlannerTest {
    private val halloween = Holiday.HALLOWEEN
    private val thanksgiving = Holiday.THANKSGIVING

    @Test
    fun holidayAppliesWhenItsWindowOpensAndRestoresWhenItCloses() {
        val start = HolidayPlanner.step(State(), halloween)
        assertEquals(Action.APPLY, start.action)
        assertEquals("halloween", start.state.activeId)

        val during = HolidayPlanner.step(start.state, halloween)
        assertEquals(Action.NONE, during.action)
        assertEquals("halloween", during.state.activeId)

        val after = HolidayPlanner.step(during.state, null)
        assertEquals(Action.RESTORE, after.action)
        assertNull(after.state.activeId)

        assertEquals("nothing happens on an ordinary day", Action.NONE, HolidayPlanner.step(after.state, null).action)
    }

    @Test
    fun changingTheDesignDuringTheWindowDismissesTheHolidayWithNoSwitchBack() {
        val showing = HolidayPlanner.step(State(), halloween).state
        val chosen = HolidayPlanner.userChanged(showing)
        assertNull(chosen.activeId)
        assertEquals("halloween", chosen.dismissedId)

        // The rest of the window leaves the child's choice alone...
        val later = HolidayPlanner.step(chosen, halloween)
        assertEquals(Action.NONE, later.action)
        assertNull(later.state.activeId)
        // ...and the day after there is nothing to restore.
        val after = HolidayPlanner.step(later.state, null)
        assertEquals(Action.NONE, after.action)
        assertNull("the dismissal is forgotten once the window has passed", after.state.dismissedId)
    }

    @Test
    fun aDismissalIsForOneHolidayOnly() {
        val dismissed = HolidayPlanner.userChanged(HolidayPlanner.step(State(), halloween).state)
        val next = HolidayPlanner.step(dismissed, thanksgiving)
        assertEquals(Action.APPLY, next.action)
        assertEquals("thanksgiving", next.state.activeId)
        assertNull(next.state.dismissedId)
    }

    @Test
    fun userChangeOutsideAHolidayChangesNothing() {
        val s = State(dismissedId = null)
        assertEquals(s, HolidayPlanner.userChanged(s))
    }

    @Test
    fun turningTheFeatureOffRestoresAtOnceAndKeepsItOff() {
        val showing = HolidayPlanner.step(State(), halloween).state
        val off = HolidayPlanner.step(showing.copy(enabled = false), halloween)
        assertEquals(Action.RESTORE, off.action)
        assertNull(off.state.activeId)
        assertEquals(Action.NONE, HolidayPlanner.step(off.state, halloween).action)
    }

    @Test
    fun turningItBackOnMidWindowAppliesAgain() {
        val off = State(enabled = false)
        val on = HolidayPlanner.step(off.copy(enabled = true), halloween)
        assertEquals(Action.APPLY, on.action)
    }
}
