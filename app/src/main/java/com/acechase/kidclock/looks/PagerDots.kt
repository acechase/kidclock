package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

/** The row of pager dots shared by the look picker and the background strip: a pill for the current page. */
object PagerDots {
    /** Width of the row for [count] pages at screen height [h]. */
    fun width(h: Float, count: Int): Float = h * 0.05f + (count - 1) * h * 0.035f

    /** Draws the row centred on [cx] at [cy]; [index] outside 0 until [count] lights no dot. */
    fun draw(c: Canvas, cx: Float, cy: Float, h: Float, index: Int, count: Int, ink: Int, fill: Paint, rect: RectF) {
        val dot = h * 0.0175f; val active = h * 0.05f; val gap = h * 0.0175f
        var x = cx - width(h, count) / 2f
        for (i in 0 until count) {
            val dw = if (i == index) active else dot
            rect.set(x, cy - dot / 2f, x + dw, cy + dot / 2f)
            fill.color = ink
            fill.alpha = if (i == index) 255 else 128
            c.drawRoundRect(rect, dot / 2f, dot / 2f, fill)
            x += dw + gap
        }
        fill.alpha = 255
    }
}
