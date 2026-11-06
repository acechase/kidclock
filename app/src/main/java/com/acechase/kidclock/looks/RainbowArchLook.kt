package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

/**
 * A rainbow the way a child draws one: seven bands arching up behind the time, red outside to
 * violet inside, with fat white Luckiest Guy digits in a plum edge sitting in front of it, AM/PM
 * beside them and the date in white underneath. Made for the Rainbow background. At night the
 * arch is a few faint amber lines and the digits are plain amber.
 */
class RainbowArchLook : Look() {
    override val name = "Rainbow Arch"
    override fun labelTypeface(fonts: Fonts) = fonts.luckiestGuy
    private val arc = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val oval = RectF()

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
        paint.textSize = h * 0.36f
        val gap = if (am != null) h * 0.03f else 0f
        var aw = 0f
        while (true) {
            if (am != null) { val ts = paint.textSize; paint.textSize = ts / 3f; aw = paint.measureText(am); paint.textSize = ts }
            if (paint.measureText(text) + gap + aw <= w * 0.66f || paint.textSize <= 8f) break
            paint.textSize *= 0.95f
        }
        val ts = paint.textSize
        val tw = paint.measureText(text)
        val group = tw + gap + aw
        val cx = w / 2f - (gap + aw) / 2f
        val base = h * 0.56f
        x.timeRect.set(cx - tw / 2f - h * 0.05f, base - ts * 0.85f, cx + tw / 2f + gap + aw + h * 0.05f, base + h * 0.05f)

        // The arch: a half ellipse of seven bands, wide enough to hold the digits and AM/PM.
        val rx = group / 2f + ts * 0.16f
        val ry = ts * 0.98f
        val cy = base + ts * 0.08f
        val bandW = if (x.night) h * 0.003f else ry * 0.125f
        val step = ry * 0.125f
        for (i in BANDS.indices) {
            val inset = step * i + step / 2f
            oval.set(w / 2f - rx + inset, cy - ry + inset, w / 2f + rx - inset, cy + ry - inset)
            arc.strokeWidth = bandW + if (x.night) 0f else 1.5f   // a hair of overlap hides the seams
            arc.color = if (x.night) x.ink else BANDS[i]
            arc.alpha = if (x.night) 80 else 235
            c.drawArc(oval, 180f, 180f, false, arc)
        }

        if (x.night) {
            paint.color = x.ink
            c.drawText(text, cx, base, paint)
        } else {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = h * 0.016f
            paint.color = PLUM
            shadow(paint, x, 12f, 5f)
            c.drawText(text, cx, base, paint)
            paint.clearShadowLayer()
            paint.style = Paint.Style.FILL
            paint.color = Color.WHITE
            c.drawText(text, cx, base, paint)
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
                paint.strokeWidth = h * 0.008f
                paint.color = PLUM
                c.drawText(am, ax, ay, paint)
                paint.style = Paint.Style.FILL
                paint.color = Color.WHITE
                c.drawText(am, ax, ay, paint)
            }
            paint.textAlign = Paint.Align.CENTER
        }

        paint.textSize = h * 0.062f
        paint.letterSpacing = 0.03f
        paint.color = if (x.night) x.ink else Color.WHITE
        paint.alpha = if (x.night) 140 else 255
        shadow(paint, x, 10f, 4f)
        c.drawText(longDate(x), w / 2f, base + h * 0.115f, paint)
        paint.alpha = 255
        paint.letterSpacing = 0f
        paint.clearShadowLayer()

        drawAlarmLine(c, x, w / 2f, h * 0.935f, h * 0.045f, tf)
    }

    companion object {
        val BANDS = intArrayOf(
            0xFFFF4D4D.toInt(), 0xFFFF9F2E.toInt(), 0xFFFFE23A.toInt(), 0xFF5CD65C.toInt(),
            0xFF3DAEFF.toInt(), 0xFF5C6BFF.toInt(), 0xFFB36BFF.toInt(),
        )
        const val PLUM = 0xFF5E2A84.toInt()
    }
}
