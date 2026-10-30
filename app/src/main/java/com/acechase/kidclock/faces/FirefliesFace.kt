package com.acechase.kidclock.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** A dusk forest of pine silhouettes with fireflies drifting and pulsing. At night a few stay lit as a nightlight. */
class FirefliesFace(context: Context) : ClockFace(context) {
    private class Fly(val x: Float, val y: Float, val ax: Float, val ay: Float, val sx: Float, val sy: Float, val phase: Float, val blink: Float)
    private class Tree(val x: Float, val hgt: Float, val wid: Float, val shade: Int)
    private val rnd = Random(5)
    private val flies = List(26) { Fly(rnd.nextFloat(), 0.35f + rnd.nextFloat() * 0.55f, 0.02f + rnd.nextFloat() * 0.05f, 0.02f + rnd.nextFloat() * 0.04f, 0.2f + rnd.nextFloat() * 0.5f, 0.2f + rnd.nextFloat() * 0.5f, rnd.nextFloat() * 6.28f, 0.8f + rnd.nextFloat() * 1.5f) }
    private val trees = List(16) { Tree(it / 15f + (rnd.nextFloat() - 0.5f) * 0.05f, 0.18f + rnd.nextFloat() * 0.22f, 0.05f + rnd.nextFloat() * 0.04f, if (it % 2 == 0) 0xFF0B2A1E.toInt() else 0xFF123B2A.toInt()) }
    private val bg = Paint()
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private var shader: Shader? = null
    private var shaderH = 0f

    override fun dayBackgroundForMoonBite() = 0xFF1C4A5A.toInt()

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        val t = nowMs / 1000.0
        if (night) {
            c.drawColor(Color.BLACK)
            for (f in flies.take(6)) {
                val glow = 0.5f + 0.5f * sin(t * f.blink * 0.5 + f.phase).toFloat()
                p.color = NIGHT_TEXT; p.alpha = (20 + 40 * glow).toInt()
                c.drawCircle((f.x + f.ax * sin(t * f.sx * 0.3 + f.phase).toFloat()) * w, (f.y + f.ay * cos(t * f.sy * 0.3 + f.phase).toFloat()) * h, 3f, p)
            }
            return
        }
        if (shader == null || shaderH != h) {
            shader = LinearGradient(0f, 0f, 0f, h, 0xFF1C4A5A.toInt(), 0xFF0E2D22.toInt(), Shader.TileMode.CLAMP); shaderH = h
        }
        bg.shader = shader
        c.drawRect(0f, 0f, w, h, bg)
        // Pines along the bottom
        for (tr in trees) {
            p.color = tr.shade
            val bx = tr.x * w; val top = h - tr.hgt * h; val half = tr.wid * w
            path.reset()
            path.moveTo(bx, top); path.lineTo(bx + half, h); path.lineTo(bx - half, h); path.close()
            c.drawPath(path, p)
            path.reset()
            path.moveTo(bx, top + tr.hgt * h * 0.35f); path.lineTo(bx + half * 1.25f, h); path.lineTo(bx - half * 1.25f, h); path.close()
            c.drawPath(path, p)
        }
        p.color = 0xFF07201A.toInt()
        c.drawRect(0f, h * 0.97f, w, h, p)
        // Fireflies
        for (f in flies) {
            val x = (f.x + f.ax * sin(t * f.sx + f.phase).toFloat()) * w
            val y = (f.y + f.ay * cos(t * f.sy + f.phase * 0.7).toFloat()) * h
            val glow = (0.5f + 0.5f * sin(t * f.blink + f.phase).toFloat()).let { it * it }
            p.color = 0xFFD4FF5A.toInt(); p.alpha = (glow * 90).toInt()
            c.drawCircle(x, y, 11f, p)
            p.alpha = (glow * 255).toInt()
            c.drawCircle(x, y, 3.5f, p)
        }
    }
}
