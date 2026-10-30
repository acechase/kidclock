package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.random.Random

/**
 * Hand-drawn: marker lettering on a torn paper scrap stuck on with tape, a scribbled underline,
 * a circled AM and a doodled star. At night the paper goes away and the lettering turns amber.
 */
class SketchLook : Look() {
    override val name = "Sketch"
    override fun labelTypeface(fonts: Fonts) = fonts.patrickHand
    override val quoteInk: Int get() = INK
    override fun quoteBox(w: Float, h: Float) = RectF(w * 0.22f, h * 0.745f, w * 0.78f, h * 0.825f)

    private val paper = Path()
    private var paperW = 0f
    private var paperH = 0f
    private val scribble = Path()

    override fun draw(c: Canvas, x: LookContext) {
        val w = x.w; val h = x.h
        val ink = if (x.night) x.ink else INK
        val pcx = w / 2f; val pcy = h * 0.50f
        x.timeRect.set(w * 0.15f, h * 0.16f, w * 0.85f, h * 0.70f)

        if (!x.night) {
            paperPath(w, h)
            paint.style = Paint.Style.FILL
            paint.clearShadowLayer()
            c.save()
            c.rotate(-2f, pcx, pcy)
            paint.color = 0x4D000000
            c.translate(0f, h * 0.0175f)
            c.drawPath(paper, paint)
            c.translate(0f, -h * 0.0175f)
            paint.color = PAPER
            c.drawPath(paper, paint)
            c.restore()
            c.save()
            c.rotate(3f, w / 2f, h * 0.16f)
            paint.color = 0xCCFFEB78.toInt()
            rect.set(w / 2f - w * 0.0625f, h * 0.133f, w / 2f + w * 0.0625f, h * 0.188f)
            c.drawRect(rect, paint)
            c.restore()
        }

        c.save()
        c.rotate(-2f, pcx, pcy)
        paint.style = Paint.Style.FILL
        paint.typeface = x.fonts.caveat
        paint.textAlign = Paint.Align.CENTER
        paint.color = ink
        paint.textSize = h * 0.40f
        paint.clearShadowLayer()
        fit(paint, x.timeText, w * 0.60f)
        c.drawText(x.timeText, w / 2f, h * 0.56f, paint)
        val tw = paint.measureText(x.timeText)

        // Scribbled underline, two strokes like a marker going back over itself.
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = if (x.night) withAlpha(x.ink, 150) else RED
        scribble.reset()
        val ul = w / 2f - tw / 2f; val ur = w / 2f + tw / 2f; val uy = h * 0.615f
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

        // Date in a neater hand.
        paint.style = Paint.Style.FILL
        paint.typeface = x.fonts.patrickHand
        paint.textSize = h * 0.075f
        paint.color = ink
        paint.alpha = if (x.night) 140 else 255
        c.drawText(longDate(x), w / 2f, h * 0.71f, paint)
        paint.alpha = 255
        c.restore()

        // AM/PM with a wobbly circle round it.
        x.ampm?.let { am ->
            val ax = w * 0.80f; val ay = h * 0.30f
            c.save()
            c.rotate(6f, ax, ay)
            paint.typeface = x.fonts.patrickHand
            paint.textSize = h * 0.0775f
            paint.color = ink
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
            c.restore()
        }

        // A doodled star near the corner of the paper.
        val sx = w * 0.845f; val sy = h * 0.72f; val sr = h * 0.05f
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.strokeJoin = Paint.Join.ROUND
        paint.color = ink
        paint.alpha = if (x.night) 100 else 255
        scribble.reset()
        for (i in 0 until 5) {
            val a = Math.toRadians((-90 + i * 144).toDouble())
            val px = sx + (Math.cos(a) * sr).toFloat(); val py = sy + (Math.sin(a) * sr).toFloat()
            if (i == 0) scribble.moveTo(px, py) else scribble.lineTo(px, py)
        }
        scribble.close()
        c.save()
        c.rotate(14f, sx, sy)
        c.drawPath(scribble, paint)
        c.restore()
        paint.alpha = 255
        paint.style = Paint.Style.FILL

        drawAlarmLine(c, x, w / 2f, h * 0.92f, h * 0.05f, x.fonts.patrickHand)
    }

    /** The torn edge: a rectangle whose sides wander a little, the same way every frame. */
    private fun paperPath(w: Float, h: Float) {
        if (paperW == w && paperH == h) return
        paperW = w; paperH = h
        val rnd = Random(11)
        val l = w * 0.148f; val t = h * 0.1625f; val r = w * 0.852f; val b = h * 0.8375f
        val j = h * 0.008f
        fun jit() = (rnd.nextFloat() * 2f - 1f) * j
        val n = 16
        paper.reset()
        paper.moveTo(l + jit(), t + jit())
        for (i in 1..n) paper.lineTo(l + (r - l) * i / n + jit(), t + jit())
        for (i in 1..n) paper.lineTo(r + jit(), t + (b - t) * i / n + jit())
        for (i in 1..n) paper.lineTo(r - (r - l) * i / n + jit(), b + jit())
        for (i in 1..n) paper.lineTo(l + jit(), b - (b - t) * i / n + jit())
        paper.close()
    }

    companion object {
        const val INK = 0xFF2A2F45.toInt()
        const val PAPER = 0xFFFFF8E7.toInt()
        const val RED = 0xFFE05252.toInt()
    }
}
