package com.acechase.kidclock.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import kotlin.math.sin

/** Teal sky over rolling waves. */
class OceanFace(context: Context) : ClockFace(context) {
    private val bg = Paint()
    private val wave = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private var shader: Shader? = null
    private var shaderH = 0f
    private val waveColors = intArrayOf(0xAA1E88E5.toInt(), 0xAA1565C0.toInt(), 0xCC0D47A1.toInt())

    override fun dayBackgroundForMoonBite() = 0xFF2BB5C9.toInt()

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        if (night) {
            c.drawColor(Color.BLACK)
            wave.color = NIGHT_TEXT; wave.alpha = 30
            drawWave(c, w, h, 0.86f, h * 0.02f, 0.0, 1.6f)
            return
        }
        if (shader == null || shaderH != h) {
            shader = LinearGradient(0f, 0f, 0f, h, 0xFF4FC3F7.toInt(), 0xFF26A69A.toInt(), Shader.TileMode.CLAMP)
            shaderH = h
        }
        bg.shader = shader
        c.drawRect(0f, 0f, w, h, bg)
        val t = nowMs / 1000.0
        for (i in waveColors.indices) {
            wave.color = waveColors[i]
            drawWave(c, w, h, 0.78f + i * 0.06f, h * (0.03f - i * 0.005f), t * (0.6 + i * 0.25) + i, 1.2f + i * 0.4f)
        }
    }

    private fun drawWave(c: Canvas, w: Float, h: Float, baseFrac: Float, amp: Float, phase: Double, freq: Float) {
        val base = h * baseFrac
        path.reset()
        path.moveTo(0f, h)
        var x = 0f
        while (x <= w + 8f) {
            val y = base + amp * sin(phase + x / w * 6.283 * freq).toFloat()
            path.lineTo(x, y)
            x += 8f
        }
        path.lineTo(w, h)
        path.close()
        c.drawPath(path, wave)
    }
}
