package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import java.util.Locale
import kotlin.math.sin

/**
 * Space-station lettering: wide Orbitron digits filled with a nebula gradient, white at the top
 * through pink to a cyan foot, glowing magenta, with a few four-pointed stars twinkling around
 * them; AM/PM in cyan beside them and the date in spaced lavender capitals underneath. Made for
 * the Galaxy background. At night it is plain amber with small still stars.
 */
class NebulaLook : Look() {
    override val name = "Nebula"
    override fun labelTypeface(fonts: Fonts) = fonts.orbitron
    private val star = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeCap = Paint.Cap.ROUND }

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val tf = x.fonts.orbitron
        val text = x.timeText
        val am = x.ampm

        paint.reset(); paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.typeface = tf
        paint.textAlign = Paint.Align.CENTER
        paint.letterSpacing = 0.02f
        paint.textSize = h * 0.30f
        val gap = if (am != null) h * 0.03f else 0f
        var aw = 0f
        while (true) {
            if (am != null) { val ts = paint.textSize; paint.textSize = ts / 3f; aw = paint.measureText(am); paint.textSize = ts }
            if (paint.measureText(text) + gap + aw <= w * 0.74f || paint.textSize <= 8f) break
            paint.textSize *= 0.95f
        }
        val ts = paint.textSize
        val tw = paint.measureText(text)
        val cx = w / 2f - (gap + aw) / 2f
        val base = h * 0.52f
        val top = base - ts * 0.75f
        x.timeRect.set(cx - tw / 2f - h * 0.05f, top - h * 0.04f, cx + tw / 2f + gap + aw + h * 0.05f, base + h * 0.05f)

        if (x.night) {
            paint.color = x.ink
            c.drawText(text, cx, base, paint)
        } else {
            // The glow first: magenta text with a wide magenta shadow, then the gradient over it.
            paint.color = MAGENTA
            paint.setShadowLayer(h * 0.03f, 0f, 0f, withAlpha(MAGENTA, 190))
            c.drawText(text, cx, base, paint)
            paint.clearShadowLayer()
            paint.shader = LinearGradient(0f, top, 0f, base, intArrayOf(Color.WHITE, PINK, CYAN), floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP)
            c.drawText(text, cx, base, paint)
            paint.shader = null
        }

        // Four-pointed stars around the digits, each twinkling at its own pace.
        val t = x.nowMs / 1000.0
        val left = cx - tw / 2f
        val right = cx + tw / 2f
        val spots = floatArrayOf(
            left - ts * 0.10f, top + ts * 0.05f, 0.9f,
            right + ts * 0.06f, base - ts * 0.15f, 0.7f,
            left + tw * 0.62f, top - ts * 0.14f, 0.55f,
            left + tw * 0.25f, base + ts * 0.12f, 0.5f,
        )
        star.color = if (x.night) x.ink else Color.WHITE
        for (i in spots.indices step 3) {
            val tw2 = if (x.night) 0.5f else (0.55f + 0.45f * sin(t * (1.1 + i * 0.2) + i).toFloat())
            val s = ts * 0.11f * spots[i + 2] * (0.7f + 0.3f * tw2)
            star.alpha = if (x.night) 110 else (120 + 135 * tw2).toInt()
            star.strokeWidth = s * 0.16f
            c.drawLine(spots[i] - s, spots[i + 1], spots[i] + s, spots[i + 1], star)
            c.drawLine(spots[i], spots[i + 1] - s, spots[i], spots[i + 1] + s, star)
            star.strokeWidth = s * 0.09f
            c.drawLine(spots[i] - s * 0.4f, spots[i + 1] - s * 0.4f, spots[i] + s * 0.4f, spots[i + 1] + s * 0.4f, star)
            c.drawLine(spots[i] - s * 0.4f, spots[i + 1] + s * 0.4f, spots[i] + s * 0.4f, spots[i + 1] - s * 0.4f, star)
        }

        if (am != null) {
            paint.textSize = ts / 3f
            paint.textAlign = Paint.Align.LEFT
            paint.letterSpacing = 0.1f
            paint.color = if (x.night) x.ink else CYAN
            paint.alpha = if (x.night) 150 else 255
            if (!x.night) paint.setShadowLayer(h * 0.012f, 0f, 0f, withAlpha(CYAN, 150))
            c.drawText(am, cx + tw / 2f + gap, base - ts * 0.03f, paint)
            paint.clearShadowLayer()
            paint.alpha = 255
            paint.textAlign = Paint.Align.CENTER
        }

        paint.textSize = h * 0.046f
        paint.letterSpacing = 0.14f
        paint.color = if (x.night) x.ink else LAVENDER
        paint.alpha = if (x.night) 140 else 235
        val date = longDate(x).uppercase(Locale.getDefault())
        fit(paint, date, w * 0.80f)
        shadow(paint, x, 8f, 3f)
        c.drawText(date, w / 2f, base + h * 0.105f, paint)
        paint.alpha = 255
        paint.letterSpacing = 0f
        paint.clearShadowLayer()

        paint.letterSpacing = 0.06f
        drawAlarmLine(c, x, w / 2f, h * 0.935f, h * 0.038f, tf)
        paint.letterSpacing = 0f
    }

    companion object {
        const val MAGENTA = 0xFFE040FB.toInt()
        const val PINK = 0xFFFF80AB.toInt()
        const val CYAN = 0xFF40C4FF.toInt()
        const val LAVENDER = 0xFFE1BEE7.toInt()
    }
}
