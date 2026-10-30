package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

/**
 * A classic dial with numerals and a sweeping second hand, so the clock also teaches analog time.
 * The dial sits left of centre over whatever background is showing; the digital time and date
 * are on the right.
 */
class AnalogLook : Look() {
    override val name = "Analog"
    private val dial = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeCap = Paint.Cap.ROUND }
    private val hand = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeCap = Paint.Cap.ROUND }

    override fun quoteBox(w: Float, h: Float) = RectF(w * 0.56f, h * 0.61f, w * 0.96f, h * 0.84f)

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val cx = w * 0.30f; val cy = h * 0.47f; val r = h * 0.36f
        val ink = if (x.night) x.ink else INK

        dial.style = Paint.Style.FILL
        dial.color = if (x.night) Color.BLACK else Color.WHITE
        c.drawCircle(cx, cy, r, dial)
        dial.style = Paint.Style.STROKE
        dial.strokeWidth = if (x.night) 3f else 8f
        dial.color = if (x.night) withAlpha(x.ink, 0x66) else 0xFFB8A98A.toInt()
        c.drawCircle(cx, cy, r, dial)

        tick.color = ink
        for (i in 0 until 60) {
            val a = i * 6.0 * Math.PI / 180 - Math.PI / 2
            val major = i % 5 == 0
            tick.strokeWidth = if (major) 6f else 2f
            tick.alpha = if (x.night) 120 else if (major) 255 else 120
            val r0 = r * (if (major) 0.86f else 0.91f)
            c.drawLine(cx + (cos(a) * r0).toFloat(), cy + (sin(a) * r0).toFloat(), cx + (cos(a) * r * 0.96f).toFloat(), cy + (sin(a) * r * 0.96f).toFloat(), tick)
        }
        paint.style = Paint.Style.FILL
        paint.clearShadowLayer()
        paint.typeface = x.fonts.medium
        paint.textAlign = Paint.Align.CENTER
        paint.color = ink
        paint.alpha = if (x.night) 160 else 255
        paint.textSize = r * 0.17f
        for (i in 1..12) {
            val a = i * 30.0 * Math.PI / 180 - Math.PI / 2
            c.drawText(i.toString(), cx + (cos(a) * r * 0.72f).toFloat(), cy + (sin(a) * r * 0.72f).toFloat() + paint.textSize * 0.35f, paint)
        }
        paint.alpha = 255

        val sec = x.cal.get(Calendar.SECOND) + x.cal.get(Calendar.MILLISECOND) / 1000.0
        val min = x.cal.get(Calendar.MINUTE) + sec / 60.0
        val hr = x.cal.get(Calendar.HOUR) + min / 60.0
        hand.color = ink
        drawHand(c, cx, cy, hr * 30.0, r * 0.50f, 12f)
        drawHand(c, cx, cy, min * 6.0, r * 0.74f, 8f)
        if (!x.night) {
            hand.color = 0xFFE05252.toInt()
            drawHand(c, cx, cy, sec * 6.0, r * 0.82f, 3f)
        }
        dial.style = Paint.Style.FILL
        dial.color = ink
        c.drawCircle(cx, cy, 9f, dial)
        x.timeRect.set(cx - r, cy - r, w * 0.98f, cy + r)

        // Digital time and date on the right.
        val rx = w * 0.76f
        paint.typeface = x.fonts.light
        paint.color = x.ink
        paint.alpha = if (x.night) 200 else 255
        paint.textSize = h * 0.125f
        shadow(paint, x, 12f, 6f)
        val digital = if (x.ampm != null) "${x.timeText} ${x.ampm}" else x.timeText
        fit(paint, digital, w * 0.42f)
        c.drawText(digital, rx, h * 0.44f, paint)
        paint.typeface = x.fonts.regular
        paint.textSize = h * 0.065f
        paint.alpha = if (x.night) 140 else 230
        shadow(paint, x, 8f, 3f)
        c.drawText(longDate(x), rx, h * 0.56f, paint)
        paint.alpha = 255

        drawAlarmLine(c, x, w / 2f, h * 0.92f, h * 0.045f, x.fonts.regular)
    }

    private fun drawHand(c: Canvas, cx: Float, cy: Float, deg: Double, len: Float, width: Float) {
        val a = deg * Math.PI / 180 - Math.PI / 2
        hand.strokeWidth = width
        c.drawLine(cx, cy, cx + (cos(a) * len).toFloat(), cy + (sin(a) * len).toFloat(), hand)
    }

    companion object {
        const val INK = 0xFF2B2B33.toInt()
    }
}
