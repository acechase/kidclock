package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import java.util.Locale

/** Flip-clock feel: the hour over the minutes on the left, a rule, and the day written large on the right. */
class StackedLook : Look() {
    override val name = "Stacked"
    override fun labelTypeface(fonts: Fonts) = fonts.oswald
    override fun quoteBox(w: Float, h: Float) = RectF(w * 0.50f, h * 0.78f, w * 0.97f, h * 0.90f)

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val tf = x.fonts.oswald
        val hour = x.timeText.substringBefore(':')
        val minute = x.timeText.substringAfter(':')

        paint.style = Paint.Style.FILL
        paint.typeface = tf
        paint.color = x.ink
        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = h * 0.40f
        shadow(paint, x, 12f, 6f)
        val right = w * 0.42f
        c.drawText(hour, right, h * 0.45f, paint)
        c.drawText(minute, right, h * 0.81f, paint)
        x.timeRect.set(w * 0.08f, h * 0.12f, w * 0.46f, h * 0.84f)

        paint.clearShadowLayer()
        paint.strokeWidth = 3f
        paint.alpha = if (x.night) 90 else 140
        c.drawLine(w * 0.47f, h * 0.20f, w * 0.47f, h * 0.76f, paint)
        paint.alpha = 255

        val left = w * 0.515f
        paint.textAlign = Paint.Align.LEFT
        shadow(paint, x, 10f, 4f)
        x.ampm?.let { am ->
            paint.textSize = h * 0.09f
            paint.alpha = if (x.night) 140 else 230
            c.drawText(am, left, h * 0.27f, paint)
        }
        paint.textSize = h * 0.1375f
        paint.alpha = if (x.night) 140 else 255
        c.drawText(dayName(x).uppercase(Locale.getDefault()), left, h * 0.42f, paint)
        paint.textSize = h * 0.075f
        paint.alpha = if (x.night) 140 else 230
        c.drawText(monthDay(x), left, h * 0.52f, paint)
        paint.alpha = 255

        drawAlarmLine(c, x, left, h * 0.74f, h * 0.045f, tf, align = Paint.Align.LEFT)
    }
}
