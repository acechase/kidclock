package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import java.util.Locale
import kotlin.math.sin

/**
 * Tall Bebas Neue digits filled with the aurora itself, mint and green through blue to violet,
 * the colours drifting up and down the digits the way the ribbons ripple, with a hair-thin frost
 * edge and a deep shadow so they stand off the stars; AM/PM in white beside them and the date in
 * spaced snow-white capitals underneath. Made for the Aurora background. At night it is plain amber.
 */
class BorealisLook : Look() {
    override val name = "Borealis"
    override fun labelTypeface(fonts: Fonts) = fonts.bebas
    private val matrix = Matrix()
    private var shader: LinearGradient? = null
    private var shaderSpan = 0f

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val tf = x.fonts.bebas
        val text = x.timeText
        val am = x.ampm

        paint.reset(); paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.typeface = tf
        paint.textAlign = Paint.Align.CENTER
        paint.letterSpacing = 0.05f
        paint.textSize = h * 0.46f
        val gap = if (am != null) h * 0.03f else 0f
        var aw = 0f
        while (true) {
            if (am != null) { val ts = paint.textSize; paint.textSize = ts / 3f; aw = paint.measureText(am); paint.textSize = ts }
            if (paint.measureText(text) + gap + aw <= w * 0.60f || paint.textSize <= 8f) break
            paint.textSize *= 0.95f
        }
        val ts = paint.textSize
        val tw = paint.measureText(text)
        val cx = w / 2f - (gap + aw) / 2f
        val base = h * 0.57f
        val top = base - ts * 0.88f
        x.timeRect.set(cx - tw / 2f - h * 0.05f, top - h * 0.03f, cx + tw / 2f + gap + aw + h * 0.05f, base + h * 0.04f)

        if (x.night) {
            paint.color = x.ink
            c.drawText(text, cx, base, paint)
        } else {
            // The gradient repeats over twice the digit height and slides, so the colours ripple through.
            val span = ts * 1.8f
            if (shader == null || shaderSpan != span) {
                shader = LinearGradient(0f, 0f, 0f, span, RIBBON, null, Shader.TileMode.MIRROR)
                shaderSpan = span
            }
            val t = x.nowMs / 1000.0
            matrix.setTranslate(0f, top + span * 0.5f * sin(t * 0.45).toFloat())
            shader!!.setLocalMatrix(matrix)
            paint.color = 0xFFFFFFFF.toInt()
            paint.setShadowLayer(h * 0.02f, 0f, h * 0.008f, 0xB0000000.toInt())
            paint.shader = shader
            c.drawText(text, cx, base, paint)
            paint.shader = null
            paint.clearShadowLayer()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = h * 0.0025f
            paint.color = FROST
            paint.alpha = 150
            c.drawText(text, cx, base, paint)
            paint.alpha = 255
            paint.style = Paint.Style.FILL
        }

        if (am != null) {
            paint.textSize = ts / 3f
            paint.textAlign = Paint.Align.LEFT
            paint.letterSpacing = 0.1f
            paint.color = if (x.night) x.ink else FROST
            paint.alpha = if (x.night) 150 else 230
            shadow(paint, x, 8f, 3f)
            c.drawText(am, cx + tw / 2f + gap, base - ts * 0.02f, paint)
            paint.clearShadowLayer()
            paint.alpha = 255
            paint.textAlign = Paint.Align.CENTER
        }

        paint.textSize = h * 0.068f
        paint.letterSpacing = 0.12f
        paint.color = if (x.night) x.ink else FROST
        paint.alpha = if (x.night) 140 else 235
        val date = longDate(x).uppercase(Locale.getDefault())
        fit(paint, date, w * 0.80f)
        shadow(paint, x, 8f, 3f)
        c.drawText(date, w / 2f, base + h * 0.095f, paint)
        paint.alpha = 255
        paint.letterSpacing = 0f
        paint.clearShadowLayer()

        paint.letterSpacing = 0.08f
        drawAlarmLine(c, x, w / 2f, h * 0.955f, h * 0.048f, tf)
        paint.letterSpacing = 0f
    }

    companion object {
        val RIBBON = intArrayOf(
            0xFFB9FFE3.toInt(), 0xFF3DFFA0.toInt(), 0xFF7CFFD6.toInt(),
            0xFF7C9CFF.toInt(), 0xFFB48CFF.toInt(), 0xFF3DFFA0.toInt(), 0xFFB9FFE3.toInt(),
        )
        const val FROST = 0xFFDCE9F7.toInt()
    }
}
