package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

/**
 * Rainbow digits: fat Luckiest Guy digits filled with six horizontal bands, red at the top down to
 * purple, inside a thick white edge so they read on the Rainbow background whatever colour it has
 * drifted to, tilted a touch, AM/PM in white beside them and the date in white underneath. At
 * night the digits are amber outlines ruled with faint amber stripes.
 */
class StripesLook : Look() {
    override val name = "Stripes"
    override fun labelTypeface(fonts: Fonts) = fonts.luckiestGuy
    private val textPath = Path()
    private val bounds = RectF()
    private val band = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val tf = x.fonts.luckiestGuy
        val text = x.timeText
        val am = x.ampm

        paint.reset(); paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.typeface = tf
        paint.textAlign = Paint.Align.CENTER
        paint.strokeJoin = Paint.Join.ROUND
        paint.textSize = h * 0.38f
        val gap = if (am != null) h * 0.03f else 0f
        var aw = 0f
        while (true) {
            if (am != null) { val ts = paint.textSize; paint.textSize = ts / 3f; aw = paint.measureText(am); paint.textSize = ts }
            if (paint.measureText(text) + gap + aw <= w * 0.70f || paint.textSize <= 8f) break
            paint.textSize *= 0.95f
        }
        val ts = paint.textSize
        val tw = paint.measureText(text)
        val cx = w / 2f - (gap + aw) / 2f
        val base = h * 0.54f
        x.timeRect.set(cx - tw / 2f - h * 0.05f, base - ts * 0.85f, cx + tw / 2f + gap + aw + h * 0.05f, base + h * 0.05f)

        c.save()
        c.rotate(-3f, cx, base - ts * 0.35f)
        paint.getTextPath(text, 0, text.length, cx, base, textPath)
        textPath.computeBounds(bounds, true)
        val bandH = bounds.height() / COLORS.size
        if (x.night) {
            c.save()
            c.clipPath(textPath)
            band.style = Paint.Style.STROKE
            band.strokeWidth = h * 0.004f
            band.color = x.ink
            band.alpha = 110
            for (i in 1 until COLORS.size) {
                val y = bounds.top + bandH * i
                c.drawLine(bounds.left, y, bounds.right, y, band)
            }
            c.restore()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = h * 0.008f
            paint.color = x.ink
            c.drawText(text, cx, base, paint)
            paint.style = Paint.Style.FILL
        } else {
            // The white edge, drawn as a thick stroke under everything, with a soft shadow.
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = h * 0.022f
            paint.color = Color.WHITE
            shadow(paint, x, 14f, 6f)
            c.drawText(text, cx, base, paint)
            paint.clearShadowLayer()
            paint.style = Paint.Style.FILL
            c.drawText(text, cx, base, paint)
            c.save()
            c.clipPath(textPath)
            band.style = Paint.Style.FILL
            for (i in COLORS.indices) {
                band.color = COLORS[i]
                c.drawRect(bounds.left - 4f, bounds.top + bandH * i, bounds.right + 4f, bounds.top + bandH * (i + 1) + 1f, band)
            }
            c.restore()
        }
        if (am != null) {
            paint.textSize = ts / 3f
            paint.textAlign = Paint.Align.LEFT
            val ax = cx + tw / 2f + gap
            val ay = base - ts * 0.04f
            if (x.night) {
                paint.color = x.ink
                paint.alpha = 150
                c.drawText(am, ax, ay, paint)
                paint.alpha = 255
            } else {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = h * 0.009f
                paint.color = PLUM
                c.drawText(am, ax, ay, paint)
                paint.style = Paint.Style.FILL
                paint.color = Color.WHITE
                c.drawText(am, ax, ay, paint)
            }
            paint.textAlign = Paint.Align.CENTER
        }
        c.restore()

        paint.textSize = h * 0.062f
        paint.letterSpacing = 0.03f
        paint.color = if (x.night) x.ink else Color.WHITE
        paint.alpha = if (x.night) 140 else 255
        shadow(paint, x, 10f, 4f)
        c.drawText(longDate(x), w / 2f, base + h * 0.11f, paint)
        paint.alpha = 255
        paint.letterSpacing = 0f
        paint.clearShadowLayer()

        drawAlarmLine(c, x, w / 2f, h * 0.935f, h * 0.045f, tf)
    }

    companion object {
        val COLORS = intArrayOf(
            0xFFFF5252.toInt(), 0xFFFFA726.toInt(), 0xFFFFEB3B.toInt(),
            0xFF66BB6A.toInt(), 0xFF42A5F5.toInt(), 0xFFAB47BC.toInt(),
        )
        const val PLUM = 0xFF6A1B9A.toInt()
    }
}
