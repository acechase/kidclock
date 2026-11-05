package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

/**
 * Stencil digits in Black Ops One, the way a part number is sprayed on a crate: white, a touch
 * letter-spaced, AM/PM beside them in the same face at a third of the size, the date in spaced
 * capitals underneath, and the alarm line pulled into the left 62% so it clears the Blueprint
 * title block. Made for the Blueprint background but it reads on any of them; at night it is all
 * amber with no shadows.
 */
class StencilLook : Look() {
    override val name = "Stencil"
    override fun labelTypeface(fonts: Fonts) = fonts.medium
    override fun quoteBox(w: Float, h: Float) = RectF(w * 0.06f, h * 0.74f, w * 0.60f, h * 0.86f)

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h

        // Digits, with AM/PM as one centred group.
        paint.style = Paint.Style.FILL
        paint.typeface = x.fonts.blackOps
        paint.textAlign = Paint.Align.CENTER
        paint.color = x.ink
        paint.letterSpacing = 0.02f
        paint.textSize = h * 0.3125f
        fit(paint, x.timeText, w * 0.70f)
        val gap = h * 0.025f
        var amW = 0f
        val am = x.ampm
        if (am != null) {
            while (true) {
                val ts = paint.textSize
                paint.textSize = ts / 3f
                amW = paint.measureText(am)
                paint.textSize = ts
                if (paint.measureText(x.timeText) + gap + amW <= w * 0.70f || ts <= 8f) break
                paint.textSize = ts * 0.95f
            }
        }
        val ts = paint.textSize
        val tw = paint.measureText(x.timeText)
        val extra = if (am != null) gap + amW else 0f
        val cx = w / 2f - extra / 2f
        val base = h * 0.52f
        shadow(paint, x, 10f, 4f)
        c.drawText(x.timeText, cx, base, paint)
        x.timeRect.set(cx - tw / 2f - h * 0.04f, base - ts * 0.8f, cx + tw / 2f + extra + h * 0.04f, base + ts * 0.12f)

        if (am != null) {
            paint.textSize = ts / 3f
            paint.textAlign = Paint.Align.LEFT
            c.drawText(am, cx + tw / 2f + gap, base - ts * 0.08f, paint)
            paint.textAlign = Paint.Align.CENTER
        }
        paint.letterSpacing = 0f

        // The date in spaced capitals.
        paint.typeface = x.fonts.medium
        paint.textSize = h * 0.0417f
        paint.letterSpacing = 0.15f
        paint.alpha = if (x.night) 140 else 255
        val date = longDate(x).uppercase()
        fit(paint, date, w * 0.80f)
        shadow(paint, x, 8f, 3f)
        c.drawText(date, w / 2f, h * 0.615f, paint)
        paint.alpha = 255
        paint.letterSpacing = 0f
        paint.clearShadowLayer()

        // Alarm line, centred in the left 62% of the width so it clears the title block.
        paint.letterSpacing = 0.1f
        drawAlarmLine(c, x, w * 0.31f, h * 0.93f, h * 0.04f, x.fonts.medium)
        paint.letterSpacing = 0f
    }
}
