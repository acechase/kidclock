package com.acechase.kidclock.holiday

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import com.acechase.kidclock.art.Doodles
import com.acechase.kidclock.faces.ClockFace
import com.acechase.kidclock.faces.StaticLayer
import kotlin.math.sin
import kotlin.random.Random

/**
 * Fairy Lights: deep pine, a string of glowing bulbs in gold, red, cream and pink across the top
 * that twinkle, soft snow falling, holly with berries in the bottom corners and candy-cane hearts.
 */
class ChristmasFace(context: Context) : ClockFace(context) {
    private class Flake(val x: Float, val phase: Float, val speed: Float, val r: Float)
    private class Bulb(val x: Float, val y: Float, val rot: Float, val color: Int, val phase: Float)
    private val rnd = Random(25)
    private val flakes = List(40) { Flake(rnd.nextFloat(), rnd.nextFloat(), 0.012f + rnd.nextFloat() * 0.02f, 2f + rnd.nextFloat() * 2.5f) }
    private val bulbColors = intArrayOf(0xFFFFD166.toInt(), 0xFFFF6B8A.toInt(), 0xFFFFF4D6.toInt(), 0xFFF48FB1.toInt())
    private val bulbs = List(10) {
        val x = floatArrayOf(80f, 200f, 320f, 440f, 560f, 720f, 840f, 960f, 1080f, 1200f)[it]
        val y = floatArrayOf(104f, 142f, 168f, 182f, 186f, 186f, 182f, 168f, 142f, 104f)[it]
        val rot = floatArrayOf(12f, 8f, 5f, 3f, 0f, 0f, -3f, -5f, -8f, -12f)[it]
        Bulb(x, y, rot, bulbColors[it % 4], it * 0.8f)
    }
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bg = Paint()
    private val layer = StaticLayer(::paintStill)
    private val path = Path()

    override val dayTextColor = Color.WHITE
    override val dayShadowColor = 0x80000000.toInt()
    override fun dayBackgroundForMoonBite() = 0xFF0B2A22.toInt()

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        if (night) { c.drawColor(Color.BLACK); return }
        layer.draw(c, w, h)
        val sx = w / 1280f; val sy = h / 800f; val s = sy
        val t = nowMs / 1000.0
        for (b in bulbs) {
            val tw = (0.75 + 0.25 * sin(t * 1.6 + b.phase)).toFloat()
            val bx = b.x * sx; val by = b.y * sy
            c.save(); c.translate(bx, by); c.rotate(b.rot); c.scale(s, s)
            p.reset(); p.isAntiAlias = true
            p.shader = RadialGradient(0f, 34f, 26f, Doodles.withAlpha(b.color, (150 * tw).toInt()), Doodles.withAlpha(b.color, 0), Shader.TileMode.CLAMP)
            c.drawCircle(0f, 34f, 26f, p)
            p.shader = null
            p.style = Paint.Style.FILL; p.color = b.color; p.alpha = (255 * (0.8f + 0.2f * tw)).toInt()
            c.drawPath(Doodles.bulb, p)
            p.style = Paint.Style.STROKE; p.strokeWidth = 1f; p.color = 0xFF0B2A22.toInt(); p.alpha = 255
            c.drawPath(Doodles.bulb, p)
            c.restore()
        }
        p.reset(); p.isAntiAlias = true; p.color = Color.WHITE
        for (f in flakes) {
            val prog = ((t * f.speed + f.phase) % 1.1).toFloat()
            val y = h * (prog * 1.1f - 0.05f)
            val x = f.x * w + (sin(t * 0.5 + f.phase * 9.0) * 16.0).toFloat() * sx
            p.alpha = 150 + (f.r * 20).toInt()
            c.drawCircle(x, y, f.r * s, p)
        }
    }

    private fun paintStill(c: Canvas, w: Float, h: Float) {
        val sx = w / 1280f; val sy = h / 800f; val s = sy
        bg.shader = LinearGradient(0f, 0f, 0f, h, intArrayOf(0xFF071F19.toInt(), 0xFF0F3A2E.toInt(), 0xFF164A3B.toInt()), floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, bg)
        // The wire
        p.reset(); p.isAntiAlias = true; p.style = Paint.Style.STROKE; p.strokeWidth = 3f * s; p.color = 0xFF0B2A22.toInt()
        path.reset(); path.moveTo(-10f * sx, 70f * sy); path.quadTo(320f * sx, 190f * sy, 640f * sx, 150f * sy); path.quadTo(960f * sx, 110f * sy, 1290f * sx, 70f * sy)
        c.drawPath(path, p)
        // Holly in the bottom corners
        for (side in intArrayOf(1, -1)) {
            val ox = if (side == 1) 0f else w
            fun x(v: Float) = ox + side * v * sx
            holly(c, x(60f), 740f * sy, if (side == 1) -30f else 210f, 1.3f * s)
            holly(c, x(120f), 770f * sy, if (side == 1) 20f else 160f, 1.1f * s)
            p.style = Paint.Style.FILL; p.color = 0xFFD7263D.toInt()
            c.drawCircle(x(92f), 748f * sy, 8f * s, p); c.drawCircle(x(108f), 736f * sy, 7f * s, p); c.drawCircle(x(100f), 762f * sy, 7f * s, p)
            p.style = Paint.Style.STROKE; p.strokeWidth = 1.5f * s; p.color = 0xFF8E1424.toInt()
            c.drawCircle(x(92f), 748f * sy, 8f * s, p); c.drawCircle(x(108f), 736f * sy, 7f * s, p); c.drawCircle(x(100f), 762f * sy, 7f * s, p)
        }
        // Candy-cane hearts and two small blush ones
        Doodles.heartStriped(c, p, 200f * sx, 560f * sy, 90f * s, -12f, 0xFFFFF4F4.toInt(), 0xFFD7263D.toInt())
        Doodles.heartStriped(c, p, 1080f * sx, 560f * sy, 110f * s, 10f, 0xFFFFF4F4.toInt(), 0xFFD7263D.toInt())
        Doodles.heartStriped(c, p, 640f * sx, 690f * sy, 70f * s, -6f, 0xFFFFF4F4.toInt(), 0xFFD7263D.toInt())
        Doodles.heart(c, p, 360f * sx, 650f * sy, 28f * s, 14f, 0xFFF48FB1.toInt(), Doodles.Style.SOLID)
        Doodles.heart(c, p, 900f * sx, 700f * sy, 24f * s, -10f, 0xFFF48FB1.toInt(), Doodles.Style.SOLID)
    }

    private fun holly(c: Canvas, x: Float, y: Float, rot: Float, s: Float) {
        c.save(); c.translate(x, y); c.rotate(rot); c.scale(s, s)
        p.reset(); p.isAntiAlias = true
        p.style = Paint.Style.FILL; p.color = 0xFF2E7D5B.toInt(); c.drawPath(Doodles.hollyLeaf, p)
        p.style = Paint.Style.STROKE; p.strokeWidth = 2f; p.color = 0xFF1C5A40.toInt(); c.drawPath(Doodles.hollyLeaf, p)
        c.restore()
    }
}
