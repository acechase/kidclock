package com.acechase.kidclock.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.acechase.kidclock.art.BoyDoodles
import com.acechase.kidclock.looks.Fonts

/**
 * The boy's notebook: the same ruled page as [NotebookFace] but cream, covered in marker doodles
 * (rocket, tank, monsters, dragon, race car, UFO, dinosaurs, volcano, robot, shark, snake...) and
 * shouted words in Caveat. The middle is left mostly clear for the time. Nothing moves; it is
 * painted once. Positions come from the 1440x960 design canvas: x as a fraction of the width,
 * y and sizes as a fraction of the height.
 */
class DoodleNotebookFace(context: Context) : ClockFace(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Fonts.get(context).caveat
        textAlign = Paint.Align.LEFT
    }
    private val layer = StaticLayer(::paintPage)

    override val dayTextColor = BoyDoodles.NAVY
    override val dayShadowColor = 0x14000000
    override fun dayBackgroundForMoonBite() = PAPER

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        if (night) { c.drawColor(Color.BLACK); return }
        layer.draw(c, w, h)
    }

    private fun paintPage(c: Canvas, w: Float, h: Float) {
        c.drawColor(PAPER)
        val sx = w / 1440f
        val sy = h / 960f

        // Ruled lines and the red margin.
        p.reset(); p.isAntiAlias = true
        p.style = Paint.Style.STROKE; p.strokeWidth = 1.8f * sy; p.color = RULE
        var y = h * 0.082f
        while (y < h) { c.drawLine(0f, y, w, y, p); y += h * 0.0575f }
        p.strokeWidth = 2.4f * sy; p.color = MARGIN
        c.drawLine(w * 0.092f, 0f, w * 0.092f, h, p)

        // Each doodle is authored in a 100x100 box; place it like the canvas's
        // translate(tx ty) rotate(rot 50 50) scale(scale), with the page scaled to the screen.
        fun at(tx: Float, ty: Float, rot: Float, scale: Float, body: () -> Unit) {
            c.save()
            c.translate(tx * sx, ty * sy)
            c.scale(sy, sy)
            if (rot != 0f) c.rotate(rot, 50f, 50f)
            c.scale(scale, scale)
            body()
            c.restore()
        }

        // Top row, left to right.
        at(30f, 10f, -22f, 1.3f) { BoyDoodles.rocket(c, p) }
        at(190f, 30f, 6f, 1.1f) { BoyDoodles.blobMonster(c, p) }
        at(330f, 24f, 0f, 1.2f) { BoyDoodles.tank(c, p) }
        at(580f, 6f, -6f, 1f) { BoyDoodles.squareMonster(c, p) }
        at(700f, 16f, 4f, 1.2f) { BoyDoodles.dragon(c, p, BoyDoodles.GREEN, BoyDoodles.ORANGE, BoyDoodles.RED, true) }
        at(860f, 30f, -4f, 1.1f) { BoyDoodles.raceCar(c, p) }
        at(1040f, 0f, 0f, 1.1f) { BoyDoodles.ufo(c, p) }
        at(1170f, 26f, -5f, 1.1f) { BoyDoodles.stegosaurus(c, p) }
        at(1320f, 8f, 6f, 1.1f) { BoyDoodles.jollyRoger(c, p) }
        // Left column.
        at(14f, 176f, -6f, 1.2f) { BoyDoodles.volcano(c, p) }
        at(20f, 320f, 8f, 1.3f) { BoyDoodles.trex(c, p) }
        at(40f, 470f, -8f, 1.1f) { BoyDoodles.swordShield(c, p) }
        at(30f, 606f, 6f, 1.1f) { BoyDoodles.robot(c, p) }
        at(120f, 740f, -12f, 1.2f) { BoyDoodles.skateboard(c, p) }
        at(30f, 760f, 0f, 1f) { BoyDoodles.spider(c, p) }
        at(160f, 850f, -4f, 1f) { BoyDoodles.submarine(c, p, BoyDoodles.YELLOW, BoyDoodles.NAVY, BoyDoodles.SKY, true) }
        // Right column.
        at(1270f, 186f, 0f, 1.2f) { BoyDoodles.powBurst(c, p) }
        at(1256f, 330f, -5f, 1.2f) { BoyDoodles.monsterTruck(c, p) }
        at(1250f, 470f, 8f, 1.2f) { BoyDoodles.shark(c, p) }
        at(1310f, 606f, 15f, 0.9f) { BoyDoodles.throwingStar(c, p, BoyDoodles.NAVY, true) }
        at(1236f, 700f, -8f, 1.1f) { BoyDoodles.paperAirplane(c, p) }
        at(1270f, 820f, 0f, 1.2f) { BoyDoodles.snake(c, p) }
        at(1180f, 880f, 0f, 0.8f) { BoyDoodles.zapBolt(c, p) }
        // Small ones along the foot of the page.
        at(380f, 716f, 10f, 0.9f) { BoyDoodles.batAndBall(c, p) }
        at(520f, 730f, -6f, 0.8f) { BoyDoodles.cyclops(c, p) }
        at(660f, 744f, 0f, 0.8f) { BoyDoodles.submarine(c, p, 0, BoyDoodles.BLUE, 0, false) }
        at(790f, 728f, 0f, 0.7f) { BoyDoodles.zapBolt(c, p) }
        at(900f, 716f, 4f, 0.8f) { BoyDoodles.dragon(c, p, BoyDoodles.RED, BoyDoodles.YELLOW, BoyDoodles.ORANGE, false) }
        at(1020f, 736f, -10f, 0.7f) { BoyDoodles.throwingStar(c, p, BoyDoodles.BLUE, false) }
        // Stray sparkle crosses.
        BoyDoodles.cross(c, p, 600f * sx, 170f * sy, 6f * sy, 2.5f * sy)
        BoyDoodles.cross(c, p, 1110f * sx, 560f * sy, 7f * sy, 2.5f * sy)
        BoyDoodles.cross(c, p, 340f * sx, 600f * sy, 6f * sy, 2.5f * sy)

        // Shouted words in Caveat, each turned a little about its middle.
        fun word(s: String, x: Float, y: Float, size: Float, color: Int, rot: Float) {
            text.textSize = size * sy
            text.color = color
            val tw = text.measureText(s)
            val bx = x * sx
            val by = y * sy
            c.save()
            c.rotate(rot, bx + tw / 2f, by + size * sy * 0.6f)
            c.drawText(s, bx, by + size * sy * 0.85f, text)
            c.restore()
        }
        word("BOOM!", 470f, 120f, 60f, BoyDoodles.RED, -8f)
        word("RAWR", 190f, 330f, 54f, BoyDoodles.GREEN, 10f)
        word("zoom!", 960f, 142f, 50f, BoyDoodles.BLUE, -4f)
        word("POW!", 1296f, 222f, 46f, BoyDoodles.RED, -6f)
        word("zap", 1120f, 846f, 44f, BoyDoodles.ORANGE, 8f)
    }

    companion object {
        const val PAPER = 0xFFFBF6E9.toInt()
        const val RULE = 0xFFC5D8EA.toInt()
        const val MARGIN = 0xFFF2A4AE.toInt()
    }
}
