package com.acechase.kidclock.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import com.acechase.kidclock.art.Doodles
import com.acechase.kidclock.art.Doodles.Style
import kotlin.math.sin

/**
 * Nine thin strings drawn in pink marker rise up a blush ground, threaded with beads (some
 * coloured in, some just circled) and hearts doodled in a different style each. The strings
 * sway gently and everything on them sways along.
 */
class DoodleStringsFace(context: Context) : ClockFace(context) {
    private class Bead(val at: Int, val r: Float, val filled: Boolean)
    private class Heart(val at: Int, val size: Float, val color: Int, val style: Style)
    private class Str(val xs: FloatArray, val ctrlX: Float, val ctrlY: Float, val beads: List<Bead>, val hearts: List<Heart>, val phase: Float)

    private val ys = floatArrayOf(820f, 700f, 580f, 450f, 320f, 190f, 60f, -20f)
    private val strings = listOf(
        Str(floatArrayOf(84f, 96f, 80f, 100f, 88f, 104f, 90f, 98f), 70f, 760f,
            listOf(Bead(1, 5f, true), Bead(3, 6f, false), Bead(4, 4f, true), Bead(6, 5f, false)),
            listOf(Heart(2, 70f, RED, Style.CROSSHATCH), Heart(5, 46f, MAGENTA, Style.POLKA)), 0.0f),
        Str(floatArrayOf(240f, 222f, 238f, 224f, 242f, 226f, 240f, 230f), 256f, 755f,
            listOf(Bead(1, 4f, false), Bead(3, 6f, true), Bead(4, 5f, false), Bead(6, 4f, true)),
            listOf(Heart(2, 40f, LIGHT, Style.SOLID), Heart(5, 80f, RED, Style.LAYERED)), 0.9f),
        Str(floatArrayOf(350f, 366f, 352f, 368f, 354f, 370f, 356f, 364f), 336f, 765f,
            listOf(Bead(1, 5f, true), Bead(2, 4f, false), Bead(4, 6f, false), Bead(5, 4f, true)),
            listOf(Heart(3, 56f, PINK, Style.SKETCHED), Heart(6, 36f, MAGENTA, Style.DASHED)), 1.7f),
        Str(floatArrayOf(510f, 494f, 508f, 496f, 510f, 498f, 512f, 504f), 524f, 760f,
            listOf(Bead(2, 5f, false), Bead(3, 3f, true), Bead(4, 4f, false), Bead(5, 5f, true)),
            listOf(Heart(1, 50f, RED, Style.STRIPED), Heart(6, 34f, PLUM, Style.SOLID)), 2.6f),
        Str(floatArrayOf(630f, 646f, 634f, 648f, 636f, 650f, 638f, 644f), 616f, 755f,
            listOf(Bead(2, 5f, true), Bead(3, 4f, false), Bead(4, 3f, true), Bead(5, 5f, false), Bead(6, 4f, true)),
            listOf(Heart(1, 44f, PINK, Style.GINGHAM)), 3.4f),
        Str(floatArrayOf(790f, 774f, 788f, 776f, 790f, 778f, 792f, 784f), 804f, 760f,
            listOf(Bead(1, 6f, false), Bead(3, 4f, true), Bead(4, 3f, false), Bead(5, 5f, true)),
            listOf(Heart(2, 38f, PLUM, Style.DASHED), Heart(6, 40f, LIGHT, Style.SCRIBBLE)), 4.1f),
        Str(floatArrayOf(900f, 916f, 902f, 918f, 904f, 920f, 906f, 914f), 886f, 765f,
            listOf(Bead(2, 5f, true), Bead(3, 4f, false), Bead(5, 6f, true), Bead(6, 4f, false)),
            listOf(Heart(1, 60f, RED, Style.POLKA), Heart(4, 44f, PINK, Style.LAYERED)), 5.0f),
        Str(floatArrayOf(1050f, 1034f, 1048f, 1036f, 1050f, 1038f, 1052f, 1044f), 1064f, 755f,
            listOf(Bead(1, 4f, true), Bead(3, 6f, false), Bead(4, 5f, true), Bead(5, 4f, false)),
            listOf(Heart(2, 84f, MAGENTA, Style.CROSSHATCH), Heart(6, 36f, RED, Style.SOLID)), 5.8f),
        Str(floatArrayOf(1170f, 1186f, 1172f, 1188f, 1174f, 1190f, 1176f, 1184f), 1156f, 760f,
            listOf(Bead(2, 5f, false), Bead(4, 6f, true), Bead(5, 4f, false), Bead(6, 3f, true)),
            listOf(Heart(3, 56f, RED, Style.STITCHED), Heart(1, 66f, RED, Style.SKETCHED)), 0.5f),
    )
    private val bg = Paint()
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private var shader: Shader? = null
    private var shaderH = 0f

    override val dayTextColor = 0xFF7A1F3D.toInt()
    override val dayShadowColor = 0x1A000000
    override fun dayBackgroundForMoonBite() = 0xFFFFF7F3.toInt()

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        if (night) { c.drawColor(Color.BLACK); return }
        if (shader == null || shaderH != h) {
            shader = LinearGradient(0f, 0f, 0f, h, 0xFFFFF7F3.toInt(), 0xFFFFE6EE.toInt(), Shader.TileMode.CLAMP); shaderH = h
        }
        bg.shader = shader
        c.drawRect(0f, 0f, w, h, bg)
        val sx = w / 1280f; val sy = h / 800f; val s = sy
        val t = nowMs / 1000.0
        for (str in strings) {
            // The whole string sways a little, more at the top where it hangs free.
            fun swayAt(y: Float) = (sin(t * 0.45 + str.phase) * 9.0 * (1.0 - y / 820.0)).toFloat() * sx
            fun px(i: Int) = str.xs[i] * sx + swayAt(ys[i])
            fun py(i: Int) = ys[i] * sy
            path.reset()
            path.moveTo(px(0), py(0))
            var cx = str.ctrlX * sx + swayAt(str.ctrlY); var cy = str.ctrlY * sy
            path.quadTo(cx, cy, px(1), py(1))
            for (i in 2 until ys.size) {
                // Smooth continuation: reflect the previous control point through the last point.
                cx = 2f * px(i - 1) - cx; cy = 2f * py(i - 1) - cy
                path.quadTo(cx, cy, px(i), py(i))
            }
            p.reset(); p.isAntiAlias = true
            p.style = Paint.Style.STROKE; p.strokeWidth = 2.2f * s; p.color = PINK; p.alpha = 205
            c.drawPath(path, p)
            p.strokeWidth = 2.2f * s; p.alpha = 255
            for (b in str.beads) {
                p.style = if (b.filled) Paint.Style.FILL else Paint.Style.STROKE
                c.drawCircle(px(b.at), py(b.at), b.r * s, p)
            }
            for (ht in str.hearts) Doodles.heart(c, p, px(ht.at), py(ht.at), ht.size * s, swayAt(ys[ht.at]) * 0.6f, ht.color, ht.style)
        }
    }

    companion object {
        const val RED = 0xFFD32F2F.toInt()
        const val MAGENTA = 0xFFC2185B.toInt()
        const val PINK = 0xFFE0558A.toInt()
        const val LIGHT = 0xFFF06292.toInt()
        const val PLUM = 0xFF8E1C5A.toInt()
    }
}
