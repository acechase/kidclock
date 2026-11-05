package com.acechase.kidclock.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.PathParser

/**
 * A hermit crab tank seen from the front: pale sky with soft clouds behind the glass, a bed of sand
 * over a darker substrate, a plastic plant, a driftwood branch, a coconut hut, a water dish, a food
 * dish, a sponge, a few spare shells, and the crab itself at bottom-left in a banded spiral shell.
 * Nothing moves; the tank is painted once into a bitmap. At night the tank goes dark.
 *
 * The scene was drawn on a 1440x960 artboard: x positions scale with the width, y positions and
 * sizes with the height, so it fits both the 3:2 and 16:10 screens.
 */
class CrabTankFace(context: Context) : ClockFace(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val rect = RectF()
    private val layer = StaticLayer(::paintTank)

    override val dayTextColor = NAVY
    override val dayShadowColor = 0x22000000
    override fun dayBackgroundForMoonBite() = SKY

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        if (night) { c.drawColor(Color.BLACK); return }
        layer.draw(c, w, h)
    }

    // Shapes traced on the artboard, in its own units (the crab in the crab group's local units).
    private val driftwoodMain = svg("M1040 700 C1100 660 1160 610 1240 560 C1270 540 1300 520 1330 500")
    private val driftwoodFork = svg("M1160 612 C1150 580 1140 560 1120 540")
    private val driftwoodGrain = svg("M1060 690 C1120 650 1180 600 1250 555")
    private val hutFibres = svg("M1150 700 l10 -24 M1190 670 l12 -22 M1240 640 l8 -24 M1290 630 l4 -26 M1340 640 l-6 -24 M1390 668 l-10 -22")
    private val dishRim = svg("M522 744 Q640 800 758 744")
    private val shellCream = svg("M760 880 C740 880 736 850 760 842 C780 836 790 856 778 866 C770 872 762 866 766 860")
    private val shellPink = svg("M1000 892 C980 892 972 858 1004 850 C1028 844 1040 868 1026 880 C1016 888 1006 880 1010 872")
    private val shellCone = svg("M1090 860 L1120 800 L1150 860 C1150 878 1090 878 1090 860 Z")
    private val shellConeStripes = svg("M1100 846 H1140 M1106 830 H1134 M1112 816 H1128")
    private val shellMint = svg("M440 870 C420 870 414 842 440 836 C460 830 470 850 458 860 C450 866 444 860 446 854")

    private val crabShellTop = svg("M124 128 a84 84 0 0 1 168 0")
    private val crabBands = svg("M150 90 C180 70 240 70 266 96 M138 128 C160 110 256 110 278 128 M150 166 C180 186 240 186 266 160")
    private val crabSpiral = svg("M208 128 m-62 0 a62 62 0 1 1 124 0 a46 46 0 1 1 -92 0 a30 30 0 1 1 60 0 a15 15 0 1 1 -30 0")
    private val crabAperture = svg("M128 140 C110 120 110 100 130 92 C150 86 160 110 150 130 Z")
    private val crabEyestalks = svg("M82 104 C74 86 70 72 72 60 M110 104 C110 86 114 72 120 60")
    private val crabAntennae = svg("M74 110 C40 100 20 92 2 110 M118 110 C130 80 150 70 170 80")
    private val crabLeftArm = svg("M64 136 C50 134 36 140 30 150")
    private val crabBigClaw = svg("M36 150 C8 142 -10 166 2 190 C12 208 40 206 50 190 L40 176 L54 172 Z")
    private val crabClawNotch = svg("M18 172 l10 6 M14 186 l12 4")
    private val crabRightArm = svg("M120 146 C128 158 134 166 138 176")
    private val crabSmallClaw = svg("M140 174 C150 186 146 204 130 206 C118 208 110 196 116 186 L126 188 L124 178 Z")
    private val crabLegs = svg("M76 152 L56 184 L46 214 M98 156 L88 192 L84 220")
    private val crabLegTips = svg("M48 206 L46 214 M86 212 L84 220")

    private fun paintTank(c: Canvas, w: Float, h: Float) {
        val sx = w / 1440f; val sy = h / 960f; val s = sy
        c.drawColor(SKY)

        // Soft clouds behind the glass.
        fill(Color.WHITE, 204)
        oval(c, 300f * sx, 150f * sy, 120f * s, 34f * s)
        oval(c, 250f * sx, 165f * sy, 80f * s, 28f * s)
        fill(Color.WHITE, 178)
        oval(c, 1020f * sx, 110f * sy, 110f * s, 30f * s)

        // Plastic plant, back-left; the sand later buries the foot of its pot.
        placed(c, 290f, 560f, sx, sy, s) {
            leaf(c, 250f, 560f, 22f, 90f, -18f, 0xFF3AA37C.toInt())
            leaf(c, 290f, 540f, 22f, 110f, 6f, 0xFF2E8B57.toInt())
            leaf(c, 330f, 570f, 20f, 86f, 24f, 0xFF4CBF8A.toInt())
            leaf(c, 215f, 590f, 18f, 70f, -36f, 0xFF2E8B57.toInt())
            fill(0xFF5B4A3A.toInt())
            rect.set(232f, 628f, 342f, 658f)
            c.drawRoundRect(rect, 8f, 8f, p)
        }

        // Driftwood branch rising to the right.
        placed(c, 1180f, 600f, sx, sy, s) {
            stroke(0xFF8A5A3B.toInt(), 28f)
            c.drawPath(driftwoodMain, p)
            p.strokeWidth = 18f
            c.drawPath(driftwoodFork, p)
            stroke(0xFF6B4328.toInt(), 4f)
            c.drawPath(driftwoodGrain, p)
        }

        // Sand with a pale surface line, over the darker substrate.
        fill(SAND)
        path.reset()
        path.moveTo(0f, 660f * sy)
        path.cubicTo(200f * sx, 640f * sy, 320f * sx, 668f * sy, 480f * sx, 652f * sy)
        path.cubicTo(640f * sx, 636f * sy, 760f * sx, 664f * sy, 900f * sx, 648f * sy)
        path.cubicTo(1060f * sx, 632f * sy, 1240f * sx, 662f * sy, w, 646f * sy)
        path.lineTo(w, h); path.lineTo(0f, h); path.close()
        c.drawPath(path, p)
        stroke(0xFFF6EBD0.toInt(), 6f * s)
        path.reset()
        path.moveTo(0f, 668f * sy)
        path.cubicTo(200f * sx, 648f * sy, 320f * sx, 676f * sy, 480f * sx, 660f * sy)
        path.cubicTo(640f * sx, 644f * sy, 760f * sx, 672f * sy, 900f * sx, 656f * sy)
        path.cubicTo(1060f * sx, 640f * sy, 1240f * sx, 670f * sy, w, 654f * sy)
        c.drawPath(path, p)
        fill(SUBSTRATE)
        c.drawRect(0f, 880f * sy, w, h, p)
        stroke(0xFFB8945A.toInt(), 4f * s)
        path.reset()
        path.moveTo(0f, 880f * sy)
        path.cubicTo(180f * sx, 870f * sy, 300f * sx, 892f * sy, 480f * sx, 882f * sy)
        path.cubicTo(660f * sx, 872f * sy, 820f * sx, 894f * sy, 1000f * sx, 884f * sy)
        path.cubicTo(1160f * sx, 876f * sy, 1300f * sx, 890f * sy, w, 882f * sy)
        c.drawPath(path, p)
        fill(SUBSTRATE)
        c.drawCircle(160f * sx, 720f * sy, 3f * s, p)
        c.drawCircle(420f * sx, 760f * sy, 3f * s, p)
        c.drawCircle(640f * sx, 840f * sy, 3f * s, p)
        c.drawCircle(900f * sx, 720f * sy, 3f * s, p)
        c.drawCircle(1120f * sx, 820f * sy, 3f * s, p)
        c.drawCircle(1340f * sx, 760f * sy, 3f * s, p)

        // Coconut hut, bottom-right: a half dome with fibre lines and a dark doorway.
        placed(c, 1280f, 760f, sx, sy, s) {
            fill(0xFF5B3A21.toInt())
            rect.set(1130f, 610f, 1430f, 910f)
            c.drawArc(rect, 180f, 180f, true, p)
            stroke(0xFF3F2816.toInt(), 3f)
            rect.set(1160f, 640f, 1400f, 880f)
            c.drawArc(rect, 180f, 180f, false, p)
            rect.set(1200f, 680f, 1360f, 840f)
            c.drawArc(rect, 180f, 180f, false, p)
            fill(0xFF1D1309.toInt())
            rect.set(1250f, 712f, 1330f, 808f)
            c.drawArc(rect, 180f, 180f, true, p)
            stroke(0xFF7A5230.toInt(), 3f)
            c.drawPath(hutFibres, p)
            stroke(0xFF3F2816.toInt(), 5f)
            rect.set(1130f, 610f, 1430f, 910f)
            c.drawArc(rect, 180f, 180f, false, p)
        }

        // Blue water dish.
        placed(c, 640f, 744f, sx, sy, s) {
            fill(0xFF2F6FB5.toInt())
            oval(c, 640f, 744f, 118f, 40f)
            fill(0xFF7FC8EC.toInt())
            oval(c, 640f, 736f, 100f, 30f)
            fill(Color.WHITE, 153)
            oval(c, 612f, 730f, 30f, 8f)
            stroke(0xFF22508A.toInt(), 4f)
            c.drawPath(dishRim, p)
        }

        // Orange food dish with a few bits in it.
        placed(c, 870f, 804f, sx, sy, s) {
            fill(ORANGE)
            oval(c, 870f, 804f, 64f, 24f)
            fill(0xFFF4A460.toInt())
            oval(c, 870f, 798f, 54f, 17f)
            fill(0xFF7FB069.toInt()); c.drawCircle(856f, 796f, 7f, p)
            fill(0xFFD9414F.toInt()); c.drawCircle(878f, 800f, 6f, p)
            fill(0xFFF2C14E.toInt()); c.drawCircle(890f, 792f, 5f, p)
            fill(0xFF5B3A21.toInt()); c.drawCircle(862f, 806f, 4f, p)
        }

        // Yellow sponge.
        placed(c, 950f, 735f, sx, sy, s) {
            fill(0xFFFFD23F.toInt())
            rect.set(920f, 716f, 980f, 754f)
            c.drawRoundRect(rect, 8f, 8f, p)
            stroke(0xFFC9A200.toInt(), 3f)
            c.drawRoundRect(rect, 8f, 8f, p)
            fill(0xFFC9A200.toInt())
            c.drawCircle(936f, 728f, 3f, p)
            c.drawCircle(958f, 738f, 3f, p)
            c.drawCircle(948f, 724f, 2f, p)
        }

        // Spare shells: cream spiral, pink spiral, striped cone, and a small mint one near the crab.
        placed(c, 760f, 860f, sx, sy, s) { shell(c, shellCream, 0xFFF4E3C1.toInt(), 0xFFA9794A.toInt()) }
        placed(c, 1000f, 870f, sx, sy, s) { shell(c, shellPink, 0xFFE8C7D0.toInt(), 0xFFA9794A.toInt()) }
        placed(c, 1120f, 840f, sx, sy, s) {
            fill(0xFFD9A066.toInt())
            c.drawPath(shellCone, p)
            stroke(0xFF7A4A22.toInt(), 3f)
            p.strokeJoin = Paint.Join.ROUND
            c.drawPath(shellCone, p)
            p.strokeWidth = 2f
            c.drawPath(shellConeStripes, p)
        }
        placed(c, 440f, 852f, sx, sy, s) { shell(c, shellMint, 0xFFCFE6D8.toInt(), 0xFF6B8F7A.toInt()) }

        // The hermit crab, bottom-left, in its group's local units (anchored at 60,560 and 1.5x).
        placed(c, 60f, 560f, sx, sy, s) {
            c.translate(60f, 560f)
            c.scale(1.5f, 1.5f)
            crab(c)
        }
    }

    private fun crab(c: Canvas) {
        // Soft shadow on the substrate.
        fill(Color.BLACK, 34)
        oval(c, 120f, 218f, 124f, 11f)

        // Shell: banded spiral with a spire and a cream aperture.
        fill(SHELL)
        c.drawCircle(208f, 128f, 84f, p)
        fill(0xFFC48A52.toInt())
        c.drawPath(crabShellTop, p)
        stroke(0xFFA86B3A.toInt(), 7f)
        c.drawPath(crabBands, p)
        stroke(SHELL_LINE, 4f)
        c.drawCircle(208f, 128f, 84f, p)
        c.drawPath(crabSpiral, p)
        fill(SHELL)
        c.drawCircle(250f, 66f, 26f, p)
        stroke(SHELL_LINE, 4f)
        c.drawCircle(250f, 66f, 26f, p)
        fill(0xFFC48A52.toInt())
        c.drawCircle(262f, 52f, 10f, p)
        stroke(SHELL_LINE, 3f)
        c.drawCircle(262f, 52f, 10f, p)
        fill(0xFFF4E3C1.toInt())
        c.drawPath(crabAperture, p)
        stroke(SHELL_LINE, 4f)
        c.drawPath(crabAperture, p)

        // Body with lighter spots.
        fill(BODY)
        oval(c, 96f, 128f, 40f, 30f)
        stroke(BODY_LINE, 4f)
        oval(c, 96f, 128f, 40f, 30f)
        fill(0xFFB78BC7.toInt())
        c.drawCircle(84f, 118f, 5f, p)
        c.drawCircle(104f, 134f, 4f, p)
        c.drawCircle(98f, 114f, 3f, p)

        // Eyestalks, eyes, antennae.
        stroke(BODY_LINE, 7f)
        c.drawPath(crabEyestalks, p)
        fill(0xFF111111.toInt())
        c.drawCircle(72f, 56f, 11f, p)
        c.drawCircle(120f, 56f, 11f, p)
        fill(Color.WHITE)
        c.drawCircle(69f, 52f, 4f, p)
        c.drawCircle(117f, 52f, 4f, p)
        stroke(BODY_LINE, 3f)
        c.drawPath(crabAntennae, p)

        // Big left claw and small right claw.
        stroke(BODY, 16f)
        c.drawPath(crabLeftArm, p)
        fill(CLAW)
        c.drawPath(crabBigClaw, p)
        stroke(BODY_LINE, 4f)
        p.strokeJoin = Paint.Join.ROUND
        c.drawPath(crabBigClaw, p)
        p.strokeWidth = 2.5f
        c.drawPath(crabClawNotch, p)
        stroke(BODY, 12f)
        c.drawPath(crabRightArm, p)
        fill(CLAW)
        c.drawPath(crabSmallClaw, p)
        stroke(BODY_LINE, 4f)
        p.strokeJoin = Paint.Join.ROUND
        c.drawPath(crabSmallClaw, p)

        // Two jointed walking legs with orange tips and joint dots.
        stroke(BODY, 12f)
        p.strokeJoin = Paint.Join.ROUND
        c.drawPath(crabLegs, p)
        stroke(BODY_LINE, 4f)
        p.strokeJoin = Paint.Join.ROUND
        p.alpha = 128
        c.drawPath(crabLegs, p)
        stroke(ORANGE, 12f)
        c.drawPath(crabLegTips, p)
        fill(0xFF6A3F7A.toInt())
        c.drawCircle(56f, 184f, 6f, p)
        c.drawCircle(88f, 192f, 6f, p)
    }

    /** A rotated leaf of the plastic plant. */
    private fun leaf(c: Canvas, cx: Float, cy: Float, rx: Float, ry: Float, rot: Float, color: Int) {
        fill(color)
        c.save()
        c.rotate(rot, cx, cy)
        oval(c, cx, cy, rx, ry)
        c.restore()
    }

    /** A spiral shell: fill then outline, both from the same traced curve. */
    private fun shell(c: Canvas, shape: Path, fillColor: Int, line: Int) {
        fill(fillColor)
        c.drawPath(shape, p)
        stroke(line, 3f)
        c.drawPath(shape, p)
    }

    /**
     * Runs [body] in artboard units with the point ([ax],[ay]) landing at (ax*sx, ay*sy) and
     * everything around it scaled uniformly by [s], so objects keep their shape on both screens.
     */
    private inline fun placed(c: Canvas, ax: Float, ay: Float, sx: Float, sy: Float, s: Float, body: () -> Unit) {
        c.save()
        c.translate(ax * sx - ax * s, ay * sy - ay * s)
        c.scale(s, s)
        body()
        c.restore()
    }

    private fun oval(c: Canvas, cx: Float, cy: Float, rx: Float, ry: Float) {
        rect.set(cx - rx, cy - ry, cx + rx, cy + ry)
        c.drawOval(rect, p)
    }

    private fun fill(color: Int, alpha: Int = 255) {
        p.reset(); p.isAntiAlias = true
        p.style = Paint.Style.FILL
        p.color = color
        if (alpha != 255) p.alpha = alpha
    }

    private fun stroke(color: Int, width: Float) {
        p.reset(); p.isAntiAlias = true
        p.style = Paint.Style.STROKE
        p.strokeWidth = width
        p.strokeCap = Paint.Cap.ROUND
        p.strokeJoin = Paint.Join.ROUND
        p.color = color
    }

    @Suppress("RestrictedApi")
    private fun svg(d: String): Path = PathParser.createPathFromPathData(d)

    companion object {
        const val SKY = 0xFFCFE6EE.toInt()
        const val NAVY = 0xFF1F3B4D.toInt()
        const val SAND = 0xFFE9D2A0.toInt()
        const val SUBSTRATE = 0xFFC9A86C.toInt()
        const val ORANGE = 0xFFE07A2C.toInt()
        const val SHELL = 0xFFD9A066.toInt()
        const val SHELL_LINE = 0xFF5A3214.toInt()
        const val BODY = 0xFF8C5A9E.toInt()
        const val BODY_LINE = 0xFF4A2C58.toInt()
        const val CLAW = 0xFFA46BB6.toInt()
    }
}
