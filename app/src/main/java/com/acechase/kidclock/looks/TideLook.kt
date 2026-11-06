package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.sin

/**
 * Digits half under water: chunky white Lilita One digits with a navy edge, the lower part of
 * each filled with deep sea blue behind a rolling white-capped waterline that laps up and down,
 * AM/PM beside them at a third of the size and the date in white underneath. Made for the Ocean
 * background. At night the digits are amber outlines with a dim amber tide inside.
 */
class TideLook : Look() {
    override val name = "Tide"
    override fun labelTypeface(fonts: Fonts) = fonts.lilita
    private val textPath = Path()
    private val wavePath = Path()
    private val bounds = RectF()
    private val water = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val tf = x.fonts.lilita
        val text = x.timeText
        val am = x.ampm

        paint.reset(); paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.typeface = tf
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = h * 0.40f
        val gap = if (am != null) h * 0.025f else 0f
        var aw = 0f
        while (true) {
            if (am != null) { val ts = paint.textSize; paint.textSize = ts / 3f; aw = paint.measureText(am); paint.textSize = ts }
            if (paint.measureText(text) + gap + aw <= w * 0.72f || paint.textSize <= 8f) break
            paint.textSize *= 0.95f
        }
        val ts = paint.textSize
        val tw = paint.measureText(text)
        val cx = w / 2f - (gap + aw) / 2f
        val base = h * 0.53f
        paint.getTextPath(text, 0, text.length, cx, base, textPath)
        textPath.computeBounds(bounds, true)
        x.timeRect.set(bounds.left - h * 0.04f, bounds.top - h * 0.03f, bounds.right + gap + aw + h * 0.04f, base + h * 0.04f)

        // The waterline rolls across the digits; the tide also rises and falls a little.
        val t = x.nowMs / 1000.0
        val waterline = base - ts * 0.33f + ts * 0.04f * sin(t * 0.7).toFloat()
        val amp = ts * 0.035f
        wavePath.reset()
        var px = bounds.left - 8f
        while (px <= bounds.right + 8f) {
            val py = waterline + amp * sin(px / w * 6.283 * 2.2 + t * 1.6).toFloat()
            if (px <= bounds.left - 8f) wavePath.moveTo(px, py) else wavePath.lineTo(px, py)
            px += 6f
        }
        wavePath.lineTo(bounds.right + 8f, bounds.bottom + 8f)
        wavePath.lineTo(bounds.left - 8f, bounds.bottom + 8f)
        wavePath.close()

        if (x.night) {
            c.save()
            c.clipPath(textPath)
            water.style = Paint.Style.FILL
            water.color = x.ink
            water.alpha = 70
            c.drawPath(wavePath, water)
            c.restore()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = h * 0.008f
            paint.strokeJoin = Paint.Join.ROUND
            paint.color = x.ink
            c.drawText(text, cx, base, paint)
            paint.style = Paint.Style.FILL
        } else {
            shadow(paint, x, 14f, 6f)
            paint.color = Color.WHITE
            c.drawText(text, cx, base, paint)
            paint.clearShadowLayer()
            c.save()
            c.clipPath(textPath)
            water.style = Paint.Style.FILL
            water.color = DEEP
            c.drawPath(wavePath, water)
            // The foam: a white crest along the top of the water.
            water.style = Paint.Style.STROKE
            water.strokeWidth = ts * 0.028f
            water.color = Color.WHITE
            water.alpha = 210
            c.drawPath(wavePath, water)
            c.restore()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = h * 0.006f
            paint.strokeJoin = Paint.Join.ROUND
            paint.color = NAVY
            c.drawText(text, cx, base, paint)
            paint.style = Paint.Style.FILL
        }

        if (am != null) {
            paint.textSize = ts / 3f
            paint.textAlign = Paint.Align.LEFT
            paint.color = if (x.night) x.ink else Color.WHITE
            paint.alpha = if (x.night) 150 else 255
            shadow(paint, x, 8f, 3f)
            c.drawText(am, cx + tw / 2f + gap, base - ts * 0.04f, paint)
            paint.alpha = 255
            paint.textAlign = Paint.Align.CENTER
        }

        paint.textSize = h * 0.062f
        paint.color = if (x.night) x.ink else Color.WHITE
        paint.alpha = if (x.night) 140 else 240
        shadow(paint, x, 8f, 3f)
        c.drawText(longDate(x), w / 2f, base + h * 0.10f, paint)
        paint.alpha = 255
        paint.clearShadowLayer()

        drawAlarmLine(c, x, w / 2f, h * 0.935f, h * 0.045f, tf)
    }

    companion object {
        const val DEEP = 0xFF0D47A1.toInt()
        const val NAVY = 0xFF0B3D62.toInt()
    }
}
