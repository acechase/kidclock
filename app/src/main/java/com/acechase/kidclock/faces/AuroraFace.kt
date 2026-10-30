package com.acechase.kidclock.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import kotlin.math.sin
import kotlin.random.Random

/** Northern lights rippling above snowy mountains. */
class AuroraFace(context: Context) : ClockFace(context) {
    private class Star(val x: Float, val y: Float, val r: Float, val phase: Float)
    private class Ribbon(val color: Int, val base: Float, val amp: Float, val freq: Float, val speed: Float, val thick: Float)
    private val rnd = Random(11)
    private val stars = List(70) { Star(rnd.nextFloat(), rnd.nextFloat() * 0.6f, 1f + rnd.nextFloat() * 2f, rnd.nextFloat() * 6.28f) }
    private val ribbons = listOf(
        Ribbon(0x5A3DFFA0, 0.30f, 0.06f, 1.3f, 0.35f, 0.22f),
        Ribbon(0x4A7CFFD6, 0.38f, 0.05f, 1.9f, 0.5f, 0.16f),
        Ribbon(0x40B48CFF.toInt(), 0.26f, 0.07f, 0.9f, 0.27f, 0.20f),
    )
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ribbon = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val bg = Paint()
    private var shader: Shader? = null
    private var shaderH = 0f

    override fun dayBackgroundForMoonBite() = 0xFF081529.toInt()

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        val t = nowMs / 1000.0
        if (night) {
            c.drawColor(Color.BLACK)
            p.color = NIGHT_TEXT
            for (s in stars.take(30)) { p.alpha = 35; c.drawCircle(s.x * w, s.y * h, s.r * 0.6f, p) }
            return
        }
        if (shader == null || shaderH != h) {
            shader = LinearGradient(0f, 0f, 0f, h, 0xFF040B1C.toInt(), 0xFF0E2A4A.toInt(), Shader.TileMode.CLAMP); shaderH = h
        }
        bg.shader = shader
        c.drawRect(0f, 0f, w, h, bg)
        p.color = Color.WHITE
        for (s in stars) { p.alpha = (120 + 135 * sin(t * 1.3 + s.phase)).toInt().coerceIn(0, 255); c.drawCircle(s.x * w, s.y * h, s.r, p) }
        // Aurora ribbons: a wavy band with a vertical fade, drawn as a filled path.
        for (r in ribbons) {
            path.reset()
            var x = 0f
            while (x <= w + 16f) {
                val y = h * (r.base + r.amp * sin(x / w * 6.283 * r.freq + t * r.speed).toFloat())
                if (x == 0f) path.moveTo(x, y) else path.lineTo(x, y)
                x += 16f
            }
            x = w + 16f
            while (x >= -16f) {
                val y = h * (r.base + r.thick + r.amp * 0.6f * sin(x / w * 6.283 * r.freq * 1.4 + t * r.speed * 1.3 + 1).toFloat())
                path.lineTo(x, y)
                x -= 16f
            }
            path.close()
            ribbon.shader = LinearGradient(0f, h * r.base, 0f, h * (r.base + r.thick), r.color, 0x00000000, Shader.TileMode.CLAMP)
            c.drawPath(path, ribbon)
        }
        // Snowy mountains
        p.color = 0xFF1D3B5E.toInt()
        path.reset(); path.moveTo(0f, h)
        for (i in 0..8) { val px = w * i / 8f; path.lineTo(px, h * (0.78f + (if (i % 2 == 0) 0.10f else -0.02f) + 0.03f * sin(i * 1.7).toFloat())) }
        path.lineTo(w, h); path.close(); c.drawPath(path, p)
        p.color = 0xFFDCE9F7.toInt()
        for (i in 1..7 step 2) {
            val px = w * i / 8f; val py = h * (0.76f + 0.03f * sin(i * 1.7).toFloat())
            path.reset(); path.moveTo(px, py); path.lineTo(px + w * 0.035f, py + h * 0.07f); path.lineTo(px - w * 0.035f, py + h * 0.07f); path.close()
            c.drawPath(path, p)
        }
        p.color = 0xFF0B1E35.toInt()
        c.drawRect(0f, h * 0.90f, w, h, p)
    }
}
