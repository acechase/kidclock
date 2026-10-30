package com.acechase.kidclock.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.acechase.kidclock.Prefs

/**
 * Re-registers the next alarm after boot, time changes, or an app update. Any lost alarm still
 * within grace rings immediately (see AlarmPlanner.missedEvent).
 *
 * Also answers developer hooks for testing from adb (see tools/test-alarm.sh, tools/test-night.sh):
 *   -a com.acechase.kidclock.TEST_ALARM  --ei seconds 20      ring a test alarm in 20 s
 *   -a com.acechase.kidclock.TEST_MISSED --ei minutes_ago 5   pretend a snooze registered 5 min
 *                                                              ago was lost (as if the device had
 *                                                              been off), then reschedule
 *   -a com.acechase.kidclock.TEST_NIGHT  --ez on true         force the night window to all day
 *                                                              (on false restores the saved window)
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("BootReceiver", "action=${intent.action}")
        if (intent.action == ACTION_TEST_ALARM) {
            AlarmScheduler.scheduleTest(context, intent.getIntExtra("seconds", 10).coerceIn(1, 3600))
            return
        }
        if (intent.action == ACTION_TEST_NIGHT) {
            forceNight(Prefs(context), intent.getBooleanExtra("on", true))
            return
        }
        if (intent.action == ACTION_TEST_MISSED) {
            val at = System.currentTimeMillis() - intent.getIntExtra("minutes_ago", 5).coerceIn(0, 600) * 60_000L
            Prefs(context).apply {
                snoozeAtMs = at; snoozeAlarmId = -1
                lastScheduledAtMs = at; lastScheduledAlarmId = -1; lastScheduledIsSnooze = true
            }
        }
        AlarmScheduler.rescheduleAll(context)
    }

    /** Night all day long for checking the amber looks, remembering the real window to put back. */
    private fun forceNight(prefs: Prefs, on: Boolean) {
        if (on) {
            if (prefs.testNightBackupStart < 0) {
                prefs.testNightBackupStart = prefs.nightStartMin
                prefs.testNightBackupEnd = prefs.nightEndMin
            }
            prefs.nightStartMin = 0
            prefs.nightEndMin = 24 * 60
        } else if (prefs.testNightBackupStart >= 0) {
            prefs.nightStartMin = prefs.testNightBackupStart
            prefs.nightEndMin = prefs.testNightBackupEnd
            prefs.testNightBackupStart = -1
            prefs.testNightBackupEnd = -1
        }
    }

    companion object {
        const val ACTION_TEST_ALARM = "com.acechase.kidclock.TEST_ALARM"
        const val ACTION_TEST_MISSED = "com.acechase.kidclock.TEST_MISSED"
        const val ACTION_TEST_NIGHT = "com.acechase.kidclock.TEST_NIGHT"
    }
}
