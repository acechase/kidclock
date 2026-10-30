package com.acechase.kidclock.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

/**
 * A classic dial with numerals and a sweeping second hand, so the clock also teaches analog time.
 * The dial sits left of centre; the date is drawn on the right. During a gentle wake-up (quote
 * card showing) it falls back to the digital layout so everything fits.
 */
class AnalogFace(context: Context) : ClockFace(context) {
    private val dial = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeCap = Paint.Cap.ROUND }
    private val hand = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeCap = Paint.Cap.ROUND }
    private val num = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL) }

    override val dayTextColor = 0xFF2B2B33.toInt()
    override val dayShadowColor = 0x22000000
    override val showDigitalTime get() = wakeMode && quote != null
    override fun dateAnchor(w: Float, h: Float) = if (showDigitalTime) super.dateAnchor(w, h) else Pair(w * 0.76f, h * 0.56f)
    override fun clockQuoteBox(w: Float, h: Float) = RectF(w * 0.56f, h * 0.61f, w * 0.96f, h * 0.84f)
    override fun dayBackgroundForMoonBite() = 0xFFF6EEDC.toInt()

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        c.drawColor(if (night) Color.BLACK else 0xFFF6EEDC.toInt())
        if (showDigitalTime) return
        val cx = w * 0.30f; val cy = h * 0.47f; val r = h * 0.36f
        val ink = if (night) NIGHT_TEXT else 0xFF2B2B33.toInt()
        // Dial
        dial.style = Paint.Style.FILL; dial.color = if (night) Color.BLACK else Color.WHITE
        c.drawCircle(cx, cy, r, dial)
        dial.style = Paint.Style.STROKE; dial.strokeWidth = if (night) 3f else 8f; dial.color = if (night) 0x66FF8A3D else 0xFFB8A98A.toInt()
        c.drawCircle(cx, cy, r, dial)
        // Ticks and numerals
        tick.color = ink
        for (i in 0 until 60) {
            val a = i * 6.0 * Math.PI / 180 - Math.PI / 2
            val major = i % 5 == 0
            tick.strokeWidth = if (major) 6f else 2f
            tick.alpha = if (night) 120 else if (major) 255 else 120
            val r0 = r * (if (major) 0.86f else 0.91f)
            c.drawLine(cx + (cos(a) * r0).toFloat(), cy + (sin(a) * r0).toFloat(), cx + (cos(a) * r * 0.96f).toFloat(), cy + (sin(a) * r * 0.96f).toFloat(), tick)
        }
        num.color = ink; num.alpha = if (night) 160 else 255; num.textSize = r * 0.17f
        for (i in 1..12) {
            val a = i * 30.0 * Math.PI / 180 - Math.PI / 2
            c.drawText(i.toString(), cx + (cos(a) * r * 0.72f).toFloat(), cy + (sin(a) * r * 0.72f).toFloat() + num.textSize * 0.35f, num)
        }
        // Hands
        val cal = Calendar.getInstance().apply { timeInMillis = nowMs }
        val sec = cal.get(Calendar.SECOND) + cal.get(Calendar.MILLISECOND) / 1000.0
        val min = cal.get(Calendar.MINUTE) + sec / 60.0
        val hr = (cal.get(Calendar.HOUR) + min / 60.0)
        hand.color = ink
        drawHand(c, cx, cy, hr * 30.0, r * 0.50f, 12f)
        drawHand(c, cx, cy, min * 6.0, r * 0.74f, 8f)
        if (!night) {
            hand.color = 0xFFE05252.toInt()
            drawHand(c, cx, cy, sec * 6.0, r * 0.82f, 3f)
        }
        dial.style = Paint.Style.FILL; dial.color = ink
        c.drawCircle(cx, cy, 9f, dial)
        // Small digital time to the right, above the date.
        num.textSize = h * 0.125f; num.typeface = Typeface.create("sans-serif-light", Typeface.NORMAL); num.alpha = if (night) 200 else 255
        val hh = cal.get(Calendar.HOUR_OF_DAY); val mm = cal.get(Calendar.MINUTE)
        val txt = if (use24h) "%02d:%02d".format(hh, mm) else "%d:%02d %s".format(if (hh % 12 == 0) 12 else hh % 12, mm, if (hh < 12) "AM" else "PM")
        c.drawText(txt, w * 0.76f, h * 0.44f, num)
        num.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }

    private fun drawHand(c: Canvas, cx: Float, cy: Float, deg: Double, len: Float, width: Float) {
        val a = deg * Math.PI / 180 - Math.PI / 2
        hand.strokeWidth = width
        c.drawLine(cx - (cos(a) * len * 0.15f).toFloat(), cy - (sin(a) * len * 0.15f).toFloat(), cx + (cos(a) * len).toFloat(), cy + (sin(a) * len).toFloat(), hand)
    }
}
