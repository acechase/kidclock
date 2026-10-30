package com.acechase.kidclock.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import java.util.Calendar
import kotlin.math.sin
import kotlin.random.Random

/**
 * A sky that follows the real time of day: sunrise, blue sky with sun and clouds, sunset,
 * then stars after dark. Doubles as a soft nightlight in the sleep window.
 */
class SkyFace(context: Context) : ClockFace(context) {
    private class Star(val x: Float, val y: Float, val r: Float, val phase: Float, val speed: Float)
    private class Cloud(val y: Float, val scale: Float, val speed: Float, val offset: Float)
    private val rnd = Random(7)
    private val stars = List(90) { Star(rnd.nextFloat(), rnd.nextFloat(), 1.5f + rnd.nextFloat() * 3f, rnd.nextFloat() * 6.28f, 0.5f + rnd.nextFloat() * 1.5f) }
    private val clouds = List(4) { Cloud(0.06f + rnd.nextFloat() * 0.07f, 0.7f + rnd.nextFloat() * 0.4f, 0.004f + rnd.nextFloat() * 0.004f, rnd.nextFloat()) }
    private val bg = Paint()
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private var shader: Shader? = null
    private var shaderKey = ""

    private enum class Scene { SUNRISE, DAY, SUNSET, STARS }

    private fun scene(nowMs: Long): Scene {
        val c = Calendar.getInstance().apply { timeInMillis = nowMs }
        val m = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
        return when {
            m in 6 * 60 until 7 * 60 + 30 -> Scene.SUNRISE
            m in 7 * 60 + 30 until 17 * 60 -> Scene.DAY
            m in 17 * 60 until 18 * 60 + 30 -> Scene.SUNSET
            else -> Scene.STARS
        }
    }

    override fun dayBackgroundForMoonBite(): Int = 0xFF102040.toInt()

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        if (night) {
            c.drawColor(Color.BLACK)
            p.color = NIGHT_TEXT
            for (s in stars) { p.alpha = 40; c.drawCircle(s.x * w, s.y * h, s.r * 0.7f, p) }
            return
        }
        val sc = scene(nowMs)
        val (top, bottom) = when (sc) {
            Scene.SUNRISE -> 0xFF5C6BC0.toInt() to 0xFFFFB74D.toInt()
            Scene.DAY -> 0xFF3F8FE0.toInt() to 0xFF9BD1FF.toInt()
            Scene.SUNSET -> 0xFF5E35B1.toInt() to 0xFFFF7043.toInt()
            Scene.STARS -> 0xFF0B1A3A.toInt() to 0xFF1B2F5E.toInt()
        }
        val key = "$sc-$h"
        if (shader == null || shaderKey != key) {
            shader = LinearGradient(0f, 0f, 0f, h, top, bottom, Shader.TileMode.CLAMP)
            shaderKey = key
        }
        bg.shader = shader
        c.drawRect(0f, 0f, w, h, bg)
        val t = nowMs / 1000.0

        if (sc == Scene.STARS) {
            p.color = Color.WHITE
            for (s in stars) {
                val tw = 0.55f + 0.45f * sin(t * s.speed + s.phase).toFloat()
                p.alpha = (tw * 255).toInt()
                c.drawCircle(s.x * w, s.y * h, s.r * tw, p)
            }
            // Moon, top-left of centre so it never collides with the weather or the lamp.
            p.alpha = 235; p.color = 0xFFFFF4C2.toInt()
            c.drawCircle(w * 0.24f, h * 0.14f, h * 0.06f, p)
            p.color = top
            c.drawCircle(w * 0.24f + h * 0.025f, h * 0.125f, h * 0.05f, p)
            return
        }

        // Sun: low on the horizon at sunrise/sunset, high by day.
        val sunY = when (sc) { Scene.DAY -> h * 0.13f; else -> h * 0.30f }
        val sunColor = if (sc == Scene.DAY) 0xFFFFE082.toInt() else 0xFFFFCC80.toInt()
        p.color = sunColor
        p.setShadowLayer(h * 0.05f, 0f, 0f, sunColor)
        c.drawCircle(w * 0.24f, sunY, h * 0.065f, p)
        p.clearShadowLayer()

        // Clouds drift slowly left to right and wrap. Opaque so the overlapping blobs show no seams.
        p.color = if (sc == Scene.DAY) 0xFFF4F9FF.toInt() else 0xFFFFE0C8.toInt()
        for (cl in clouds) {
            val x = (((t * cl.speed + cl.offset) % 1.3) * (w * 1.3f) - w * 0.15f).toFloat()
            drawCloud(c, x, cl.y * h, h * 0.035f * cl.scale)
        }
    }

    private fun drawCloud(c: Canvas, x: Float, y: Float, r: Float) {
        c.drawCircle(x, y, r, p)
        c.drawCircle(x + r * 1.1f, y - r * 0.5f, r * 1.25f, p)
        c.drawCircle(x + r * 2.4f, y, r, p)
        c.drawRect(x, y, x + r * 2.4f, y + r, p)
    }
}
