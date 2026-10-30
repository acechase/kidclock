package com.acechase.kidclock.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.acechase.kidclock.ClockActivity
import com.acechase.kidclock.Prefs

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
        val isSnooze = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_SNOOZE, false)
        val isPre = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_PRE, false)
        val mainAt = intent.getLongExtra(AlarmScheduler.EXTRA_MAIN_AT, 0L)
        val label = intent.getStringExtra(AlarmScheduler.EXTRA_LABEL) ?: ""
        Log.i("AlarmReceiver", "fire alarm=$alarmId snooze=$isSnooze pre=$isPre")

        // Keep the CPU awake long enough for the next screen to come up.
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "kidclock:fire").acquire(15_000L)

        val prefs = Prefs(context)
        prefs.lastScheduledAtMs = 0L   // consumed; a stale value would mean a missed alarm (AlarmPlanner)
        if (isPre) {
            // Gentle wake-up: open the wake window until the main alarm and let the clock play the soft chime.
            prefs.wakeUntilMs = mainAt
            prefs.wakeAlarmId = alarmId
            prefs.awakeUntilMs = 0L
            AlarmScheduler.rescheduleAll(context, System.currentTimeMillis() + 30_000L)
            context.startActivity(Intent(context, ClockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(ClockActivity.EXTRA_GENTLE_WAKE, true)
            })
            return
        }

        if (isSnooze) AlarmScheduler.clearSnooze(context)
        prefs.wakeUntilMs = 0L
        prefs.wakeAlarmId = -1
        val store = AlarmStore(context)
        store.get(alarmId)?.let { a ->
            if (a.days.isEmpty() && !isSnooze) {
                if (a.label == Alarm.TEST_LABEL) store.delete(a.id) else { a.enabled = false; store.upsert(a) }
            }
        }
        // Look slightly ahead so the alarm that just fired is not re-registered for "now".
        AlarmScheduler.rescheduleAll(context, System.currentTimeMillis() + 30_000L)

        context.startActivity(Intent(context, RingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_LABEL, if (label == Alarm.TEST_LABEL) "" else label)
        })
    }
}
