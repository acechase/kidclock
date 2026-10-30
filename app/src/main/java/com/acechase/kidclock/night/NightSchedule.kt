package com.acechase.kidclock.night

import com.acechase.kidclock.Prefs
import java.util.Calendar

/** The sleep window and the okay-to-wake state derived from it. */
class NightSchedule(private val prefs: Prefs) {
    enum class Phase { DAY, NIGHT, WAKE_OK }

    private fun minuteOfDay(now: Calendar) = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

    /** Inside the configured night window. This is what dims the screen. */
    fun isScheduledNight(now: Calendar = Calendar.getInstance()): Boolean =
        inWindow(minuteOfDay(now), prefs.nightStartMin, prefs.nightEndMin)

    /**
     * Night colours (amber on black): the night window, or all day when "Always night" is on.
     * Always night changes only the colours; brightness still follows [isScheduledNight].
     */
    fun isNight(now: Calendar = Calendar.getInstance()): Boolean = prefs.alwaysNight || isScheduledNight(now)

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

    companion object {
        /** Whether [minuteOfDay] falls in the window from [startMin] until [endMin], which may cross midnight. Pure, unit-tested. */
        fun inWindow(minuteOfDay: Int, startMin: Int, endMin: Int): Boolean =
            if (startMin <= endMin) minuteOfDay in startMin until endMin
            else minuteOfDay >= startMin || minuteOfDay < endMin

        /** Night colours: the window, or always. */
        fun isNight(minuteOfDay: Int, startMin: Int, endMin: Int, alwaysNight: Boolean): Boolean =
            alwaysNight || inWindow(minuteOfDay, startMin, endMin)
    }
}
