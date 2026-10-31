package com.acechase.kidclock.holiday

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import com.acechase.kidclock.art.Doodles
import com.acechase.kidclock.faces.ClockFace
import com.acechase.kidclock.faces.StaticLayer
import kotlin.math.abs
import kotlin.math.sin

/**
 * Witching Hour: a plum night with a pale moon, faint stars, bats crossing the sky, a cobweb with
 * a small spider in the corner, pumpkin hearts, bat-wing hearts, a candy-corn heart and glowing
 * ember hearts. The bats drift and flap; the embers breathe.
 */
class HalloweenFace(context: Context) : ClockFace(context) {
    private class Bat(val x: Float, val y: Float, val size: Float, val speed: Float, val phase: Float)
    private val bats = listOf(Bat(272f, 118f, 84f, 0.5f, 0f), Bat(382f, 203f, 54f, 0.7f, 1.3f), Bat(487f, 90f, 42f, 0.9f, 2.1f),
        Bat(905f, 135f, 60f, 0.6f, 3.0f), Bat(1128f, 301f, 44f, 0.8f, 4.2f), Bat(755f, 659f, 36f, 0.4f, 5.1f))
    private val embers = floatArrayOf(429f, 569f, 18f, 667f, 597f, 14f, 870f, 550f, 20f, 68f, 608f, 16f, 1187f, 527f, 14f, 986f, 256f, 12f)
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bg = Paint()
    private val layer = StaticLayer(::paintStill)

    override val dayTextColor = 0xFFE9D5FF.toInt()
    override val dayShadowColor = 0x80000000.toInt()
    override fun dayBackgroundForMoonBite() = 0xFF1A0E33.toInt()

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        if (night) { c.drawColor(Color.BLACK); return }
        layer.draw(c, w, h)
        val sx = w / 1280f; val sy = h / 800f; val t = nowMs / 1000.0
        for (b in bats) {
            val x = b.x * sx + (sin(t * b.speed + b.phase) * 26.0).toFloat() * sx
            val y = b.y * sy + (sin(t * b.speed * 1.7 + b.phase) * 10.0).toFloat() * sy
            val flap = 0.75f + 0.25f * abs(sin(t * 6 + b.phase)).toFloat()
            c.save(); c.translate(x, y); c.rotate((sin(t * b.speed + b.phase) * 8.0).toFloat()); c.scale(1f, flap)
            Doodles.fill(c, p, Doodles.bat, 0f, 0f, b.size * sy, 0f, 0xFF140A20.toInt(), box = 100f)
            c.restore()
        }
        var i = 0
        while (i < embers.size) {
            val pulse = 0.65f + 0.35f * sin(t * 1.3 + i).toFloat()
            val cx = embers[i] * sx; val cy = embers[i + 1] * sy; val size = embers[i + 2] * sy
            p.reset(); p.isAntiAlias = true
            p.shader = RadialGradient(cx, cy, size * 1.4f, Doodles.withAlpha(0xFFFF8C42.toInt(), (120 * pulse).toInt()), Doodles.withAlpha(0xFFFF8C42.toInt(), 0), Shader.TileMode.CLAMP)
            c.drawCircle(cx, cy, size * 1.4f, p)
            p.shader = null
            Doodles.heart(c, p, cx, cy, size, 0f, 0xFFFFB266.toInt(), Doodles.Style.SOLID)
            i += 3
        }
    }

    private fun paintStill(c: Canvas, w: Float, h: Float) {
        val sx = w / 1280f; val sy = h / 800f; val s = sy
        bg.shader = LinearGradient(0f, 0f, 0f, h, intArrayOf(0xFF0B0714.toInt(), 0xFF22103F.toInt(), 0xFF3A1657.toInt()), floatArrayOf(0f, 0.58f, 1f), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, bg)
        // Nebula glows
        glow(c, 1080f * sx, 650f * sy, 400f * s, 0xFF7C2D8A.toInt(), 60)
        // Moon with a halo
        glow(c, 210f * sx, 150f * sy, 150f * s, 0xFFF5E9CF.toInt(), 70)
        p.reset(); p.isAntiAlias = true; p.color = 0xFFF5E9CF.toInt()
        c.drawCircle(210f * sx, 150f * sy, 90f * s, p)
        p.color = 0xFFE6D7B5.toInt(); p.alpha = 200
        c.drawCircle(176f * sx, 121f * sy, 11f * s, p); c.drawCircle(237f * sx, 167f * sy, 7f * s, p); c.drawCircle(204f * sx, 194f * sy, 4.5f * s, p)
        // Stars
        p.color = Color.WHITE
        val stars = floatArrayOf(420f, 60f, 3f, 560f, 120f, 2f, 650f, 40f, 3f, 800f, 90f, 2f, 940f, 180f, 3f, 1240f, 190f, 2f, 60f, 330f, 2f, 360f, 300f, 3f,
            1060f, 330f, 2f, 150f, 520f, 3f, 300f, 640f, 2f, 520f, 700f, 3f, 880f, 620f, 2f, 40f, 700f, 2f, 980f, 500f, 3f, 760f, 560f, 2f)
        var i = 0
        while (i < stars.size) { p.alpha = if (stars[i + 2] > 2f) 200 else 150; c.drawCircle(stars[i] * sx, stars[i + 1] * sy, stars[i + 2] * 0.5f * s, p); i += 3 }
        // Cobweb in the bottom-right corner with a spider
        p.reset(); p.isAntiAlias = true; p.style = Paint.Style.STROKE; p.strokeWidth = 1.2f * s; p.color = 0xFFC4B5FD.toInt(); p.alpha = 120
        val cx = w; val cy = h; val r = 280f * s
        for (k in 0..4) {
            val a = Math.toRadians(180.0 + k * 22.5)
            c.drawLine(cx, cy, cx + (Math.cos(a) * r).toFloat(), cy + (Math.sin(a) * r).toFloat(), p)
        }
        for (rr in floatArrayOf(0.24f, 0.48f, 0.72f)) {
            val rad = r * rr
            val web = android.graphics.Path()
            for (k in 0..4) {
                val a = Math.toRadians(180.0 + k * 22.5)
                val px = cx + (Math.cos(a) * rad).toFloat(); val py = cy + (Math.sin(a) * rad).toFloat()
                if (k == 0) web.moveTo(px, py) else {
                    val a0 = Math.toRadians(180.0 + (k - 0.5) * 22.5)
                    web.quadTo(cx + (Math.cos(a0) * rad * 0.93).toFloat(), cy + (Math.sin(a0) * rad * 0.93).toFloat(), px, py)
                }
            }
            c.drawPath(web, p)
        }
        val spx = cx - r * 0.56f; val spy = cy - r * 0.37f
        c.drawLine(spx, spy - 16f * s, spx, spy, p)
        p.style = Paint.Style.FILL; c.drawCircle(spx, spy + 3f * s, 3f * s, p)
        p.style = Paint.Style.STROKE
        for (side in intArrayOf(-1, 1)) for (k in 0..2) c.drawLine(spx + side * 3f * s, spy + 1f * s + k * 2f * s, spx + side * 8f * s, spy - 2f * s + k * 3f * s, p)
        // Pumpkin hearts, bat-wing hearts, candy corn
        Doodles.pumpkinHeart(c, p, 165f * sx, 485f * sy, 110f * s, -8f)
        Doodles.pumpkinHeart(c, p, 1038f * sx, 598f * sy, 76f * s, 10f)
        Doodles.batHeart(c, p, 340f * sx, 610f * sy, 100f * s, -6f, 0xFF2A1348.toInt(), 0xFFC4B5FD.toInt())
        Doodles.batHeart(c, p, 1155f * sx, 436f * sy, 72f * s, 8f, 0xFF2A1348.toInt(), 0xFFC4B5FD.toInt())
        Doodles.heartBands(c, p, 589f * sx, 669f * sy, 58f * s, -10f, intArrayOf(0xFFFFF4E0.toInt(), 0xFFFF8C42.toInt(), 0xFFFFD166.toInt()), floatArrayOf(0.38f, 0.66f, 1f))
    }

    private fun glow(c: Canvas, cx: Float, cy: Float, r: Float, color: Int, alpha: Int) {
        p.reset(); p.isAntiAlias = true
        p.shader = RadialGradient(cx, cy, r, Doodles.withAlpha(color, alpha), Doodles.withAlpha(color, 0), Shader.TileMode.CLAMP)
        c.drawCircle(cx, cy, r, p)
        p.shader = null
    }
}
