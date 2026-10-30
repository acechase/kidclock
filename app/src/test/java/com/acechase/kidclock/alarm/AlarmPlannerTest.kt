package com.acechase.kidclock.alarm

import com.acechase.kidclock.alarm.AlarmPlanner.MISSED_GRACE_MS
import com.acechase.kidclock.alarm.AlarmPlanner.Plan
import com.acechase.kidclock.alarm.AlarmPlanner.State
import com.acechase.kidclock.alarm.TestClock.HOUR
import com.acechase.kidclock.alarm.TestClock.MINUTE
import com.acechase.kidclock.alarm.TestClock.TZ
import com.acechase.kidclock.alarm.TestClock.at
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Scenarios from the AOSP Clock / BlackyHawky Clock alarm state machine, reduced to the one
 * registration this app keeps. "Thursday" below is 2026-10-29, a school day.
 */
class AlarmPlannerTest {
    private val seven = Alarm(1, 7, 0, Alarm.WEEKDAYS.toMutableSet(), label = "School")
    private val sevenOhOne = Alarm(2, 7, 1, Alarm.WEEKDAYS.toMutableSet())
    private val onceAtSeven = Alarm(3, 7, 0) // one-time
    private val day = 29

    private fun t(h: Int, m: Int, s: Int = 0, ms: Long = 0) = at(2026, 10, day, h, m, s) + ms

    private fun plan(state: State, now: Long, after: Long = now) = AlarmPlanner.plan(state, now, after, TZ)

    // --- picking the next event -------------------------------------------------------------

    @Test fun `earliest alarm wins`() {
        val p = plan(State(listOf(sevenOhOne, seven)), t(6, 0))
        p as Plan.Schedule
        assertEquals(t(7, 0), p.event.atMs)
        assertEquals(seven, p.event.alarm)
        assertFalse(p.event.isSnooze)
    }

    @Test fun `pending snooze beats a later alarm and carries its alarm`() {
        val state = State(listOf(seven), snoozeAtMs = t(6, 30), snoozeAlarmId = 1)
        val p = plan(state, t(6, 25)) as Plan.Schedule
        assertEquals(t(6, 30), p.event.atMs)
        assertTrue(p.event.isSnooze)
        assertEquals(seven, p.event.alarm)
    }

    @Test fun `snooze in the past is ignored for scheduling`() {
        val state = State(listOf(seven), snoozeAtMs = t(5, 0), snoozeAlarmId = 1)
        val p = plan(state, t(6, 0)) as Plan.Schedule
        assertFalse(p.event.isSnooze)
        assertEquals(t(7, 0), p.event.atMs)
    }

    @Test fun `nothing enabled is idle`() {
        assertEquals(Plan.Idle, plan(State(listOf(seven.copy(enabled = false))), t(6, 0)))
        assertEquals(Plan.Idle, plan(State(emptyList()), t(6, 0)))
    }

    // --- what happens right after a fire ------------------------------------------------------

    @Test fun `after 7-00 fires, 7-01 is next today (regression for the old plus-one-minute lookahead)`() {
        // Receiver runs half a second late and looks strictly after the fired time.
        val firedAt = t(7, 0)
        val now = t(7, 0, 0, 500)
        val p = plan(State(listOf(seven, sevenOhOne)), now, after = maxOf(firedAt, now)) as Plan.Schedule
        assertEquals(t(7, 1), p.event.atMs)
        assertEquals(sevenOhOne, p.event.alarm)
    }

    @Test fun `after firing, the same alarm is not re-registered for the same minute`() {
        val p = plan(State(listOf(seven)), t(7, 0), after = t(7, 0)) as Plan.Schedule
        assertEquals(at(2026, 10, 30, 7, 0), p.event.atMs) // Friday
    }

    @Test fun `two alarms set to the same minute are both due`() {
        val due = AlarmPlanner.dueAlarms(listOf(seven, onceAtSeven, sevenOhOne), t(7, 0), TZ)
        assertEquals(setOf(1, 3), due.map { it.id }.toSet())
    }

    @Test fun `an alarm is not due at a time it does not ring`() {
        assertTrue(AlarmPlanner.dueAlarms(listOf(seven), at(2026, 10, 31, 7, 0), TZ).isEmpty()) // Saturday
        assertTrue(AlarmPlanner.dueAlarms(listOf(seven), t(7, 0, 30), TZ).isEmpty())
    }

    // --- missed alarms: powered off, reinstalled, or clock set forward ----------------------

    private fun registered(vararg alarms: Alarm, atMs: Long, id: Int) =
        State(alarms.toList(), lastScheduledAtMs = atMs, lastScheduledAlarmId = id)

    @Test fun `alarm missed by a few minutes rings now`() {
        val p = plan(registered(seven, atMs = t(7, 0), id = 1), now = t(7, 4))
        p as Plan.FireNow
        assertEquals(t(7, 0), p.event.atMs)
        assertEquals(seven, p.event.alarm)
    }

    @Test fun `missed alarm at the edge of grace still rings`() {
        val p = plan(registered(seven, atMs = t(7, 0), id = 1), now = t(7, 0) + MISSED_GRACE_MS)
        assertTrue(p is Plan.FireNow)
    }

    @Test fun `alarm missed by more than the grace period is skipped, next one scheduled`() {
        val p = plan(registered(seven, atMs = t(7, 0), id = 1), now = t(7, 0) + MISSED_GRACE_MS + 1)
        p as Plan.Schedule
        assertEquals(at(2026, 10, 30, 7, 0), p.event.atMs)
    }

    @Test fun `device powered off overnight and back on at 7-10 rings`() {
        // Registered Wednesday night for Thursday 7:00; boot at 7:10 Thursday.
        val p = plan(registered(seven, atMs = t(7, 0), id = 1), now = t(7, 10))
        assertTrue(p is Plan.FireNow)
    }

    @Test fun `clock set forward past the alarm rings`() {
        // It was 6:50, the alarm was registered for 7:00, then TIME_SET lands us at 7:20.
        val p = plan(registered(seven, atMs = t(7, 0), id = 1), now = t(7, 20))
        assertTrue(p is Plan.FireNow)
    }

    @Test fun `clock set backward before the alarm just reschedules it`() {
        val p = plan(registered(seven, atMs = t(7, 0), id = 1), now = t(6, 0))
        p as Plan.Schedule
        assertEquals(t(7, 0), p.event.atMs)
    }

    @Test fun `registration that was consumed (cleared) is not fired again`() {
        val p = plan(State(listOf(seven), lastScheduledAtMs = 0L), now = t(7, 4))
        assertTrue(p is Plan.Schedule)
    }

    @Test fun `missed alarm that has since been disabled or deleted is not fired`() {
        val disabled = plan(registered(seven.copy(enabled = false), atMs = t(7, 0), id = 1), now = t(7, 4))
        assertEquals(Plan.Idle, disabled)
        val deleted = plan(registered(sevenOhOne, atMs = t(7, 0), id = 1), now = t(7, 4))
        assertTrue(deleted is Plan.Schedule)
    }

    @Test fun `missed alarm that was edited to another time is not fired`() {
        val moved = seven.copy(hour = 8)
        val p = plan(registered(moved, atMs = t(7, 0), id = 1), now = t(7, 4)) as Plan.Schedule
        assertEquals(t(8, 0), p.event.atMs)
    }

    @Test fun `missed snooze still pending rings as a snooze`() {
        val state = State(
            listOf(seven), snoozeAtMs = t(7, 9), snoozeAlarmId = 1,
            lastScheduledAtMs = t(7, 9), lastScheduledAlarmId = 1, lastScheduledIsSnooze = true,
        )
        val p = plan(state, now = t(7, 12)) as Plan.FireNow
        assertTrue(p.event.isSnooze)
        assertEquals(seven, p.event.alarm)
    }

    @Test fun `missed snooze that was dismissed meanwhile does not ring`() {
        val state = State(
            listOf(seven), snoozeAtMs = 0L, snoozeAlarmId = -1,
            lastScheduledAtMs = t(7, 9), lastScheduledAlarmId = 1, lastScheduledIsSnooze = true,
        )
        assertTrue(plan(state, now = t(7, 12)) is Plan.Schedule)
    }

    @Test fun `missed test alarm (no alarm id) rings`() {
        val state = State(emptyList(), snoozeAtMs = t(7, 9), snoozeAlarmId = -1,
            lastScheduledAtMs = t(7, 9), lastScheduledAlarmId = -1, lastScheduledIsSnooze = true)
        val p = plan(state, now = t(7, 10)) as Plan.FireNow
        assertNull(p.event.alarm)
        assertTrue(p.event.isSnooze)
    }

    @Test fun `stale snooze is flagged for clearing`() {
        val fresh = State(listOf(seven), snoozeAtMs = t(7, 9), snoozeAlarmId = 1)
        assertFalse(AlarmPlanner.isStaleSnooze(fresh, t(7, 20)))
        assertTrue(AlarmPlanner.isStaleSnooze(fresh, t(7, 9) + MISSED_GRACE_MS + 1))
        assertFalse(AlarmPlanner.isStaleSnooze(State(listOf(seven)), t(12, 0)))
    }

    // --- DST through the planner --------------------------------------------------------------

    @Test fun `daily alarm across fall back is registered 13 real hours after Saturday 7pm`() {
        val satEvening = at(2026, 10, 31, 19, 0) // 12 wall-clock hours to 07:00, plus the repeated hour
        val p = plan(State(listOf(Alarm(1, 7, 0))), satEvening) as Plan.Schedule
        assertEquals(13 * HOUR, p.event.atMs - satEvening)
    }

    @Test fun `snooze is real elapsed time, not wall clock, across fall back`() {
        // Snooze 9 minutes at 01:55 PDT: fires at 01:04 PST, 9 real minutes later.
        val snoozedAt = at(2026, 11, 1, 1, 55) // first 01:55, DST
        val state = State(emptyList(), snoozeAtMs = snoozedAt + 9 * MINUTE, snoozeAlarmId = -1)
        val p = plan(state, snoozedAt) as Plan.Schedule
        assertEquals(9 * MINUTE, p.event.atMs - snoozedAt)
    }
}
