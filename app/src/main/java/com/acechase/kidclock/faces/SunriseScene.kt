package com.acechase.kidclock.faces

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import com.acechase.kidclock.night.Sunrise
import kotlin.math.sin
import kotlin.random.Random

/**
 * The gentle wake-up scene, painted in place of the chosen background: a sunrise over rolling
 * hills that starts as deep night with stars, warms to first light, lifts the sun over the hills
 * on the left and ends in full morning with birds. [draw] takes the sunrise's progress 0..1
 * (night/Sunrise does the arithmetic); the sun keeps to the left so the time, the quote card and
 * the weather stay clear of it.
 */
class SunriseScene {
    private class Star(val x: Float, val y: Float, val r: Float, val phase: Float, val speed: Float)
    private class Cloud(val x: Float, val y: Float, val scale: Float, val speed: Float, val phase: Float)
    private class Bird(val y: Float, val speed: Float, val offset: Float, val size: Float, val phase: Float)

    private val rnd = Random(21)
    private val stars = List(90) { Star(rnd.nextFloat(), rnd.nextFloat() * 0.66f, 1.2f + rnd.nextFloat() * 2.6f, rnd.nextFloat() * 6.28f, 0.6f + rnd.nextFloat() * 1.4f) }
    // Clouds sit where nothing else does: two along the top edge above the time, one low on the left
    // beside the sun's column. They sway rather than drift, so none ends up behind the weather.
    private val clouds = listOf(
        Cloud(0.30f, 0.075f, 1.0f, 0.05f, 0.0f), Cloud(0.58f, 0.065f, 0.8f, 0.04f, 2.1f), Cloud(0.05f, 0.33f, 0.9f, 0.06f, 4.0f),
    )
    private val birds = List(3) { Bird(0.05f + rnd.nextFloat() * 0.07f, 0.012f + rnd.nextFloat() * 0.008f, rnd.nextFloat(), 0.9f + rnd.nextFloat() * 0.5f, rnd.nextFloat() * 6.28f) }

    private val bg = Paint()
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val birdPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
    private val path = Path()
    private var shaderKey = ""

    fun draw(c: Canvas, w: Float, h: Float, nowMs: Long, progress: Float) {
        val pr = progress.coerceIn(0f, 1f)
        val t = nowMs / 1000.0
        val horizon = h * HORIZON
        val sunR = h * 0.085f
        val sunX = w * SUN_X
        // The sun climbs from behind the hills to the upper left; slow to show, then steady.
        val sunY = horizon + sunR * 1.6f - (horizon + sunR * 1.6f - h * 0.22f) * Sunrise.sunHeight(pr)
        val sunColor = Sunrise.keyColor(Sunrise.SUN, pr)

        // Shaders change with the progress; rebuild them a couple of hundred times per sunrise, not per frame.
        val key = "${(pr * 200).toInt()}-$w-$h"
        if (shaderKey != key) {
            shaderKey = key
            bg.shader = LinearGradient(
                0f, 0f, 0f, horizon,
                intArrayOf(Sunrise.keyColor(Sunrise.SKY_TOP, pr), Sunrise.keyColor(Sunrise.SKY_MID, pr), Sunrise.keyColor(Sunrise.SKY_LOW, pr)),
                floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP,
            )
            val glow = Sunrise.keyColor(Sunrise.GLOW, pr)
            val strength = (255 * Sunrise.keyValue(Sunrise.GLOW_STRENGTH, pr)).toInt()
            glowPaint.shader = RadialGradient(sunX, horizon, w * 0.55f, withAlpha(glow, strength), withAlpha(glow, 0), Shader.TileMode.CLAMP)
            val haloA = (150 * ((pr - 0.2f) / 0.4f).coerceIn(0f, 1f)).toInt()
            haloPaint.shader = RadialGradient(0f, 0f, sunR * 3.4f, withAlpha(sunColor, haloA), withAlpha(sunColor, 0), Shader.TileMode.CLAMP)
        }

        // Sky, then the glow that sits on the horizon where the sun will come up.
        c.drawRect(0f, 0f, w, h, bg)
        c.drawRect(0f, 0f, w, horizon, glowPaint)

        // Stars twinkle out as first light comes.
        val starA = Sunrise.starAlpha(pr)
        if (starA > 0f) {
            p.color = 0xFFFFFFFF.toInt()
            for (s in stars) {
                val tw = 0.5f + 0.5f * sin(t * s.speed + s.phase).toFloat()
                p.alpha = (235 * tw * starA).toInt()
                c.drawCircle(s.x * w, s.y * h, s.r * h / 800f, p)
            }
        }

        // Sun: a soft halo, then the disc. Drawn before the hills so it rises from behind them.
        c.save(); c.translate(sunX, sunY)
        c.drawCircle(0f, 0f, sunR * 3.4f, haloPaint)
        c.restore()
        p.color = sunColor; p.alpha = 255
        c.drawCircle(sunX, sunY, sunR, p)

        // Clouds sway a little, catching the colour of the sky around them.
        val cloudColor = Sunrise.keyColor(Sunrise.CLOUD, pr)
        for (cl in clouds) {
            val cx = (cl.x + 0.02f * sin(t * cl.speed + cl.phase).toFloat()) * w
            val cy = cl.y * h
            val s = cl.scale * h * 0.05f
            p.color = cloudColor; p.alpha = 225
            c.drawCircle(cx, cy, s, p)
            c.drawCircle(cx + s * 1.3f, cy + s * 0.15f, s * 0.85f, p)
            c.drawCircle(cx - s * 1.2f, cy + s * 0.2f, s * 0.75f, p)
            c.drawCircle(cx + s * 0.4f, cy - s * 0.5f, s * 0.8f, p)
            c.drawRoundRect(cx - s * 1.7f, cy, cx + s * 1.9f, cy + s * 0.7f, s * 0.5f, s * 0.5f, p)
        }

        // Hills: a far ridge the sun comes over, a nearer one the status line sits on.
        hill(c, w, h, horizon, Sunrise.keyColor(Sunrise.HILL_FAR, pr), 0.78f, 0.86f, 0.70f, 0.82f)
        hill(c, w, h, horizon, Sunrise.keyColor(Sunrise.HILL_NEAR, pr), 0.90f, 0.80f, 0.98f, 0.86f)

        // Birds glide in once the sun is well up, along the top edge from the right, clear of the weather.
        val birdA = Sunrise.birdAlpha(pr)
        if (birdA > 0f) {
            birdPaint.color = 0xFF1E2A44.toInt(); birdPaint.alpha = (200 * birdA).toInt()
            birdPaint.strokeWidth = h * 0.004f
            for (b in birds) {
                val bx = (0.68f - ((b.offset + t * b.speed) % 0.8).toFloat()) * w
                val by = (b.y + 0.02f * sin(t * 0.7 + b.phase).toFloat()) * h
                val span = h * 0.022f * b.size
                val flap = (0.35f + 0.5f * sin(t * 4.0 + b.phase).toFloat()) * span
                path.reset()
                path.moveTo(bx - span, by + flap * 0.4f); path.quadTo(bx - span * 0.5f, by - flap, bx, by)
                path.quadTo(bx + span * 0.5f, by - flap, bx + span, by + flap * 0.4f)
                c.drawPath(path, birdPaint)
            }
        }
    }

    /** A ridge: left edge at [y0], two cubic bends through [y1] and [y2] to the right edge at [y3], filled to the bottom. */
    private fun hill(c: Canvas, w: Float, h: Float, horizon: Float, color: Int, y0: Float, y1: Float, y2: Float, y3: Float) {
        p.color = color; p.alpha = 255
        path.reset(); path.moveTo(0f, h); path.lineTo(0f, h * y0)
        path.cubicTo(w * 0.25f, h * y1, w * 0.45f, h * y2, w * 0.68f, h * (y2 + 0.05f))
        path.cubicTo(w * 0.82f, h * (y2 + 0.08f), w * 0.9f, h * y3, w, h * y3)
        path.lineTo(w, h); path.close()
        c.drawPath(path, p)
    }

    private fun withAlpha(color: Int, alpha: Int): Int = (color and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)

    companion object {
        /** Where the far ridge meets the sky, as a fraction of the height. */
        const val HORIZON = 0.80f
        /** The sun's column, as a fraction of the width: left of the time and the quote card. */
        const val SUN_X = 0.14f
        /** Left edge of the quote card over the sunrise, clear of the sun's column. */
        const val CARD_LEFT = 0.28f
    }
}
