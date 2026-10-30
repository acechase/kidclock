package com.acechase.kidclock.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.acechase.kidclock.ClockActivity
import com.acechase.kidclock.Prefs

/**
 * Keeps exactly one AlarmManager alarm registered: the earliest of the next main alarm, the next
 * gentle pre-alarm, and any pending snooze. setAlarmClock() is exempt from Doze and shows up as
 * the system "next alarm", which is the most reliable path on API 28.
 *
 * Missed alarms (see [AlarmPlanner]): what we registered is remembered in Prefs and cleared by the
 * receiver when it runs. If [rescheduleAll] finds that moment already in the past, still within
 * [AlarmPlanner.MISSED_GRACE_MS] and still valid, it fires it immediately instead of silently
 * dropping it, which covers power cuts, reinstalls and clock jumps.
 */
object AlarmScheduler {
    private const val TAG = "AlarmScheduler"
    const val EXTRA_ALARM_ID = "alarm_id"
    const val EXTRA_IS_SNOOZE = "is_snooze"
    const val EXTRA_IS_PRE = "is_pre"
    const val EXTRA_MAIN_AT = "main_at"
    const val EXTRA_LABEL = "label"
    private const val REQ_FIRE = 1001
    private const val REQ_SHOW = 1002

    /** [atMs] is when this event fires; [mainAtMs] is the main alarm time (same unless [isPre]). */
    data class NextEvent(val atMs: Long, val alarm: Alarm?, val isSnooze: Boolean, val isPre: Boolean = false, val mainAtMs: Long = atMs)

    /** The alarm's next main trigger after [afterMs], honouring a one-time "I'm awake" skip. */
    private fun nextMainTrigger(a: Alarm, afterMs: Long, prefs: Prefs): Long? {
        var t = a.nextTriggerMs(afterMs) ?: return null
        if (a.id == prefs.skipAlarmId && t == prefs.skipAtMs) t = a.nextTriggerMs(t) ?: return null
        return t
    }

    fun nextEvent(context: Context, afterMs: Long = System.currentTimeMillis()): NextEvent? {
        val prefs = Prefs(context)
        val store = AlarmStore(context)
        var best: NextEvent? = null
        fun offer(e: NextEvent) { if (best == null || e.atMs < best!!.atMs) best = e }
        for (a in store.all()) {
            val t = nextMainTrigger(a, afterMs, prefs) ?: continue
            offer(NextEvent(t, a, isSnooze = false))
            if (a.preMinutes > 0) {
                val pre = t - a.preMinutes * 60_000L
                if (pre > afterMs) offer(NextEvent(pre, a, isSnooze = false, isPre = true, mainAtMs = t))
            }
        }
        val snoozeAt = prefs.snoozeAtMs
        if (snoozeAt > afterMs) offer(NextEvent(snoozeAt, store.get(prefs.snoozeAlarmId), isSnooze = true))
        return best
    }

    /** The next *main* alarm (ignoring pre-alarms), for the clock face's "next alarm" line. */
    fun nextMainEvent(context: Context): NextEvent? {
        val prefs = Prefs(context)
        val store = AlarmStore(context)
        val now = System.currentTimeMillis()
        var best: NextEvent? = null
        for (a in store.all()) {
            val t = nextMainTrigger(a, now, prefs) ?: continue
            if (best == null || t < best.atMs) best = NextEvent(t, a, false)
        }
        val snoozeAt = prefs.snoozeAtMs
        if (snoozeAt > now && (best == null || snoozeAt < best.atMs)) best = NextEvent(snoozeAt, store.get(prefs.snoozeAlarmId), true)
        return best
    }

    /** The event we last registered if its time passed unconsumed, is within grace, and is still valid. */
    private fun missedEvent(context: Context, nowMs: Long): NextEvent? {
        val prefs = Prefs(context)
        val at = prefs.lastScheduledAtMs
        if (at <= 0L || at > nowMs || nowMs - at > AlarmPlanner.MISSED_GRACE_MS) return null
        val store = AlarmStore(context)
        if (prefs.lastScheduledIsSnooze) {
            if (prefs.snoozeAtMs != at) return null
            return NextEvent(at, store.get(prefs.snoozeAlarmId), isSnooze = true)
        }
        val alarm = store.get(prefs.lastScheduledAlarmId) ?: return null
        if (prefs.lastScheduledIsPre) {
            val mainAt = at + alarm.preMinutes * 60_000L
            if (alarm.preMinutes <= 0 || nextMainTrigger(alarm, at - 1, prefs) != mainAt || mainAt <= nowMs) return null
            return NextEvent(at, alarm, isSnooze = false, isPre = true, mainAtMs = mainAt)
        }
        if (nextMainTrigger(alarm, at - 1, prefs) != at) return null
        return NextEvent(at, alarm, isSnooze = false)
    }

    fun rescheduleAll(context: Context, afterMs: Long = System.currentTimeMillis()) {
        val now = System.currentTimeMillis()
        missedEvent(context, now)?.let { missed ->
            Log.w(TAG, "missed ${describe(missed)} at ${java.util.Date(missed.atMs)}; firing now")
            Prefs(context).lastScheduledAtMs = 0L
            context.sendBroadcast(fireIntent(context, missed))
            return
        }
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val prefs = Prefs(context)
        val next = nextEvent(context, afterMs)
        if (next == null) {
            am.cancel(firePendingIntent(context, null))
            prefs.lastScheduledAtMs = 0L
            Log.i(TAG, "no alarms scheduled")
            return
        }
        val op = firePendingIntent(context, next)
        val show = PendingIntent.getActivity(
            context, REQ_SHOW, Intent(context, ClockActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        am.setAlarmClock(AlarmManager.AlarmClockInfo(next.atMs, show), op)
        prefs.lastScheduledAtMs = next.atMs
        prefs.lastScheduledAlarmId = next.alarm?.id ?: -1
        prefs.lastScheduledIsSnooze = next.isSnooze
        prefs.lastScheduledIsPre = next.isPre
        Log.i(TAG, "scheduled ${describe(next)} at ${java.util.Date(next.atMs)}")
    }

    private fun describe(e: NextEvent) = when { e.isSnooze -> "snooze"; e.isPre -> "gentle pre-alarm ${e.alarm?.id}"; else -> "alarm ${e.alarm?.id}" }

    fun snooze(context: Context, alarmId: Int, minutes: Int) {
        val prefs = Prefs(context)
        prefs.snoozeAtMs = System.currentTimeMillis() + minutes * 60_000L
        prefs.snoozeAlarmId = alarmId
        rescheduleAll(context)
    }

    fun clearSnooze(context: Context) {
        val prefs = Prefs(context)
        prefs.snoozeAtMs = 0L
        prefs.snoozeAlarmId = -1
    }

    /**
     * "I'm awake" during a gentle wake-up: cancel today's main alarm for the alarm that opened the
     * window, keep the bright okay-to-wake display until that time, and release the window.
     */
    fun markAwake(context: Context) {
        val prefs = Prefs(context)
        val mainAt = prefs.wakeUntilMs
        if (mainAt <= 0L) return
        val store = AlarmStore(context)
        store.get(prefs.wakeAlarmId)?.let { a ->
            if (a.days.isEmpty()) {
                if (a.label == Alarm.TEST_LABEL) store.delete(a.id) else { a.enabled = false; store.upsert(a) }
            } else {
                prefs.skipAlarmId = a.id
                prefs.skipAtMs = mainAt
            }
        }
        prefs.awakeUntilMs = mainAt
        prefs.wakeUntilMs = 0L
        prefs.wakeAlarmId = -1
        Log.i(TAG, "awake: alarm at ${java.util.Date(mainAt)} cancelled for today")
        rescheduleAll(context)
    }

    /** Fire a test alarm shortly, without touching the saved alarms. */
    fun scheduleTest(context: Context, inSeconds: Int) {
        val prefs = Prefs(context)
        prefs.snoozeAtMs = System.currentTimeMillis() + inSeconds * 1000L
        prefs.snoozeAlarmId = -1
        rescheduleAll(context)
    }

    /** One-shot test: gentle wake-up in ~1 minute, main alarm ~1 minute after that. Deletes itself. */
    fun scheduleGentleTest(context: Context) {
        val store = AlarmStore(context)
        store.all().filter { it.label == Alarm.TEST_LABEL }.forEach { store.delete(it.id) }
        val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.MINUTE, 2); add(java.util.Calendar.SECOND, 5) }
        val a = store.newAlarm(cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE)).apply {
            days.clear(); label = Alarm.TEST_LABEL; preMinutes = 1
        }
        store.upsert(a)
        rescheduleAll(context)
    }

    private fun fireIntent(context: Context, next: NextEvent?): Intent =
        Intent(context, AlarmReceiver::class.java).apply {
            action = "com.acechase.kidclock.FIRE"
            if (next != null) {
                putExtra(EXTRA_ALARM_ID, next.alarm?.id ?: -1)
                putExtra(EXTRA_IS_SNOOZE, next.isSnooze)
                putExtra(EXTRA_IS_PRE, next.isPre)
                putExtra(EXTRA_MAIN_AT, next.mainAtMs)
                putExtra(EXTRA_LABEL, next.alarm?.label ?: "")
            }
        }

    private fun firePendingIntent(context: Context, next: NextEvent?): PendingIntent =
        PendingIntent.getBroadcast(
            context, REQ_FIRE, fireIntent(context, next), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
