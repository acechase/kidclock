package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path

/**
 * Each digit of the time floats in its own light-blue bubble with a darker crescent for
 * roundness, the colon between them, the date and alarm line in white underneath. Made for the
 * Night Reef background but it sits on any of them. At night the bubbles become amber rings.
 */
class BubbleLook : Look() {
    override val name = "Bubble"
    override fun labelTypeface(fonts: Fonts) = fonts.fredoka

    private val bubble = Paint(Paint.ANTI_ALIAS_FLAG)
    private val digit = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        fontFeatureSettings = "lnum"
    }
    private val outer = Path()
    private val inner = Path()
    private val crescent = Path()
    private var crescentD = -1f

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val tf = x.fonts.fredoka
        val blue = if (x.night) x.ink else BLUE
        val navy = if (x.night) x.ink else NAVY
        val white = if (x.night) x.ink else Color.WHITE
        val text = x.timeText
        val am = x.ampm
        digit.typeface = tf
        digit.clearShadowLayer()

        // Measure the row at full size, then shrink everything together so "12:00" fits w*0.80.
        var d = h * 0.30f
        var gap = h * 0.019f
        digit.textSize = d * 0.58f
        var colonW = digit.measureText(":")
        var amW = 0f
        if (am != null) {
            digit.textSize = d * 0.74f / 3f
            amW = digit.measureText(am) + gap
        }
        var total = amW + gap * (text.length - 1)
        for (ch in text) total += if (ch == ':') colonW else d
        val maxW = w * 0.80f
        if (total > maxW) {
            val k = maxW / total
            d *= k; gap *= k; colonW *= k; amW *= k; total = maxW
        }
        val ts = d * 0.74f
        val colonTs = d * 0.58f
        val cy = h * 0.345f
        var left = w / 2f - total / 2f
        x.timeRect.set(left - h * 0.02f, cy - d / 2f - h * 0.02f, left + total + h * 0.02f, cy + d / 2f + h * 0.02f)

        if (!x.night && crescentD != d) {
            crescentD = d
            outer.reset(); outer.addCircle(0f, 0f, d / 2f, Path.Direction.CW)
            inner.reset(); inner.addCircle(-d * 0.055f, -d * 0.055f, d / 2f, Path.Direction.CW)
            crescent.op(outer, inner, Path.Op.DIFFERENCE)
        }

        for (i in text.indices) {
            if (text[i] == ':') {
                digit.textSize = colonTs
                digit.color = blue
                c.drawText(text, i, i + 1, left + colonW / 2f, cy + colonTs * 0.3f, digit)
                left += colonW + gap
            } else {
                val cx = left + d / 2f
                if (x.night) {
                    bubble.style = Paint.Style.STROKE
                    bubble.strokeWidth = h * 0.008f
                    bubble.color = x.ink
                    c.drawCircle(cx, cy, d / 2f - bubble.strokeWidth / 2f, bubble)
                } else {
                    bubble.style = Paint.Style.FILL
                    bubble.color = BLUE
                    c.drawCircle(cx, cy, d / 2f, bubble)
                    bubble.color = NAVY
                    bubble.alpha = 38
                    c.save()
                    c.translate(cx, cy)
                    c.drawPath(crescent, bubble)
                    c.restore()
                    bubble.alpha = 255
                }
                digit.textSize = ts
                digit.color = navy
                c.drawText(text, i, i + 1, cx, cy + ts * 0.36f, digit)
                left += d + gap
            }
        }

        if (am != null) {
            digit.textSize = ts / 3f
            digit.color = blue
            digit.textAlign = Paint.Align.LEFT
            c.drawText(am, left, cy - d * 0.10f, digit)
            digit.textAlign = Paint.Align.CENTER
        }

        // Date under the bubbles.
        digit.textSize = h * 0.06f
        digit.color = white
        digit.alpha = if (x.night) 140 else 230
        shadow(digit, x, 8f, 3f)
        c.drawText(longDate(x), w / 2f, cy + d / 2f + h * 0.095f, digit)
        digit.alpha = 255
        digit.clearShadowLayer()

        drawAlarmLine(c, x, w / 2f, h * 0.935f, h * 0.045f, tf, color = white)
    }

    companion object {
        const val BLUE = 0xFF7FD3E8.toInt()
        const val NAVY = 0xFF0F2A44.toInt()
    }
}
