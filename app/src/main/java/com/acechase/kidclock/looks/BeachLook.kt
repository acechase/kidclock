package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Paint

/**
 * Rounded navy digits with a hard light-blue drop, AM/PM beside them at a third of the size, the
 * full date under them, and the alarm line in brown near the bottom so it reads on sand. The
 * digits sit high, in the sky, clear of whatever furniture a background keeps on the floor. At
 * night it is all amber, no drop.
 */
class BeachLook : Look() {
    override val name = "Beach"
    override fun labelTypeface(fonts: Fonts) = fonts.fredoka

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val tf = x.fonts.fredoka
        val navy = if (x.night) x.ink else NAVY
        val drop = h * 0.012f

        paint.style = Paint.Style.FILL
        paint.typeface = tf
        paint.textAlign = Paint.Align.CENTER
        paint.clearShadowLayer()
        paint.textSize = h * 0.40f
        fit(paint, x.timeText, w * 0.70f)
        val ts = paint.textSize
        val tw = paint.measureText(x.timeText)
        val ampmSize = ts / 3f
        val gap = if (x.ampm != null) h * 0.025f else 0f
        var aw = 0f
        val am = x.ampm
        if (am != null) {
            paint.textSize = ampmSize
            aw = paint.measureText(am)
            paint.textSize = ts
        }
        // Centre the digits and the AM/PM together.
        val cx = w / 2f - (gap + aw) / 2f
        val base = h * 0.48f
        if (!x.night) {
            paint.color = LIGHT_BLUE
            c.drawText(x.timeText, cx + drop, base + drop, paint)
        }
        paint.color = navy
        c.drawText(x.timeText, cx, base, paint)
        x.timeRect.set(cx - tw / 2f - h * 0.04f, base - ts * 0.78f, cx + tw / 2f + gap + aw + h * 0.04f, base + h * 0.04f)

        if (am != null) {
            paint.textSize = ampmSize
            paint.textAlign = Paint.Align.LEFT
            val ax = cx + tw / 2f + gap
            if (!x.night) {
                paint.color = LIGHT_BLUE
                c.drawText(am, ax + drop * 0.6f, base + drop * 0.6f, paint)
            }
            paint.color = navy
            c.drawText(am, ax, base, paint)
            paint.textAlign = Paint.Align.CENTER
        }

        // The full date under the digits.
        paint.textSize = h * 0.062f
        paint.color = navy
        paint.alpha = if (x.night) 140 else 255
        c.drawText(longDate(x), w / 2f, base + h * 0.085f, paint)
        paint.alpha = 255

        drawAlarmLine(c, x, w / 2f, h * 0.955f, h * 0.042f, tf, color = if (x.night) x.ink else BROWN,
            alpha = if (x.night) 110 else 255, shadowed = false)
    }

    companion object {
        const val NAVY = 0xFF1F3B4D.toInt()
        const val LIGHT_BLUE = 0xFF9FD6E8.toInt()
        const val BROWN = 0xFF5B3A1E.toInt()
    }
}
