package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Paint

/** The original layout: big light digits in the middle, date under them, alarm line at the bottom. */
class ClassicLook : Look() {
    override val name = "Classic"

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        paint.style = Paint.Style.FILL
        paint.typeface = x.fonts.light
        paint.textAlign = Paint.Align.CENTER
        paint.color = x.ink
        paint.textSize = h * 0.42f
        fit(paint, x.timeText, w * 0.85f)
        shadow(paint, x, 12f, 6f)
        val baseY = h * 0.56f
        c.drawText(x.timeText, w / 2f, baseY, paint)
        val ts = paint.textSize
        val tw = paint.measureText(x.timeText)
        x.timeRect.set(w / 2f - tw / 2f - h * 0.05f, baseY - ts * 0.8f, w / 2f + tw / 2f + h * 0.12f, baseY + ts * 0.15f)

        paint.typeface = x.fonts.regular
        shadow(paint, x, 8f, 3f)
        x.ampm?.let { am ->
            paint.textSize = h * 0.06f
            paint.textAlign = Paint.Align.LEFT
            c.drawText(am, w / 2f + tw / 2f + h * 0.02f, baseY - ts * 0.62f, paint)
            paint.textAlign = Paint.Align.CENTER
        }

        paint.textSize = h * 0.07f
        paint.alpha = if (x.night) 140 else 230
        c.drawText(longDate(x), w / 2f, h * 0.70f, paint)
        paint.alpha = 255

        drawAlarmLine(c, x, w / 2f, h * 0.92f, h * 0.045f, x.fonts.regular)
    }
}
