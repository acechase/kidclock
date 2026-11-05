package com.acechase.kidclock.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.cos
import kotlin.math.sin

/**
 * A stadium at night: a dark sky, two floodlights glowing on tall grey poles at the left and right
 * edges, a strip of green pitch along the bottom with a white touchline and the goal arc, and a
 * soccer ball resting on the grass. Nothing moves; the page is painted once. At night the floodlights
 * stay on as two very dim amber discs.
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
            c.drawCircle(w * LAMP_X, h * LAMP_Y, h * LAMP_R, p)
            c.drawCircle(w * (1f - LAMP_X), h * LAMP_Y, h * LAMP_R, p)
            p.alpha = 255
            return
        }
        layer.draw(c, w, h)
    }

    private fun paintPage(c: Canvas, w: Float, h: Float) {
        c.drawColor(SKY)
        val pitchTop = h * (1f - PITCH)

        // Pitch strip, touchline and the goal arc.
        p.reset(); p.isAntiAlias = true
        p.style = Paint.Style.FILL
        p.color = GRASS
        c.drawRect(0f, pitchTop, w, h, p)
        p.color = LINE
        c.drawRect(0f, pitchTop, w, pitchTop + h * 0.00625f, p)
        p.style = Paint.Style.STROKE
        p.strokeWidth = h * 0.00625f
        val arcHalf = w * 0.111f
        val arcH = h * 0.104f
        arcRect.set(w / 2f - arcHalf, pitchTop - arcH, w / 2f + arcHalf, pitchTop + arcH)
        c.drawArc(arcRect, 180f, 180f, false, p)

        // Floodlights: a grey pole up from the pitch, a warm disc with a soft glow at the top.
        for (side in 0..1) {
            val cx = if (side == 0) w * LAMP_X else w * (1f - LAMP_X)
            val cy = h * LAMP_Y
            val r = h * LAMP_R
            p.style = Paint.Style.FILL
            p.color = POLE
            c.drawRect(cx - h * 0.003f, cy + r * 0.6f, cx + h * 0.003f, pitchTop, p)
            glowPaint.shader = RadialGradient(cx, cy, r * 3.2f, 0x66FFF7CC, 0x00FFF7CC, Shader.TileMode.CLAMP)
            c.drawCircle(cx, cy, r * 3.2f, glowPaint)
            p.color = LAMP
            p.alpha = 230
            c.drawCircle(cx, cy, r, p)
            p.alpha = 255
        }

        // Soccer ball on the grass: a white disc, a black centre pentagon, five more pentagons at the
        // rim clipped to the disc, and seams joining them.
        drawBall(c, w * 0.667f, h * 0.896f, h * 0.0575f)
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
        const val SEAM = 0xFF1F2A1F.toInt()
        /** The pitch is the bottom slice of the screen. */
        const val PITCH = 0.156f
        /** Floodlight heads sit below the lamp (top-left) and the weather (top-right) so both stay readable. */
        const val LAMP_X = 0.083f
        const val LAMP_Y = 0.30f
        const val LAMP_R = 0.031f
    }
}
