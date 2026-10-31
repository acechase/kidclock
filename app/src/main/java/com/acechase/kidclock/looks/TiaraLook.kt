package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import com.acechase.kidclock.art.Doodles

/**
 * Teen rather than kid: slim Cormorant digits in a champagne-rose metal, a fine gold tiara resting
 * on the hour, the date in Great Vibes script over a thin gold rule, quiet serif labels. On a
 * background with dark ink the metal goes plum so it still reads. At night everything is amber.
 */
class TiaraLook : Look() {
    override val name = "Tiara"
    override fun labelTypeface(fonts: Fonts) = fonts.cormorant
    private val orn = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val lightInk = luminance(x.ink) > 0.5f
        val metal = if (x.night) null else if (lightInk) LIGHT_METAL else DARK_METAL
        val soft = if (x.night) x.ink else if (lightInk) 0xFFD9B3BD.toInt() else 0xFF7A2E4D.toInt()
        val script = if (x.night) x.ink else if (lightInk) 0xFFE9C9D1.toInt() else 0xFF6E2A46.toInt()
        val gold = if (x.night) x.ink else if (lightInk) 0xFFE3B98A.toInt() else 0xFFB8862E.toInt()

        // Digits
        paint.style = Paint.Style.FILL
        paint.typeface = x.fonts.cormorant
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = h * 0.43f
        fit(paint, x.timeText, w * 0.70f)
        val ts = paint.textSize
        val baseY = h * 0.575f
        val tw = paint.measureText(x.timeText)
        paint.letterSpacing = -0.02f
        if (metal != null) {
            paint.color = 0xFFFFFFFF.toInt()
            paint.shader = LinearGradient(0f, baseY - ts * 0.72f, 0f, baseY, metal, floatArrayOf(0f, 0.42f, 0.58f, 1f), Shader.TileMode.CLAMP)
            paint.setShadowLayer(ts * 0.06f, 0f, ts * 0.02f, 0x8C000000.toInt())
        } else {
            paint.shader = null
            paint.color = x.ink
            paint.clearShadowLayer()
        }
        c.drawText(x.timeText, w / 2f, baseY, paint)
        paint.shader = null
        paint.clearShadowLayer()
        paint.letterSpacing = 0f
        x.timeRect.set(w / 2f - tw / 2f - h * 0.05f, baseY - ts * 0.8f, w / 2f + tw / 2f + h * 0.14f, baseY + ts * 0.15f)

        // The tiara, resting on the first digit.
        val tiaraW = h * 0.165f
        val tiaraCx = w / 2f - tw / 2f + tiaraW * 0.42f
        val tiaraCy = baseY - ts * 0.70f
        if (x.night) orn.alpha = 150
        Doodles.fineTiara(c, orn, tiaraCx, tiaraCy, tiaraW, -10f, gold,
            if (x.night) withAlpha(x.ink, 200) else 0xFFF4C7D2.toInt(), if (x.night) withAlpha(x.ink, 200) else 0xFFF3DDE2.toInt())

        // AM/PM, spaced, to the right of the digits.
        x.ampm?.let { am ->
            paint.textSize = h * 0.09f
            paint.textAlign = Paint.Align.LEFT
            paint.letterSpacing = 0.08f
            paint.color = soft
            paint.alpha = if (x.night) 150 else 255
            c.drawText(am, w / 2f + tw / 2f + h * 0.03f, baseY, paint)
            paint.letterSpacing = 0f
            paint.alpha = 255
            paint.textAlign = Paint.Align.CENTER
        }

        // Date in script, then a thin gold rule that fades at both ends.
        paint.typeface = x.fonts.greatVibes
        paint.textSize = h * 0.0975f
        paint.color = script
        paint.alpha = if (x.night) 150 else 255
        if (!x.night) paint.setShadowLayer(12f, 0f, 2f, 0x66000000)
        c.drawText(longDate(x), w / 2f, h * 0.715f, paint)
        paint.clearShadowLayer()
        paint.alpha = 255
        val ruleW = h * 0.275f
        paint.shader = LinearGradient(w / 2f - ruleW / 2f, 0f, w / 2f + ruleW / 2f, 0f,
            intArrayOf(withAlpha(gold, 0), gold, withAlpha(gold, 0)), null, Shader.TileMode.CLAMP)
        paint.strokeWidth = 1.5f
        c.drawLine(w / 2f - ruleW / 2f, h * 0.745f, w / 2f + ruleW / 2f, h * 0.745f, paint)
        paint.shader = null

        drawAlarmLine(c, x, w / 2f, h * 0.935f, h * 0.0425f, x.fonts.cormorant, color = if (x.night) x.ink else soft, alpha = if (x.night) 110 else 230, shadowed = false)
    }

    private fun luminance(color: Int): Float =
        (0.299f * ((color shr 16) and 0xFF) + 0.587f * ((color shr 8) and 0xFF) + 0.114f * (color and 0xFF)) / 255f

    companion object {
        private val LIGHT_METAL = intArrayOf(0xFFF7E6E9.toInt(), 0xFFE7BFC9.toInt(), 0xFFCFA0AE.toInt(), 0xFFF0D2D9.toInt())
        private val DARK_METAL = intArrayOf(0xFF9E4A6B.toInt(), 0xFF7A2E4D.toInt(), 0xFF5A1F38.toInt(), 0xFF8E3B5A.toInt())
    }
}
