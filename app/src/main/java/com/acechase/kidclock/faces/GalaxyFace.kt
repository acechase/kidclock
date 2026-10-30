package com.acechase.kidclock.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.math.sin
import kotlin.random.Random

/** Purple-pink nebula clouds, a dense star field, and a shooting star every few seconds. */
class GalaxyFace(context: Context) : ClockFace(context) {
    private class Star(val x: Float, val y: Float, val r: Float, val phase: Float, val speed: Float)
    private class Blob(val x: Float, val y: Float, val r: Float, val color: Int, val dx: Float, val dy: Float, val phase: Float)
    private val rnd = Random(21)
    private val stars = List(140) { Star(rnd.nextFloat(), rnd.nextFloat(), 1f + rnd.nextFloat() * 2.6f, rnd.nextFloat() * 6.28f, 0.6f + rnd.nextFloat() * 2f) }
    private val blobs = listOf(
        Blob(0.25f, 0.35f, 0.55f, 0x66B04CC8.toInt(), 0.05f, 0.03f, 0f),
        Blob(0.70f, 0.60f, 0.60f, 0x55FF4FA3.toInt(), 0.04f, 0.05f, 2f),
        Blob(0.55f, 0.20f, 0.45f, 0x553F8CFF.toInt(), 0.06f, 0.02f, 4f),
    )
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val blobPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val streak = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 3f; strokeCap = Paint.Cap.ROUND }

    override val dayShadowColor = 0x88000000.toInt()
    override fun dayBackgroundForMoonBite() = 0xFF160A2E.toInt()

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        if (night) {
            c.drawColor(Color.BLACK)
            p.color = NIGHT_TEXT
            for (s in stars.take(60)) { p.alpha = 35; c.drawCircle(s.x * w, s.y * h, s.r * 0.6f, p) }
            return
        }
        c.drawColor(0xFF160A2E.toInt())
        val t = nowMs / 1000.0
        for (b in blobs) {
            val cx = (b.x + 0.06f * sin(t * 0.05 + b.phase).toFloat() * b.dx * 10f) * w
            val cy = (b.y + 0.05f * sin(t * 0.07 + b.phase * 1.3).toFloat() * b.dy * 10f) * h
            val r = b.r * h
            blobPaint.shader = RadialGradient(cx, cy, r, b.color, 0x00000000, Shader.TileMode.CLAMP)
            c.drawCircle(cx, cy, r, blobPaint)
        }
        p.color = Color.WHITE
        for (s in stars) {
            val tw = 0.5f + 0.5f * sin(t * s.speed + s.phase).toFloat()
            p.alpha = (tw * 255).toInt()
            c.drawCircle(s.x * w, s.y * h, s.r * (0.7f + 0.3f * tw), p)
        }
        // Shooting star: a 1.2 s streak every 7 s, from a pseudo-random start point.
        val cycle = 7.0
        val phase = (t % cycle)
        if (phase < 1.2) {
            val seed = (t / cycle).toInt()
            val r2 = Random(seed)
            val sx = w * (0.1f + 0.6f * r2.nextFloat()); val sy = h * (0.05f + 0.3f * r2.nextFloat())
            val f = (phase / 1.2).toFloat()
            val ex = sx + w * 0.28f * f; val ey = sy + h * 0.18f * f
            val tail = 0.12f
            val tx = sx + w * 0.28f * maxOf(0f, f - tail); val ty = sy + h * 0.18f * maxOf(0f, f - tail)
            streak.color = Color.WHITE; streak.alpha = ((1f - f) * 255).toInt()
            c.drawLine(tx, ty, ex, ey, streak)
        }
    }
}
