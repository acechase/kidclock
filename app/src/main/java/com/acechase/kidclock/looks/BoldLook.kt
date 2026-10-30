package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

/**
 * Chunky rounded digits with a navy outline and a hard drop, AM/PM in a navy pill, the date on a
 * white pill. At night the outline and pills fade to amber tints.
 */
class BoldLook : Look() {
    override val name = "Bold"
    override fun labelTypeface(fonts: Fonts) = fonts.fredoka

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val tf = x.fonts.fredoka
        val navy = if (x.night) withAlpha(x.ink, 0x66) else NAVY
        val white = if (x.night) x.ink else Color.WHITE

        paint.style = Paint.Style.FILL
        paint.typeface = tf
        paint.textAlign = Paint.Align.CENTER
        paint.clearShadowLayer()
        paint.textSize = h * 0.46f
        fit(paint, x.timeText, w * 0.70f)
        val ts = paint.textSize
        val tw = paint.measureText(x.timeText)
        val pillW = if (x.ampm != null) h * 0.16f else 0f
        val cx = w / 2f - pillW / 2f
        val base = h * 0.56f
        if (!x.night) {
            paint.color = NAVY
            c.drawText(x.timeText, cx, base + h * 0.015f, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = h * 0.02f
            paint.strokeJoin = Paint.Join.ROUND
            c.drawText(x.timeText, cx, base, paint)
            paint.style = Paint.Style.FILL
        }
        paint.color = white
        c.drawText(x.timeText, cx, base, paint)
        x.timeRect.set(cx - tw / 2f - h * 0.04f, base - ts * 0.75f, cx + tw / 2f + pillW + h * 0.04f, base + h * 0.03f)

        x.ampm?.let { am ->
            paint.textSize = h * 0.07f
            val aw = paint.measureText(am)
            val pl = cx + tw / 2f + h * 0.035f
            val pb = base - h * 0.0375f
            rect.set(pl, pb - h * 0.11f, pl + aw + h * 0.055f, pb)
            paint.color = navy
            c.drawRoundRect(rect, h * 0.055f, h * 0.055f, paint)
            paint.color = white
            paint.textAlign = Paint.Align.LEFT
            c.drawText(am, pl + h * 0.0275f, pb - h * 0.035f, paint)
            paint.textAlign = Paint.Align.CENTER
        }

        val date = longDate(x)
        paint.textSize = h * 0.07f
        val dw = paint.measureText(date)
        val dcy = h * 0.675f
        if (!x.night) {
            rect.set(w / 2f - dw / 2f - h * 0.0425f, dcy - h * 0.0525f, w / 2f + dw / 2f + h * 0.0425f, dcy + h * 0.0525f)
            paint.color = 0xD9FFFFFF.toInt()
            c.drawRoundRect(rect, h * 0.0525f, h * 0.0525f, paint)
        }
        paint.color = if (x.night) x.ink else NAVY
        if (x.night) paint.alpha = 140
        c.drawText(date, w / 2f, dcy + h * 0.025f, paint)
        paint.alpha = 255

        if (x.showChrome && x.nextAlarmText.isNotEmpty() && !x.night) {
            paint.textSize = h * 0.045f
            paint.color = NAVY
            c.drawText("⏰ ${x.nextAlarmText}", w / 2f, h * 0.92f + h * 0.004f, paint)
        }
        drawAlarmLine(c, x, w / 2f, h * 0.92f, h * 0.045f, tf, color = white, alpha = if (x.night) 110 else 255, shadowed = false)
    }

    companion object {
        const val NAVY = 0xFF1E3A6E.toInt()
    }
}
