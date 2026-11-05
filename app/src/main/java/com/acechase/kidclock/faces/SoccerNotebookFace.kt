package com.acechase.kidclock.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.acechase.kidclock.art.Doodles
import com.acechase.kidclock.looks.Fonts

/**
 * The ruled notebook page again, this time covered in marker doodles of soccer things: balls in
 * the real black-and-white panel pattern, boots with studs, goals with nets, whistles, cones,
 * trophies, number-9 jerseys, pennants, and "GOAL!" and "go team" scrawled in the margins. The
 * middle of the page is left clear for the time. Nothing moves; it is painted once.
 */
class SoccerNotebookFace(context: Context) : ClockFace(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fonts = Fonts.get(context)
    private val layer = StaticLayer(::paintPage)

    override val dayTextColor = NAVY
    override val dayShadowColor = 0x14000000
    override fun dayBackgroundForMoonBite() = PAPER

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        if (night) { c.drawColor(Color.BLACK); return }
        layer.draw(c, w, h)
    }

    // Mock-up geometry: a 1440x960 artboard. x as a fraction of the width, y and sizes of the height.
    private var sx = 1f
    private var sy = 1f

    private fun paintPage(c: Canvas, w: Float, h: Float) {
        sx = w / 1440f; sy = h / 960f
        c.drawColor(PAPER)
        p.reset(); p.isAntiAlias = true
        p.style = Paint.Style.STROKE; p.strokeWidth = 1.8f * sy; p.color = RULE
        var y = 79f * sy
        while (y < h) { c.drawLine(0f, y, w, y, p); y += 55f * sy }
        p.strokeWidth = 2.4f * sy; p.color = MARGIN
        c.drawLine(133f * sx, 0f, 133f * sx, h, p)

        // Top band
        ball(c, 40f, 50f, 80f, -8f)
        boot(c, 160f, 30f, 90f, 10f, GREEN)
        goal(c, 290f, 10f, 120f, -4f, NAVY)
        whistle(c, 470f, 20f, 70f, 12f, ORANGE)
        ball(c, 620f, 30f, 56f, 0f)
        jersey(c, 720f, 0f, 100f, -6f, BLUE)
        cone(c, 880f, 30f, 70f, 8f, NAVY)
        trophy(c, 1000f, 0f, 100f, -5f, ORANGE)
        ball(c, 1140f, 30f, 90f, 6f)
        pennant(c, 1270f, 10f, 90f, 10f, RED)
        // Left column
        boot(c, 20f, 180f, 110f, -14f, NAVY)
        ball(c, 60f, 320f, 70f, 8f)
        cone(c, 30f, 440f, 80f, -6f, NAVY)
        goal(c, 40f, 560f, 140f, 4f, GREEN)
        whistle(c, 30f, 720f, 90f, -10f, BLUE)
        ball(c, 160f, 800f, 100f, 12f)
        jersey(c, 40f, 860f, 80f, 6f, RED)
        // Right column
        trophy(c, 1270f, 190f, 110f, 8f, GREEN)
        ball(c, 1330f, 330f, 80f, -10f)
        pennant(c, 1260f, 430f, 100f, -8f, BLUE)
        boot(c, 1290f, 560f, 120f, 10f, ORANGE)
        goal(c, 1200f, 690f, 150f, -5f, NAVY)
        ball(c, 1350f, 860f, 70f, 6f)
        whistle(c, 1220f, 860f, 80f, 12f, GREEN)
        // Bottom band
        cone(c, 380f, 730f, 64f, -8f, NAVY)
        ball(c, 520f, 745f, 56f, 0f)
        boot(c, 660f, 720f, 90f, -6f, BLUE)
        trophy(c, 830f, 730f, 76f, 10f, RED)
        jersey(c, 980f, 725f, 84f, -4f, GREEN)
        // Small strays
        ball(c, 590f, 160f, 36f, 0f)
        ball(c, 1110f, 560f, 40f, 0f)
        whistle(c, 330f, 590f, 50f, -10f, NAVY)

        // Words in the margins, in marker.
        text.reset(); text.isAntiAlias = true; text.typeface = fonts.caveat
        word(c, "GOAL!", 210f, 120f, 64f, -8f, GREEN)
        word(c, "go team", 1060f, 630f, 54f, 6f, BLUE)
    }

    /** Caveat text whose CSS box sits at ([left],[top]) in artboard px with [size] px font, turned about its centre. */
    private fun word(c: Canvas, s: String, left: Float, top: Float, size: Float, rot: Float, color: Int) {
        text.color = color
        text.textSize = size * sy
        text.textAlign = Paint.Align.LEFT
        val tw = text.measureText(s)
        val cx = left * sx + tw / 2f
        val cy = (top + size * 0.62f) * sy
        c.save()
        c.rotate(rot, cx, cy)
        c.drawText(s, left * sx, (top + size * 0.86f) * sy, text)
        c.restore()
    }

    /** Runs [body] in a 100-unit box for a doodle whose [size]-px box has its top-left at ([x],[y]) on the artboard. */
    private inline fun place(c: Canvas, x: Float, y: Float, size: Float, rot: Float, body: () -> Unit) {
        Doodles.draw(c, (x + size / 2f) * sx, (y + size / 2f) * sy, size * sy, rot, 100f, body)
    }

    private fun marker(color: Int, width: Float) {
        p.reset(); p.isAntiAlias = true
        p.style = Paint.Style.STROKE; p.strokeWidth = width
        p.strokeJoin = Paint.Join.ROUND; p.strokeCap = Paint.Cap.ROUND
        p.color = color
    }

    // --- The soccer things, each authored in a 100x100 box like the mock-up's symbols. ---

    private val circle = Path().apply { addCircle(50f, 50f, 46f, Path.Direction.CW) }
    private val centrePentagon = polygon(50f, 33f, 66.2f, 44.7f, 60f, 63.8f, 40f, 63.8f, 33.8f, 44.7f)
    private val edgePentagon = polygon(50f, 17f, 35.7f, 6.6f, 41.2f, -10.1f, 58.8f, -10.1f, 64.3f, 6.6f)

    /** A ball with the real panel pattern: a black pentagon in the middle, five more round the rim, seams between. */
    private fun ball(c: Canvas, x: Float, y: Float, size: Float, rot: Float) = place(c, x, y, size, rot) {
        p.reset(); p.isAntiAlias = true
        p.style = Paint.Style.FILL; p.color = Color.WHITE
        c.drawPath(circle, p)
        c.save()
        c.clipPath(circle)
        p.color = BALL_INK
        c.drawPath(centrePentagon, p)
        for (k in 0 until 5) {
            c.save()
            c.rotate(k * 72f, 50f, 50f)
            p.style = Paint.Style.FILL
            c.drawPath(edgePentagon, p)
            p.style = Paint.Style.STROKE; p.strokeWidth = 2.5f
            c.drawLine(50f, 33f, 50f, 17f, p)
            c.restore()
        }
        c.restore()
        p.style = Paint.Style.STROKE; p.strokeWidth = 3f; p.color = BALL_INK
        c.drawPath(circle, p)
    }

    private val bootPath = Path().apply {
        moveTo(8f, 70f)
        cubicTo(8f, 60f, 20f, 56f, 30f, 54f)
        lineTo(44f, 40f)
        cubicTo(50f, 34f, 58f, 34f, 62f, 40f)
        lineTo(70f, 52f)
        cubicTo(78f, 56f, 90f, 58f, 92f, 68f)
        lineTo(92f, 76f); lineTo(8f, 76f); close()
    }
    private val bootStuds = Path().apply {
        moveTo(16f, 76f); rLineTo(0f, 8f)
        moveTo(30f, 76f); rLineTo(0f, 8f)
        moveTo(48f, 76f); rLineTo(0f, 8f)
        moveTo(66f, 76f); rLineTo(0f, 8f)
        moveTo(82f, 76f); rLineTo(0f, 8f)
        moveTo(46f, 46f); rLineTo(8f, 4f)
        moveTo(44f, 52f); rLineTo(8f, 4f)
    }

    private fun boot(c: Canvas, x: Float, y: Float, size: Float, rot: Float, color: Int) = place(c, x, y, size, rot) {
        marker(color, 4f)
        c.drawPath(bootPath, p)
        c.drawPath(bootStuds, p)
    }

    private val goalFrame = Path().apply { moveTo(10f, 90f); lineTo(10f, 20f); lineTo(90f, 20f); lineTo(90f, 90f) }
    private val goalNet = Path().apply {
        moveTo(10f, 40f); lineTo(90f, 40f)
        moveTo(10f, 60f); lineTo(90f, 60f)
        moveTo(10f, 78f); lineTo(90f, 78f)
        moveTo(30f, 20f); lineTo(30f, 90f)
        moveTo(50f, 20f); lineTo(50f, 90f)
        moveTo(70f, 20f); lineTo(70f, 90f)
    }

    private fun goal(c: Canvas, x: Float, y: Float, size: Float, rot: Float, color: Int) = place(c, x, y, size, rot) {
        marker(color, 4f)
        c.drawPath(goalFrame, p)
        p.strokeWidth = 1.6f
        c.drawPath(goalNet, p)
    }

    private val whistleBody = Path().apply {
        moveTo(40f, 48f); lineTo(88f, 48f)
        quadTo(92f, 48f, 92f, 52f); lineTo(92f, 60f)
        quadTo(92f, 64f, 88f, 64f); lineTo(58f, 64f); lineTo(50f, 76f)
    }
    private val whistleCord = Path().apply {
        moveTo(20f, 40f); rLineTo(-6f, -14f)
        moveTo(30f, 38f); rLineTo(2f, -14f)
    }

    private fun whistle(c: Canvas, x: Float, y: Float, size: Float, rot: Float, color: Int) = place(c, x, y, size, rot) {
        marker(color, 4f)
        c.drawCircle(26f, 60f, 16f, p)
        c.drawPath(whistleBody, p)
        p.strokeWidth = 3f
        c.drawPath(whistleCord, p)
    }

    private val conePath = polygon(50f, 14f, 30f, 80f, 70f, 80f)

    private fun cone(c: Canvas, x: Float, y: Float, size: Float, rot: Float, color: Int) = place(c, x, y, size, rot) {
        p.reset(); p.isAntiAlias = true
        p.style = Paint.Style.FILL; p.color = CONE_ORANGE
        c.drawPath(conePath, p)
        marker(color, 4f)
        c.drawPath(conePath, p)
        p.color = Color.WHITE; p.strokeWidth = 6f
        c.drawLine(40f, 52f, 60f, 52f, p)
        p.color = color; p.strokeWidth = 4f
        c.drawLine(20f, 80f, 80f, 80f, p)
    }

    private val trophyCup = Path().apply {
        moveTo(30f, 16f); lineTo(70f, 16f); lineTo(70f, 40f)
        arcTo(30f, 20f, 70f, 60f, 0f, 180f, false)
        close()
    }
    private val trophyRest = Path().apply {
        moveTo(30f, 24f); lineTo(16f, 24f); lineTo(16f, 34f); quadTo(16f, 42f, 30f, 40f)
        moveTo(70f, 24f); lineTo(84f, 24f); lineTo(84f, 34f); quadTo(84f, 42f, 70f, 40f)
        moveTo(50f, 60f); lineTo(50f, 72f)
        moveTo(36f, 84f); lineTo(64f, 84f); lineTo(60f, 72f); lineTo(40f, 72f); close()
    }

    private fun trophy(c: Canvas, x: Float, y: Float, size: Float, rot: Float, color: Int) = place(c, x, y, size, rot) {
        marker(color, 4f)
        c.drawPath(trophyCup, p)
        c.drawPath(trophyRest, p)
    }

    private val jerseyPath = polygon(34f, 14f, 50f, 22f, 66f, 14f, 86f, 26f, 78f, 42f, 70f, 38f, 70f, 86f, 30f, 86f, 30f, 38f, 22f, 42f, 14f, 26f)

    private fun jersey(c: Canvas, x: Float, y: Float, size: Float, rot: Float, color: Int) = place(c, x, y, size, rot) {
        marker(color, 4f)
        c.drawPath(jerseyPath, p)
        text.reset(); text.isAntiAlias = true
        text.typeface = fonts.fredoka; text.textSize = 34f; text.textAlign = Paint.Align.CENTER; text.color = color
        c.drawText("9", 50f, 74f, text)
    }

    private val pennantFlag = polygon(20f, 14f, 84f, 32f, 20f, 50f)

    private fun pennant(c: Canvas, x: Float, y: Float, size: Float, rot: Float, color: Int) = place(c, x, y, size, rot) {
        marker(color, 4f)
        c.drawLine(20f, 10f, 20f, 90f, p)
        c.drawPath(pennantFlag, p)
    }

    companion object {
        const val PAPER = 0xFFFBF6E9.toInt()
        const val RULE = 0xFFC5D8EA.toInt()
        const val MARGIN = 0xFFF2A4AE.toInt()
        const val NAVY = 0xFF2A2F45.toInt()
        const val GREEN = 0xFF2E8B57.toInt()
        const val BLUE = 0xFF2F6FB5.toInt()
        const val ORANGE = 0xFFE8782A.toInt()
        const val RED = 0xFFD9414F.toInt()
        const val BALL_INK = 0xFF222733.toInt()
        const val CONE_ORANGE = 0xFFF2A24A.toInt()

        /** A closed polygon from x,y pairs. */
        private fun polygon(vararg xy: Float): Path = Path().apply {
            moveTo(xy[0], xy[1])
            var i = 2
            while (i < xy.size) { lineTo(xy[i], xy[i + 1]); i += 2 }
            close()
        }
    }
}
