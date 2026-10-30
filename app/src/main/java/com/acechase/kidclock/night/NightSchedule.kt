package com.acechase.kidclock.night

import com.acechase.kidclock.Prefs
import java.util.Calendar

/** The sleep window and the okay-to-wake state derived from it. */
class NightSchedule(private val prefs: Prefs) {
    enum class Phase { DAY, NIGHT, WAKE_OK }

    private fun minuteOfDay(now: Calendar) = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

    fun isNight(now: Calendar = Calendar.getInstance()): Boolean {
        val m = minuteOfDay(now)
        val s = prefs.nightStartMin
        val e = prefs.nightEndMin
        return if (s <= e) m in s until e else (m >= s || m < e)
    }

    /** WAKE_OK shows a green light for 90 minutes after the night ends. */
    /** True while a gentle wake-up is in progress or after "I'm awake" until the cancelled alarm's time. */
    fun inWakeWindow(nowMs: Long = System.currentTimeMillis()): Boolean = isPending(prefs.wakeUntilMs, nowMs) || isPending(prefs.awakeUntilMs, nowMs)

    /** Gentle wake-up in progress with the main alarm still armed. */
    fun inGentleWake(nowMs: Long = System.currentTimeMillis()): Boolean = isPending(prefs.wakeUntilMs, nowMs)

    private fun isPending(until: Long, nowMs: Long) = until > nowMs && until - nowMs < 3 * 60 * 60_000L

    fun phase(now: Calendar = Calendar.getInstance()): Phase {
        if (inWakeWindow(now.timeInMillis)) return Phase.WAKE_OK
        if (isNight(now)) return Phase.NIGHT
        val sinceEnd = ((minuteOfDay(now) - prefs.nightEndMin) + 24 * 60) % (24 * 60)
        return if (sinceEnd < 90) Phase.WAKE_OK else Phase.DAY
    }
}
