package com.acechase.kidclock.looks

/**
 * Where a finger may rest to start the hold that opens the look picker. Pure, so it can be unit-tested.
 *
 * Every look draws the time and date somewhere different, and a small child does not aim well, so the zone
 * is the whole middle band of the screen plus whatever the look reported as its time box, minus the places
 * that do something else on a touch: the settings corner (top-left), the weather (top-right), the next-alarm
 * line and the quote.
 */
object HoldZone {
    /** A screen box in pixels; an empty or inverted box matches nothing. */
    data class Box(val left: Float, val top: Float, val right: Float, val bottom: Float) {
        operator fun contains(p: Pair<Float, Float>): Boolean =
            right > left && bottom > top && p.first >= left && p.first < right && p.second >= top && p.second < bottom
    }

    /** The middle band, as fractions of the height: below the top chrome, above the status line. */
    const val BAND_TOP = 0.10f
    const val BAND_BOTTOM = 0.88f

    fun contains(x: Float, y: Float, w: Float, h: Float, time: Box, blocked: List<Box>, cornerPx: Float): Boolean {
        if (x < cornerPx && y < cornerPx) return false
        val p = x to y
        if (blocked.any { p in it }) return false
        if (p in time) return true
        return x >= 0f && x < w && y >= h * BAND_TOP && y < h * BAND_BOTTOM
    }
}
