package com.acechase.kidclock.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Pastel sky over rolling green hills, flowers, and fluttering butterflies. */
class MeadowFace(context: Context) : ClockFace(context) {
    private class Flower(val x: Float, val y: Float, val r: Float, val color: Int)
    private class Fly(val x: Float, val y: Float, val color: Int, val speed: Float, val phase: Float, val size: Float)
    private val rnd = Random(9)
    private val petalColors = intArrayOf(0xFFFF6FA3.toInt(), 0xFFFFD166.toInt(), 0xFFB388FF.toInt(), 0xFFFF8A65.toInt(), 0xFFFFFFFF.toInt())
    private val flowers = List(34) { Flower(rnd.nextFloat(), 0.72f + rnd.nextFloat() * 0.24f, 7f + rnd.nextFloat() * 7f, petalColors[rnd.nextInt(petalColors.size)]) }
    private val flies = List(6) { Fly(rnd.nextFloat(), 0.15f + rnd.nextFloat() * 0.45f, petalColors[rnd.nextInt(4)], 0.15f + rnd.nextFloat() * 0.2f, rnd.nextFloat() * 6.28f, 22f + rnd.nextFloat() * 14f) }
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bg = Paint()
    private val path = Path()
    private var shader: Shader? = null
    private var shaderH = 0f

    override val dayTextColor = 0xFF2F3E46.toInt()
    override val dayShadowColor = 0x33FFFFFF
    override fun dayBackgroundForMoonBite() = 0xFFBDE7FF.toInt()

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        if (night) { c.drawColor(Color.BLACK); return }
        val t = nowMs / 1000.0
        if (shader == null || shaderH != h) {
            shader = LinearGradient(0f, 0f, 0f, h * 0.8f, 0xFF9FD8FF.toInt(), 0xFFFFF3C4.toInt(), Shader.TileMode.CLAMP); shaderH = h
        }
        bg.shader = shader
        c.drawRect(0f, 0f, w, h, bg)
        // Hills
        p.color = 0xFF8BD17C.toInt()
        path.reset(); path.moveTo(0f, h); path.lineTo(0f, h * 0.78f)
        path.cubicTo(w * 0.3f, h * 0.62f, w * 0.6f, h * 0.86f, w, h * 0.70f); path.lineTo(w, h); path.close(); c.drawPath(path, p)
        p.color = 0xFF6CBF5E.toInt()
        path.reset(); path.moveTo(0f, h); path.lineTo(0f, h * 0.88f)
        path.cubicTo(w * 0.35f, h * 0.76f, w * 0.65f, h * 0.98f, w, h * 0.84f); path.lineTo(w, h); path.close(); c.drawPath(path, p)
        // Flowers: five petals around a centre
        for (f in flowers) {
            val cx = f.x * w; val cy = f.y * h
            p.color = f.color
            for (k in 0 until 5) { val a = k * 1.2566f + sin(t + cx).toFloat() * 0.08f; c.drawCircle(cx + cos(a) * f.r * 0.9f, cy + sin(a) * f.r * 0.9f, f.r * 0.6f, p) }
            p.color = 0xFFFFE082.toInt(); c.drawCircle(cx, cy, f.r * 0.45f, p)
        }
        // Butterflies
        for (b in flies) {
            val x = (b.x + 0.08f * sin(t * b.speed + b.phase).toFloat()) * w
            val y = (b.y + 0.05f * sin(t * b.speed * 1.7 + b.phase).toFloat()) * h
            val flap = abs(sin(t * 9 + b.phase)).toFloat() * 0.8f + 0.2f
            p.color = b.color
            for (side in intArrayOf(-1, 1)) {
                path.reset()
                path.moveTo(x, y)
                path.lineTo(x + side * b.size * flap, y - b.size * 0.9f)
                path.lineTo(x + side * b.size * 1.1f * flap, y + b.size * 0.1f)
                path.lineTo(x + side * b.size * 0.7f * flap, y + b.size * 0.8f)
                path.close()
                c.drawPath(path, p)
            }
            p.color = 0xFF4A3B2A.toInt()
            c.drawRect(x - 1.5f, y - b.size * 0.6f, x + 1.5f, y + b.size * 0.6f, p)
        }
    }
}
