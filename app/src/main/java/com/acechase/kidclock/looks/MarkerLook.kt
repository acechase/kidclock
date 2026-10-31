package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.acechase.kidclock.art.Doodles

/**
 * The Heart Note hand without the paper: marker digits in Caveat, a scribbled pink underline, the
 * date in Patrick Hand with a little heart, AM/PM in a wobbly circle. Made for the Notebook
 * background but it sits on any of them. At night the pink turns amber with the rest.
 */
class MarkerLook : Look() {
    override val name = "Marker"
    override fun labelTypeface(fonts: Fonts) = fonts.patrickHand
    private val scribble = Path()
    private val doodle = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val pink = if (x.night) withAlpha(x.ink, 150) else PINK
        x.timeRect.set(w * 0.2f, h * 0.17f, w * 0.8f, h * 0.60f)

        paint.style = Paint.Style.FILL
        paint.typeface = x.fonts.caveat
        paint.textAlign = Paint.Align.CENTER
        paint.color = x.ink
        paint.textSize = h * 0.40f
        paint.clearShadowLayer()
        fit(paint, x.timeText, w * 0.60f)
        val baseY = h * 0.50f
        c.drawText(x.timeText, w / 2f, baseY, paint)
        val tw = paint.measureText(x.timeText)

        // Scribbled underline, two strokes like a marker going back over itself.
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = pink
        val ul = w / 2f - tw / 2f; val ur = w / 2f + tw / 2f; val uy = h * 0.555f
        scribble.reset()
        scribble.moveTo(ul, uy)
        scribble.cubicTo(ul + (ur - ul) * 0.2f, uy - h * 0.02f, ul + (ur - ul) * 0.4f, uy + h * 0.01f, ul + (ur - ul) * 0.6f, uy - h * 0.012f)
        scribble.cubicTo(ul + (ur - ul) * 0.8f, uy - h * 0.03f, ur - h * 0.02f, uy - h * 0.02f, ur, uy - h * 0.005f)
        paint.strokeWidth = h * 0.011f
        c.drawPath(scribble, paint)
        scribble.reset()
        scribble.moveTo(ul + h * 0.04f, uy + h * 0.014f)
        scribble.cubicTo(ul + (ur - ul) * 0.3f, uy, ul + (ur - ul) * 0.6f, uy + h * 0.02f, ur - h * 0.03f, uy + h * 0.008f)
        paint.strokeWidth = h * 0.006f
        paint.alpha = if (x.night) 100 else 180
        c.drawPath(scribble, paint)
        paint.alpha = 255

        // Date in a neater hand, with a small heart after it.
        paint.style = Paint.Style.FILL
        paint.typeface = x.fonts.patrickHand
        paint.textSize = h * 0.0775f
        paint.color = x.ink
        paint.alpha = if (x.night) 140 else 255
        val date = longDate(x)
        val dw = paint.measureText(date)
        val heartW = h * 0.045f
        val dateCx = w / 2f - heartW / 2f
        c.drawText(date, dateCx, h * 0.665f, paint)
        paint.alpha = 255
        Doodles.heart(c, doodle, dateCx + dw / 2f + h * 0.02f + heartW / 2f, h * 0.645f, heartW, 0f, pink, Doodles.Style.SOLID)

        // AM/PM with a wobbly circle round it, up to the right.
        x.ampm?.let { am ->
            val ax = w * 0.80f; val ay = h * 0.235f
            c.save()
            c.rotate(6f, ax, ay)
            paint.typeface = x.fonts.patrickHand
            paint.textSize = h * 0.0775f
            paint.color = x.ink
            paint.alpha = if (x.night) 140 else 255
            c.drawText(am, ax, ay + h * 0.027f, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            scribble.reset()
            val rx = h * 0.085f; val ry = h * 0.062f
            scribble.moveTo(ax - rx * 0.7f, ay - ry * 0.9f)
            scribble.cubicTo(ax + rx * 0.2f, ay - ry * 1.4f, ax + rx * 1.2f, ay - ry * 0.6f, ax + rx * 0.95f, ay + ry * 0.3f)
            scribble.cubicTo(ax + rx * 0.7f, ay + ry * 1.3f, ax - rx * 0.6f, ay + ry * 1.3f, ax - rx * 1.0f, ay + ry * 0.4f)
            scribble.cubicTo(ax - rx * 1.3f, ay - ry * 0.4f, ax - rx * 0.9f, ay - ry * 1.1f, ax - rx * 0.4f, ay - ry * 1.0f)
            c.drawPath(scribble, paint)
            paint.style = Paint.Style.FILL
            paint.alpha = 255
            c.restore()
        }

        drawAlarmLine(c, x, w / 2f, h * 0.92f, h * 0.05f, x.fonts.patrickHand)
    }

    companion object {
        const val PINK = 0xFFE0558A.toInt()
    }
}
