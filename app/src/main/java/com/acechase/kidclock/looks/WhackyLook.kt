package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.graphics.Typeface
import java.util.Locale

/**
 * Surreal on purpose: every digit in a different font, size, colour and tilt, shadows that fall
 * upward, a melting last digit, AM in a balloon, the date riding a wave and a checkerboard floor
 * tipping into the distance. The digits stay in order so it still reads at a glance.
 */
class WhackyLook : Look() {
    override val name = "Whacky"
    override fun labelTypeface(fonts: Fonts) = fonts.rubikMono
    override fun quoteBox(w: Float, h: Float) = RectF(w * 0.06f, h * 0.03f, w * 0.62f, h * 0.13f)

    private val path = Path()
    private val measure = PathMeasure()
    private val matrix = Matrix()
    private val widths = FloatArray(6)

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val f = x.fonts
        val fonts = arrayOf(f.bungeeShade, f.rubikMono, f.monoton, f.bungeeShade, f.rubikMono, f.monoton)
        val colors = if (x.night) IntArray(6) { i -> withAlpha(x.ink, NIGHT_ALPHA[i]) } else COLORS
        val navy = if (x.night) withAlpha(x.ink, 0x99) else NAVY

        paint.style = Paint.Style.FILL
        paint.clearShadowLayer()
        paint.textAlign = Paint.Align.CENTER

        if (!x.night) drawFloor(c, w, h)

        // Lay the characters out side by side, each at its own size, then centre the row.
        val s = x.timeText
        val n = s.length.coerceAtMost(6)
        var total = 0f
        for (i in 0 until n) {
            paint.typeface = fonts[i]
            paint.textSize = h * SIZES[i]
            widths[i] = paint.measureText(s, i, i + 1)
            total += widths[i]
        }
        val gap = w * 0.015f
        total += gap * (n - 1)
        val shift = if (x.ampm != null) w * 0.075f else 0f
        var cx = w / 2f - total / 2f - shift
        x.timeRect.set(cx - h * 0.04f, h * 0.10f, cx + total + h * 0.04f, h * 0.72f)
        for (i in 0 until n) {
            val ch = s.substring(i, i + 1)
            val size = h * SIZES[i]
            val mid = cx + widths[i] / 2f
            val base = h * BASELINES[i]
            paint.typeface = fonts[i]
            paint.textSize = size
            c.save()
            c.rotate(ROTATIONS[i], mid, base - size * 0.35f)
            if (i == 2) c.skew(-0.14f, 0f)
            if (!x.night) {
                paint.color = 0x591E0A3C
                c.drawText(ch, mid, base - h * 0.0275f, paint)
            }
            paint.color = colors[i]
            c.drawText(ch, mid, base, paint)
            c.restore()
            if (i == n - 1 && !x.night) drawDrips(c, h, cx, widths[i], base, colors[i])
            cx += widths[i] + gap
        }

        x.ampm?.let { am -> drawBalloon(c, x, am, navy) }

        // Date riding a wave.
        val date = (dayName(x) + " · " + monthDay(x)).uppercase(Locale.getDefault())
        path.reset()
        path.moveTo(w * 0.07f, h * 0.76f)
        path.cubicTo(w * 0.20f, h * 0.66f, w * 0.30f, h * 0.86f, w * 0.42f, h * 0.76f)
        path.cubicTo(w * 0.54f, h * 0.66f, w * 0.66f, h * 0.66f, w * 0.78f, h * 0.76f)
        path.cubicTo(w * 0.86f, h * 0.84f, w * 0.90f, h * 0.84f, w * 0.95f, h * 0.74f)
        measure.setPath(path, false)
        paint.typeface = f.rubikMono
        paint.textSize = h * 0.0575f
        paint.textAlign = Paint.Align.LEFT
        fit(paint, date, measure.length * 0.95f)
        val off = ((measure.length - paint.measureText(date)) / 2f).coerceAtLeast(0f)
        if (!x.night) {
            paint.color = NAVY
            c.save(); c.translate(w * 0.005f, -h * 0.0075f)
            c.drawTextOnPath(date, path, off, 0f, paint)
            c.restore()
        }
        paint.color = if (x.night) withAlpha(x.ink, 170) else 0xFFFFFFFF.toInt()
        c.drawTextOnPath(date, path, off, 0f, paint)
        paint.textAlign = Paint.Align.CENTER

        drawAlarmBox(c, x, navy)
    }

    private fun drawFloor(c: Canvas, w: Float, h: Float) {
        val top = h * 0.75f
        val src = floatArrayOf(0f, top, w, top, w, h, 0f, h)
        val dst = floatArrayOf(w * 0.28f, top, w * 0.72f, top, w * 1.15f, h, -w * 0.15f, h)
        matrix.setPolyToPoly(src, 0, dst, 0, 4)
        c.save()
        c.concat(matrix)
        val cell = w / 11f
        val rows = 2
        val rowH = (h - top) / rows
        for (r in 0 until rows) for (i in 0 until 11) {
            paint.color = if ((r + i) % 2 == 0) withAlpha(PINK, 0xD9) else withAlpha(YELLOW, 0xD9)
            c.drawRect(i * cell, top + r * rowH, (i + 1) * cell, top + (r + 1) * rowH, paint)
        }
        c.restore()
    }

    private fun drawDrips(c: Canvas, h: Float, left: Float, cw: Float, base: Float, color: Int) {
        paint.color = color
        val top = base - h * 0.01f
        val xs = floatArrayOf(left + cw * 0.25f, left + cw * 0.55f, left + cw * 0.8f)
        val lens = floatArrayOf(h * 0.11f, h * 0.17f, h * 0.075f)
        for (i in 0 until 3) {
            val dw = h * 0.025f
            rect.set(xs[i], top, xs[i] + dw, top + lens[i])
            c.drawRoundRect(rect, dw / 2f, dw / 2f, paint)
        }
    }

    private fun drawBalloon(c: Canvas, x: LookContext, am: String, navy: Int) {
        val w = x.w; val h = x.h
        // Floating at the right, below the weather and clear of the last digit.
        val bx = w * 0.915f; val by = h * 0.40f
        c.save()
        c.rotate(-12f, bx, by)
        rect.set(bx - w * 0.05f, by - h * 0.09f, bx + w * 0.05f, by + h * 0.09f)
        if (x.night) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = withAlpha(x.ink, 170)
            c.drawOval(rect, paint)
            paint.style = Paint.Style.FILL
        } else {
            paint.color = YELLOW
            c.drawOval(rect, paint)
            paint.color = 0x1F000000
            rect.inset(w * 0.012f, h * 0.02f)
            rect.offset(-w * 0.006f, -h * 0.012f)
            c.drawOval(rect, paint)
        }
        paint.typeface = x.fonts.rubikMono
        paint.textSize = h * 0.0675f
        paint.color = if (x.night) x.ink else NAVY
        c.drawText(am, bx, by + h * 0.024f, paint)
        c.restore()
        path.reset()
        path.moveTo(bx, by + h * 0.09f)
        path.cubicTo(bx - w * 0.03f, by + h * 0.16f, bx + w * 0.03f, by + h * 0.20f, bx - w * 0.01f, by + h * 0.27f)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = navy
        c.drawPath(path, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawAlarmBox(c: Canvas, x: LookContext, navy: Int) {
        if (!x.showChrome || x.nextAlarmText.isEmpty()) { x.alarmRect.setEmpty(); return }
        val w = x.w; val h = x.h
        val s = "⏰ ${x.nextAlarmText.uppercase(Locale.getDefault())}"
        paint.typeface = x.fonts.rubikMono
        paint.textSize = h * 0.035f
        fit(paint, s, w * 0.6f)
        val tw = paint.measureText(s)
        val cx = w * 0.30f; val cy = h * 0.90f
        rect.set(cx - tw / 2f - h * 0.035f, cy - h * 0.045f, cx + tw / 2f + h * 0.035f, cy + h * 0.035f)
        c.save()
        c.rotate(-3f, cx, cy)
        if (x.night) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            paint.color = withAlpha(x.ink, 110)
            c.drawRect(rect, paint)
            paint.style = Paint.Style.FILL
            paint.color = withAlpha(x.ink, 110)
        } else {
            paint.color = PINK
            c.drawRect(rect.left - 8f, rect.top + 8f, rect.right - 8f, rect.bottom + 8f, paint)
            paint.color = GREEN
            c.drawRect(rect, paint)
            paint.color = navy
        }
        c.drawText(s, cx, cy + h * 0.012f, paint)
        c.restore()
        x.alarmRect.set(rect.left - h * 0.03f, rect.top - h * 0.03f, rect.right + h * 0.03f, h)
    }

    companion object {
        const val NAVY = 0xFF1E3A6E.toInt()
        const val PINK = 0xFFFF3DA5.toInt()
        const val YELLOW = 0xFFFFE600.toInt()
        const val GREEN = 0xFF39FF14.toInt()
        private val COLORS = intArrayOf(PINK, YELLOW, GREEN, 0xFF00E5FF.toInt(), 0xFFFF8A3D.toInt(), PINK)
        private val NIGHT_ALPHA = intArrayOf(255, 200, 230, 180, 210, 255)
        private val SIZES = floatArrayOf(0.475f, 0.275f, 0.50f, 0.40f, 0.42f, 0.40f)
        private val BASELINES = floatArrayOf(0.60f, 0.62f, 0.58f, 0.64f, 0.60f, 0.62f)
        private val ROTATIONS = floatArrayOf(-10f, 18f, 5f, -14f, 8f, -6f)
    }
}
