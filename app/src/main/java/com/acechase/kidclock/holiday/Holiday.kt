package com.acechase.kidclock.holiday

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * The holidays that get a special background and look. Each one switches on [HolidayCalendar.LEAD_DAYS]
 * before the day and off the day after (see HolidayPlanner). Pure Kotlin so it is unit-testable;
 * the drawing lives in HolidayDesigns.
 */
enum class Holiday(val id: String, val title: String, val lookName: String) {
    NEW_YEARS_EVE("newyear", "New Year's Eve", "Midnight"),
    VALENTINES("valentine", "Valentine's Day", "Rose"),
    ST_PATRICKS("stpatrick", "St. Patrick's Day", "Clover"),
    EASTER("easter", "Easter", "Pastel Eggs"),
    FOURTH_OF_JULY("july4", "Fourth of July", "Fireworks"),
    HALLOWEEN("halloween", "Halloween", "Witching Hour"),
    THANKSGIVING("thanksgiving", "Thanksgiving", "Harvest"),
    CHRISTMAS("christmas", "Christmas", "Fairy Lights");

    companion object {
        fun byId(id: String?): Holiday? = values().firstOrNull { it.id == id }
    }
}

/** Which day each holiday falls on, and which holiday (if any) a date belongs to. */
object HolidayCalendar {
    /** The special design appears this many days before the holiday itself. */
    const val LEAD_DAYS = 3L

    fun dateOf(holiday: Holiday, year: Int): LocalDate = when (holiday) {
        Holiday.NEW_YEARS_EVE -> LocalDate.of(year, 12, 31)
        Holiday.VALENTINES -> LocalDate.of(year, 2, 14)
        Holiday.ST_PATRICKS -> LocalDate.of(year, 3, 17)
        Holiday.EASTER -> easter(year)
        Holiday.FOURTH_OF_JULY -> LocalDate.of(year, 7, 4)
        Holiday.HALLOWEEN -> LocalDate.of(year, 10, 31)
        Holiday.THANKSGIVING -> LocalDate.of(year, 11, 1).with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY))
        Holiday.CHRISTMAS -> LocalDate.of(year, 12, 25)
    }

    /** Western (Gregorian) Easter Sunday, by the anonymous algorithm. */
    fun easter(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = (h + l - 7 * m + 114) % 31 + 1
        return LocalDate.of(year, month, day)
    }

    /** The holiday whose window ([LEAD_DAYS] before, through the day itself) contains [date], or null. */
    fun activeOn(date: LocalDate): Holiday? {
        // A window can start in the previous year only in theory (none does), but checking both years is cheap.
        for (year in intArrayOf(date.year, date.year - 1)) {
            for (h in Holiday.values()) {
                val day = dateOf(h, year)
                if (!date.isAfter(day) && !date.isBefore(day.minusDays(LEAD_DAYS))) return h
            }
        }
        return null
    }
}
