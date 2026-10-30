package com.acechase.kidclock.alarm

import com.acechase.kidclock.alarm.TestClock.HOUR
import com.acechase.kidclock.alarm.TestClock.TZ
import com.acechase.kidclock.alarm.TestClock.at
import com.acechase.kidclock.alarm.TestClock.fmt
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AlarmTest {
    private fun daily(h: Int, m: Int) = Alarm(1, h, m)
    private fun weekdays(h: Int, m: Int) = Alarm(1, h, m, Alarm.WEEKDAYS.toMutableSet())

    @Test fun `rings later today when the time has not passed`() {
        val now = at(2026, 10, 30, 6, 0) // Friday
        assertEquals(at(2026, 10, 30, 7, 0), daily(7, 0).nextTriggerMs(now, TZ))
    }

    @Test fun `rings tomorrow when the time has passed`() {
        val now = at(2026, 10, 30, 7, 0, 1)
        assertEquals(at(2026, 10, 31, 7, 0), daily(7, 0).nextTriggerMs(now, TZ))
    }

    @Test fun `exactly now counts as passed so a fired alarm is not re-registered`() {
        val now = at(2026, 10, 30, 7, 0)
        assertEquals(at(2026, 10, 31, 7, 0), daily(7, 0).nextTriggerMs(now, TZ))
    }

    @Test fun `disabled alarm never rings`() {
        assertNull(daily(7, 0).apply { enabled = false }.nextTriggerMs(at(2026, 10, 30, 6, 0), TZ))
    }

    @Test fun `weekday alarm skips the weekend`() {
        val fridayEvening = at(2026, 10, 30, 20, 0)
        assertEquals(at(2026, 11, 2, 7, 0), weekdays(7, 0).nextTriggerMs(fridayEvening, TZ)) // Monday
        val saturday = at(2026, 10, 31, 12, 0)
        assertEquals(at(2026, 11, 2, 7, 0), weekdays(7, 0).nextTriggerMs(saturday, TZ))
    }

    @Test fun `single-day alarm wraps a full week`() {
        val sundayOnly = Alarm(1, 9, 0, mutableSetOf(Calendar.SUNDAY))
        val sundayAfter = at(2026, 10, 25, 9, 0, 1)
        assertEquals(at(2026, 11, 1, 9, 0), sundayOnly.nextTriggerMs(sundayAfter, TZ))
    }

    @Test fun `spring forward keeps the wall-clock time (one hour less real time)`() {
        val saturdayNight = at(2026, 3, 7, 20, 0) // PST
        val next = daily(7, 0).nextTriggerMs(saturdayNight, TZ)!!
        assertEquals(fmt(at(2026, 3, 8, 7, 0)), fmt(next))
        assertEquals("only 10 real hours pass", 10 * HOUR, next - saturdayNight)
    }

    @Test fun `fall back keeps the wall-clock time (one hour more real time)`() {
        val saturdayNight = at(2026, 10, 31, 20, 0) // PDT
        val next = daily(7, 0).nextTriggerMs(saturdayNight, TZ)!!
        assertEquals(fmt(at(2026, 11, 1, 7, 0)), fmt(next))
        assertEquals("12 real hours pass", 12 * HOUR, next - saturdayNight)
    }

    @Test fun `weekday alarm stepping across a DST change keeps its hour`() {
        // Friday Mar 6 evening -> Monday Mar 9, with the change on Sunday in between.
        val next = weekdays(6, 45).nextTriggerMs(at(2026, 3, 6, 20, 0), TZ)!!
        assertEquals("2026-03-09 06:45:00 DST", fmt(next))
    }

    @Test fun `alarm in the spring-forward gap rings an hour later, once, then returns to normal`() {
        val a = daily(2, 30)
        val first = a.nextTriggerMs(at(2026, 3, 8, 0, 0), TZ)!!
        assertEquals("02:30 does not exist; Calendar resolves it to 03:30 PDT", "2026-03-08 03:30:00 DST", fmt(first))
        val second = a.nextTriggerMs(first, TZ)!!
        assertEquals("2026-03-09 02:30:00 DST", fmt(second))
        assertTrue("must not ring twice on the change day", second - first > 20 * HOUR)
    }

    @Test fun `alarm in the fall-back repeated hour rings only once`() {
        // 01:30 happens twice on Nov 1. Calendar resolves an ambiguous wall time to standard
        // time, so the alarm rings at the second 01:30 (PST), 2.5 real hours after midnight.
        val a = daily(1, 30)
        val midnight = at(2026, 11, 1, 0, 0)
        val first = a.nextTriggerMs(midnight, TZ)!!
        assertEquals("2026-11-01 01:30:00 STD", fmt(first))
        assertEquals(2 * HOUR + HOUR / 2, first - midnight)
        val second = a.nextTriggerMs(first, TZ)!!
        assertTrue("next ring is ${fmt(second)}; must be the next day, not the second 01:30", second - first > 20 * HOUR)
    }

    @Test fun `json round trip`() {
        val a = Alarm(7, 6, 15, mutableSetOf(Calendar.MONDAY, Calendar.FRIDAY), enabled = false, label = "School")
        val back = Alarm.fromJson(JSONObject(a.toJson().toString()))
        assertEquals(a, back)
    }

    @Test fun `json without optional fields uses defaults`() {
        val back = Alarm.fromJson(JSONObject("""{"id":3,"hour":7,"minute":0}"""))
        assertEquals(Alarm(3, 7, 0), back)
    }
}
