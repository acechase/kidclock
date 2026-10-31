package com.acechase.kidclock.holiday

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.acechase.kidclock.art.Doodles
import com.acechase.kidclock.looks.Fonts
import com.acechase.kidclock.looks.Look
import com.acechase.kidclock.looks.LookContext
import java.util.Locale

/**
 * The lettering that ships with each holiday background: a display face for the digits, an
 * ornament resting on the hour (a witch hat, a holly sprig, a sparkler), AM/PM to the right and
 * the date in a second face underneath. They share one centred layout, built by the helpers in
 * [HolidayLook]; at night every one of them is plain amber like the rest of the app.
 */
abstract class HolidayLook(override val name: String) : Look() {
    protected val orn = Paint(Paint.ANTI_ALIAS_FLAG)
    /** The date sits lower than in Classic, so the quiet quote moves down under it. */
    override fun quoteBox(w: Float, h: Float) = RectF(w * 0.14f, h * 0.775f, w * 0.86f, h * 0.885f)

    /** Where the digits ended up, so the ornament and AM/PM can sit against them. */
    protected class Digits(val left: Float, val right: Float, val baseY: Float, val size: Float)

    /** Big centred digits, filled with a vertical [metal] gradient by day (or [solid]) and amber at night. */
    protected fun digits(
        c: Canvas, x: LookContext, tf: Typeface, sizeFrac: Float, fitFrac: Float = 0.70f, spacing: Float = -0.02f,
        metal: IntArray? = null, stops: FloatArray? = null, solid: Int = 0xFFFFFFFF.toInt(),
        shadow: Int = 0x8C000000.toInt(), glow: Boolean = false, baseFrac: Float = 0.575f,
    ): Digits {
        val w = x.w; val h = x.h
        paint.reset(); paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.typeface = tf
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = h * sizeFrac
        paint.fontFeatureSettings = LINING   // Cormorant, Playfair and friends default to short old-style figures
        fit(paint, x.timeText, w * fitFrac)
        val ts = paint.textSize
        val baseY = h * baseFrac
        paint.letterSpacing = spacing
        val tw = paint.measureText(x.timeText)
        if (x.night) {
            paint.color = x.ink
        } else {
            paint.color = solid
            if (metal != null) {
                paint.color = 0xFFFFFFFF.toInt()
                paint.shader = LinearGradient(0f, baseY - ts * 0.72f, 0f, baseY, metal, stops, Shader.TileMode.CLAMP)
            }
            if (shadow != 0) paint.setShadowLayer(if (glow) ts * 0.11f else ts * 0.06f, 0f, if (glow) 0f else ts * 0.02f, shadow)
        }
        c.drawText(x.timeText, w / 2f, baseY, paint)
        paint.shader = null
        paint.clearShadowLayer()
        paint.letterSpacing = 0f
        paint.fontFeatureSettings = null
        x.timeRect.set(w / 2f - tw / 2f - h * 0.05f, baseY - ts * 0.8f, w / 2f + tw / 2f + h * 0.14f, baseY + ts * 0.15f)
        return Digits(w / 2f - tw / 2f, w / 2f + tw / 2f, baseY, ts)
    }

    /** Digits in a solid colour with two hard offset copies behind them, the retro poster trick. */
    protected fun offsetDigits(c: Canvas, x: LookContext, tf: Typeface, sizeFrac: Float, front: Int, back1: Int, back2: Int, spacing: Float = 0.02f): Digits {
        val w = x.w; val h = x.h
        paint.reset(); paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.typeface = tf
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = h * sizeFrac
        fit(paint, x.timeText, w * 0.70f)
        val ts = paint.textSize
        val baseY = h * 0.60f
        paint.letterSpacing = spacing
        val tw = paint.measureText(x.timeText)
        if (x.night) {
            paint.color = x.ink
            c.drawText(x.timeText, w / 2f, baseY, paint)
        } else {
            val o = ts * 0.018f
            paint.color = back2; c.drawText(x.timeText, w / 2f + 2 * o, baseY + 2 * o, paint)
            paint.color = back1; c.drawText(x.timeText, w / 2f + o, baseY + o, paint)
            paint.color = front
            paint.setShadowLayer(ts * 0.07f, ts * 0.04f, ts * 0.05f, 0x73000000)
            c.drawText(x.timeText, w / 2f, baseY, paint)
            paint.clearShadowLayer()
        }
        paint.letterSpacing = 0f
        x.timeRect.set(w / 2f - tw / 2f - h * 0.05f, baseY - ts * 0.8f, w / 2f + tw / 2f + h * 0.14f, baseY + ts * 0.15f)
        return Digits(w / 2f - tw / 2f, w / 2f + tw / 2f, baseY, ts)
    }

    protected fun ampm(c: Canvas, x: LookContext, d: Digits, tf: Typeface, sizeFrac: Float, color: Int, spacing: Float = 0f, italic: Boolean = false) {
        val am = x.ampm ?: return
        paint.reset(); paint.isAntiAlias = true
        paint.typeface = tf
        paint.textSkewX = if (italic) -0.1f else 0f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = x.h * sizeFrac
        paint.letterSpacing = spacing
        paint.fontFeatureSettings = LINING
        paint.color = if (x.night) x.ink else color
        paint.alpha = if (x.night) 150 else 255
        c.drawText(am, d.right + x.h * 0.03f, d.baseY, paint)
        paint.fontFeatureSettings = null
        paint.letterSpacing = 0f; paint.textSkewX = 0f; paint.alpha = 255
    }

    protected fun date(c: Canvas, x: LookContext, text: String, tf: Typeface, sizeFrac: Float, color: Int, yFrac: Float, spacing: Float = 0f) {
        paint.reset(); paint.isAntiAlias = true
        paint.typeface = tf
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = x.h * sizeFrac
        paint.letterSpacing = spacing
        paint.fontFeatureSettings = LINING
        paint.color = if (x.night) x.ink else color
        paint.alpha = if (x.night) 150 else 255
        if (!x.night) paint.setShadowLayer(12f, 0f, 2f, 0x66000000)
        fit(paint, text, x.w * 0.9f)
        c.drawText(text, x.w / 2f, x.h * yFrac, paint)
        paint.clearShadowLayer(); paint.letterSpacing = 0f; paint.alpha = 255; paint.fontFeatureSettings = null
    }

    /** A thin rule that fades out at both ends. */
    protected fun rule(c: Canvas, x: LookContext, color: Int, yFrac: Float, widthFrac: Float = 0.275f) {
        val col = if (x.night) withAlpha(x.ink, 140) else color
        val half = x.h * widthFrac / 2f
        paint.reset(); paint.isAntiAlias = true
        paint.shader = LinearGradient(x.w / 2f - half, 0f, x.w / 2f + half, 0f, intArrayOf(withAlpha(col, 0), col, withAlpha(col, 0)), null, Shader.TileMode.CLAMP)
        paint.strokeWidth = 1.5f
        c.drawLine(x.w / 2f - half, x.h * yFrac, x.w / 2f + half, x.h * yFrac, paint)
        paint.shader = null
    }

    /** "THURSDAY · DECEMBER 31" */
    protected fun spacedDate(x: LookContext) = "${dayName(x)} · ${monthDay(x)}".uppercase(Locale.getDefault())

    protected fun alarm(c: Canvas, x: LookContext, tf: Typeface, color: Int, sizeFrac: Float = 0.0425f) =
        drawAlarmLine(c, x, x.w / 2f, x.h * 0.935f, x.h * sizeFrac, tf, color = if (x.night) x.ink else color, alpha = if (x.night) 110 else 220, shadowed = false)

    /** Ornament colour by day, dim amber at night. */
    protected companion object { const val LINING = "lnum" }

    protected fun orn(x: LookContext, day: Int, nightAlpha: Int = 150) = if (x.night) withAlpha(x.ink, nightAlpha) else day
}

/** New Year's Eve: Cinzel digits in champagne gold, a trail of sparkles over the hour, the date in spaced capitals. */
class MidnightLook : HolidayLook("Midnight") {
    override fun labelTypeface(fonts: Fonts) = fonts.josefin
    override fun draw(c: Canvas, x: LookContext) {
        val d = digits(c, x, x.fonts.cinzel, 0.375f, metal = GOLD, stops = GOLD_STOPS, spacing = -0.01f)
        val s = x.h * 0.0015f; val ox = d.left + x.h * 0.02f; val oy = d.baseY - d.size * 0.82f
        val col = orn(x, 0xFFFFF1C9.toInt())
        Doodles.sparkle(c, orn, ox + 30f * s, oy + 60f * s, 38f * s, col, fat = true)
        Doodles.sparkle(c, orn, ox + 78f * s, oy + 30f * s, 26f * s, col, 230, fat = true)
        Doodles.sparkle(c, orn, ox + 110f * s, oy + 58f * s, 17f * s, col, 200, fat = true)
        orn.style = Paint.Style.FILL; orn.color = col; orn.alpha = 200
        c.drawCircle(ox + 140f * s, oy + 40f * s, 2.2f * s, orn); c.drawCircle(ox + 160f * s, oy + 62f * s, 1.6f * s, orn); c.drawCircle(ox + 55f * s, oy + 22f * s, 1.8f * s, orn)
        ampm(c, x, d, x.fonts.josefin, 0.08f, 0xFFF3DDA0.toInt(), spacing = 0.1f)
        date(c, x, spacedDate(x), x.fonts.josefin, 0.045f, 0xFFFFF1C9.toInt(), 0.705f, spacing = 0.28f)
        rule(c, x, 0xFFF3DDA0.toInt(), 0.745f, 0.325f)
        alarm(c, x, x.fonts.josefin, 0xFFF3DDA0.toInt())
    }
    companion object {
        val GOLD = intArrayOf(0xFFFFF6DC.toInt(), 0xFFF3DDA0.toInt(), 0xFFC9A55A.toInt(), 0xFFF7E7B8.toInt())
        val GOLD_STOPS = floatArrayOf(0f, 0.42f, 0.6f, 1f)
    }
}

/** Valentine's Day: Playfair italic digits in rose gold, a small arrowed heart on the hour, the date in Parisienne. */
class RoseLook : HolidayLook("Rose") {
    override fun labelTypeface(fonts: Fonts) = fonts.playfairItalic
    override fun draw(c: Canvas, x: LookContext) {
        val d = digits(c, x, x.fonts.playfairItalic, 0.40f, metal = ROSE, stops = floatArrayOf(0f, 0.4f, 0.58f, 1f), spacing = -0.03f)
        val size = x.h * 0.1f
        val cx = d.left + size * 0.5f; val cy = d.baseY - d.size * 0.78f
        if (!x.night) Doodles.heart(c, orn, cx, cy, size, -12f, 0xFFE8577F.toInt(), Doodles.Style.SOLID)
        Doodles.heart(c, orn, cx, cy, size, -12f, orn(x, 0xFFF3DDA0.toInt()), Doodles.Style.ARROW)
        ampm(c, x, d, x.fonts.playfairItalic, 0.09f, 0xFFF4C7D2.toInt())
        date(c, x, longDate(x), x.fonts.parisienne, 0.105f, 0xFFFFD6E0.toInt(), 0.72f)
        alarm(c, x, x.fonts.playfairItalic, 0xFFFFD6E0.toInt())
    }
    companion object {
        val ROSE = intArrayOf(0xFFFFE9EF.toInt(), 0xFFF4C7D2.toInt(), 0xFFD9909F.toInt(), 0xFFF9D7DE.toInt())
    }
}

/** St. Patrick's Day: Uncial Antiqua digits in gold, a four-heart clover resting on the hour. */
class CloverLook : HolidayLook("Clover") {
    override fun labelTypeface(fonts: Fonts) = fonts.uncial
    override fun draw(c: Canvas, x: LookContext) {
        val d = digits(c, x, x.fonts.uncial, 0.375f, metal = MidnightLook.GOLD, stops = MidnightLook.GOLD_STOPS, spacing = 0f)
        Doodles.clover(c, orn, d.left + x.h * 0.04f, d.baseY - d.size * 0.84f, x.h * 0.14f, -14f, orn(x, 0xFF3FBF7F.toInt()))
        ampm(c, x, d, x.fonts.uncial, 0.08f, 0xFFF3DDA0.toInt())
        date(c, x, longDate(x), x.fonts.uncial, 0.07f, 0xFFF0F7E8.toInt(), 0.705f)
        alarm(c, x, x.fonts.uncial, 0xFFF0F7E8.toInt())
    }
}

/** Easter: rounded Comfortaa digits in plum with a white halo, a striped egg on the hour, the date in Quicksand. */
class PastelEggsLook : HolidayLook("Pastel Eggs") {
    override fun labelTypeface(fonts: Fonts) = fonts.quicksand
    private val stripes = Path()
    override fun draw(c: Canvas, x: LookContext) {
        val d = digits(c, x, x.fonts.comfortaa, 0.375f, solid = 0xFF6A3D9A.toInt(), shadow = 0xE6FFFFFF.toInt(), glow = true, spacing = -0.02f)
        // The egg: blush, with pink stripes and a tiny white heart.
        val size = x.h * 0.12f
        val cx = d.left + size * 0.25f; val cy = d.baseY - d.size * 0.8f
        orn.reset(); orn.isAntiAlias = true
        Doodles.draw(c, cx, cy, size, -18f) {
            orn.style = Paint.Style.FILL; orn.color = orn(x, 0xFFFFC2D1.toInt(), 60)
            c.drawPath(Doodles.egg, orn)
            c.save(); c.clipPath(Doodles.egg)
            orn.style = Paint.Style.STROKE; orn.strokeWidth = 7f; orn.color = orn(x, 0xFFF48FB1.toInt(), 160)
            for (yy in floatArrayOf(30f, 48f, 66f, 84f)) c.drawLine(0f, yy, 100f, yy, orn)
            c.restore()
            orn.style = Paint.Style.FILL; orn.color = if (x.night) x.ink else 0xFFFFFFFF.toInt()
            c.save(); c.translate(50f, 57f); c.scale(0.14f, 0.14f); c.translate(-50f, -50f); c.drawPath(Doodles.heart, orn); c.restore()
        }
        ampm(c, x, d, x.fonts.comfortaa, 0.09f, 0xFF8E5BC2.toInt())
        date(c, x, longDate(x), x.fonts.quicksand, 0.0675f, 0xFF6A3D9A.toInt(), 0.705f, spacing = 0.04f)
        alarm(c, x, x.fonts.quicksand, 0xFF6A3D9A.toInt())
    }
}

/** Fourth of July: tall Bebas Neue digits in white with a coral and blue offset, a sparkler on the hour. */
class FireworksLook : HolidayLook("Fireworks") {
    override fun labelTypeface(fonts: Fonts) = fonts.bebas
    override fun draw(c: Canvas, x: LookContext) {
        val d = offsetDigits(c, x, x.fonts.bebas, 0.4125f, 0xFFFFFFFF.toInt(), 0xFFFF6B6B.toInt(), 0xFF2B5CB8.toInt())
        // Sparkler: a stick with a burst at the top.
        val s = x.h * 0.0012f
        val cx = d.left + x.h * 0.02f; val cy = d.baseY - d.size * 0.86f
        c.save(); c.translate(cx, cy); c.rotate(-22f); c.scale(s, s)
        orn.reset(); orn.isAntiAlias = true; orn.strokeCap = Paint.Cap.ROUND
        orn.style = Paint.Style.STROKE; orn.strokeWidth = 4f; orn.color = orn(x, 0xFFC9A55A.toInt())
        c.drawLine(0f, 12f, 0f, 72f, orn)
        orn.strokeWidth = 2.5f; orn.color = orn(x, 0xFFFFF1C9.toInt())
        for (k in 0 until 8) {
            val a = Math.toRadians(k * 45.0); val outer = if (k % 2 == 0) 34f else 30f
            c.drawLine(Math.cos(a).toFloat() * 10f, Math.sin(a).toFloat() * 10f, Math.cos(a).toFloat() * outer, Math.sin(a).toFloat() * outer, orn)
        }
        orn.style = Paint.Style.FILL
        c.drawCircle(0f, 0f, 4f, orn)
        for (k in 0 until 5) { val a = Math.toRadians(-90.0 + k * 45.0); c.drawCircle(Math.cos(a).toFloat() * 38f, Math.sin(a).toFloat() * 38f, 2f, orn) }
        c.restore()
        ampm(c, x, d, x.fonts.bebas, 0.12f, 0xFFFF6B6B.toInt(), spacing = 0.04f)
        date(c, x, spacedDate(x), x.fonts.bebas, 0.065f, 0xFF7CC4FF.toInt(), 0.745f, spacing = 0.2f)
        alarm(c, x, x.fonts.bebas, 0xFFFFFFFF.toInt(), sizeFrac = 0.05f)
    }
}

/** Thanksgiving: soft Fraunces digits in amber, a maple leaf resting on the hour, the date in Lora italic. */
class HarvestLook : HolidayLook("Harvest") {
    override fun labelTypeface(fonts: Fonts) = fonts.loraItalic
    override fun draw(c: Canvas, x: LookContext) {
        val d = digits(c, x, x.fonts.fraunces, 0.3875f, metal = AMBER, stops = floatArrayOf(0f, 0.45f, 0.62f, 1f), spacing = -0.03f)
        val size = x.h * 0.1375f
        Doodles.fill(c, orn, Doodles.mapleLeaf, d.left + size * 0.2f, d.baseY - d.size * 0.8f, size, -26f, orn(x, 0xFFC2410C.toInt()))
        ampm(c, x, d, x.fonts.loraItalic, 0.0875f, 0xFFF6C76B.toInt())
        date(c, x, longDate(x), x.fonts.loraItalic, 0.075f, 0xFFFBE9C8.toInt(), 0.715f)
        alarm(c, x, x.fonts.loraItalic, 0xFFFBE9C8.toInt())
    }
    companion object {
        val AMBER = intArrayOf(0xFFFBE9C8.toInt(), 0xFFE0A030.toInt(), 0xFFB4531A.toInt(), 0xFFF6C76B.toInt())
    }
}

/** Christmas: Libre Baskerville digits in snow white with a warm glow, a holly sprig on the hour, the date in Pinyon Script. */
class FairyLightsLook : HolidayLook("Fairy Lights") {
    override fun labelTypeface(fonts: Fonts) = fonts.baskerville
    override fun draw(c: Canvas, x: LookContext) {
        val d = digits(c, x, x.fonts.baskerville, 0.375f, solid = 0xFFFFFFFF.toInt(), shadow = 0xB3FFD166.toInt(), glow = true, spacing = -0.02f)
        val s = x.h * 0.0012f
        val cx = d.left + x.h * 0.05f; val cy = d.baseY - d.size * 0.86f
        c.save(); c.translate(cx, cy); c.rotate(-10f); c.scale(s, s)
        orn.reset(); orn.isAntiAlias = true
        for (pass in 0..1) {
            orn.style = if (pass == 0) Paint.Style.FILL else Paint.Style.STROKE; orn.strokeWidth = 2f
            orn.color = if (pass == 0) orn(x, 0xFF2E7D5B.toInt(), 60) else orn(x, 0xFF1C5A40.toInt())
            c.save(); c.rotate(-150f); c.scale(0.9f, 0.9f); c.drawPath(Doodles.hollyLeaf, orn); c.restore()
            c.save(); c.rotate(20f); c.scale(0.9f, 0.9f); c.drawPath(Doodles.hollyLeaf, orn); c.restore()
        }
        orn.style = Paint.Style.FILL; orn.color = orn(x, 0xFFD7263D.toInt())
        c.drawCircle(-2f, 2f, 7f, orn); c.drawCircle(10f, -6f, 6f, orn); c.drawCircle(8f, 10f, 6f, orn)
        c.restore()
        ampm(c, x, d, x.fonts.baskerville, 0.08f, 0xFFFFD166.toInt())
        date(c, x, longDate(x), x.fonts.pinyon, 0.105f, 0xFFFBE9C8.toInt(), 0.725f)
        alarm(c, x, x.fonts.baskerville, 0xFFFFFFFF.toInt(), sizeFrac = 0.04f)
    }
}

/** Halloween: the Tiara treatment in copper, with a witch hat where the tiara was. */
class WitchingHourLook : HolidayLook("Witching Hour") {
    override fun labelTypeface(fonts: Fonts) = fonts.cormorant
    override fun draw(c: Canvas, x: LookContext) {
        val d = digits(c, x, x.fonts.cormorant, 0.4125f, metal = COPPER, stops = floatArrayOf(0f, 0.4f, 0.58f, 0.82f, 1f))
        if (x.night) {
            orn.reset(); orn.isAntiAlias = true; orn.style = Paint.Style.STROKE; orn.strokeWidth = 3f; orn.color = withAlpha(x.ink, 150)
            Doodles.draw(c, d.left + x.h * 0.07f, d.baseY - d.size * 0.86f, x.h * 0.2f, -14f, 120f) {
                c.drawLine(10f, 86f, 110f, 86f, orn); c.drawLine(26f, 86f, 80f, 6f, orn); c.drawLine(80f, 6f, 96f, 86f, orn)
            }
        } else {
            Doodles.witchHat(c, orn, d.left + x.h * 0.07f, d.baseY - d.size * 0.86f, x.h * 0.2f, -14f)
        }
        ampm(c, x, d, x.fonts.cormorant, 0.09f, 0xFFD8B4FE.toInt(), spacing = 0.08f)
        date(c, x, longDate(x), x.fonts.greatVibes, 0.0975f, 0xFFD8B4FE.toInt(), 0.715f)
        rule(c, x, 0xFFF6A95C.toInt(), 0.745f)
        alarm(c, x, x.fonts.cormorant, 0xFFE9D5FF.toInt())
    }
    companion object {
        val COPPER = intArrayOf(0xFFFFE6C7.toInt(), 0xFFF6A95C.toInt(), 0xFFC2410C.toInt(), 0xFFF9C58E.toInt(), 0xFFFFF0DC.toInt())
    }
}
