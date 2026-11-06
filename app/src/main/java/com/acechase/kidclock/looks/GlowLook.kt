package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Paint
import kotlin.math.cos
import kotlin.math.sin

/**
 * Firefly light: rounded Comfortaa digits in the fireflies' yellow-green with a wide soft glow
 * around them, a few small fireflies drifting and blinking about the digits, AM/PM glowing beside
 * them and the date in pale cream underneath. Made for the Fireflies background. At night it is
 * plain amber with no glow, so the room stays dark.
 */
class GlowLook : Look() {
    override val name = "Glow"
    override fun labelTypeface(fonts: Fonts) = fonts.comfortaa
    private val fly = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val tf = x.fonts.comfortaa
        val text = x.timeText
        val am = x.ampm
        val light = if (x.night) x.ink else LIGHT

        paint.reset(); paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.typeface = tf
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = h * 0.40f
        val gap = if (am != null) h * 0.03f else 0f
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
        x.timeRect.set(cx - tw / 2f - h * 0.05f, base - ts * 0.8f, cx + tw / 2f + gap + aw + h * 0.05f, base + h * 0.05f)

        paint.color = light
        if (x.night) {
            c.drawText(text, cx, base, paint)
        } else {
            // Two glows, wide and faint then tight and bright, under the digits themselves.
            paint.setShadowLayer(h * 0.07f, 0f, 0f, withAlpha(LIGHT, 110))
            c.drawText(text, cx, base, paint)
            paint.setShadowLayer(h * 0.022f, 0f, 0f, withAlpha(LIGHT, 220))
            c.drawText(text, cx, base, paint)
            paint.clearShadowLayer()
            paint.color = CORE
            c.drawText(text, cx, base, paint)
        }

        if (am != null) {
            paint.textSize = ts / 3f
            paint.textAlign = Paint.Align.LEFT
            paint.color = light
            paint.alpha = if (x.night) 150 else 255
            if (!x.night) paint.setShadowLayer(h * 0.018f, 0f, 0f, withAlpha(LIGHT, 200))
            c.drawText(am, cx + tw / 2f + gap, base - ts * 0.04f, paint)
            paint.clearShadowLayer()
            paint.alpha = 255
            paint.textAlign = Paint.Align.CENTER
        }

        // Fireflies about the digits: each drifts on its own little loop and blinks.
        val t = x.nowMs / 1000.0
        val left = cx - tw / 2f
        val count = if (x.night) 3 else 7
        for (i in 0 until count) {
            val fx = left + tw * (0.05f + 0.9f * ((i * 0.37f + 0.1f) % 1f)) + ts * 0.12f * sin(t * (0.5 + i * 0.11) + i).toFloat()
            val fy = base - ts * (0.95f - 1.25f * ((i * 0.61f + 0.2f) % 1f)) + ts * 0.08f * cos(t * (0.4 + i * 0.09) + i * 2).toFloat()
            val blink = (0.5f + 0.5f * sin(t * (0.9 + i * 0.17) + i * 1.7).toFloat()).let { it * it }
            fly.color = light
            if (!x.night) {
                fly.alpha = (blink * 80).toInt()
                c.drawCircle(fx, fy, h * 0.012f, fly)
            }
            fly.alpha = (if (x.night) 40 + 60 * blink else 90 + 165 * blink).toInt()
            c.drawCircle(fx, fy, h * 0.0035f, fly)
        }

        paint.textSize = h * 0.06f
        paint.color = if (x.night) x.ink else CREAM
        paint.alpha = if (x.night) 140 else 230
        shadow(paint, x, 10f, 4f)
        c.drawText(longDate(x), w / 2f, base + h * 0.105f, paint)
        paint.alpha = 255
        paint.clearShadowLayer()

        drawAlarmLine(c, x, w / 2f, h * 0.935f, h * 0.045f, tf, color = if (x.night) x.ink else CREAM)
    }

    companion object {
        const val LIGHT = 0xFFD4FF5A.toInt()
        const val CORE = 0xFFF4FFC2.toInt()
        const val CREAM = 0xFFEAF2D0.toInt()
    }
}
