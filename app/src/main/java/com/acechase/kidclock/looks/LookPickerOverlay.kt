package com.acechase.kidclock.looks

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View

/**
 * What sits over the clock while a look is being chosen: a "Choose a look" pill at the top,
 * chevrons at the edges, and the look's name over pager dots at the bottom. It never takes
 * touches; the activity handles swipes and taps on the root.
 */
class LookPickerOverlay(context: Context) : View(context) {
    var lookName = ""
        set(v) { if (field != v) { field = v; invalidate() } }
    var index = 0
        set(v) { if (field != v) { field = v; invalidate() } }
    var count = 1
        set(v) { if (field != v) { field = v; invalidate() } }
    var night = false
        set(v) { if (field != v) { field = v; invalidate() } }

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
    private val text = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private val path = Path()
    private val fonts = Fonts.get(context)

    init { isClickable = false }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val ink = if (night) AMBER else 0xFFFFFFFF.toInt()

        // Top pill.
        text.typeface = fonts.medium
        text.textSize = h * 0.04f
        val title = "Choose a look"
        val tw = text.measureText(title)
        text.typeface = fonts.regular
        text.textSize = h * 0.0325f
        val hint = "swipe sideways · tap to keep"
        val hw = text.measureText(hint)
        val gap = h * 0.022f
        val padX = w * 0.023f; val padY = h * 0.0175f
        val pillW = tw + gap + hw + padX * 2
        val pillH = h * 0.04f + padY * 2
        rect.set(w / 2f - pillW / 2f, h * 0.035f, w / 2f + pillW / 2f, h * 0.035f + pillH)
        fill.color = if (night) 0xCC000000.toInt() else 0x990A1E3C.toInt()
        c.drawRoundRect(rect, pillH / 2f, pillH / 2f, fill)
        if (night) {
            stroke.color = (AMBER and 0x00FFFFFF) or (0x80 shl 24)
            stroke.strokeWidth = 2f
            c.drawRoundRect(rect, pillH / 2f, pillH / 2f, stroke)
        }
        text.color = ink
        text.textAlign = Paint.Align.LEFT
        val baseline = rect.centerY() + h * 0.014f
        text.typeface = fonts.medium
        text.textSize = h * 0.04f
        c.drawText(title, rect.left + padX, baseline, text)
        text.typeface = fonts.regular
        text.textSize = h * 0.0325f
        text.alpha = 200
        c.drawText(hint, rect.left + padX + tw + gap, baseline, text)
        text.alpha = 255

        // Chevrons at the edges.
        stroke.color = ink
        stroke.alpha = 215
        stroke.strokeWidth = h * 0.006f
        val ch = h * 0.11f; val cw = h * 0.045f; val cy = h * 0.5f
        path.reset()
        path.moveTo(w * 0.03f + cw, cy - ch / 2f); path.lineTo(w * 0.03f, cy); path.lineTo(w * 0.03f + cw, cy + ch / 2f)
        c.drawPath(path, stroke)
        path.reset()
        path.moveTo(w * 0.97f - cw, cy - ch / 2f); path.lineTo(w * 0.97f, cy); path.lineTo(w * 0.97f - cw, cy + ch / 2f)
        c.drawPath(path, stroke)
        stroke.alpha = 255

        // Name and dots.
        text.typeface = fonts.medium
        text.textSize = h * 0.05f
        text.textAlign = Paint.Align.CENTER
        text.color = ink
        if (night) text.clearShadowLayer() else text.setShadowLayer(8f, 0f, 3f, 0x73000000)
        c.drawText(lookName, w / 2f, h * 0.875f, text)
        text.clearShadowLayer()
        val dot = h * 0.0175f; val active = h * 0.05f; val dgap = h * 0.0175f
        val dotsW = active + (count - 1) * dot + (count - 1) * dgap
        var dx = w / 2f - dotsW / 2f
        val dy = h * 0.935f
        for (i in 0 until count) {
            val dw = if (i == index) active else dot
            rect.set(dx, dy - dot / 2f, dx + dw, dy + dot / 2f)
            fill.color = ink
            fill.alpha = if (i == index) 255 else 128
            c.drawRoundRect(rect, dot / 2f, dot / 2f, fill)
            dx += dw + dgap
        }
        fill.alpha = 255
    }

    companion object {
        const val AMBER = 0xFFFF8A3D.toInt()
    }
}
