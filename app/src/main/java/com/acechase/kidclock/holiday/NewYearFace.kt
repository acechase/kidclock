package com.acechase.kidclock.holiday

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import com.acechase.kidclock.art.Doodles
import com.acechase.kidclock.faces.ClockFace
import kotlin.math.sin
import kotlin.random.Random

/**
 * Midnight: near-black navy, three thin gold firework bursts that breathe, and confetti in gold,
 * champagne, rose gold and white drifting down, with a few gold hearts falling among it.
 */
class NewYearFace(context: Context) : ClockFace(context) {
    private class Bit(val x: Float, val phase: Float, val speed: Float, val color: Int, val kind: Int, val size: Float, val spin: Float)
    private val rnd = Random(31)
    private val colors = intArrayOf(0xFFF3DDA0.toInt(), 0xFFFFF1C9.toInt(), 0xFFE8B4A0.toInt(), 0xFFFFFFFF.toInt())
    private val bits = List(34) {
        Bit(rnd.nextFloat(), rnd.nextFloat(), 0.025f + rnd.nextFloat() * 0.03f, colors[rnd.nextInt(colors.size)],
            when { it % 7 == 0 -> 2; it % 3 == 0 -> 1; else -> 0 }, 5f + rnd.nextFloat() * 4f, rnd.nextFloat() * 360f)
    }
    private val bg = Paint()
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private var shader: Shader? = null
    private var shaderH = 0f

    override val dayTextColor = 0xFFF3DDA0.toInt()
    override val dayShadowColor = 0x80000000.toInt()
    override fun dayBackgroundForMoonBite() = 0xFF0A0C1E.toInt()

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        if (night) { c.drawColor(Color.BLACK); return }
        if (shader == null || shaderH != h) {
            shader = LinearGradient(0f, 0f, 0f, h, intArrayOf(0xFF05060F.toInt(), 0xFF141A3A.toInt(), 0xFF2A1F4E.toInt()), floatArrayOf(0f, 0.6f, 1f), Shader.TileMode.CLAMP)
            shaderH = h
        }
        bg.shader = shader
        c.drawRect(0f, 0f, w, h, bg)
        val sx = w / 1280f; val sy = h / 800f; val s = sy
        val t = nowMs / 1000.0
        Doodles.burst(c, p, 230f * sx, 190f * sy, 240f * s, 0xFFF3DDA0.toInt(), (170 + 60 * sin(t * 0.8)).toInt(), 1.4f * s)
        Doodles.burst(c, p, 1080f * sx, 300f * sy, 300f * s, 0xFFE8B4A0.toInt(), (160 + 60 * sin(t * 0.8 + 2.0)).toInt(), 1.4f * s)
        Doodles.burst(c, p, 775f * sx, 95f * sy, 150f * s, 0xFFF3DDA0.toInt(), (150 + 60 * sin(t * 0.8 + 4.0)).toInt(), 1.6f * s)
        for (b in bits) {
            val prog = ((t * b.speed + b.phase) % 1.3).toFloat()
            val y = h * (prog * 1.3f - 0.15f)
            val x = b.x * w + (sin(t * 0.6 + b.phase * 6.0) * 18.0).toFloat() * sx
            when (b.kind) {
                0 -> {
                    p.reset(); p.isAntiAlias = true; p.color = b.color; p.alpha = if (b.color == Color.WHITE) 200 else 255
                    c.save(); c.rotate(b.spin + (t * 40).toFloat(), x, y)
                    c.drawRoundRect(x - b.size * 0.45f * s, y - b.size * 1.3f * s, x + b.size * 0.45f * s, y + b.size * 1.3f * s, 2f * s, 2f * s, p)
                    c.restore()
                }
                1 -> { p.reset(); p.isAntiAlias = true; p.color = b.color; c.drawCircle(x, y, b.size * 0.4f * s, p) }
                else -> Doodles.heart(c, p, x, y, b.size * 3f * s, b.spin % 40f - 20f, b.color, Doodles.Style.SOLID)
            }
        }
    }
}
