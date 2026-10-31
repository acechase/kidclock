package com.acechase.kidclock.holiday

/**
 * When a holiday design appears and when the clock goes back to what the child had chosen. Pure,
 * so it can be unit-tested; HolidayManager feeds it from Prefs once a second.
 *
 * Rules, as agreed on 2026-10-31:
 *  - Three days before a holiday the holiday's background and look take over the screen. The
 *    child's own choice is not touched in Prefs, only hidden, so nothing has to be "restored".
 *  - The day after the holiday the override is dropped and the previous design shows again.
 *  - If the child swipes to another background or picks another look while the holiday design
 *    is showing, that counts as a decision: the holiday is dismissed for this year's window and
 *    their new choice stays, with no switch-back later.
 *  - The Settings switch turns the whole feature off.
 */
object HolidayPlanner {
    /** What Prefs remembers between ticks. */
    data class State(
        val enabled: Boolean = true,
        /** The holiday currently overriding the display, or null. */
        val activeId: String? = null,
        /** A holiday the child opted out of by changing the design during its window. */
        val dismissedId: String? = null,
    )

    enum class Action { NONE, APPLY, RESTORE }

    data class Result(val state: State, val action: Action) {
        val holiday: Holiday? get() = Holiday.byId(state.activeId)
    }

    /** One tick: [today] is the holiday whose window contains the current date, or null. */
    fun step(state: State, today: Holiday?): Result {
        val active = Holiday.byId(state.activeId)
        // A dismissal only lasts for that holiday's window.
        val dismissed = state.dismissedId?.takeIf { it == today?.id }
        if (!state.enabled || today == null) {
            val next = state.copy(activeId = null, dismissedId = dismissed)
            return Result(next, if (active != null) Action.RESTORE else Action.NONE)
        }
        if (active == today) return Result(state.copy(dismissedId = dismissed), Action.NONE)
        if (dismissed != null) {
            // Opted out of this one; drop any other holiday that might still be active (never overlaps in practice).
            val next = state.copy(activeId = null, dismissedId = dismissed)
            return Result(next, if (active != null) Action.RESTORE else Action.NONE)
        }
        return Result(state.copy(activeId = today.id, dismissedId = null), Action.APPLY)
    }

    /** The child changed the background or the look. Returns the new state (unchanged when no holiday was showing). */
    fun userChanged(state: State): State =
        if (state.activeId == null) state else state.copy(activeId = null, dismissedId = state.activeId)
}
