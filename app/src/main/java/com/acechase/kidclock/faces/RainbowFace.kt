package com.acechase.kidclock.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import kotlin.math.sin
import kotlin.random.Random

/** Slowly shifting pastel gradient with floating bubbles. */
class RainbowFace(context: Context) : ClockFace(context) {
    private class Bubble(val x: Float, val r: Float, val speed: Float, val phase: Float)
    private val rnd = Random(3)
    private val bubbles = List(18) { Bubble(rnd.nextFloat(), 14f + rnd.nextFloat() * 30f, 0.02f + rnd.nextFloat() * 0.03f, rnd.nextFloat() * 6.28f) }
    private val bg = Paint()
    private val bubble = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; alpha = 70 }
    private val hsv = FloatArray(3)

    override val dayShadowColor = 0x55000000
    override fun dayBackgroundForMoonBite() = 0xFFF48FB1.toInt()

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        if (night) { c.drawColor(Color.BLACK); return }
        val hue = ((nowMs / 120_000.0) % 1.0 * 360.0).toFloat() // full cycle every 2 minutes
        val c1 = hsvColor(hue, 0.45f, 0.95f)
        val c2 = hsvColor((hue + 60f) % 360f, 0.45f, 0.95f)
        val c3 = hsvColor((hue + 120f) % 360f, 0.45f, 0.95f)
        bg.shader = LinearGradient(0f, 0f, w, h, intArrayOf(c1, c2, c3), null, Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, bg)
        val t = nowMs / 1000.0
        for (b in bubbles) {
            val y = h * (1.2f - ((t * b.speed + b.phase) % 1.4).toFloat())
            val x = b.x * w + sin(t * 0.7 + b.phase).toFloat() * 20f
            c.drawCircle(x, y, b.r, bubble)
        }
    }

    private fun hsvColor(hue: Float, s: Float, v: Float): Int {
        hsv[0] = hue; hsv[1] = s; hsv[2] = v
        return Color.HSVToColor(hsv)
    }
}
