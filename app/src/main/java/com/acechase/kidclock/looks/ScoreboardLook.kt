package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import java.util.Locale

/**
 * A stadium scoreboard: a near-black panel in a thick grey frame, the date across the top in red
 * LED capitals, huge glowing yellow digits in the middle with AM/PM beside them, and the next
 * alarm along the bottom in green. Made for the Stadium background but it sits on any of them.
 * At night the frame and every line go amber with no glow.
 */
class ScoreboardLook : Look() {
    override val name = "Scoreboard"
    override fun labelTypeface(fonts: Fonts) = fonts.rubikMono
    private val quoteRect = RectF()
    private val panel = RectF()

    override fun quoteBox(w: Float, h: Float): RectF {
        quoteRect.set(w * 0.14f, h * 0.725f, w * 0.86f, h * 0.84f)
        return quoteRect
    }

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val tf = x.fonts.rubikMono
        val red = if (x.night) x.ink else RED
        val yellow = if (x.night) x.ink else YELLOW
        val green = if (x.night) x.ink else GREEN

        // The panel and its frame.
        val stroke = h * 0.012f
        panel.set(w * 0.15f, h * 0.09f, w * 0.85f, h * 0.72f)
        val corner = h * 0.029f
        paint.clearShadowLayer()
        paint.letterSpacing = 0f
        paint.style = Paint.Style.FILL
        paint.color = PANEL
        if (!x.night) c.drawRoundRect(panel, corner, corner, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = stroke
        paint.color = if (x.night) x.ink else FRAME
        paint.alpha = if (x.night) 120 else 255
        rect.set(panel.left + stroke / 2f, panel.top + stroke / 2f, panel.right - stroke / 2f, panel.bottom - stroke / 2f)
        c.drawRoundRect(rect, corner, corner, paint)
        paint.alpha = 255
        paint.style = Paint.Style.FILL

        val pad = h * 0.045f
        val left = panel.left + stroke + pad
        val right = panel.right - stroke - pad
        val innerW = right - left

        // Top row: the date in red LED capitals, left-aligned.
        paint.typeface = tf
        paint.textAlign = Paint.Align.LEFT
        paint.letterSpacing = 0.12f
        paint.fontFeatureSettings = "lnum"
        paint.textSize = h * 0.034f
        val date = longDate(x).uppercase(Locale.getDefault())
        fit(paint, date, innerW)
        paint.color = red
        paint.alpha = if (x.night) 140 else 255
        val dateBase = panel.top + stroke + pad + paint.textSize * 0.95f
        c.drawText(date, left, dateBase, paint)
        paint.alpha = 255

        // Middle: the digits, glowing yellow, with AM/PM a third as tall to their right.
        paint.letterSpacing = 0.04f
        paint.textSize = h * 0.30f
        val am = x.ampm
        val gap = if (am != null) h * 0.03f else 0f
        var amW = 0f
        if (am != null) { paint.textSize = h * 0.10f; amW = paint.measureText(am); paint.textSize = h * 0.30f }
        // Shrink the digits until digits, gap and AM/PM all sit inside the panel.
        while (paint.measureText(x.timeText) + gap + amW > innerW && paint.textSize > 8f) {
            paint.textSize *= 0.95f
            if (am != null) { val ts = paint.textSize; paint.textSize = ts / 3f; amW = paint.measureText(am); paint.textSize = ts }
        }
        fit(paint, x.timeText, w * 0.70f)
        val ts = paint.textSize
        val tw = paint.measureText(x.timeText)
        val alarmBase = panel.bottom - stroke - pad
        val alarmSize = h * 0.034f
        val bandTop = dateBase + h * 0.02f
        val bandBottom = alarmBase - alarmSize * 1.1f
        val capH = ts * 0.72f
        val base = (bandTop + bandBottom) / 2f + capH / 2f
        val startX = w / 2f - (tw + gap + amW) / 2f
        paint.color = yellow
        if (x.night) paint.clearShadowLayer() else paint.setShadowLayer(h * 0.02f, 0f, 0f, withAlpha(YELLOW, 140))
        c.drawText(x.timeText, startX, base, paint)
        x.timeRect.set(startX - h * 0.03f, base - capH - h * 0.04f, startX + tw + gap + amW + h * 0.03f, base + h * 0.04f)
        if (am != null) {
            paint.textSize = ts / 3f
            if (!x.night) paint.setShadowLayer(h * 0.014f, 0f, 0f, withAlpha(YELLOW, 140))
            c.drawText(am, startX + tw + gap, base - ts * 0.05f, paint)
        }
        paint.clearShadowLayer()

        // Bottom row: the next alarm in green at the panel's left.
        paint.letterSpacing = 0.12f
        paint.textSize = alarmSize
        fit(paint, "⏰ ${x.nextAlarmText}", innerW)
        val fitted = paint.textSize
        drawAlarmLine(c, x, left, alarmBase, fitted, tf, align = Paint.Align.LEFT, color = green,
            alpha = if (x.night) 110 else 255, shadowed = false)
        paint.letterSpacing = 0f
        paint.fontFeatureSettings = null
        paint.textAlign = Paint.Align.CENTER
    }

    companion object {
        const val PANEL = 0xFF0B0D11.toInt()
        const val FRAME = 0xFF3A414C.toInt()
        const val RED = 0xFFFF5A5A.toInt()
        const val YELLOW = 0xFFFFD23F.toInt()
        const val GREEN = 0xFF6EE7A0.toInt()
    }
}
