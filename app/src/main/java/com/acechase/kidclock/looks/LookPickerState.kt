package com.acechase.kidclock.looks

import kotlin.math.hypot

/**
 * The hold-to-pick gesture and the picker's idle timeout, with no Android in it so it can be
 * unit-tested. The activity feeds it touch events and asks [advance] when its timers fire.
 *
 * Flow: finger down on the time → hold for [holdMs] without moving → picking. While picking a
 * horizontal swipe changes the look, a tap keeps it, and [idleMs] without any touch keeps it too.
 */
class LookPickerState(
    private val holdMs: Long = HOLD_MS,
    private val idleMs: Long = IDLE_MS,
    private val slopPx: Float = 40f,
) {
    enum class Event { ENTERED, EXITED }

    var picking = false
        private set
    /** When the current hold started, 0 when no hold is in progress. */
    var holdStartMs = 0L
        private set
    val holding: Boolean get() = holdStartMs != 0L

    private var downX = 0f
    private var downY = 0f
    private var lastTouchMs = 0L

    /** Finger down. Returns true when this starts a hold toward the picker. */
    fun down(x: Float, y: Float, onTime: Boolean, now: Long): Boolean {
        lastTouchMs = now
        if (picking || !onTime) return false
        downX = x; downY = y
        holdStartMs = now
        return true
    }

    /** Finger moved. Returns true when the move cancelled a hold (the finger drifted too far). */
    fun move(x: Float, y: Float): Boolean {
        if (!holding) return false
        if (hypot(x - downX, y - downY) <= slopPx) return false
        holdStartMs = 0L
        return true
    }

    /** Finger lifted or the touch was cancelled: any unfinished hold is abandoned. */
    fun up() {
        holdStartMs = 0L
    }

    /** 0..1 progress of the hold for the on-screen ring; 0 when not holding. */
    fun holdProgress(now: Long): Float =
        if (!holding) 0f else ((now - holdStartMs).toFloat() / holdMs).coerceIn(0f, 1f)

    /** Time passed: did the hold complete, or did the picker time out? */
    fun advance(now: Long): Event? {
        if (holding && now - holdStartMs >= holdMs) {
            holdStartMs = 0L
            picking = true
            lastTouchMs = now
            return Event.ENTERED
        }
        if (picking && now - lastTouchMs >= idleMs) {
            picking = false
            return Event.EXITED
        }
        return null
    }

    /** Horizontal fling while picking: +1 for the next look (swipe left), -1 for the previous. Null otherwise. */
    fun swipe(dx: Float, now: Long): Int? {
        if (!picking) return null
        lastTouchMs = now
        return if (dx < 0) 1 else -1
    }

    /** A tap while picking keeps the current look and closes the picker. Returns true when it did. */
    fun tap(): Boolean {
        if (!picking) return false
        picking = false
        return true
    }

    /** Leaving the screen: forget everything. */
    fun reset() {
        picking = false
        holdStartMs = 0L
    }

    companion object {
        const val HOLD_MS = 2000L
        const val IDLE_MS = 8000L
    }
}
