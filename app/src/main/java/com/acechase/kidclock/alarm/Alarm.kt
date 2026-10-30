package com.acechase.kidclock.alarm

import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.TimeZone

/**
 * One alarm. [days] holds Calendar.DAY_OF_WEEK values (1 = Sunday .. 7 = Saturday).
 * An empty set means "once": the alarm disables itself after ringing.
 */
data class Alarm(
    val id: Int,
    var hour: Int,
    var minute: Int,
    var days: MutableSet<Int> = mutableSetOf(),
    var enabled: Boolean = true,
    var label: String = "",
    /** Minutes before [hour]:[minute] to play the gentle wake-up and light the screen. 0 = off. */
    var preMinutes: Int = 0,
) {
    /**
     * Next time this alarm should ring strictly after [afterMs], or null when disabled.
     *
     * Wall-clock arithmetic, the same way AOSP Clock does it: pick the alarm's hour/minute on the
     * day of [afterMs], step forward whole days until a selected weekday, then pin hour/minute
     * again because stepping across a DST change can shift them. A time that does not exist on
     * a spring-forward day (e.g. 02:30) resolves one hour later, which is what Calendar does.
     */
    fun nextTriggerMs(afterMs: Long, tz: TimeZone = TimeZone.getDefault()): Long? {
        if (!enabled) return null
        val cal = Calendar.getInstance(tz).apply {
            timeInMillis = afterMs
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= afterMs) cal.add(Calendar.DAY_OF_YEAR, 1)
        repeat(8) {
            if (days.isEmpty() || cal.get(Calendar.DAY_OF_WEEK) in days) {
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                return cal.timeInMillis
            }
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return null
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id); put("hour", hour); put("minute", minute)
        put("days", JSONArray(days.sorted())); put("enabled", enabled); put("label", label)
        put("pre", preMinutes)
    }

    companion object {
        const val TEST_LABEL = "__test__"
        val PRE_CHOICES = intArrayOf(0, 5, 10, 15, 20, 30, 45, 60)
        val WEEKDAYS = mutableSetOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY)

        fun fromJson(o: JSONObject): Alarm {
            val d = o.optJSONArray("days") ?: JSONArray()
            val days = mutableSetOf<Int>()
            for (i in 0 until d.length()) days += d.getInt(i)
            return Alarm(
                id = o.getInt("id"), hour = o.getInt("hour"), minute = o.getInt("minute"),
                days = days, enabled = o.optBoolean("enabled", true), label = o.optString("label", ""),
                preMinutes = o.optInt("pre", 0),
            )
        }
    }
}
