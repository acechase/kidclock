package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

/**
 * The Marker hand in a boy's colours: blue Caveat digits with a lowercase "pm" beside them, a
 * green double scribble underline, the full date in Caveat navy under it, and the alarm line in
 * Patrick Hand. Made for the Doodle Notebook background but it sits on any of them. At night
 * everything is amber on black.
 */
class MarkerBlueLook : Look() {
    override val name = "Marker Blue"
    override fun labelTypeface(fonts: Fonts) = fonts.patrickHand
    private val scribble = Path()

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val blue = if (x.night) x.ink else BLUE
        val green = if (x.night) withAlpha(x.ink, 150) else GREEN
        val navy = if (x.night) x.ink else NAVY

        // Digits, with a small lowercase am/pm sitting beside them; the pair is centred.
        paint.style = Paint.Style.FILL
        paint.typeface = x.fonts.caveat
        paint.textAlign = Paint.Align.LEFT
        paint.color = blue
        paint.textSize = h * 0.375f
        paint.clearShadowLayer()
        val am = x.ampm?.lowercase()
        fit(paint, x.timeText, if (am != null) w * 0.62f else w * 0.70f)
        val ts = paint.textSize
        val tw = paint.measureText(x.timeText)
        val gap = w * 0.0125f
        var pmW = 0f
        if (am != null) {
            paint.textSize = ts * 0.3f
            pmW = paint.measureText(am) + gap
            paint.textSize = ts
        }
        val baseY = h * 0.515f
        val left = w / 2f - (tw + pmW) / 2f
        c.drawText(x.timeText, left, baseY, paint)
        if (am != null) {
            paint.textSize = ts * 0.3f
            c.drawText(am, left + tw + gap, baseY - h * 0.02f, paint)
            paint.textSize = ts
        }
        x.timeRect.set(left - h * 0.03f, baseY - ts * 0.72f, left + tw + pmW + h * 0.03f, baseY + ts * 0.12f)

        // Green scribble underline: a wavy marker stroke and a thinner one back over it.
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.color = green
        val ul = left; val span = tw; val uy = h * 0.598f
        val a = h * 0.015f
        scribble.reset()
        scribble.moveTo(ul, uy + a * 0.3f)
        var t0 = 0f
        for (i in 0 until 4) {
            val t1 = 0.92f * (i + 1) / 4f
            val seg = t1 - t0
            scribble.cubicTo(ul + span * (t0 + seg / 3f), uy - a, ul + span * (t0 + seg * 2f / 3f), uy + a * 0.8f, ul + span * t1, uy - a * 0.2f)
            t0 = t1
        }
        scribble.quadTo(ul + span * 0.96f, uy + a * 0.2f, ul + span, uy - a * 0.3f)
        paint.strokeWidth = h * 0.0073f
        c.drawPath(scribble, paint)
        scribble.reset()
        scribble.moveTo(ul + span * 0.04f, uy + a * 0.6f)
        t0 = 0.04f
        for (i in 0 until 3) {
            val t1 = 0.04f + 0.93f * (i + 1) / 3f
            val seg = t1 - t0
            scribble.cubicTo(ul + span * (t0 + seg * 0.28f), uy - a * 0.1f, ul + span * (t0 + seg * 0.6f), uy + a, ul + span * t1, uy + a * 0.4f)
            t0 = t1
        }
        paint.strokeWidth = h * 0.0042f
        paint.alpha = if (x.night) 100 else 180
        c.drawPath(scribble, paint)
        paint.alpha = 255

        // The full date in the same hand, navy, under the underline.
        paint.style = Paint.Style.FILL
        paint.typeface = x.fonts.caveat
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = h * 0.067f
        paint.color = navy
        paint.alpha = if (x.night) 140 else 255
        val date = longDate(x)
        fit(paint, date, w * 0.80f)
        c.drawText(date, w / 2f, h * 0.69f, paint)
        paint.alpha = 255

        drawAlarmLine(c, x, w / 2f, h * 0.92f, h * 0.05f, x.fonts.patrickHand, color = navy)
    }

    companion object {
        const val BLUE = 0xFF2F6FB5.toInt()
        const val GREEN = 0xFF2E8B57.toInt()
        const val NAVY = 0xFF2A2F45.toInt()
    }
}
