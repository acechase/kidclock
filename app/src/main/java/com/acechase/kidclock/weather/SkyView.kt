package com.acechase.kidclock.weather

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

/**
 * The weather page's background: a sky painted in code for the current [WeatherSky], like the clock backgrounds.
 * Everything is drawn in fractions of the view so it fits any screen; nothing animates, so it costs one draw.
 */
class SkyView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    var sky: WeatherSky = WeatherSky.CLOUDY
        set(v) { if (field != v) { field = v; invalidate() } }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
    private val rect = RectF()
    private val path = Path()

    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val g = sky.gradient
        paint.shader = LinearGradient(0f, 0f, 0f, h, g, if (g.size == 3) floatArrayOf(0f, 0.6f, 1f) else null, Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, paint)
        paint.shader = null
        when (sky) {
            WeatherSky.SUNNY -> {
                sun(c, w * 0.985f, 0f, w * 0.09f)
                cloud(c, w * 0.22f, h * 0.80f, w * 0.17f, 0x42FFFFFF)
                cloud(c, w * 0.74f, h * 0.90f, w * 0.13f, 0x2EFFFFFF)
            }
            WeatherSky.CLOUDY -> {
                cloud(c, w * 0.16f, h * 0.12f, w * 0.22f, 0x2EFFFFFF)
                cloud(c, w * 0.80f, h * 0.17f, w * 0.20f, 0x26FFFFFF)
                cloud(c, w * 0.52f, h * 0.92f, w * 0.28f, 0x1AFFFFFF)
            }
            WeatherSky.RAINY -> {
                cloud(c, w * 0.18f, h * 0.02f, w * 0.30f, 0x38000000)
                cloud(c, w * 0.78f, h * 0.04f, w * 0.26f, 0x33000000)
                rain(c, w, h, 0x33BEDCFF, w * 0.034f, 0f)
                rain(c, w, h, 0x1CBEDCFF, w * 0.045f, w * 0.017f)
                paint.color = 0x24BEDCFF
                rect.set(w * 0.06f, h * 0.925f, w * 0.39f, h * 0.97f); c.drawOval(rect, paint)
                rect.set(w * 0.56f, h * 0.945f, w * 0.80f, h * 0.98f); c.drawOval(rect, paint)
            }
            WeatherSky.SNOWY -> {
                cloud(c, w * 0.20f, h * 0.0f, w * 0.28f, 0x73FFFFFF)
                cloud(c, w * 0.78f, h * 0.02f, w * 0.24f, 0x66FFFFFF)
                paint.color = 0xE6FFFFFF.toInt()
                for (f in FLAKES) c.drawCircle(w * f[0], h * f[1], w * f[2], paint)
                paint.color = 0xA6FFFFFF.toInt()
                rect.set(-w * 0.08f, h * 0.90f, w * 0.55f, h * 1.15f); c.drawOval(rect, paint)
                paint.color = 0xB3FFFFFF.toInt()
                rect.set(w * 0.39f, h * 0.925f, w * 1.1f, h * 1.18f); c.drawOval(rect, paint)
            }
            WeatherSky.STORMY -> {
                cloud(c, w * 0.22f, h * 0.0f, w * 0.34f, 0x57000000)
                cloud(c, w * 0.80f, h * 0.02f, w * 0.28f, 0x4D000000)
                paint.color = 0x2EFFE27A
                c.drawCircle(w * 0.96f, h * 0.08f, w * 0.10f, paint)
                paint.color = 0x1AFFE27A
                c.drawCircle(w * 0.96f, h * 0.08f, w * 0.16f, paint)
                bolt(c, w * 0.96f, -h * 0.02f, h * 0.22f)
                rain(c, w, h, 0x24C8BEFF, w * 0.03f, 0f)
            }
            WeatherSky.NIGHT -> {
                paint.color = 0xEBFFFFFF.toInt()
                for (s in STARS) c.drawCircle(w * s[0], h * s[1], w * s[2], paint)
                moon(c, w * 0.955f, h * 0.05f, w * 0.05f)
                paint.color = 0x59000000
                rect.set(-w * 0.16f, h * 0.875f, w * 0.63f, h * 1.25f); c.drawOval(rect, paint)
                paint.color = 0x4D000000
                rect.set(w * 0.47f, h * 0.91f, w * 1.25f, h * 1.28f); c.drawOval(rect, paint)
            }
        }
    }

    /** Three bumps on a rounded base, centred on (cx, cy) with the base `r` wide. */
    private fun cloud(c: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        paint.color = color
        path.reset()
        path.addCircle(cx - r * 0.45f, cy, r * 0.42f, Path.Direction.CW)
        path.addCircle(cx + r * 0.05f, cy - r * 0.22f, r * 0.55f, Path.Direction.CW)
        path.addCircle(cx + r * 0.55f, cy + r * 0.02f, r * 0.40f, Path.Direction.CW)
        rect.set(cx - r * 0.85f, cy - r * 0.05f, cx + r * 0.95f, cy + r * 0.42f)
        path.addRoundRect(rect, r * 0.25f, r * 0.25f, Path.Direction.CW)
        c.drawPath(path, paint)
    }

    private fun sun(c: Canvas, cx: Float, cy: Float, r: Float) {
        stroke.color = 0x59FFECA0; stroke.strokeWidth = r * 0.09f
        for (i in 0 until 16) {
            val a = Math.toRadians(i * 22.5)
            val inner = r * (if (i % 2 == 0) 1.3f else 1.45f); val outer = r * (if (i % 2 == 0) 1.85f else 1.65f)
            c.drawLine(cx + inner * Math.cos(a).toFloat(), cy + inner * Math.sin(a).toFloat(), cx + outer * Math.cos(a).toFloat(), cy + outer * Math.sin(a).toFloat(), stroke)
        }
        paint.color = 0xFFFFE27A.toInt(); c.drawCircle(cx, cy, r, paint)
        stroke.color = 0x80FFFFFF.toInt(); stroke.strokeWidth = r * 0.05f; c.drawCircle(cx, cy, r, stroke)
    }

    private fun moon(c: Canvas, cx: Float, cy: Float, r: Float) {
        paint.color = 0x29FFF1B8; c.drawCircle(cx, cy, r * 1.9f, paint)
        paint.color = 0xFFFFF1B8.toInt(); c.drawCircle(cx, cy, r, paint)
        paint.color = sky.gradient[0]; c.drawCircle(cx + r * 0.45f, cy - r * 0.25f, r * 0.82f, paint)
    }

    private fun bolt(c: Canvas, cx: Float, top: Float, height: Float) {
        val u = height / 40f
        path.reset()
        path.moveTo(cx + 2 * u, top); path.lineTo(cx - 10 * u, top + 22 * u); path.lineTo(cx - 2 * u, top + 22 * u)
        path.lineTo(cx - 5 * u, top + 40 * u); path.lineTo(cx + 10 * u, top + 16 * u); path.lineTo(cx + 2 * u, top + 16 * u)
        path.lineTo(cx + 6 * u, top); path.close()
        paint.color = 0xFFFFE27A.toInt(); c.drawPath(path, paint)
    }

    /** Diagonal streaks across the whole view, every `gap` px, shifted by `offset`. */
    private fun rain(c: Canvas, w: Float, h: Float, color: Int, gap: Float, offset: Float) {
        stroke.color = color; stroke.strokeWidth = 2f
        val dx = h * 0.4f
        var x = -dx + offset
        while (x < w + dx) { c.drawLine(x + dx, 0f, x, h, stroke); x += gap }
    }

    companion object {
        /** x, y as fractions of the view; radius as a fraction of the width. */
        private val FLAKES = arrayOf(
            floatArrayOf(0.05f, 0.10f, 0.004f), floatArrayOf(0.14f, 0.05f, 0.003f), floatArrayOf(0.26f, 0.15f, 0.005f), floatArrayOf(0.37f, 0.04f, 0.003f),
            floatArrayOf(0.47f, 0.19f, 0.004f), floatArrayOf(0.56f, 0.09f, 0.004f), floatArrayOf(0.67f, 0.04f, 0.003f), floatArrayOf(0.77f, 0.14f, 0.004f),
            floatArrayOf(0.88f, 0.06f, 0.005f), floatArrayOf(0.95f, 0.20f, 0.003f), floatArrayOf(0.09f, 0.33f, 0.003f), floatArrayOf(0.22f, 0.41f, 0.004f),
            floatArrayOf(0.74f, 0.38f, 0.004f), floatArrayOf(0.92f, 0.41f, 0.003f), floatArrayOf(0.03f, 0.65f, 0.004f), floatArrayOf(0.97f, 0.70f, 0.004f),
            floatArrayOf(0.55f, 0.53f, 0.003f), floatArrayOf(0.44f, 0.75f, 0.004f), floatArrayOf(0.70f, 0.65f, 0.003f), floatArrayOf(0.86f, 0.80f, 0.004f),
        )
        private val STARS = arrayOf(
            floatArrayOf(0.04f, 0.08f, 0.0016f), floatArrayOf(0.11f, 0.15f, 0.0012f), floatArrayOf(0.17f, 0.05f, 0.0020f), floatArrayOf(0.26f, 0.11f, 0.0012f),
            floatArrayOf(0.32f, 0.21f, 0.0016f), floatArrayOf(0.41f, 0.06f, 0.0012f), floatArrayOf(0.50f, 0.14f, 0.0020f), floatArrayOf(0.59f, 0.05f, 0.0012f),
            floatArrayOf(0.69f, 0.19f, 0.0016f), floatArrayOf(0.77f, 0.08f, 0.0012f), floatArrayOf(0.96f, 0.10f, 0.0012f), floatArrayOf(0.06f, 0.38f, 0.0012f),
            floatArrayOf(0.94f, 0.38f, 0.0020f), floatArrayOf(0.47f, 0.31f, 0.0012f), floatArrayOf(0.23f, 0.31f, 0.0016f), floatArrayOf(0.78f, 0.33f, 0.0012f),
            floatArrayOf(0.36f, 0.41f, 0.0012f), floatArrayOf(0.59f, 0.27f, 0.0008f), floatArrayOf(0.14f, 0.25f, 0.0008f), floatArrayOf(0.90f, 0.29f, 0.0008f),
            floatArrayOf(0.44f, 0.16f, 0.0008f), floatArrayOf(0.63f, 0.42f, 0.0010f), floatArrayOf(0.85f, 0.55f, 0.0010f), floatArrayOf(0.30f, 0.60f, 0.0008f),
        )
    }
}
