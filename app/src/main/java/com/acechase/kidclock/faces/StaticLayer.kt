package com.acechase.kidclock.faces

import android.graphics.Bitmap
import android.graphics.Canvas

/**
 * A background that never moves (a notebook page, a ruled sheet) is painted once into a bitmap
 * and blitted every frame, so a page full of doodles costs nothing at 20 frames a second. The
 * bitmap is rebuilt when the size changes.
 */
class StaticLayer(private val paint: (Canvas, Float, Float) -> Unit) {
    private var bitmap: Bitmap? = null

    fun draw(c: Canvas, w: Float, h: Float) {
        var b = bitmap
        if (b == null || b.width != w.toInt() || b.height != h.toInt()) {
            b = Bitmap.createBitmap(w.toInt().coerceAtLeast(1), h.toInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888)
            paint(Canvas(b), w, h)
            bitmap = b
        }
        c.drawBitmap(b, 0f, 0f, null)
    }
}
