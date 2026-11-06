package com.acechase.kidclock.looks

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

/**
 * A strip that appears at the bottom while backgrounds are being swiped through: the background's name
 * over the same pager dots as the look picker, drawn just as the picker draws them but in the background's
 * own ink, a touch lower so the name clears the quote card. The activity shows it on each swipe and fades it out once the swiping
 * stops. It never takes touches.
 */
class BackgroundStrip(context: Context) : View(context) {
    var name = ""
        set(v) { if (field != v) { field = v; invalidate() } }
    var index = 0
        set(v) { if (field != v) { field = v; invalidate() } }
    var count = 1
        set(v) { if (field != v) { field = v; invalidate() } }
    /** The background's own ink and shadow, so the strip reads on pale paper as well as a night sea. */
    var ink = 0xFFFFFFFF.toInt()
        set(v) { if (field != v) { field = v; invalidate() } }
    var shadow = 0
        set(v) { if (field != v) { field = v; invalidate() } }

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val rect = RectF()
    private val fonts = Fonts.get(context)

    init { isClickable = false }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        text.typeface = fonts.medium
        text.textSize = h * 0.05f
        text.color = ink
        if (shadow == 0) text.clearShadowLayer() else text.setShadowLayer(8f, 0f, 3f, shadow)
        c.drawText(name, w / 2f, h * 0.885f, text)
        text.clearShadowLayer()
        PagerDots.draw(c, w / 2f, h * 0.945f, h, index, count, ink, fill, rect)
    }
}
