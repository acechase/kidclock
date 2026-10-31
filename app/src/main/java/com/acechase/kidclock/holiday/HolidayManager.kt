package com.acechase.kidclock.holiday

import com.acechase.kidclock.Prefs
import java.time.LocalDate

/**
 * Glue between [HolidayPlanner] and Prefs. The clock calls [sync] once a second; when it answers
 * APPLY or RESTORE the screen is rebuilt from [active]. A developer can pin a holiday for
 * screenshots with tools/test-holiday.sh, which sets [Prefs.holidayForce].
 */
class HolidayManager(private val prefs: Prefs) {
    /** The holiday whose design is on screen right now, or null. */
    val active: Holiday? get() = Holiday.byId(prefs.holidayActive)

    /** The holiday whose window covers [date] (or the forced one while testing). */
    fun today(date: LocalDate = LocalDate.now()): Holiday? =
        Holiday.byId(prefs.holidayForce) ?: HolidayCalendar.activeOn(date)

    fun sync(): HolidayPlanner.Action {
        val r = HolidayPlanner.step(state(), today())
        save(r.state)
        return r.action
    }

    /** The child swiped to another background or picked another look. */
    fun userChanged() = save(HolidayPlanner.userChanged(state()))

    private fun state() = HolidayPlanner.State(prefs.holidaysEnabled, prefs.holidayActive, prefs.holidayDismissed)

    private fun save(s: HolidayPlanner.State) {
        if (prefs.holidayActive != s.activeId) prefs.holidayActive = s.activeId
        if (prefs.holidayDismissed != s.dismissedId) prefs.holidayDismissed = s.dismissedId
    }
}
