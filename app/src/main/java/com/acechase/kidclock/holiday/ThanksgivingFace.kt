package com.acechase.kidclock.holiday

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import com.acechase.kidclock.art.Doodles
import com.acechase.kidclock.faces.ClockFace
import com.acechase.kidclock.faces.StaticLayer
import kotlin.math.sin
import kotlin.random.Random

/**
 * Harvest: warm brown to amber, maple leaves in rust, ochre, burgundy and gold tumbling slowly
 * down, wheat stalks at the foot, a pumpkin heart in the corner and a few small amber hearts.
 */
class ThanksgivingFace(context: Context) : ClockFace(context) {
    private class Leaf(val x: Float, val phase: Float, val speed: Float, val color: Int, val size: Float, val spin: Float)
    private val rnd = Random(26)
    private val leafColors = intArrayOf(0xFFC2410C.toInt(), 0xFFE0A030.toInt(), 0xFF7F1D1D.toInt(), 0xFFF6C76B.toInt())
    private val leaves = List(16) { Leaf(rnd.nextFloat(), rnd.nextFloat(), 0.012f + rnd.nextFloat() * 0.018f, leafColors[it % 4], 45f + rnd.nextFloat() * 60f, rnd.nextFloat() * 360f) }
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bg = Paint()
    private val layer = StaticLayer(::paintStill)
    private val path = Path()

    override val dayTextColor = 0xFFFBE9C8.toInt()
    override val dayShadowColor = 0x80000000.toInt()
    override fun dayBackgroundForMoonBite() = 0xFF3A1C0C.toInt()

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        if (night) { c.drawColor(Color.BLACK); return }
        layer.draw(c, w, h)
        val sx = w / 1280f; val sy = h / 800f; val s = sy
        val t = nowMs / 1000.0
        for (l in leaves) {
            val prog = ((t * l.speed + l.phase) % 1.3).toFloat()
            val y = h * (prog * 1.3f - 0.15f)
            val x = l.x * w + (sin(t * 0.35 + l.phase * 7.0) * 50.0).toFloat() * sx
            Doodles.fill(c, p, Doodles.mapleLeaf, x, y, l.size * s, l.spin + (sin(t * 0.5 + l.phase) * 40.0).toFloat(), l.color, alpha = 235)
        }
    }

    private fun paintStill(c: Canvas, w: Float, h: Float) {
        val sx = w / 1280f; val sy = h / 800f; val s = sy
        bg.shader = LinearGradient(0f, 0f, 0f, h, intArrayOf(0xFF2B1408.toInt(), 0xFF6B3410.toInt(), 0xFFA0531F.toInt()), floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, bg)
        for (st in arrayOf(floatArrayOf(60f, -8f, 1f), floatArrayOf(100f, 4f, 0.9f), floatArrayOf(140f, -3f, 1.05f), floatArrayOf(1200f, 6f, 0.95f), floatArrayOf(1240f, -5f, 0.85f))) {
            wheat(c, st[0] * sx, h, st[1], st[2] * s)
        }
        Doodles.pumpkinHeart(c, p, 1090f * sx, 700f * sy, 110f * s, -8f)
        Doodles.heart(c, p, 300f * sx, 700f * sy, 30f * s, 10f, 0xFFF6C76B.toInt(), Doodles.Style.SOLID)
        Doodles.heart(c, p, 760f * sx, 720f * sy, 24f * s, -12f, 0xFFFBE9C8.toInt(), Doodles.Style.SOLID)
        Doodles.heart(c, p, 1050f * sx, 160f * sy, 22f * s, 8f, 0xFFF6C76B.toInt(), Doodles.Style.SOLID)
        Doodles.heart(c, p, 160f * sx, 240f * sy, 20f * s, -6f, 0xFFFBE9C8.toInt(), Doodles.Style.SOLID)
    }

    private fun wheat(c: Canvas, x: Float, base: Float, rot: Float, s: Float) {
        c.save(); c.translate(x, base); c.rotate(rot); c.scale(s, s)
        p.reset(); p.isAntiAlias = true; p.style = Paint.Style.STROKE; p.strokeWidth = 2f; p.strokeCap = Paint.Cap.ROUND; p.color = 0xFFE0A030.toInt()
        c.drawLine(0f, 0f, 0f, -120f, p)
        p.style = Paint.Style.FILL; p.alpha = 230
        for (yy in floatArrayOf(-30f, -52f, -74f, -96f)) {
            val len = if (yy == -96f) 22f else 24f
            for (side in intArrayOf(-1, 1)) {
                path.reset(); path.moveTo(0f, yy); path.quadTo(side * 14f, yy - 8f, side * 16f, yy - len); path.quadTo(side * 2f, yy - len + 4f, 0f, yy); c.drawPath(path, p)
            }
        }
        c.restore()
    }
}
