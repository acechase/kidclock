package com.acechase.kidclock.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.sin
import kotlin.random.Random

/**
 * A reef after dark: deep sea blue over a sandy floor, four seaweed fronds swaying, a pale moon
 * up top, bubbles rising and wobbling their way to the surface, and a hermit crab ambling about
 * at the bottom right. Redrawn every frame. At night only a few dim bubbles keep rising.
 */
class NightReefFace(context: Context) : ClockFace(context) {
    /** Positions and sizes are fractions of the screen: x of w, everything else of h. */
    private class Bubble(val x: Float, val r: Float, val speed: Float, val offset: Float, val wobble: Float, val rate: Float, val phase: Float, val alpha: Int)
    private class Frond(val fromRight: Boolean, val inset: Float, val width: Float, val height: Float, val rot: Float, val color: Int, val phase: Float)

    private val rnd = Random(17)
    private val bubbles = List(12) {
        Bubble(
            x = 0.05f + rnd.nextFloat() * 0.90f,
            r = 0.006f + rnd.nextFloat() * 0.010f,
            speed = 0.035f + rnd.nextFloat() * 0.035f,
            offset = rnd.nextFloat() * 1.15f,
            wobble = 0.006f + rnd.nextFloat() * 0.010f,
            rate = 0.8f + rnd.nextFloat() * 0.8f,
            phase = rnd.nextFloat() * 6.28f,
            alpha = 90 + rnd.nextInt(40),
        )
    }
    private val fronds = listOf(
        Frond(false, 0.149f, 0.048f, 0.375f, -8f, WEED_DARK, 0f),
        Frond(false, 0.219f, 0.042f, 0.292f, 6f, WEED_LIGHT, 1.5f),
        Frond(true, 0.143f, 0.048f, 0.333f, 7f, WEED_DARK, 3f),
        Frond(true, 0.085f, 0.0375f, 0.250f, -5f, WEED_LIGHT, 4.5f),
    )

    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    // The crab is authored in a 200x140 box (see the artboard) and scaled to the screen.
    private val spiral = Path().apply {
        moveTo(80f, 70f)
        arcTo(RectF(80f, 30f, 160f, 110f), 180f, 180f)
        arcTo(RectF(100f, 40f, 160f, 100f), 0f, 180f)
        arcTo(RectF(100f, 50f, 140f, 90f), 180f, 180f)
        arcTo(RectF(120f, 60f, 140f, 80f), 0f, 180f)
    }
    private val bodyRect = RectF(36f, 74f, 88f, 110f)
    private val claw = Path().apply {
        moveTo(40f, 88f); lineTo(18f, 80f); lineTo(24f, 92f); lineTo(14f, 98f)
    }

    override val dayTextColor = Color.WHITE
    override fun dayBackgroundForMoonBite() = SEA

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        val t = nowMs / 1000.0
        p.style = Paint.Style.FILL
        if (night) {
            c.drawColor(Color.BLACK)
            p.style = Paint.Style.STROKE
            p.strokeWidth = 2f
            p.color = NIGHT_TEXT
            for (b in bubbles.take(5)) {
                p.alpha = 45
                drawBubble(c, w, h, t, b)
            }
            p.alpha = 255
            return
        }

        c.drawColor(SEA)
        // Sea floor band.
        p.color = FLOOR
        c.drawRect(0f, h * 0.88f, w, h, p)

        // Seaweed rooted in the floor, swaying from the base.
        val baseY = h * 0.896f
        for (f in fronds) {
            val cx = if (f.fromRight) w - f.inset * h else f.inset * h
            val sway = f.rot + 3f * sin(t / 1.5 + f.phase).toFloat()
            val hw = f.width * h / 2f
            c.save()
            c.rotate(sway, cx, baseY)
            rect.set(cx - hw, baseY - f.height * h, cx + hw, baseY)
            p.color = f.color
            c.drawRoundRect(rect, hw, hw, p)
            c.restore()
        }

        // Bubbles rising.
        p.style = Paint.Style.STROKE
        p.strokeWidth = h * 0.003f
        p.color = Color.WHITE
        for (b in bubbles) {
            p.alpha = b.alpha
            drawBubble(c, w, h, t, b)
        }
        p.alpha = 255
        p.style = Paint.Style.FILL

        // Moon, top centre-right (left of the weather).
        p.color = MOON
        c.drawCircle(w * 0.62f, h * 0.125f, h * 0.0625f, p)

        drawCrab(c, w, h)
    }

    /** One ring, rising from below the floor to above the top and wrapping round again. */
    private fun drawBubble(c: Canvas, w: Float, h: Float, t: Double, b: Bubble) {
        val cycle = ((t * b.speed + b.offset) % 1.15).toFloat()
        val y = (1.08f - cycle) * h
        val x = (b.x + b.wobble * sin(t * b.rate + b.phase).toFloat()) * w
        c.drawCircle(x, y, b.r * h, p)
    }

    /** Hermit crab at bottom-right: tan spiral shell, orange body, eyestalks, a claw and legs. */
    private fun drawCrab(c: Canvas, w: Float, h: Float) {
        val u = h * 0.001875f
        val ox = w - h * 0.125f - 200f * u
        val oy = h * 0.719f
        c.save()
        c.translate(ox, oy)
        c.scale(u, u)
        // Shell
        p.style = Paint.Style.FILL
        p.color = SHELL
        c.drawCircle(120f, 70f, 55f, p)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 4f
        p.strokeCap = Paint.Cap.ROUND
        p.strokeJoin = Paint.Join.ROUND
        p.color = SHELL_LINE
        c.drawCircle(120f, 70f, 55f, p)
        c.drawPath(spiral, p)
        // Body
        p.style = Paint.Style.FILL
        p.color = BODY
        c.drawOval(bodyRect, p)
        p.style = Paint.Style.STROKE
        p.color = BODY_LINE
        c.drawOval(bodyRect, p)
        // Legs
        p.strokeWidth = 5f
        c.drawLine(50f, 105f, 36f, 127f, p)
        c.drawLine(62f, 108f, 58f, 132f, p)
        c.drawLine(74f, 106f, 84f, 128f, p)
        // Claw
        p.strokeWidth = 6f
        c.drawPath(claw, p)
        // Eyestalks and eyes
        p.strokeWidth = 4f
        c.drawLine(54f, 78f, 46f, 56f, p)
        c.drawLine(68f, 76f, 70f, 52f, p)
        p.style = Paint.Style.FILL
        p.color = EYE
        c.drawCircle(46f, 54f, 6f, p)
        c.drawCircle(70f, 50f, 6f, p)
        c.restore()
    }

    companion object {
        const val SEA = 0xFF0F2A44.toInt()
        const val FLOOR = 0xFF1C3A52.toInt()
        const val WEED_DARK = 0xFF2E8B6A.toInt()
        const val WEED_LIGHT = 0xFF3AA37C.toInt()
        const val MOON = 0xE6FFF3C4.toInt()
        const val SHELL = 0xFFD99A5B.toInt()
        const val SHELL_LINE = 0xFF6B3418.toInt()
        const val BODY = 0xFFF07A55.toInt()
        const val BODY_LINE = 0xFF6B2A16.toInt()
        const val EYE = 0xFF111111.toInt()
    }
}
