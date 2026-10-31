package com.acechase.kidclock.holiday

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class HolidayCalendarTest {
    @Test
    fun easterMatchesKnownYears() {
        assertEquals(LocalDate.of(2024, 3, 31), HolidayCalendar.easter(2024))
        assertEquals(LocalDate.of(2025, 4, 20), HolidayCalendar.easter(2025))
        assertEquals(LocalDate.of(2026, 4, 5), HolidayCalendar.easter(2026))
        assertEquals(LocalDate.of(2027, 3, 28), HolidayCalendar.easter(2027))
        assertEquals(LocalDate.of(2038, 4, 25), HolidayCalendar.easter(2038)) // the latest possible Easter
    }

    @Test
    fun thanksgivingIsTheFourthThursdayOfNovember() {
        assertEquals(LocalDate.of(2026, 11, 26), HolidayCalendar.dateOf(Holiday.THANKSGIVING, 2026))
        assertEquals(LocalDate.of(2027, 11, 25), HolidayCalendar.dateOf(Holiday.THANKSGIVING, 2027))
        assertEquals(LocalDate.of(2029, 11, 22), HolidayCalendar.dateOf(Holiday.THANKSGIVING, 2029))
    }

    @Test
    fun windowOpensThreeDaysBeforeAndClosesTheDayAfter() {
        assertNull(HolidayCalendar.activeOn(LocalDate.of(2026, 10, 27)))
        assertEquals(Holiday.HALLOWEEN, HolidayCalendar.activeOn(LocalDate.of(2026, 10, 28)))
        assertEquals(Holiday.HALLOWEEN, HolidayCalendar.activeOn(LocalDate.of(2026, 10, 31)))
        assertNull(HolidayCalendar.activeOn(LocalDate.of(2026, 11, 1)))
    }

    @Test
    fun decemberHolidaysDoNotOverlapAndNewYearsEveEndsOnNewYearsDay() {
        assertEquals(Holiday.CHRISTMAS, HolidayCalendar.activeOn(LocalDate.of(2026, 12, 22)))
        assertEquals(Holiday.CHRISTMAS, HolidayCalendar.activeOn(LocalDate.of(2026, 12, 25)))
        assertNull(HolidayCalendar.activeOn(LocalDate.of(2026, 12, 27)))
        assertEquals(Holiday.NEW_YEARS_EVE, HolidayCalendar.activeOn(LocalDate.of(2026, 12, 28)))
        assertEquals(Holiday.NEW_YEARS_EVE, HolidayCalendar.activeOn(LocalDate.of(2026, 12, 31)))
        assertNull(HolidayCalendar.activeOn(LocalDate.of(2027, 1, 1)))
    }

    @Test
    fun everyHolidayHasAWindowInAnOrdinaryYear() {
        for (h in Holiday.values()) {
            val day = HolidayCalendar.dateOf(h, 2026)
            assertEquals(h, HolidayCalendar.activeOn(day))
            assertEquals(h, HolidayCalendar.activeOn(day.minusDays(3)))
            assertNull("$h should be over the day after", HolidayCalendar.activeOn(day.plusDays(1)))
        }
    }

    @Test
    fun idsRoundTrip() {
        for (h in Holiday.values()) assertEquals(h, Holiday.byId(h.id))
        assertNull(Holiday.byId(null))
        assertNull(Holiday.byId("arbor-day"))
    }
}
