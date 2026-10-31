package com.acechase.kidclock.holiday

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import com.acechase.kidclock.art.Doodles
import com.acechase.kidclock.art.Doodles.Style
import com.acechase.kidclock.faces.ClockFace
import com.acechase.kidclock.faces.StaticLayer
import kotlin.math.sin
import kotlin.random.Random

/**
 * Rose: burgundy to rose with a warm glow low on the right, rose petals drifting down, two large
 * layered rose-gold hearts, a gold arrow heart, blush hearts and a few twinkling sparkles.
 */
class ValentineFace(context: Context) : ClockFace(context) {
    private class Petal(val x: Float, val phase: Float, val speed: Float, val color: Int, val size: Float, val spin: Float)
    private val rnd = Random(14)
    private val petalColors = intArrayOf(0xFFE8577F.toInt(), 0xFFF08BA6.toInt(), 0xFFD43D6E.toInt())
    private val petals = List(14) { Petal(rnd.nextFloat(), rnd.nextFloat(), 0.015f + rnd.nextFloat() * 0.02f, petalColors[rnd.nextInt(3)], 8f + rnd.nextFloat() * 5f, rnd.nextFloat() * 360f) }
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bg = Paint()
    private val layer = StaticLayer(::paintStill)

    override val dayTextColor = 0xFFFFD6E0.toInt()
    override val dayShadowColor = 0x80000000.toInt()
    override fun dayBackgroundForMoonBite() = 0xFF4A1030.toInt()

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        if (night) { c.drawColor(Color.BLACK); return }
        layer.draw(c, w, h)
        val sx = w / 1280f; val sy = h / 800f; val s = sy
        val t = nowMs / 1000.0
        for (pt in petals) {
            val prog = ((t * pt.speed + pt.phase) % 1.2).toFloat()
            val y = h * (prog * 1.2f - 0.1f)
            val x = pt.x * w + (sin(t * 0.5 + pt.phase * 7.0) * 30.0).toFloat() * sx
            p.reset(); p.isAntiAlias = true; p.color = pt.color; p.alpha = 205
            c.save(); c.rotate(pt.spin + (t * 25).toFloat(), x, y)
            c.drawOval(x - pt.size * s, y - pt.size * 1.5f * s, x + pt.size * s, y + pt.size * 1.5f * s, p)
            c.restore()
        }
        val sp = floatArrayOf(241f, 261f, 22f, 1019f, 569f, 18f, 608f, 708f, 16f)
        var i = 0
        while (i < sp.size) { Doodles.sparkle(c, p, sp[i] * sx, sp[i + 1] * sy, sp[i + 2] * s, 0xFFFFF1F3.toInt(), (150 + 100 * sin(t * 2 + i)).toInt()); i += 3 }
    }

    private fun paintStill(c: Canvas, w: Float, h: Float) {
        val sx = w / 1280f; val sy = h / 800f; val s = sy
        bg.shader = LinearGradient(0f, 0f, 0f, h, intArrayOf(0xFF3B0A22.toInt(), 0xFF7A1545.toInt(), 0xFFA52A5E.toInt()), floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, bg)
        p.reset(); p.isAntiAlias = true
        p.shader = RadialGradient(1110f * sx, 630f * sy, 420f * s, Doodles.withAlpha(0xFFE8577F.toInt(), 70), Doodles.withAlpha(0xFFE8577F.toInt(), 0), Shader.TileMode.CLAMP)
        c.drawCircle(1110f * sx, 630f * sy, 420f * s, p)
        p.shader = null
        val gold = 0xFFF4C7D2.toInt()
        Doodles.heart(c, p, 125f * sx, 365f * sy, 130f * s, -10f, gold, Style.LAYERED)
        Doodles.heart(c, p, 1145f * sx, 495f * sy, 150f * s, 8f, gold, Style.LAYERED)
        Doodles.heart(c, p, 480f * sx, 670f * sy, 80f * s, -6f, gold, Style.LAYERED)
        Doodles.heart(c, p, 268f * sx, 578f * sy, 36f * s, 12f, 0xFFFFB3C6.toInt(), Style.SOLID)
        Doodles.heart(c, p, 902f * sx, 662f * sy, 44f * s, -14f, 0xFFFFB3C6.toInt(), Style.SOLID)
        Doodles.heart(c, p, 164f * sx, 94f * sy, 28f * s, 8f, gold, Style.SOLID)
        Doodles.heart(c, p, 1052f * sx, 312f * sy, 24f * s, -8f, gold, Style.SOLID)
        Doodles.heart(c, p, 710f * sx, 570f * sy, 20f * s, 16f, 0xFFFFB3C6.toInt(), Style.SOLID)
        Doodles.heart(c, p, 990f * sx, 120f * sy, 80f * s, -8f, 0xFFF3DDA0.toInt(), Style.ARROW)
    }
}
