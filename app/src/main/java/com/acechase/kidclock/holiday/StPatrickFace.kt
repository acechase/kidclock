package com.acechase.kidclock.holiday

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import com.acechase.kidclock.art.Doodles
import com.acechase.kidclock.faces.ClockFace
import com.acechase.kidclock.faces.StaticLayer
import kotlin.math.sin

/** Clover: deep green with four-leaf clovers built from hearts, one in gold, and gold sparkles that twinkle. */
class StPatrickFace(context: Context) : ClockFace(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bg = Paint()
    private val layer = StaticLayer(::paintStill)
    private val sparkles = floatArrayOf(240f, 300f, 26f, 640f, 600f, 20f, 1000f, 300f, 22f, 180f, 700f, 18f, 1230f, 420f, 20f, 560f, 60f, 16f, 860f, 80f, 20f)

    override val dayTextColor = 0xFFF0F7E8.toInt()
    override val dayShadowColor = 0x80000000.toInt()
    override fun dayBackgroundForMoonBite() = 0xFF0B3528.toInt()

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        if (night) { c.drawColor(Color.BLACK); return }
        layer.draw(c, w, h)
        val sx = w / 1280f; val sy = h / 800f
        val t = nowMs / 1000.0
        var i = 0
        while (i < sparkles.size) {
            Doodles.sparkle(c, p, sparkles[i] * sx, sparkles[i + 1] * sy, sparkles[i + 2] * sy, 0xFFF3DDA0.toInt(), (140 + 110 * sin(t * 1.7 + i * 0.9)).toInt())
            i += 3
        }
    }

    private fun paintStill(c: Canvas, w: Float, h: Float) {
        val sx = w / 1280f; val sy = h / 800f; val s = sy
        bg.shader = LinearGradient(0f, 0f, 0f, h, intArrayOf(0xFF07271E.toInt(), 0xFF0F4A36.toInt(), 0xFF1C6B4A.toInt()), floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, bg)
        val bright = 0xFF3FBF7F.toInt(); val mid = 0xFF2FA66E.toInt(); val mint = 0xFF6EE7B7.toInt(); val gold = 0xFFF3DDA0.toInt()
        Doodles.clover(c, p, 120f * sx, 160f * sy, 140f * s, -12f, bright)
        Doodles.clover(c, p, 300f * sx, 620f * sy, 100f * s, 18f, mid)
        Doodles.clover(c, p, 1100f * sx, 180f * sy, 120f * s, 8f, mid)
        Doodles.clover(c, p, 1180f * sx, 600f * sy, 160f * s, -20f, bright)
        Doodles.clover(c, p, 560f * sx, 700f * sy, 80f * s, 10f, gold)
        Doodles.clover(c, p, 900f * sx, 640f * sy, 110f * s, -6f, mid)
        Doodles.clover(c, p, 60f * sx, 520f * sy, 70f * s, 24f, mint)
        Doodles.clover(c, p, 1020f * sx, 420f * sy, 60f * s, -14f, mint)
        Doodles.clover(c, p, 420f * sx, 80f * sy, 60f * s, 30f, mint)
        Doodles.clover(c, p, 760f * sx, 660f * sy, 50f * s, -28f, mint)
        p.reset(); p.isAntiAlias = true; p.color = gold
        c.drawCircle(700f * sx, 740f * sy, 3f * s, p); c.drawCircle(140f * sx, 380f * sy, 2.5f * s, p); c.drawCircle(1140f * sx, 340f * sy, 2.5f * s, p)
    }
}
