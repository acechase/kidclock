package com.acechase.kidclock.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.acechase.kidclock.art.Doodles
import com.acechase.kidclock.art.Doodles.Style

/**
 * A ruled notebook page with a red margin, covered in hearts doodled in red, pink, magenta and
 * plum marker: crosshatched, polka-dotted, layered, sketched, dashed, striped, gingham, stitched,
 * a balloon, an arrow heart, a halo, a cursive chain. Nothing moves; it is painted once.
 */
class NotebookFace(context: Context) : ClockFace(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val layer = StaticLayer(::paintPage)

    override val dayTextColor = 0xFF2A2F45.toInt()
    override val dayShadowColor = 0x14000000
    override fun dayBackgroundForMoonBite() = PAPER

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        if (night) { c.drawColor(Color.BLACK); return }
        layer.draw(c, w, h)
    }

    private fun paintPage(c: Canvas, w: Float, h: Float) {
        c.drawColor(PAPER)
        val sx = w / 1280f; val sy = h / 800f; val s = sy
        p.reset(); p.isAntiAlias = true
        p.style = Paint.Style.STROKE; p.strokeWidth = 1.5f * s; p.color = 0xFFC5D8EA.toInt()
        var y = 66f * sy
        while (y < h) { c.drawLine(0f, y, w, y, p); y += 46f * sy }
        p.strokeWidth = 2f * s; p.color = 0xFFF2A4AE.toInt()
        c.drawLine(118f * sx, 0f, 118f * sx, h, p)

        fun heart(x: Float, yy: Float, size: Float, rot: Float, color: Int, style: Style) =
            Doodles.heart(c, p, x * sx, yy * sy, size * s, rot, color, style)
        // Top band
        heart(70f, 110f, 56f, -8f, RED, Style.LAYERED)
        heart(180f, 70f, 40f, 10f, MAGENTA, Style.SOLID)
        heart(300f, 120f, 70f, -6f, RED, Style.CROSSHATCH)
        heart(420f, 60f, 34f, 14f, LIGHT, Style.SOLID)
        heart(612f, 78f, 50f, -10f, PINK, Style.SKETCHED)
        heart(700f, 60f, 38f, 8f, MAGENTA, Style.DASHED)
        heart(860f, 100f, 60f, -4f, PLUM, Style.POLKA)
        heart(860f, 500f, 46f, 12f, LIGHT, Style.SCRIBBLE)
        heart(1150f, 200f, 70f, -6f, RED, Style.SHINING)
        heart(1230f, 330f, 44f, 10f, MAGENTA, Style.STRIPED)
        heart(1044f, 480f, 40f, -4f, LIGHT, Style.OUTLINE)
        heart(1075f, 484f, 40f, -4f, RED, Style.OUTLINE)
        // Left column
        heart(60f, 262f, 62f, -14f, PINK, Style.BALLOON)
        heart(190f, 220f, 80f, 6f, PINK, Style.GINGHAM)
        heart(300f, 300f, 40f, -8f, RED, Style.SOLID)
        heart(80f, 400f, 66f, -6f, PLUM, Style.HALO)
        heart(230f, 420f, 54f, -10f, MAGENTA, Style.LOOP)
        heart(60f, 500f, 44f, 8f, RED, Style.STITCHED)
        heart(160f, 600f, 72f, 6f, RED, Style.ARROW)
        heart(400f, 740f, 50f, -8f, PLUM, Style.DASHED)
        Doodles.chain(c, p, 121f * sx, 727f * sy, 202f * s, -3f, PINK)
        heart(340f, 750f, 40f, 10f, MAGENTA, Style.SCRIBBLE)
        // Middle and bottom
        heart(480f, 730f, 38f, -6f, RED, Style.SOLID)
        heart(560f, 620f, 40f, 8f, LIGHT, Style.STRIPED)
        heart(640f, 560f, 22f, -10f, MAGENTA, Style.SOLID)
        heart(760f, 640f, 46f, -5f, PINK, Style.GINGHAM)
        heart(880f, 580f, 36f, 12f, MAGENTA, Style.SOLID)
        heart(600f, 150f, 18f, 10f, LIGHT, Style.SOLID)
        heart(1040f, 140f, 20f, -8f, RED, Style.SOLID)
        // Right column
        heart(1080f, 400f, 36f, 8f, LIGHT, Style.SOLID)
        heart(1180f, 480f, 60f, -12f, PINK, Style.SKETCHED)
        heart(1000f, 540f, 52f, 6f, LIGHT, Style.SHINING)
        heart(1230f, 600f, 52f, 10f, RED, Style.POLKA)
        heart(1120f, 680f, 70f, -6f, MAGENTA, Style.LAYERED)
        heart(990f, 720f, 44f, 8f, LIGHT, Style.CROSSHATCH)
        heart(1240f, 740f, 30f, -10f, PLUM, Style.SOLID)
    }

    companion object {
        const val PAPER = 0xFFFFFEFA.toInt()
        const val RED = 0xFFD32F2F.toInt()
        const val MAGENTA = 0xFFC2185B.toInt()
        const val PINK = 0xFFE0558A.toInt()
        const val LIGHT = 0xFFF06292.toInt()
        const val PLUM = 0xFF8E1C5A.toInt()
    }
}
