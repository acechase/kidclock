package com.acechase.kidclock.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.LinearGradient
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.cos
import kotlin.math.sin

/**
 * A stadium at night: a dark sky, a floodlight mast at each edge carrying a bank of eight lamps that
 * throw soft beams down onto the pitch, a strip of green pitch along the bottom with a white touchline,
 * a goal in the middle (posts, crossbar, side nets and a mesh net behind), and a soccer ball resting on
 * the grass. Nothing moves; the page is painted once. At night the lamp banks stay on as two very dim
 * amber bars.
 */
class StadiumFace(context: Context) : ClockFace(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val arcRect = RectF()
    private val ballClip = Path()
    private val poly = Path()
    private val layer = StaticLayer(::paintPage)

    override val dayTextColor = Color.WHITE
    override val dayShadowColor = 0x80000000.toInt()
    override fun dayBackgroundForMoonBite() = SKY

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        if (night) {
            c.drawColor(Color.BLACK)
            p.style = Paint.Style.FILL
            p.color = NIGHT_TEXT
            p.alpha = 38
            for (side in 0..1) {
                val cx = if (side == 0) w * LAMP_X else w * (1f - LAMP_X)
                c.drawRoundRect(cx - w * BANK_W / 2f, h * (LAMP_Y - BANK_H / 2f), cx + w * BANK_W / 2f, h * (LAMP_Y + BANK_H / 2f),
                    h * 0.006f, h * 0.006f, p)
            }
            p.alpha = 255
            return
        }
        layer.draw(c, w, h)
    }

    private fun paintPage(c: Canvas, w: Float, h: Float) {
        c.drawColor(SKY)
        val pitchTop = h * (1f - PITCH)

        // Pitch strip and touchline.
        p.reset(); p.isAntiAlias = true
        p.style = Paint.Style.FILL
        p.color = GRASS
        c.drawRect(0f, pitchTop, w, h, p)
        p.color = LINE
        c.drawRect(0f, pitchTop, w, pitchTop + h * 0.00625f, p)

        for (side in 0..1) drawFloodlight(c, w, h, pitchTop, side)
        drawGoal(c, w, h, pitchTop)

        // Soccer ball on the grass: a white disc, a black centre pentagon, five more pentagons at the
        // rim clipped to the disc, and seams joining them.
        drawBall(c, w * 0.91f, h * 0.905f, h * 0.0575f)
    }

    /**
     * A floodlight mast: a tapered grey pole up from the grass, two braces, a dark housing holding two
     * rows of four lamps, a warm glow round the bank, and a soft beam fanning down to the pitch.
     */
    private fun drawFloodlight(c: Canvas, w: Float, h: Float, pitchTop: Float, side: Int) {
        val cx = if (side == 0) w * LAMP_X else w * (1f - LAMP_X)
        val cy = h * LAMP_Y
        val bw = w * BANK_W; val bh = h * BANK_H
        val toward = if (side == 0) 1f else -1f   // the beam leans toward the middle of the pitch

        // Beam: a translucent wedge from the bank to a wide patch of pitch, fading as it falls.
        poly.reset()
        poly.moveTo(cx - bw * 0.45f, cy + bh * 0.5f)
        poly.lineTo(cx + bw * 0.45f, cy + bh * 0.5f)
        poly.lineTo(cx + toward * w * 0.30f + bw, h)
        poly.lineTo(cx + toward * w * 0.30f - bw * 2.2f, h)
        poly.close()
        glowPaint.shader = LinearGradient(cx, cy, cx + toward * w * 0.2f, h, 0x30FFF7CC, 0x00FFF7CC, Shader.TileMode.CLAMP)
        c.drawPath(poly, glowPaint)

        // Pole: slightly wider at the base; a head plate and two braces under the bank.
        p.style = Paint.Style.FILL
        p.color = POLE
        poly.reset()
        poly.moveTo(cx - h * 0.004f, cy + bh * 0.5f)
        poly.lineTo(cx + h * 0.004f, cy + bh * 0.5f)
        poly.lineTo(cx + h * 0.007f, pitchTop)
        poly.lineTo(cx - h * 0.007f, pitchTop)
        poly.close()
        c.drawPath(poly, p)
        p.style = Paint.Style.STROKE
        p.strokeWidth = h * 0.003f
        c.drawLine(cx - bw * 0.38f, cy + bh * 0.5f, cx, cy + bh * 1.6f, p)
        c.drawLine(cx + bw * 0.38f, cy + bh * 0.5f, cx, cy + bh * 1.6f, p)
        c.drawLine(cx - bw * 0.42f, cy + bh * 0.5f, cx + bw * 0.42f, cy + bh * 0.5f, p)

        // Glow behind the bank, then the housing and its eight lamps.
        glowPaint.shader = RadialGradient(cx, cy, bw * 1.4f, 0x5CFFF7CC, 0x00FFF7CC, Shader.TileMode.CLAMP)
        c.drawCircle(cx, cy, bw * 1.4f, glowPaint)
        p.style = Paint.Style.FILL
        p.color = HOUSING
        arcRect.set(cx - bw / 2f, cy - bh / 2f, cx + bw / 2f, cy + bh / 2f)
        c.drawRoundRect(arcRect, h * 0.008f, h * 0.008f, p)
        p.style = Paint.Style.STROKE
        p.strokeWidth = h * 0.002f
        p.color = HOUSING_EDGE
        c.drawRoundRect(arcRect, h * 0.008f, h * 0.008f, p)
        p.style = Paint.Style.FILL
        val lampR = bh * 0.19f
        for (row in 0..1) for (col in 0..3) {
            val lx = cx - bw / 2f + bw * (0.125f + 0.25f * col)
            val ly = cy - bh / 2f + bh * (0.28f + 0.44f * row)
            p.color = LAMP_RIM
            c.drawCircle(lx, ly, lampR * 1.25f, p)
            p.color = LAMP
            c.drawCircle(lx, ly, lampR, p)
        }
    }

    /**
     * The goal, seen from the front and a little above: white posts and crossbar standing on the
     * grass, the back of the net on the ground behind, side nets sloping back from the posts, and a
     * diamond mesh over everything inside the frame.
     */
    private fun drawGoal(c: Canvas, w: Float, h: Float, pitchTop: Float) {
        val half = w * GOAL_HALF
        val x0 = w / 2f - half; val x1 = w / 2f + half
        val yTop = h * GOAL_TOP
        val yBase = h * GOAL_BASE
        val yBack = yBase - h * 0.04f            // where the net meets the ground, behind the line
        val inset = w * 0.022f                   // the back of the net looks narrower from here
        val bar = h * 0.009f

        // Net: faint white fill inside the frame, then a diamond mesh clipped to it.
        poly.reset()
        poly.moveTo(x0, yTop); poly.lineTo(x1, yTop); poly.lineTo(x1, yBase); poly.lineTo(x0, yBase); poly.close()
        p.style = Paint.Style.FILL
        p.color = NET_FILL
        c.drawPath(poly, p)
        c.save()
        c.clipPath(poly)
        p.style = Paint.Style.STROKE
        p.strokeWidth = h * 0.0015f
        p.color = NET
        val step = h * 0.014f
        var d = -(yBase - yTop)
        while (d < x1 - x0 + (yBase - yTop)) {
            c.drawLine(x0 + d, yBase, x0 + d + (yBase - yTop), yTop, p)
            c.drawLine(x0 + d, yTop, x0 + d + (yBase - yTop), yBase, p)
            d += step
        }
        c.restore()

        // Back of the net on the ground and the side nets' ground edges, thin.
        p.strokeWidth = h * 0.004f
        p.color = NET_EDGE
        c.drawLine(x0 + inset, yBack, x1 - inset, yBack, p)
        c.drawLine(x0, yBase, x0 + inset, yBack, p)
        c.drawLine(x1, yBase, x1 - inset, yBack, p)
        c.drawLine(x0 + inset, yBack, x0, yTop, p)
        c.drawLine(x1 - inset, yBack, x1, yTop, p)

        // Shadow on the grass under the frame, then posts and crossbar in solid white with round ends.
        p.style = Paint.Style.FILL
        p.color = 0x33000000
        c.drawOval(x0 - bar, yBase - bar, x1 + bar, yBase + bar * 1.6f, p)
        p.style = Paint.Style.STROKE
        p.strokeCap = Paint.Cap.ROUND
        p.strokeWidth = bar
        p.color = Color.WHITE
        c.drawLine(x0, yBase, x0, yTop, p)
        c.drawLine(x1, yBase, x1, yTop, p)
        c.drawLine(x0, yTop, x1, yTop, p)
        p.strokeCap = Paint.Cap.BUTT
        p.style = Paint.Style.FILL
    }

    private fun drawBall(c: Canvas, cx: Float, cy: Float, r: Float) {
        c.save()
        c.translate(cx, cy)
        val s = r / 46f
        c.scale(s, s)
        p.style = Paint.Style.FILL
        p.color = Color.WHITE
        c.drawCircle(0f, 0f, 46f, p)
        ballClip.reset()
        ballClip.addCircle(0f, 0f, 46f, Path.Direction.CW)
        c.save()
        c.clipPath(ballClip)
        p.color = SEAM
        pentagon(0f, 0f, 18f, -90f)
        c.drawPath(poly, p)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 3f
        for (k in 0 until 5) {
            val a = Math.toRadians((-90 + 72 * k).toDouble())
            val dx = cos(a).toFloat(); val dy = sin(a).toFloat()
            c.drawLine(dx * 18f, dy * 18f, dx * 40f, dy * 40f, p)
            p.style = Paint.Style.FILL
            pentagon(dx * 54f, dy * 54f, 15f, (-90 + 72 * k + 180).toFloat())
            c.drawPath(poly, p)
            p.style = Paint.Style.STROKE
        }
        c.restore()
        c.drawCircle(0f, 0f, 44.5f, p)
        p.style = Paint.Style.FILL
        c.restore()
    }

    /** Fills [poly] with a regular pentagon of circumradius [rad] whose first vertex is at [startDeg]. */
    private fun pentagon(cx: Float, cy: Float, rad: Float, startDeg: Float) {
        poly.reset()
        for (k in 0 until 5) {
            val a = Math.toRadians((startDeg + 72f * k).toDouble())
            val x = cx + rad * cos(a).toFloat(); val y = cy + rad * sin(a).toFloat()
            if (k == 0) poly.moveTo(x, y) else poly.lineTo(x, y)
        }
        poly.close()
    }

    companion object {
        const val SKY = 0xFF171B22.toInt()
        const val GRASS = 0xFF2F7A3C.toInt()
        const val LINE = 0xCCFFFFFF.toInt()
        const val POLE = 0xFF3A414C.toInt()
        const val LAMP = 0xFFFFF7CC.toInt()
        const val LAMP_RIM = 0xFFB8A878.toInt()
        const val HOUSING = 0xFF262B33.toInt()
        const val HOUSING_EDGE = 0xFF5A6370.toInt()
        const val NET = 0x66FFFFFF
        const val NET_FILL = 0x14FFFFFF
        const val NET_EDGE = 0xB3FFFFFF.toInt()
        /** The goal mouth: half its width as a fraction of the width, crossbar and base as fractions of the height. */
        const val GOAL_HALF = 0.15f
        const val GOAL_TOP = 0.852f
        const val GOAL_BASE = 0.978f
        const val SEAM = 0xFF1F2A1F.toInt()
        /** The pitch is the bottom slice of the screen. */
        const val PITCH = 0.156f
        /** Floodlight heads sit below the lamp (top-left) and the weather (top-right) so both stay readable. */
        const val LAMP_X = 0.083f
        const val LAMP_Y = 0.30f
        /** The lamp bank: width as a fraction of the width, height of the height. */
        const val BANK_W = 0.085f
        const val BANK_H = 0.052f
    }
}
