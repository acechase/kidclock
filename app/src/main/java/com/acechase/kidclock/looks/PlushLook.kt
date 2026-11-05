package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

/**
 * Soft toy lettering: cream rounded digits in Fredoka with a brown outline and a hard pink shadow
 * behind them, AM/PM the same way at a third of the size, the full date in brown underneath and
 * the alarm line in brown near the bottom. Made for the Stuffies background, where the animals sit
 * below the date. At night it is plain amber with no outline or shadow.
 */
class PlushLook : Look() {
    override val name = "Plush"
    override fun labelTypeface(fonts: Fonts) = fonts.fredoka
    /** The quote sits between the date and the stuffies' heads. */
    override fun quoteBox(w: Float, h: Float) = RectF(w * 0.14f, h * 0.615f, w * 0.86f, h * 0.725f)

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val tf = x.fonts.fredoka
        val brown = if (x.night) x.ink else BROWN

        paint.style = Paint.Style.FILL
        paint.typeface = tf
        paint.textAlign = Paint.Align.CENTER
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.clearShadowLayer()
        paint.textSize = h * 0.37f
        fit(paint, x.timeText, w * 0.70f)
        val ts = paint.textSize
        val tw = paint.measureText(x.timeText)
        val gap = h * 0.03f
        paint.textSize = ts / 3f
        val aw = x.ampm?.let { paint.measureText(it) } ?: 0f
        val groupW = tw + (if (x.ampm != null) gap + aw else 0f)
        val left = w / 2f - groupW / 2f
        val base = h * 0.49f
        val off = h * 0.015f
        val stroke = h * 0.012f

        plush(c, x, x.timeText, left + tw / 2f, base, ts, off, stroke)
        x.ampm?.let { am -> plush(c, x, am, left + tw + gap + aw / 2f, base - h * 0.01f, ts / 3f, off * 0.5f, stroke * 0.6f) }
        x.timeRect.set(left - h * 0.04f, base - ts * 0.75f, left + groupW + h * 0.04f, base + h * 0.03f)

        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = h * 0.055f
        paint.color = brown
        paint.alpha = if (x.night) 140 else 255
        c.drawText(longDate(x), w / 2f, h * 0.575f, paint)
        paint.alpha = 255

        drawAlarmLine(c, x, w / 2f, h * 0.955f, h * 0.042f, tf, color = brown, shadowed = false)
    }

    /** One run of text: pink shadow offset behind, brown outline, cream fill. Plain ink at night. */
    private fun plush(c: Canvas, x: LookContext, s: String, cx: Float, base: Float, size: Float, off: Float, stroke: Float) {
        paint.textSize = size
        paint.textAlign = Paint.Align.CENTER
        if (x.night) {
            paint.style = Paint.Style.FILL
            paint.color = x.ink
            c.drawText(s, cx, base, paint)
            return
        }
        paint.strokeWidth = stroke
        paint.color = PINK
        paint.style = Paint.Style.STROKE
        c.drawText(s, cx + off, base + off, paint)
        paint.style = Paint.Style.FILL
        c.drawText(s, cx + off, base + off, paint)
        paint.color = BROWN
        paint.style = Paint.Style.STROKE
        c.drawText(s, cx, base, paint)
        paint.style = Paint.Style.FILL
        paint.color = CREAM
        c.drawText(s, cx, base, paint)
    }

    companion object {
        const val CREAM = 0xFFFFFAF0.toInt()
        const val BROWN = 0xFF8B5E3C.toInt()
        const val PINK = 0xFFF4A6C0.toInt()
    }
}
