package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.acechase.kidclock.art.Doodles
import java.util.Locale

/**
 * The Stacked layout written in marker and pink: the hour over the minutes in Caveat on the left,
 * a wobbly pink rule, the day in Patrick Hand on the right, a tiara perched on the hour and a few
 * more doodled around the page. The lettering is pink by day whatever the background; amber at night.
 */
class MarkerStackedLook : Look() {
    override val name = "Marker Stacked"
    override fun labelTypeface(fonts: Fonts) = fonts.patrickHand
    override val quoteInk: Int get() = DEEP
    override fun quoteBox(w: Float, h: Float) = RectF(w * 0.50f, h * 0.795f, w * 0.97f, h * 0.915f)
    private val rule = Path()
    private val doodle = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val bright = if (x.night) x.ink else BRIGHT
        val deep = if (x.night) withAlpha(x.ink, 170) else DEEP
        val hour = x.timeText.substringBefore(':')
        val minute = x.timeText.substringAfter(':')

        paint.style = Paint.Style.FILL
        paint.typeface = x.fonts.caveat
        paint.color = bright
        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = h * 0.425f
        paint.clearShadowLayer()
        val right = w * 0.42f
        c.drawText(hour, right, h * 0.45f, paint)
        c.drawText(minute, right, h * 0.81f, paint)
        x.timeRect.set(w * 0.08f, h * 0.12f, w * 0.46f, h * 0.84f)

        // A tiara perched on the hour.
        val hourW = paint.measureText(hour)
        Doodles.tiara(c, doodle, right - hourW * 0.55f, h * 0.115f, h * 0.165f, -12f, if (x.night) withAlpha(x.ink, 200) else BRIGHT, Doodles.Tiara.POINTED)

        // The dividing rule, in pink marker that wanders a little.
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = h * 0.00625f
        paint.color = if (x.night) withAlpha(x.ink, 110) else withAlpha(MarkerLook.PINK, 215)
        val rx = w * 0.47f
        rule.reset()
        rule.moveTo(rx, h * 0.20f)
        rule.cubicTo(rx + 4f, h * 0.30f, rx - 6f, h * 0.39f, rx + 2f, h * 0.48f)
        rule.cubicTo(rx + 8f, h * 0.56f, rx - 4f, h * 0.67f, rx, h * 0.76f)
        c.drawPath(rule, paint)
        paint.style = Paint.Style.FILL

        val left = w * 0.515f
        paint.typeface = x.fonts.patrickHand
        paint.textAlign = Paint.Align.LEFT
        paint.color = deep
        x.ampm?.let { am ->
            paint.textSize = h * 0.09f
            c.drawText(am, left, h * 0.27f, paint)
        }
        paint.textSize = h * 0.145f
        c.drawText(dayName(x).uppercase(Locale.getDefault()), left, h * 0.42f, paint)
        paint.textSize = h * 0.08f
        val md = monthDay(x)
        c.drawText(md, left, h * 0.52f, paint)
        Doodles.heart(c, doodle, left + paint.measureText(md) + h * 0.045f, h * 0.49f, h * 0.0425f, 0f,
            if (x.night) withAlpha(x.ink, 170) else 0xFFC2185B.toInt(), Doodles.Style.SOLID)

        drawAlarmLine(c, x, left, h * 0.74f, h * 0.045f, x.fonts.patrickHand, align = Paint.Align.LEFT, color = deep,
            alpha = if (x.night) 110 else 220, shadowed = false)

        // Doodled tiaras around the page, and a sparkle beside two of them.
        if (x.showChrome) {
            val a = if (x.night) 110 else 255
            fun col(day: Int) = if (x.night) withAlpha(x.ink, a) else day
            Doodles.tiara(c, doodle, w * 0.547f, h * 0.603f, h * 0.1575f, 6f, col(MAGENTA), Doodles.Tiara.POINTED)
            Doodles.tiara(c, doodle, w * 0.8125f, h * 0.70f, h * 0.135f, -8f, col(MarkerLook.PINK), Doodles.Tiara.ROUNDED, heartGem = false)
            Doodles.tiara(c, doodle, w * 0.164f, h * 0.16f, h * 0.12f, -10f, col(RED), Doodles.Tiara.FIVE)
            Doodles.tiara(c, doodle, w * 0.227f, h * 0.86f, h * 0.12f, 8f, col(MAGENTA), Doodles.Tiara.POINTED, heartGem = false)
            Doodles.tiara(c, doodle, w * 0.609f, h * 0.155f, h * 0.108f, -6f, col(MarkerLook.PINK), Doodles.Tiara.ROUNDED)
            Doodles.tiara(c, doodle, w * 0.94f, h * 0.73f, h * 0.09f, 12f, col(PLUM), Doodles.Tiara.FIVE, heartGem = false)
            Doodles.sparkle(c, doodle, w * 0.6f, h * 0.5625f, h * 0.0275f, col(MAGENTA))
            Doodles.sparkle(c, doodle, w * 0.86f, h * 0.7175f, h * 0.0225f, col(MarkerLook.PINK))
        }
    }

    companion object {
        const val BRIGHT = 0xFFE8488A.toInt()
        const val DEEP = 0xFFD13A78.toInt()
        private const val MAGENTA = 0xFFC2185B.toInt()
        private const val RED = 0xFFD32F2F.toInt()
        private const val PLUM = 0xFF8E1C5A.toInt()
    }
}
