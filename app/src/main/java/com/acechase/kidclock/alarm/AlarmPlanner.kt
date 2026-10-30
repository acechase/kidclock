package com.acechase.kidclock.alarm

import java.util.TimeZone

/**
 * Pure scheduling decisions, with no Android dependencies so they can be unit tested.
 *
 * The model is borrowed from AOSP Clock's AlarmStateManager, simplified for a device that only
 * ever has a handful of alarms: exactly one AlarmManager registration exists at a time (the
 * earliest of the next regular alarm and a pending snooze), and we remember what we registered.
 * If that moment comes and goes without the receiver running, because the Portal was powered
 * off, the app was being reinstalled, or the clock was set forward, the alarm is "missed".
 * AOSP shows a notification for a missed alarm; nobody reads notifications on a kid's clock, so
 * we ring it late instead, as long as it is less than [MISSED_GRACE_MS] overdue.
 */
object AlarmPlanner {
    /** How long after its time an unfired alarm is still worth ringing. */
    const val MISSED_GRACE_MS = 30 * 60_000L

    data class NextEvent(val atMs: Long, val alarm: Alarm?, val isSnooze: Boolean)

    /** Everything the planner needs to know, as persisted in Prefs. */
    data class State(
        val alarms: List<Alarm>,
        val snoozeAtMs: Long = 0L,
        val snoozeAlarmId: Int = -1,
        val lastScheduledAtMs: Long = 0L,
        val lastScheduledAlarmId: Int = -1,
        val lastScheduledIsSnooze: Boolean = false,
    )

    sealed class Plan {
        /** The registered event was missed but is still within grace: ring it right now. */
        data class FireNow(val event: NextEvent) : Plan()
        /** Register this event with AlarmManager. */
        data class Schedule(val event: NextEvent) : Plan()
        /** Nothing to ring; cancel any registration. */
        object Idle : Plan()
    }

    /** Earliest upcoming event strictly after [afterMs], or null when nothing is enabled. */
    fun nextEvent(state: State, afterMs: Long, tz: TimeZone = TimeZone.getDefault()): NextEvent? {
        var best: NextEvent? = null
        for (a in state.alarms) {
            val t = a.nextTriggerMs(afterMs, tz) ?: continue
            if (best == null || t < best.atMs) best = NextEvent(t, a, false)
        }
        if (state.snoozeAtMs > afterMs && (best == null || state.snoozeAtMs < best.atMs)) {
            best = NextEvent(state.snoozeAtMs, state.alarms.firstOrNull { it.id == state.snoozeAlarmId }, true)
        }
        return best
    }

    /**
     * The event we last registered, if its time has passed without being consumed and it is
     * still within grace. The receiver clears [State.lastScheduledAtMs] when it runs, so a
     * non-zero value in the past means the fire was lost.
     */
    fun missedEvent(state: State, nowMs: Long, tz: TimeZone = TimeZone.getDefault()): NextEvent? {
        val at = state.lastScheduledAtMs
        if (at <= 0L || at > nowMs || nowMs - at > MISSED_GRACE_MS) return null
        if (state.lastScheduledIsSnooze) {
            // Only if the snooze is still pending (not cleared by a later dismiss).
            if (state.snoozeAtMs != at) return null
            return NextEvent(at, state.alarms.firstOrNull { it.id == state.snoozeAlarmId }, true)
        }
        val alarm = state.alarms.firstOrNull { it.id == state.lastScheduledAlarmId } ?: return null
        // The alarm must still be enabled and still due at that moment (it may have been edited).
        if (alarm.nextTriggerMs(at - 1, tz) != at) return null
        return NextEvent(at, alarm, false)
    }

    /**
     * Decide what to do at [nowMs]. [afterMs] is the reference for "next": normally now, but the
     * receiver passes the time of the alarm that just fired so that an alarm one minute later is
     * not skipped and the alarm that just rang is not re-registered for the same minute.
     */
    fun plan(state: State, nowMs: Long, afterMs: Long = nowMs, tz: TimeZone = TimeZone.getDefault()): Plan {
        missedEvent(state, nowMs, tz)?.let { return Plan.FireNow(it) }
        return nextEvent(state, afterMs, tz)?.let { Plan.Schedule(it) } ?: Plan.Idle
    }

    /** All alarms that ring at exactly [atMs]; two alarms set to the same minute ring as one. */
    fun dueAlarms(alarms: List<Alarm>, atMs: Long, tz: TimeZone = TimeZone.getDefault()): List<Alarm> =
        alarms.filter { it.nextTriggerMs(atMs - 1, tz) == atMs }

    /** A snooze so far in the past that it should be forgotten rather than rung. */
    fun isStaleSnooze(state: State, nowMs: Long): Boolean =
        state.snoozeAtMs != 0L && nowMs - state.snoozeAtMs > MISSED_GRACE_MS
}
