package com.acechase.kidclock.alarm

import java.util.Calendar
import java.util.TimeZone

/** Helpers for building wall-clock instants in a fixed zone. US Pacific: DST 2026 is Mar 8 .. Nov 1. */
object TestClock {
    val TZ: TimeZone = TimeZone.getTimeZone("America/Los_Angeles")

    fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int = 0, tz: TimeZone = TZ): Long =
        Calendar.getInstance(tz).apply {
            clear()
            set(year, month - 1, day, hour, minute, second)
        }.timeInMillis

    fun fmt(ms: Long, tz: TimeZone = TZ): String {
        val c = Calendar.getInstance(tz).apply { timeInMillis = ms }
        val dst = if (tz.inDaylightTime(c.time)) "DST" else "STD"
        return "%04d-%02d-%02d %02d:%02d:%02d %s".format(
            c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
            c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), dst,
        )
    }

    const val HOUR = 3_600_000L
    const val MINUTE = 60_000L
}
