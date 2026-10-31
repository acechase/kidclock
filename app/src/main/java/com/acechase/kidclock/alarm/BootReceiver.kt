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
 *   -a com.acechase.kidclock.TEST_NIGHT  --ez on true         flip the "Always night" setting
 *   -a com.acechase.kidclock.TEST_HOLIDAY --es id halloween    pretend today is in that holiday's
 *                                                              window ("off" to stop pretending)
 *   -a com.acechase.kidclock.TEST_DESIGN --ei face 8 --ei look 9   show that background and look
 *                                                              (-1 keeps one as it is; dismisses a holiday)
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("BootReceiver", "action=${intent.action}")
        if (intent.action == ACTION_TEST_ALARM) {
            AlarmScheduler.scheduleTest(context, intent.getIntExtra("seconds", 10).coerceIn(1, 3600))
            return
        }
        if (intent.action == ACTION_TEST_NIGHT) {
            Prefs(context).alwaysNight = intent.getBooleanExtra("on", true)
            return
        }
        if (intent.action == ACTION_TEST_DESIGN) {
            Prefs(context).apply {
                intent.getIntExtra("face", -1).takeIf { it >= 0 }?.let { faceIndex = it }
                intent.getIntExtra("look", -1).takeIf { it >= 0 }?.let { lookIndex = it }
                holidayActive?.let { holidayDismissed = it; holidayActive = null }
            }
            return
        }
        if (intent.action == ACTION_TEST_HOLIDAY) {
            val id = intent.getStringExtra("id")
            Prefs(context).apply {
                holidayForce = id?.takeUnless { it == "off" || it == "none" }
                holidayActive = null
                holidayDismissed = null
            }
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

    companion object {
        const val ACTION_TEST_ALARM = "com.acechase.kidclock.TEST_ALARM"
        const val ACTION_TEST_MISSED = "com.acechase.kidclock.TEST_MISSED"
        const val ACTION_TEST_NIGHT = "com.acechase.kidclock.TEST_NIGHT"
        const val ACTION_TEST_HOLIDAY = "com.acechase.kidclock.TEST_HOLIDAY"
        const val ACTION_TEST_DESIGN = "com.acechase.kidclock.TEST_DESIGN"
    }
}
