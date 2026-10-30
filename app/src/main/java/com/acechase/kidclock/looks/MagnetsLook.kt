package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

/** Fridge magnets: each digit on its own tilted coloured tile, the date on a white one. */
class MagnetsLook : Look() {
    override val name = "Magnets"
    override fun labelTypeface(fonts: Fonts) = fonts.fredoka

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val tf = x.fonts.fredoka
        paint.style = Paint.Style.FILL
        paint.clearShadowLayer()
        paint.typeface = tf
        paint.textAlign = Paint.Align.CENTER

        val s = x.timeText
        val tileH = h * 0.3375f
        val gap = w * 0.02f
        val amSize = h * 0.1625f
        var total = 0f
        for (ch in s) total += tileWidth(ch, w)
        total += gap * (s.length - 1)
        if (x.ampm != null) total += gap + amSize
        // Shrink for 24-hour mode's five characters.
        val scale = (w * 0.9f / total).coerceAtMost(1f)
        val top = h * 0.1875f
        var cx = w / 2f - total * scale / 2f
        x.timeRect.set(cx - h * 0.03f, top - h * 0.04f, cx + total * scale + h * 0.03f, top + tileH + h * 0.04f)
        for ((i, ch) in s.withIndex()) {
            val tw = tileWidth(ch, w) * scale
            val colon = ch == ':'
            val fill = if (colon) YELLOW else COLORS[i % COLORS.size]
            val inkOnTile = if (colon) NAVY else Color.WHITE
            tile(c, x, cx, top, tw, tileH, ROTATIONS[i % ROTATIONS.size], fill, inkOnTile, ch.toString(), h * (if (colon) 0.2375f else 0.26f) * scale)
            cx += tw + gap * scale
        }
        x.ampm?.let { am ->
            tile(c, x, cx, top, amSize * scale, amSize * scale, 7f, PURPLE, Color.WHITE, am, h * 0.0775f * scale)
        }

        // Date on a white tile.
        val date = longDate(x)
        paint.textSize = h * 0.07f
        val dw = paint.measureText(date)
        val dcx = w / 2f; val dcy = h * 0.66f
        c.save()
        c.rotate(-1.5f, dcx, dcy)
        rect.set(dcx - dw / 2f - w * 0.03f, dcy - h * 0.05f, dcx + dw / 2f + w * 0.03f, dcy + h * 0.05f)
        if (x.night) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            paint.color = withAlpha(x.ink, 120)
            c.drawRoundRect(rect, h * 0.03f, h * 0.03f, paint)
            paint.style = Paint.Style.FILL
            paint.color = withAlpha(x.ink, 140)
        } else {
            paint.color = 0x2E000000
            rect.offset(0f, h * 0.01f)
            c.drawRoundRect(rect, h * 0.03f, h * 0.03f, paint)
            rect.offset(0f, -h * 0.01f)
            paint.color = Color.WHITE
            c.drawRoundRect(rect, h * 0.03f, h * 0.03f, paint)
            paint.color = NAVY
        }
        c.drawText(date, dcx, dcy + h * 0.025f, paint)
        c.restore()

        drawAlarmLine(c, x, w / 2f, h * 0.92f, h * 0.045f, tf)
    }

    private fun tileWidth(ch: Char, w: Float): Float = if (ch == ':') w * 0.086f else w * 0.18f

    private fun tile(c: Canvas, x: LookContext, left: Float, top: Float, tw: Float, th: Float, rot: Float, fill: Int, textColor: Int, s: String, textSize: Float) {
        val cx = left + tw / 2f; val cy = top + th / 2f
        val radius = x.h * 0.0375f
        c.save()
        c.rotate(rot, cx, cy)
        rect.set(left, top, left + tw, top + th)
        if (x.night) {
            paint.color = withAlpha(x.ink, 0x22)
            c.drawRoundRect(rect, radius, radius, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            paint.color = withAlpha(x.ink, 150)
            c.drawRoundRect(rect, radius, radius, paint)
            paint.style = Paint.Style.FILL
            paint.color = x.ink
        } else {
            paint.color = 0x33000000
            rect.offset(0f, x.h * 0.0125f)
            c.drawRoundRect(rect, radius, radius, paint)
            rect.offset(0f, -x.h * 0.0125f)
            paint.color = fill
            c.drawRoundRect(rect, radius, radius, paint)
            // A darker lip along the bottom so the tile looks thick.
            c.save()
            c.clipRect(left, top + th - x.h * 0.0125f, left + tw, top + th)
            paint.color = 0x1F000000
            c.drawRoundRect(rect, radius, radius, paint)
            c.restore()
            paint.color = textColor
        }
        paint.textSize = textSize
        c.drawText(s, cx, cy + textSize * 0.36f, paint)
        c.restore()
    }

    companion object {
        const val NAVY = 0xFF1E3A6E.toInt()
        const val YELLOW = 0xFFFFD93D.toInt()
        const val PURPLE = 0xFFB265E6.toInt()
        private val COLORS = intArrayOf(0xFFFF6B6B.toInt(), YELLOW, 0xFF6BCB77.toInt(), 0xFF4D96FF.toInt(), PURPLE, 0xFFFF9F43.toInt())
        private val ROTATIONS = floatArrayOf(-4f, 3f, 2f, -3f, 4f, -2f)
    }
}
