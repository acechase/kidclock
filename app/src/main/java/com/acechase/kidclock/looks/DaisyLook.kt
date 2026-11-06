package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.cos
import kotlin.math.sin

/**
 * Garden lettering: bouncy Chewy digits in leaf green with a white edge, the colon replaced by two
 * little daisies, AM/PM in green beside them and the date in green with a white edge underneath,
 * all over the Meadow's pale sky. At night it is plain amber, colon and all.
 */
class DaisyLook : Look() {
    override val name = "Daisy"
    override fun labelTypeface(fonts: Fonts) = fonts.chewy
    private val petal = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val tf = x.fonts.chewy
        val text = x.timeText
        val am = x.ampm
        val green = if (x.night) x.ink else GREEN

        paint.reset(); paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.typeface = tf
        paint.textAlign = Paint.Align.LEFT
        paint.strokeJoin = Paint.Join.ROUND
        paint.textSize = h * 0.40f
        val colon = text.indexOf(':')
        val hour = if (colon >= 0) text.substring(0, colon) else text
        val minute = if (colon >= 0) text.substring(colon + 1) else ""
        val gap = if (am != null) h * 0.03f else 0f
        var aw = 0f
        var hw = 0f; var mw = 0f; var cw = 0f
        while (true) {
            val ts = paint.textSize
            if (am != null) { paint.textSize = ts / 3f; aw = paint.measureText(am); paint.textSize = ts }
            hw = paint.measureText(hour); mw = paint.measureText(minute)
            cw = if (colon >= 0) (if (x.night) paint.measureText(":") else ts * 0.34f) else 0f
            if (hw + cw + mw + gap + aw <= w * 0.72f || ts <= 8f) break
            paint.textSize = ts * 0.95f
        }
        val ts = paint.textSize
        val tw = hw + cw + mw
        val left = w / 2f - (tw + gap + aw) / 2f
        val base = h * 0.53f
        x.timeRect.set(left - h * 0.05f, base - ts * 0.8f, left + tw + gap + aw + h * 0.05f, base + h * 0.05f)

        if (x.night) {
            paint.color = x.ink
            c.drawText(text, left, base, paint)
        } else {
            edged(c, hour, left, base, h * 0.012f)
            edged(c, minute, left + hw + cw, base, h * 0.012f)
            // The colon: two daisies, one above the other, nodding slightly.
            val t = x.nowMs / 1000.0
            val dx = left + hw + cw / 2f
            daisy(c, dx, base - ts * 0.47f, ts * 0.085f, sin(t * 1.1).toFloat() * 8f)
            daisy(c, dx, base - ts * 0.16f, ts * 0.085f, sin(t * 1.1 + 1.5).toFloat() * 8f)
        }

        if (am != null) {
            paint.textSize = ts / 3f
            val ax = left + tw + gap
            val ay = base - ts * 0.04f
            if (x.night) {
                paint.color = x.ink
                paint.alpha = 150
                c.drawText(am, ax, ay, paint)
                paint.alpha = 255
            } else edged(c, am, ax, ay, h * 0.006f)
        }

        paint.textAlign = Paint.Align.CENTER
        paint.textSize = h * 0.064f
        val date = longDate(x)
        if (x.night) {
            paint.color = x.ink
            paint.alpha = 140
            c.drawText(date, w / 2f, base + h * 0.105f, paint)
            paint.alpha = 255
        } else {
            val dw = paint.measureText(date)
            paint.textAlign = Paint.Align.LEFT
            edged(c, date, w / 2f - dw / 2f, base + h * 0.105f, h * 0.005f)
            paint.textAlign = Paint.Align.CENTER
        }

        drawAlarmLine(c, x, w / 2f, h * 0.935f, h * 0.045f, tf, color = green, shadowed = !x.night)
    }

    /** Green text with a white edge: a white stroke under a green fill. */
    private fun edged(c: Canvas, s: String, x: Float, base: Float, stroke: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = stroke
        paint.color = Color.WHITE
        c.drawText(s, x, base, paint)
        paint.style = Paint.Style.FILL
        paint.color = GREEN
        c.drawText(s, x, base, paint)
    }

    /** A daisy: six white petals round a yellow centre, with a green rim, turned by [tilt] degrees. */
    private fun daisy(c: Canvas, cx: Float, cy: Float, r: Float, tilt: Float) {
        petal.style = Paint.Style.FILL
        for (k in 0 until 6) {
            val a = Math.toRadians((k * 60 + tilt).toDouble())
            val px = cx + cos(a).toFloat() * r * 0.95f
            val py = cy + sin(a).toFloat() * r * 0.95f
            petal.color = GREEN
            c.drawCircle(px, py, r * 0.62f, petal)
            petal.color = Color.WHITE
            c.drawCircle(px, py, r * 0.52f, petal)
        }
        petal.color = GREEN
        c.drawCircle(cx, cy, r * 0.56f, petal)
        petal.color = YELLOW
        c.drawCircle(cx, cy, r * 0.46f, petal)
    }

    companion object {
        const val GREEN = 0xFF2E7D32.toInt()
        const val YELLOW = 0xFFFFD54F.toInt()
    }
}
