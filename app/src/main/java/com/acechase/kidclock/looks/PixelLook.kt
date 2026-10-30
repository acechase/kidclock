package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Paint
import java.util.Locale

/** Retro game: a pixel font with chunky block shadows and three pixel hearts for lives. */
class PixelLook : Look() {
    override val name = "Pixel"
    override fun labelTypeface(fonts: Fonts) = fonts.pressStart

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val tf = x.fonts.pressStart
        paint.style = Paint.Style.FILL
        paint.clearShadowLayer()
        paint.typeface = tf
        paint.textAlign = Paint.Align.CENTER

        paint.textSize = h * 0.2375f
        fit(paint, x.timeText, w * 0.78f)
        val ts = paint.textSize
        val tw = paint.measureText(x.timeText)
        val amW = if (x.ampm != null) h * 0.12f else 0f
        val cx = w / 2f - amW / 2f
        val base = h * 0.52f
        blockText(c, x, x.timeText, cx, base, h * 0.0175f)
        x.timeRect.set(cx - tw / 2f - h * 0.04f, base - ts - h * 0.04f, cx + tw / 2f + amW + h * 0.04f, base + h * 0.06f)

        x.ampm?.let { am ->
            paint.textSize = h * 0.05f
            paint.textAlign = Paint.Align.LEFT
            blockText(c, x, am, cx + tw / 2f + h * 0.045f, base - ts + h * 0.05f, h * 0.0075f)
            paint.textAlign = Paint.Align.CENTER
        }

        // Three lives.
        val hy = h * 0.575f
        val px = h * 0.005f
        val heartW = HEART[0].length * px
        for (k in -1..1) {
            val left = w / 2f + k * (heartW + h * 0.03f) - heartW / 2f
            drawHeart(c, x, left, hy, px)
        }

        paint.textSize = h * 0.045f
        paint.alpha = 255
        blockText(c, x, shortDate(x).uppercase(Locale.getDefault()), w / 2f, h * 0.70f, h * 0.0075f)

        if (x.showChrome && x.nextAlarmText.isNotEmpty() && !x.night) {
            paint.textSize = h * 0.03f
            paint.color = NAVY
            c.drawText("⏰ ${x.nextAlarmText.uppercase(Locale.getDefault())}", w / 2f + h * 0.005f, h * 0.92f + h * 0.005f, paint)
        }
        drawAlarmLine(c, x, w / 2f, h * 0.92f, h * 0.03f, tf, alpha = if (x.night) 110 else 255, shadowed = false)
    }

    /** White text with a hard navy block shadow by day; plain amber at night. */
    private fun blockText(c: Canvas, x: LookContext, s: String, cx: Float, base: Float, drop: Float) {
        if (!x.night) {
            paint.color = NAVY
            c.drawText(s, cx + drop, base + drop, paint)
        }
        paint.color = x.ink
        if (x.night) paint.alpha = 200
        c.drawText(s, cx, base, paint)
        paint.alpha = 255
    }

    private fun drawHeart(c: Canvas, x: LookContext, left: Float, top: Float, px: Float) {
        paint.color = if (x.night) withAlpha(x.ink, 150) else PINK
        for ((r, row) in HEART.withIndex()) for ((i, ch) in row.withIndex()) {
            if (ch == 'X') c.drawRect(left + i * px, top + r * px, left + (i + 1) * px, top + (r + 1) * px, paint)
        }
    }

    companion object {
        const val NAVY = 0xFF1E3A6E.toInt()
        const val PINK = 0xFFFF3D6E.toInt()
        private val HEART = arrayOf(
            ".XX...XX.",
            "XXXXXXXXX",
            "XXXXXXXXX",
            "XXXXXXXXX",
            ".XXXXXXX.",
            "..XXXXX..",
            "...XXX...",
            "....X....",
        )
    }
}
