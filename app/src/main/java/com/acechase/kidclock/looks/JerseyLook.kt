package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import java.util.Locale

/**
 * Big navy digits in Fredoka with a hard green drop like a printed kit number, AM/PM beside them
 * in the same style, and under them a two-cell scoreboard strip: the day in yellow on navy, the
 * date in navy on white. Made for the Soccer Notebook background but it sits on any of them. At
 * night the drop goes, the strip is just an amber outline and everything is amber.
 */
class JerseyLook : Look() {
    override val name = "Jersey"
    override fun labelTypeface(fonts: Fonts) = fonts.fredoka
    private val clip = Path()
    private val inner = RectF()

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val tf = x.fonts.fredoka
        paint.reset(); paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.typeface = tf
        paint.textAlign = Paint.Align.LEFT

        // Digits, with AM/PM a third their size beside them; the pair is centred as one block.
        paint.textSize = h * 0.38f
        val ampmGap = h * 0.023f
        var ampmW = 0f
        x.ampm?.let { am ->
            paint.textSize = h * 0.38f / 3f
            ampmW = paint.measureText(am) + ampmGap
            paint.textSize = h * 0.38f
        }
        fit(paint, x.timeText, w * 0.70f - ampmW)
        val ts = paint.textSize
        val tw = paint.measureText(x.timeText)
        val left = w / 2f - (tw + ampmW) / 2f
        val base = h * 0.56f
        val drop = h * 0.012f
        if (!x.night) {
            paint.color = GREEN
            c.drawText(x.timeText, left + drop, base + drop, paint)
        }
        paint.color = if (x.night) x.ink else NAVY
        c.drawText(x.timeText, left, base, paint)
        x.timeRect.set(left - h * 0.04f, base - ts * 0.75f, left + tw + ampmW + h * 0.04f, base + h * 0.04f)

        x.ampm?.let { am ->
            paint.textSize = ts / 3f
            val ax = left + tw + ampmGap
            val ay = base - h * 0.012f
            if (!x.night) {
                paint.color = GREEN
                c.drawText(am, ax + drop * 0.5f, ay + drop * 0.5f, paint)
            }
            paint.color = if (x.night) x.ink else NAVY
            c.drawText(am, ax, ay, paint)
        }

        // Scoreboard strip: [ THURSDAY ][ NOVEMBER 5 ]
        val day = dayName(x).uppercase(Locale.getDefault())
        val date = monthDay(x).uppercase(Locale.getDefault())
        paint.textSize = h * 0.042f
        paint.letterSpacing = 0.075f
        val padX = h * 0.027f
        val border = h * 0.0042f
        val cellH = h * 0.068f
        var dayW = paint.measureText(day) + padX * 2f
        var dateW = paint.measureText(date) + padX * 2f
        while (dayW + dateW > w * 0.80f && paint.textSize > 8f) {
            paint.textSize *= 0.95f
            dayW = paint.measureText(day) + padX * 2f
            dateW = paint.measureText(date) + padX * 2f
        }
        val stripTop = base + h * 0.045f
        val stripLeft = w / 2f - (dayW + dateW) / 2f
        rect.set(stripLeft, stripTop, stripLeft + dayW + dateW, stripTop + cellH)
        val radius = h * 0.015f
        val textY = rect.centerY() + paint.textSize * 0.36f
        if (x.night) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = border
            paint.color = x.ink
            paint.alpha = 170
            c.drawRoundRect(rect, radius, radius, paint)
            c.drawLine(stripLeft + dayW, rect.top, stripLeft + dayW, rect.bottom, paint)
            paint.style = Paint.Style.FILL
            paint.alpha = 150
            c.drawText(day, stripLeft + padX, textY, paint)
            c.drawText(date, stripLeft + dayW + padX, textY, paint)
            paint.alpha = 255
        } else {
            // The navy fill is both the left cell and the border; the right cell is white inside it.
            paint.style = Paint.Style.FILL
            paint.color = NAVY
            c.drawRoundRect(rect, radius, radius, paint)
            inner.set(rect.left + border, rect.top + border, rect.right - border, rect.bottom - border)
            clip.reset()
            clip.addRoundRect(inner, radius - border, radius - border, Path.Direction.CW)
            c.save()
            c.clipPath(clip)
            paint.color = Color.WHITE
            c.drawRect(stripLeft + dayW, rect.top, rect.right, rect.bottom, paint)
            c.restore()
            paint.color = YELLOW
            c.drawText(day, stripLeft + padX, textY, paint)
            paint.color = NAVY
            c.drawText(date, stripLeft + dayW + padX, textY, paint)
        }
        paint.letterSpacing = 0f
        paint.textAlign = Paint.Align.CENTER

        drawAlarmLine(c, x, w / 2f, h * 0.92f, h * 0.045f, tf, color = if (x.night) x.ink else NAVY, alpha = if (x.night) 110 else 230, shadowed = false)
    }

    companion object {
        const val NAVY = 0xFF2A2F45.toInt()
        const val GREEN = 0xFF2E8B57.toInt()
        const val YELLOW = 0xFFFFD23F.toInt()
    }
}
