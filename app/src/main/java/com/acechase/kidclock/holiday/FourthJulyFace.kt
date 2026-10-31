package com.acechase.kidclock.holiday

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import com.acechase.kidclock.art.Doodles
import com.acechase.kidclock.faces.ClockFace
import com.acechase.kidclock.faces.StaticLayer
import kotlin.math.sin

/**
 * Fireworks: a navy night with bursts in coral, white and sky blue that brighten and fade in turn,
 * faint rising trails, and a string of heart-shaped bunting in red, white and blue across the top.
 */
class FourthJulyFace(context: Context) : ClockFace(context) {
    private class Burst(val x: Float, val y: Float, val size: Float, val color: Int, val phase: Float)
    private val bursts = listOf(Burst(200f, 330f, 260f, CORAL, 0f), Burst(1080f, 360f, 300f, SKY, 1.1f), Burst(640f, 650f, 180f, Color.WHITE, 2.2f),
        Burst(420f, 110f, 110f, Color.WHITE, 3.3f), Burst(900f, 180f, 100f, CORAL, 4.4f), Burst(120f, 640f, 120f, SKY, 5.5f), Burst(1180f, 680f, 90f, Color.WHITE, 0.7f))
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bg = Paint()
    private val layer = StaticLayer(::paintStill)
    private val path = Path()

    override val dayTextColor = Color.WHITE
    override val dayShadowColor = 0x80000000.toInt()
    override fun dayBackgroundForMoonBite() = 0xFF0C1A3C.toInt()

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        if (night) { c.drawColor(Color.BLACK); return }
        layer.draw(c, w, h)
        val sx = w / 1280f; val sy = h / 800f; val s = sy
        val t = nowMs / 1000.0
        for (b in bursts) {
            val a = (0.55 + 0.45 * sin(t * 0.9 + b.phase)).toFloat()
            Doodles.burst(c, p, b.x * sx, b.y * sy, b.size * s * (0.96f + 0.04f * a), b.color, (230 * a).toInt().coerceAtLeast(60), 1.3f * s)
        }
    }

    private fun paintStill(c: Canvas, w: Float, h: Float) {
        val sx = w / 1280f; val sy = h / 800f; val s = sy
        bg.shader = LinearGradient(0f, 0f, 0f, h, intArrayOf(0xFF07122B.toInt(), 0xFF102254.toInt(), 0xFF1B2F6B.toInt()), floatArrayOf(0f, 0.6f, 1f), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, bg)
        p.reset(); p.isAntiAlias = true; p.color = Color.WHITE
        val stars = floatArrayOf(80f, 300f, 260f, 420f, 420f, 560f, 640f, 620f, 880f, 540f, 1100f, 600f, 1220f, 420f, 160f, 700f, 980f, 720f, 560f, 740f)
        var i = 0
        while (i < stars.size) { p.alpha = if (i % 4 == 0) 180 else 140; c.drawCircle(stars[i] * sx, stars[i + 1] * sy, (if (i % 4 == 0) 1.5f else 1f) * s, p); i += 2 }
        // Rising trails
        p.style = Paint.Style.STROKE; p.strokeWidth = 1.2f * s; p.alpha = 90; p.pathEffect = DashPathEffect(floatArrayOf(2f * s, 8f * s), 0f)
        for (tr in arrayOf(floatArrayOf(200f, 330f, 190f), floatArrayOf(1080f, 360f, 1100f), floatArrayOf(640f, 650f, 634f))) {
            path.reset(); path.moveTo(tr[0] * sx, h); path.quadTo(tr[2] * sx, (tr[1] + h / sy) / 2f * sy, tr[0] * sx, tr[1] * sy); c.drawPath(path, p)
        }
        p.pathEffect = null
        // Bunting
        p.alpha = 255; p.color = 0xFFE8E2D0.toInt(); p.strokeWidth = 2f * s
        path.reset(); path.moveTo(-10f * sx, 110f * sy); path.quadTo(320f * sx, 200f * sy, 640f * sx, 186f * sy); path.quadTo(960f * sx, 172f * sy, 1290f * sx, 110f * sy)
        c.drawPath(path, p)
        val pen = floatArrayOf(70f, 142f, 8f, 180f, 164f, 6f, 290f, 178f, 4f, 400f, 186f, 2f, 510f, 190f, 0f, 640f, 190f, 0f, 770f, 190f, 0f, 880f, 186f, -2f, 990f, 178f, -4f, 1100f, 164f, -6f, 1210f, 142f, -8f)
        val colors = intArrayOf(CORAL, Color.WHITE, SKY)
        i = 0
        var k = 0
        while (i < pen.size) {
            // The heart hangs from its dip, so shift it down by half its height.
            Doodles.heart(c, p, pen[i] * sx, pen[i + 1] * sy + 15f * s, 42f * s, pen[i + 2], colors[k % 3], Doodles.Style.SOLID)
            i += 3; k++
        }
    }

    companion object {
        private const val CORAL = 0xFFFF6B6B.toInt()
        private const val SKY = 0xFF7CC4FF.toInt()
    }
}
