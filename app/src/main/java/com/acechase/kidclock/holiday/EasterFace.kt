package com.acechase.kidclock.holiday

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import com.acechase.kidclock.art.Doodles
import com.acechase.kidclock.faces.ClockFace
import com.acechase.kidclock.faces.StaticLayer
import kotlin.math.sin

/**
 * Pastel Eggs: lilac to mint to butter, a row of patterned eggs in mint grass along the bottom
 * (stripes, dots, zigzags, tiny hearts, a band, lace, a wave), blossom sprigs in the top corners,
 * and a few petals drifting in the air.
 */
class EasterFace(context: Context) : ClockFace(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bg = Paint()
    private val layer = StaticLayer(::paintStill)
    private val path = Path()
    private val petals = floatArrayOf(300f, 300f, 0f, 980f, 330f, 2.1f, 200f, 480f, 4.2f, 1090f, 520f, 1.1f, 640f, 120f, 3.3f)

    override val dayTextColor = 0xFF6A3D9A.toInt()
    override val dayShadowColor = 0x1A000000
    override fun dayBackgroundForMoonBite() = 0xFFEDE4FF.toInt()

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        if (night) { c.drawColor(Color.BLACK); return }
        layer.draw(c, w, h)
        val sx = w / 1280f; val sy = h / 800f; val s = sy
        val t = nowMs / 1000.0
        var i = 0
        while (i < petals.size) {
            val ph = petals[i + 2].toDouble()
            val x = petals[i] * sx + (sin(t * 0.4 + ph) * 24.0).toFloat() * sx
            val y = petals[i + 1] * sy + (sin(t * 0.7 + ph * 1.3) * 14.0).toFloat() * sy
            p.reset(); p.isAntiAlias = true; p.color = 0xFFF8B4C8.toInt(); p.alpha = 210
            c.save(); c.rotate((t * 30 + ph * 60).toFloat(), x, y)
            c.drawOval(x - 5f * s, y - 8f * s, x + 5f * s, y + 8f * s, p)
            c.restore()
            i += 3
        }
    }

    private fun paintStill(c: Canvas, w: Float, h: Float) {
        val sx = w / 1280f; val sy = h / 800f; val s = sy
        bg.shader = LinearGradient(0f, 0f, 0f, h, intArrayOf(0xFFEDE4FF.toInt(), 0xFFDFF7EC.toInt(), 0xFFFFF6D6.toInt()), floatArrayOf(0f, 0.52f, 1f), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, bg)
        // Grass blades
        p.reset(); p.isAntiAlias = true; p.style = Paint.Style.STROKE; p.strokeWidth = 3f * s; p.strokeCap = Paint.Cap.ROUND; p.color = 0xFF9FDFC0.toInt()
        val blades = floatArrayOf(30f, 70f, 60f, 60f, 110f, 80f, 170f, 56f, 240f, 70f, 330f, 58f, 400f, 72f, 470f, 50f, 560f, 70f, 640f, 62f, 720f, 66f, 800f, 56f, 880f, 74f, 960f, 58f, 1040f, 70f, 1110f, 60f, 1180f, 72f, 1250f, 54f)
        var i = 0
        while (i < blades.size) {
            val x = blades[i] * sx; val len = blades[i + 1] * sy
            path.reset(); path.moveTo(x, h); path.quadTo(x + (if (i % 4 == 0) 6f else -6f) * sx, h - len * 0.6f, x + (if (i % 4 == 0) -2f else 4f) * sx, h - len)
            c.drawPath(path, p)
            i += 2
        }
        // Eggs
        egg(c, 90f * sx, 640f * sy, 130f * s, -10f, 0xFFFFC2D1.toInt()) { stripes(c, 0xFFF48FB1.toInt()); heartOn(c, 50f, 57f, Color.WHITE) }
        egg(c, 290f * sx, 690f * sy, 110f * s, 8f, 0xFFC8B6FF.toInt()) { dots(c, Color.WHITE) }
        egg(c, 470f * sx, 650f * sy, 120f * s, -6f, 0xFFB8F2D6.toInt()) { zigzag(c, 0xFF3FBF7F.toInt()) }
        egg(c, 780f * sx, 680f * sy, 115f * s, 10f, 0xFFFFE8A3.toInt()) {
            heartOn(c, 36f, 36f, 0xFFF48FB1.toInt()); heartOn(c, 62f, 44f, 0xFFF48FB1.toInt()); heartOn(c, 32f, 62f, 0xFFF48FB1.toInt()); heartOn(c, 56f, 70f, 0xFFF48FB1.toInt()); heartOn(c, 74f, 66f, 0xFFF48FB1.toInt())
        }
        egg(c, 990f * sx, 650f * sy, 130f * s, -12f, 0xFFBDE0FE.toInt()) {
            p.style = Paint.Style.STROKE; p.strokeWidth = 9f; p.color = Color.WHITE; c.drawLine(0f, 50f, 100f, 50f, p)
            p.style = Paint.Style.FILL; p.color = 0xFF6A3D9A.toInt(); p.alpha = 140
            c.drawCircle(30f, 50f, 2.5f, p); c.drawCircle(50f, 50f, 2.5f, p); c.drawCircle(70f, 50f, 2.5f, p)
            p.color = Color.WHITE; c.drawCircle(44f, 28f, 3f, p); c.drawCircle(60f, 76f, 3f, p)
        }
        egg(c, 1180f * sx, 700f * sy, 90f * s, 14f, 0xFFFFC2D1.toInt()) {
            p.style = Paint.Style.STROKE; p.strokeWidth = 3f; p.color = Color.WHITE; p.strokeCap = Paint.Cap.ROUND
            p.pathEffect = android.graphics.DashPathEffect(floatArrayOf(0.5f, 6f), 0f)
            for (yy in floatArrayOf(36f, 56f, 76f)) c.drawLine(0f, yy, 100f, yy, p)
            p.pathEffect = null
        }
        egg(c, 630f * sx, 720f * sy, 80f * s, -14f, 0xFFC8B6FF.toInt()) {
            p.style = Paint.Style.STROKE; p.strokeWidth = 5f; p.color = Color.WHITE
            path.reset(); path.moveTo(0f, 44f); path.quadTo(25f, 30f, 50f, 44f); path.quadTo(75f, 58f, 100f, 44f); c.drawPath(path, p)
        }
        // Blossom sprigs in the top corners
        p.reset(); p.isAntiAlias = true; p.style = Paint.Style.STROKE; p.strokeWidth = 2.5f * s; p.strokeCap = Paint.Cap.ROUND; p.color = 0xFF8C6A4E.toInt()
        for (side in intArrayOf(1, -1)) {
            val ox = if (side == 1) 0f else w
            fun x(v: Float) = ox + side * v * sx
            path.reset(); path.moveTo(x(-10f), 60f * sy); path.quadTo(x(80f), 90f * sy, x(160f), 210f * sy); c.drawPath(path, p)
            path.reset(); path.moveTo(x(60f), 100f * sy); path.quadTo(x(90f), 90f * sy, x(110f), 110f * sy); c.drawPath(path, p)
            path.reset(); path.moveTo(x(110f), 150f * sy); path.quadTo(x(140f), 144f * sy, x(150f), 166f * sy); c.drawPath(path, p)
            blossom(c, x(70f), 92f * sy, 5f * s); blossom(c, x(112f), 112f * sy, 4f * s); blossom(c, x(152f), 168f * sy, 5f * s); blossom(c, x(40f), 68f * sy, 3.5f * s)
            p.style = Paint.Style.STROKE; p.color = 0xFF8C6A4E.toInt(); p.strokeWidth = 2.5f * s
        }
    }

    private fun blossom(c: Canvas, cx: Float, cy: Float, r: Float) {
        p.style = Paint.Style.FILL; p.color = 0xFFF8B4C8.toInt()
        for (k in 0 until 5) { val a = Math.toRadians(-90.0 + k * 72); c.drawCircle(cx + (Math.cos(a) * r * 1.4).toFloat(), cy + (Math.sin(a) * r * 1.4).toFloat(), r, p) }
        p.color = 0xFFFFE08A.toInt(); c.drawCircle(cx, cy, r * 0.6f, p)
    }

    /** An egg at [cx],[cy], [size] tall, filled with [fill]; [decorate] draws inside it in 100-unit space. */
    private inline fun egg(c: Canvas, cx: Float, cy: Float, size: Float, rot: Float, fill: Int, decorate: () -> Unit) {
        Doodles.draw(c, cx, cy, size, rot) {
            p.reset(); p.isAntiAlias = true; p.style = Paint.Style.FILL; p.color = fill
            c.drawPath(Doodles.egg, p)
            c.save(); c.clipPath(Doodles.egg)
            decorate()
            c.restore()
        }
    }
    private fun stripes(c: Canvas, color: Int) {
        p.style = Paint.Style.STROKE; p.strokeWidth = 7f; p.color = color
        for (yy in floatArrayOf(30f, 48f, 66f, 84f)) c.drawLine(0f, yy, 100f, yy, p)
    }
    private fun dots(c: Canvas, color: Int) {
        p.style = Paint.Style.FILL; p.color = color; p.alpha = 230
        for (pt in arrayOf(36f to 32f, 60f to 40f, 30f to 56f, 54f to 64f, 72f to 58f, 44f to 82f, 66f to 84f)) c.drawCircle(pt.first, pt.second, 4.5f, p)
    }
    private fun zigzag(c: Canvas, color: Int) {
        p.style = Paint.Style.STROKE; p.strokeWidth = 3.5f; p.color = color; p.strokeJoin = Paint.Join.ROUND
        for (base in floatArrayOf(40f, 62f)) {
            path.reset(); path.moveTo(0f, base)
            var x = 0f; var up = true
            while (x < 100f) { x += 10f; path.lineTo(x, if (up) base - 8f else base); up = !up }
            c.drawPath(path, p)
        }
    }
    private fun heartOn(c: Canvas, x: Float, y: Float, color: Int) {
        p.style = Paint.Style.FILL; p.color = color
        c.save(); c.translate(x, y); c.scale(0.13f, 0.13f); c.translate(-50f, -50f); c.drawPath(Doodles.heart, p); c.restore()
    }
}
