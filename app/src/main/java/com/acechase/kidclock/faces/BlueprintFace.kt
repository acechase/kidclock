package com.acechase.kidclock.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.acechase.kidclock.art.BlueprintArt
import com.acechase.kidclock.looks.Fonts

/**
 * An engineer's blueprint: drafting grid on blueprint blue inside a double border, six numbered
 * figures inked in white line (a rocket, a tank and a one-eyed monster down the left; a robot, a
 * T-rex and a UFO down the right) with dimension ticks beside them, and a title block in the
 * bottom-right corner. Nothing moves; it is painted once. At night the grid alone stays, very dim.
 */
class BlueprintFace(context: Context) : ClockFace(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ink = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fonts = Fonts.get(context)
    private val layer = StaticLayer(::paintSheet)

    override val dayTextColor = Color.WHITE
    override val dayShadowColor = 0x59000000
    override fun dayBackgroundForMoonBite() = BLUE

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        if (night) {
            c.drawColor(Color.BLACK)
            drawGrid(c, w, h, NIGHT_TEXT, 16, 22, h / 960f)
            return
        }
        layer.draw(c, w, h)
    }

    /** Minor lines every h/24, a heavier major line every fifth, from the top-left corner. */
    private fun drawGrid(c: Canvas, w: Float, h: Float, color: Int, minorAlpha: Int, majorAlpha: Int, s: Float) {
        val step = h / 24f
        p.style = Paint.Style.STROKE
        p.strokeCap = Paint.Cap.BUTT
        p.pathEffect = null
        p.color = color
        for (pass in 0..1) {
            val major = pass == 1
            p.strokeWidth = (if (major) 1.5f else 1f) * s
            p.alpha = if (major) majorAlpha else minorAlpha
            var i = 0
            while (i * step <= w) {
                if ((i % 5 == 0) == major) c.drawLine(i * step, 0f, i * step, h, p)
                i++
            }
            i = 0
            while (i * step <= h) {
                if ((i % 5 == 0) == major) c.drawLine(0f, i * step, w, i * step, p)
                i++
            }
        }
        p.alpha = 255
    }

    private fun paintSheet(c: Canvas, w: Float, h: Float) {
        val s = h / 960f
        c.drawColor(BLUE)
        drawGrid(c, w, h, Color.WHITE, 36, 71, s)

        // Double border inset from the edges.
        p.style = Paint.Style.STROKE
        p.color = Color.WHITE
        p.strokeWidth = 2f * s; p.alpha = 179
        val in1 = h * 0.025f
        c.drawRect(in1, in1, w - in1, h - in1, p)
        p.strokeWidth = 1f * s; p.alpha = 102
        val in2 = h * 0.0375f
        c.drawRect(in2, in2, w - in2, h - in2, p)
        p.alpha = 255

        // The figures: three down the left, three down the right. The right column sits lower than
        // the left so the robot stays out of the weather's corner (right 30%, top 24%).
        BlueprintArt.draw(c, ink, BlueprintArt.rocket, w * 0.104f, h * 0.26f, h * 0.1875f, -10f, Color.WHITE)
        BlueprintArt.draw(c, ink, BlueprintArt.tank, w * 0.094f, h * 0.505f, h * 0.177f, 0f, Color.WHITE)
        BlueprintArt.draw(c, ink, BlueprintArt.monster, w * 0.104f, h * 0.75f, h * 0.1667f, 0f, Color.WHITE)
        BlueprintArt.draw(c, ink, BlueprintArt.robot, w * 0.899f, h * 0.355f, h * 0.155f, 0f, Color.WHITE)
        BlueprintArt.draw(c, ink, BlueprintArt.trex, w * 0.896f, h * 0.545f, h * 0.17f, 0f, Color.WHITE)
        BlueprintArt.draw(c, ink, BlueprintArt.ufo, w * 0.896f, h * 0.755f, h * 0.16f, -6f, Color.WHITE)

        // Dimension ticks: one beside the rocket, one under it, one beside the robot.
        p.style = Paint.Style.STROKE
        p.strokeWidth = 1.5f * s; p.alpha = 179
        val tick = h * 0.00625f
        var x = w * 0.1736f
        c.drawLine(x, h * 0.175f, x, h * 0.345f, p)
        c.drawLine(x - tick, h * 0.175f, x + tick, h * 0.175f, p)
        c.drawLine(x - tick, h * 0.345f, x + tick, h * 0.345f, p)
        val y = h * 0.3594f
        c.drawLine(w * 0.0417f, y, w * 0.1597f, y, p)
        c.drawLine(w * 0.0417f, y - tick, w * 0.0417f, y + tick, p)
        c.drawLine(w * 0.1597f, y - tick, w * 0.1597f, y + tick, p)
        x = w * 0.826f
        c.drawLine(x, h * 0.29f, x, h * 0.42f, p)
        c.drawLine(x - tick, h * 0.29f, x + tick, h * 0.29f, p)
        c.drawLine(x - tick, h * 0.42f, x + tick, h * 0.42f, p)
        p.alpha = 255

        // Figure labels.
        text.typeface = fonts.medium
        text.color = Color.WHITE
        text.alpha = 217
        text.textAlign = Paint.Align.LEFT
        text.textSize = h * 0.01875f
        text.letterSpacing = 0.1f
        c.drawText("FIG. 1  ROCKET", w * 0.07f, h * 0.165f, text)
        c.drawText("FIG. 2  TANK", w * 0.0486f, h * 0.4063f, text)
        c.drawText("FIG. 3  MONSTER", w * 0.0625f, h * 0.6563f, text)
        c.drawText("FIG. 4  ROBOT", w * 0.8542f, h * 0.265f, text)
        c.drawText("FIG. 5  T-REX", w * 0.8472f, h * 0.455f, text)
        c.drawText("FIG. 6  UFO", w * 0.8542f, h * 0.665f, text)

        // Title block, tucked into the bottom-right corner against the inner border.
        val bw = w * 0.32f; val bh = h * 0.12f
        val right = w - in2; val bottom = h - in2
        val left = right - bw; val top = bottom - bh
        p.strokeWidth = 2f * s; p.alpha = 230
        c.drawRect(left, top, right, bottom, p)
        val r1 = top + bh * 0.328f; val r2 = top + bh * 0.664f
        val c1 = left + bw * 0.5f; val c2 = left + bw * 0.752f
        c.drawLine(left, r1, right, r1, p)
        c.drawLine(left, r2, right, r2, p)
        c.drawLine(c1, r1, c1, bottom, p)
        c.drawLine(c2, r2, c2, bottom, p)
        p.alpha = 255
        val pad = h * 0.0146f
        text.alpha = 242
        text.letterSpacing = 0.15f
        cell(c, "KIDCLOCK · ROOM CLOCK", left + pad, top + bh * 0.224f, h * 0.0208f, bw - pad * 2f)
        text.letterSpacing = 0.125f
        val small = h * 0.0167f
        cell(c, "DRAWN BY", left + pad, top + bh * 0.552f, small, c1 - left - pad * 2f)
        cell(c, "CHECKED BY", c1 + pad, top + bh * 0.552f, small, right - c1 - pad * 2f)
        cell(c, "SHEET 1 OF 1", left + pad, top + bh * 0.879f, small, c1 - left - pad * 2f)
        cell(c, "SCALE 1 : 1", c1 + pad, top + bh * 0.879f, small, c2 - c1 - pad * 2f)
        cell(c, "REV A", c2 + pad, top + bh * 0.879f, small, right - c2 - pad * 2f)
        text.letterSpacing = 0f
        text.alpha = 255
    }

    /** One title-block entry at [size], shrunk if the cell is too narrow for it. */
    private fun cell(c: Canvas, s: String, x: Float, baseline: Float, size: Float, maxW: Float) {
        text.textSize = size
        while (text.measureText(s) > maxW && text.textSize > 6f) text.textSize *= 0.95f
        c.drawText(s, x, baseline, text)
    }

    companion object {
        const val BLUE = 0xFF1D4F8C.toInt()
    }
}
