package com.acechase.kidclock.art

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.PathParser

/**
 * Hand-drawn shapes shared by the girly looks and backgrounds: hearts in a dozen marker styles,
 * tiaras, sparkles, and the seasonal bits (bats, leaves, eggs, holly). Every shape is authored in
 * a small box (hearts 100x100, tiaras 120x70) and drawn with [draw], which centres, rotates and
 * scales it, so a caller only says where, how big and which colour.
 *
 * The outlines were traced on the design canvas as SVG path data and are parsed once here.
 */
object Doodles {
    /** A plain heart, point at the bottom, in a 100x100 box. */
    val heart: Path = path("M50 88C30 74 8 58 8 36 8 22 18 12 30 12c9 0 15 5 20 13 5-8 11-13 20-13 12 0 22 10 22 24 0 22-22 38-42 52Z")
    private val heartOpen = path("M52 88C32 74 10 58 10 36 10 22 20 12 32 12c9 0 15 5 20 13 5-8 11-13 20-13 12 0 22 10 22 24 0 22-22 38-44 54")
    private val heartOpen2 = path("M48 86C30 72 12 56 12 38 12 24 22 14 33 14c8 0 14 5 19 12 5-7 11-12 19-12 11 0 20 10 20 24 0 20-22 36-45 50")
    private val heartLoop = path("M50 88C30 74 8 58 8 36 8 22 18 12 30 12c9 0 15 5 20 13c-8-6-9-18-1-16 8 2 3 12 1 16 5-8 11-13 20-13 12 0 22 10 22 24 0 22-22 38-42 52")
    private val crosshatch = path("M-30 30 70 130M-15 15 85 115M0 0 100 100M15-15 115 85M30-30 130 70M130 30 30 130M115 15 15 115M100 0 0 100M85-15-15 85M70-30-30 70")
    private val stripes = path("M-20 34 66 120M-6 20 80 106M8 6 94 92M22-8 108 78M36-22 122 64")
    private val scribble = path("M12 26 90 38 10 50 90 60 14 70 80 82 30 90")
    private val gingham = path("M16 0v100M34 0v100M52 0v100M70 0v100M88 0v100M0 18h100M0 36h100M0 54h100M0 72h100M0 90h100")
    private val arrow = path("M-18 32 8 42M92 66l24 10M106 64l10 12-13 6M-16 31l-3-11M-9 34l-3-11")
    private val balloon = path("M50 72C30 58 10 46 10 30 10 18 18 10 29 10c9 0 16 5 21 13 5-8 12-13 21-13 11 0 19 8 19 20 0 16-20 28-42 44")
    private val balloonTail = path("M49 73c-9 11 9 17 1 27-8 10 6 16-1 26")
    /** Three hearts written in one cursive stroke, in a 270x100 box. */
    val chain: Path = path("M-6 82C14 92 40 86 50 70 10 45 10 12 50 28 90 12 90 45 50 70c20 20 60 20 80 0-40-25-40-58 0-42 40-16 40 17 0 42 20 20 60 20 80 0-40-25-40-58 0-42 40-16 40 17 0 42 12 16 40 14 66-8")
    /** A thin four-point sparkle in a 100x100 box. */
    val sparkle: Path = path("M50 0C51 36 64 49 100 50C64 51 51 64 50 100C49 64 36 51 0 50C36 49 49 36 50 0Z")
    /** A fatter four-point sparkle. */
    val sparkleFat: Path = path("M50 0C52 30 70 48 100 50C70 52 52 70 50 100C48 70 30 52 0 50C30 48 48 30 50 0Z")

    enum class Tiara { POINTED, ROUNDED, FIVE }
    private val tiaraPointed = path("M12 60 6 22l26 20L60 8l28 34 26-20-6 38Z")
    private val tiaraRounded = path("M10 60 4 24c14 10 24 14 30 6 6-8 12-18 26-18s20 10 26 18c6 8 16 4 30-6l-6 36Z")
    private val tiaraFive = path("M10 60 6 30l18 12 12-22 14 20 14-20 12 22 18-12-4 30Z")
    private val tiaraBase = path("M12 60h96")

    /** Seasonal shapes. */
    /** A bat in flight, 100x60: pointed wingtips, two ears, a round body, scalloped trailing edges. */
    val bat: Path = path("M2 10C14 6 26 12 36 22 40 20 42 16 43 12L45 2 48 11Q50 10 52 11L55 2 57 12C58 16 60 20 64 22 74 12 86 6 98 10Q92 32 82 30 74 44 66 36 60 46 56 42C54 52 46 52 44 42Q40 46 34 36 26 44 18 30 8 32 2 10Z")
    val mapleLeaf: Path = path("M50 96V64L30 72l4-14-20-6 12-8-8-16 18 6 4-16 10 14 10-14 4 16 18-6-8 16 12 8-20 6 4 14-20-8v32Z")
    val egg: Path = path("M50 8C30 8 14 36 14 58c0 22 16 36 36 36s36-14 36-36C86 36 70 8 50 8Z")
    /** A holly leaf centred on the origin, tip pointing right. */
    val hollyLeaf: Path = path("M0 0c10-14 26-16 40-8-6 10-4 22 4 30-14 6-30 2-40-10-10 12-26 16-40 10 8-8 10-20 4-30 14-8 30-6 32 8Z")
    /** A fairy-light bulb hanging from the origin. */
    val bulb: Path = path("M0 0v10M-4 10h8v6h-8ZM0 16c-9 0-13 8-13 16 0 10 6 18 13 18s13-8 13-18c0-8-4-16-13-16Z")
    /** The witch hat, in a 120x100 box: brim, cone, band and buckle drawn by [witchHat]. */
    private val hatCone = path("M26 86C42 62 56 30 80 6 72 36 82 60 96 86Z")
    private val hatBand = path("M31 76c20-7 48-7 61 2l2 8H29Z")
    private val cloud = path("M18 42a12 12 0 0 1-1.6-23.9A17 17 0 0 1 49 22.4 10 10 0 0 1 50 42Z")

    enum class Style { SOLID, OUTLINE, CROSSHATCH, POLKA, LAYERED, SKETCHED, DASHED, DOTTED, STRIPED, GINGHAM, SCRIBBLE, STITCHED, SHINING, HALO, LOOP, ARROW, BALLOON }

    private val rect = RectF()
    private val dash = android.graphics.DashPathEffect(floatArrayOf(11f, 8f), 0f)
    private val stitch = android.graphics.DashPathEffect(floatArrayOf(7f, 6f), 0f)
    private val dots = android.graphics.DashPathEffect(floatArrayOf(0.1f, 9f), 0f)

    /**
     * Draws [shape], authored in a [box]-wide square, centred on ([cx],[cy]) at [size] px and
     * turned by [rot] degrees. [body] draws in the shape's own units with the paint prepared.
     */
    inline fun draw(c: Canvas, cx: Float, cy: Float, size: Float, rot: Float, box: Float = 100f, body: () -> Unit) {
        c.save()
        c.translate(cx, cy)
        c.rotate(rot)
        val s = size / box
        c.scale(s, s)
        c.translate(-box / 2f, -box / 2f)
        body()
        c.restore()
    }

    /** One heart in a marker style. [color] is the marker; [fill] tints the inside of STITCHED/GINGHAM. */
    fun heart(c: Canvas, p: Paint, cx: Float, cy: Float, size: Float, rot: Float, color: Int, style: Style, fill: Int = 0) {
        p.reset(); p.isAntiAlias = true
        p.strokeJoin = Paint.Join.ROUND; p.strokeCap = Paint.Cap.ROUND
        p.color = color
        val box = if (style == Style.BALLOON) 130f else 100f
        draw(c, cx, cy, size, rot, box) {
            when (style) {
                Style.SOLID -> { p.style = Paint.Style.FILL; c.drawPath(heart, p) }
                Style.OUTLINE -> { stroke(p, 5f); c.drawPath(heart, p) }
                Style.CROSSHATCH -> { clipped(c, p, 2.8f) { c.drawPath(crosshatch, p) }; stroke(p, 5f); c.drawPath(heart, p) }
                Style.STRIPED -> { clipped(c, p, 4f) { c.drawPath(stripes, p) }; stroke(p, 4.5f); c.drawPath(heart, p) }
                Style.SCRIBBLE -> { clipped(c, p, 3.2f) { c.drawPath(scribble, p) }; stroke(p, 4f); c.drawPath(heart, p) }
                Style.GINGHAM -> {
                    p.style = Paint.Style.FILL; p.color = if (fill != 0) fill else 0xFFFCE4EC.toInt(); c.drawPath(heart, p)
                    p.color = color; p.alpha = 140
                    clipped(c, p, 8f) { c.drawPath(gingham, p) }
                    p.color = color; stroke(p, 4f); c.drawPath(heart, p)
                }
                Style.POLKA -> {
                    stroke(p, 4.5f); c.drawPath(heart, p)
                    p.style = Paint.Style.FILL
                    c.drawCircle(30f, 32f, 4.5f, p); c.drawCircle(50f, 42f, 4.5f, p); c.drawCircle(70f, 32f, 4.5f, p)
                    c.drawCircle(36f, 56f, 4.5f, p); c.drawCircle(62f, 58f, 4.5f, p); c.drawCircle(50f, 72f, 4.5f, p)
                }
                Style.LAYERED -> {
                    stroke(p, 4f); c.drawPath(heart, p)
                    inner(c, 0.64f) { c.drawPath(heart, p) }
                    inner(c, 0.30f) { c.drawPath(heart, p) }
                }
                Style.SKETCHED -> {
                    stroke(p, 6f); c.drawPath(heartOpen, p)
                    p.alpha = 150
                    c.save(); c.rotate(7f, 50f, 50f); c.drawPath(heartOpen2, p); c.restore()
                }
                Style.DASHED -> { stroke(p, 5f); p.pathEffect = dash; c.drawPath(heart, p) }
                Style.DOTTED -> { stroke(p, 6f); p.pathEffect = dots; c.drawPath(heart, p) }
                Style.STITCHED -> {
                    p.style = Paint.Style.FILL; p.color = if (fill != 0) fill else 0xFFF8BBD0.toInt(); c.drawPath(heart, p)
                    p.color = color; stroke(p, 3.5f); p.pathEffect = stitch
                    inner(c, 0.8f) { c.drawPath(heart, p) }
                }
                Style.SHINING -> {
                    stroke(p, 5f); c.drawPath(heart, p)
                    p.strokeWidth = 4.5f
                    for (a in floatArrayOf(-70f, -35f, 0f, 35f, 70f)) {
                        c.save(); c.rotate(a, 50f, 50f); c.drawLine(50f, -16f, 50f, -3f, p); c.restore()
                    }
                }
                Style.HALO -> {
                    stroke(p, 5f); inner(c, 0.78f) { c.drawPath(heart, p) }
                    p.style = Paint.Style.FILL
                    for (i in 0 until 12) {
                        val a = Math.toRadians(i * 30.0)
                        c.drawCircle(50f + 58f * Math.cos(a).toFloat(), 50f + 58f * Math.sin(a).toFloat(), 3f, p)
                    }
                }
                Style.LOOP -> { stroke(p, 5f); c.drawPath(heartLoop, p) }
                Style.ARROW -> { stroke(p, 5f); c.drawPath(heart, p); c.drawPath(arrow, p) }
                Style.BALLOON -> { stroke(p, 6f); c.drawPath(balloon, p); c.drawPath(balloonTail, p) }
            }
        }
        p.pathEffect = null
    }

    /** The cursive chain of three hearts, its 270x100 box centred on ([cx],[cy]) and [width] wide. */
    fun chain(c: Canvas, p: Paint, cx: Float, cy: Float, width: Float, rot: Float, color: Int) {
        p.reset(); p.isAntiAlias = true; p.color = color
        p.strokeJoin = Paint.Join.ROUND; p.strokeCap = Paint.Cap.ROUND
        stroke(p, 5f)
        c.save()
        c.translate(cx, cy); c.rotate(rot)
        val s = width / 270f
        c.scale(s, s); c.translate(-135f, -50f)
        c.drawPath(chain, p)
        c.restore()
    }

    /** A marker tiara, its 120x70 box centred on ([cx],[cy]) and [width] wide, gems in the same colour. */
    fun tiara(c: Canvas, p: Paint, cx: Float, cy: Float, width: Float, rot: Float, color: Int, shape: Tiara, heartGem: Boolean = true) {
        p.reset(); p.isAntiAlias = true; p.color = color
        p.strokeJoin = Paint.Join.ROUND; p.strokeCap = Paint.Cap.ROUND
        c.save()
        c.translate(cx, cy); c.rotate(rot)
        val s = width / 120f
        c.scale(s, s); c.translate(-60f, -35f)
        stroke(p, 4.5f)
        c.drawPath(when (shape) { Tiara.POINTED -> tiaraPointed; Tiara.ROUNDED -> tiaraRounded; Tiara.FIVE -> tiaraFive }, p)
        c.drawPath(tiaraBase, p)
        p.style = Paint.Style.FILL
        val topY = when (shape) { Tiara.POINTED -> 8f; Tiara.ROUNDED -> 18f; Tiara.FIVE -> 12f }
        if (heartGem) draw(c, 60f, topY, 16f, 0f) { c.drawPath(heart, p) } else c.drawCircle(60f, topY, 3.5f, p)
        val gemY = if (shape == Tiara.FIVE) 22f else if (shape == Tiara.ROUNDED) 36f else 42f
        c.drawCircle(if (shape == Tiara.FIVE) 36f else 32f, gemY, 2.8f, p)
        c.drawCircle(if (shape == Tiara.FIVE) 84f else 88f, gemY, 2.8f, p)
        c.restore()
    }

    /** The Tiara look's fine gold tiara: thin outline, one rose gem, four pearls. */
    fun fineTiara(c: Canvas, p: Paint, cx: Float, cy: Float, width: Float, rot: Float, gold: Int, gem: Int, pearl: Int) {
        p.reset(); p.isAntiAlias = true
        p.strokeJoin = Paint.Join.ROUND; p.strokeCap = Paint.Cap.ROUND
        c.save()
        c.translate(cx, cy); c.rotate(rot)
        val s = width / 120f
        c.scale(s, s); c.translate(-60f, -35f)
        p.color = gold; stroke(p, 2.2f)
        c.drawPath(tiaraPointed, p); c.drawPath(tiaraBase, p)
        p.style = Paint.Style.FILL
        p.color = gem; c.drawCircle(60f, 8f, 4.5f, p)
        p.color = pearl
        c.drawCircle(32f, 42f, 2.5f, p); c.drawCircle(88f, 42f, 2.5f, p); c.drawCircle(6f, 22f, 2.2f, p); c.drawCircle(114f, 22f, 2.2f, p)
        c.restore()
    }

    /** The witch hat (120x100 box) for the Halloween look. */
    fun witchHat(c: Canvas, p: Paint, cx: Float, cy: Float, width: Float, rot: Float) {
        p.reset(); p.isAntiAlias = true; p.style = Paint.Style.FILL
        c.save()
        c.translate(cx, cy); c.rotate(rot)
        val s = width / 120f
        c.scale(s, s); c.translate(-60f, -50f)
        p.color = 0xFF17111F.toInt(); rect.set(4f, 75f, 116f, 97f); c.drawOval(rect, p)
        p.color = 0xFF1E1627.toInt(); c.drawPath(hatCone, p)
        p.color = 0xFF7C3AED.toInt(); c.drawPath(hatBand, p)
        p.color = 0xFFF6C76B.toInt(); rect.set(56f, 70f, 69f, 84f); c.drawRoundRect(rect, 2f, 2f, p)
        p.color = 0xFF1E1627.toInt(); rect.set(59.5f, 73.5f, 65.5f, 80.5f); c.drawRoundRect(rect, 1f, 1f, p)
        c.restore()
    }

    /** A heart filled with [fill] and crossed by diagonal [stripe] bands: candy cane, or stripes of any colour. */
    fun heartStriped(c: Canvas, p: Paint, cx: Float, cy: Float, size: Float, rot: Float, fill: Int, stripe: Int, width: Float = 9f) {
        p.reset(); p.isAntiAlias = true; p.style = Paint.Style.FILL; p.color = fill
        draw(c, cx, cy, size, rot) {
            c.drawPath(heart, p)
            c.save(); c.clipPath(heart)
            p.color = stripe; p.style = Paint.Style.STROKE; p.strokeWidth = width; p.strokeCap = Paint.Cap.BUTT
            var d = -40f
            while (d < 140f) { c.drawLine(d, 120f, d + 86f, 34f, p); d += 20f }
            c.restore()
        }
    }

    /** A heart in horizontal bands from the top down, e.g. candy corn (white, orange, yellow). */
    fun heartBands(c: Canvas, p: Paint, cx: Float, cy: Float, size: Float, rot: Float, colors: IntArray, stops: FloatArray) {
        p.reset(); p.isAntiAlias = true; p.style = Paint.Style.FILL
        draw(c, cx, cy, size, rot) {
            c.save(); c.clipPath(heart)
            var top = 0f
            for (i in colors.indices) {
                val bottom = if (i == colors.lastIndex) 100f else stops[i] * 100f
                p.color = colors[i]; c.drawRect(0f, top, 100f, bottom, p); top = bottom
            }
            c.restore()
        }
    }

    /** A four-leaf clover made of four hearts pointing inward, with a stem; [size] is its width. */
    fun clover(c: Canvas, p: Paint, cx: Float, cy: Float, size: Float, rot: Float, color: Int) {
        p.reset(); p.isAntiAlias = true; p.color = color
        draw(c, cx, cy, size, rot) {
            p.style = Paint.Style.FILL
            for (k in 0 until 4) {
                c.save(); c.rotate(k * 90f, 50f, 50f)
                c.translate(50f, 50f); c.scale(0.36f, 0.36f); c.translate(-50f, -88f)
                c.drawPath(heart, p)
                c.restore()
            }
            p.style = Paint.Style.STROKE; p.strokeWidth = 3.5f; p.strokeCap = Paint.Cap.ROUND
            rect.set(0f, 0f, 0f, 0f)
            val stem = Path(); stem.moveTo(50f, 52f); stem.quadTo(62f, 74f, 58f, 96f)
            c.drawPath(stem, p)
        }
    }

    /** An orange heart with a green stem and two ridges: a pumpkin. */
    fun pumpkinHeart(c: Canvas, p: Paint, cx: Float, cy: Float, size: Float, rot: Float) {
        p.reset(); p.isAntiAlias = true; p.style = Paint.Style.FILL
        draw(c, cx, cy, size, rot) {
            p.color = 0xFF5B8C3A.toInt()
            val stem = Path(); stem.moveTo(48f, 26f); stem.lineTo(45f, 14f); stem.lineTo(53f, 14f); stem.lineTo(52f, 26f); stem.close()
            c.drawPath(stem, p)
            p.shader = android.graphics.LinearGradient(0f, 0f, 30f, 100f, 0xFFFFA552.toInt(), 0xFFE85D04.toInt(), android.graphics.Shader.TileMode.CLAMP)
            c.drawPath(heart, p)
            p.shader = null
            p.color = 0xFFC2410C.toInt(); p.alpha = 150; p.style = Paint.Style.STROKE; p.strokeWidth = 2.5f; p.strokeCap = Paint.Cap.ROUND
            val ridge = Path(); ridge.moveTo(38f, 22f); ridge.quadTo(28f, 50f, 44f, 80f); ridge.moveTo(62f, 22f); ridge.quadTo(72f, 50f, 56f, 80f)
            c.drawPath(ridge, p)
        }
    }

    /** A heart with scalloped bat wings, dark fill and lilac outline. */
    fun batHeart(c: Canvas, p: Paint, cx: Float, cy: Float, size: Float, rot: Float, fill: Int, line: Int) {
        p.reset(); p.isAntiAlias = true; p.strokeJoin = Paint.Join.ROUND; p.strokeCap = Paint.Cap.ROUND
        draw(c, cx, cy, size, rot) {
            for (pass in 0..1) {
                p.style = if (pass == 0) Paint.Style.FILL else Paint.Style.STROKE
                p.color = if (pass == 0) fill else line; p.strokeWidth = 3.5f
                c.drawPath(wingLeft, p); c.drawPath(wingRight, p); c.drawPath(heart, p)
            }
        }
    }
    private val wingLeft = path("M12 36C0 24-16 26-26 36l8 4-6 8 10 2-2 8c10-4 18-2 26 4Z")
    private val wingRight = path("M88 36c12-12 28-10 38 0l-8 4 6 8-10 2 2 8c-10-4-18-2-26 4Z")

    /** A firework: thin rays from the centre with a dot at each tip; [size] is the diameter. */
    fun burst(c: Canvas, p: Paint, cx: Float, cy: Float, size: Float, color: Int, alpha: Int = 255, strokeW: Float = 1.4f) {
        p.reset(); p.isAntiAlias = true; p.color = color; p.alpha = alpha; p.strokeCap = Paint.Cap.ROUND
        c.save(); c.translate(cx, cy)
        val s = size / 200f
        c.scale(s, s)
        p.style = Paint.Style.STROKE; p.strokeWidth = strokeW / s
        for (k in 0 until 16) {
            val a = Math.toRadians(k * 22.5)
            val inner = if (k % 2 == 0) 26f else 24f; val outer = if (k % 2 == 0) 92f else 68f
            val ca = Math.cos(a).toFloat(); val sa = Math.sin(a).toFloat()
            c.drawLine(ca * inner, sa * inner, ca * outer, sa * outer, p)
        }
        p.style = Paint.Style.FILL
        for (k in 0 until 8) {
            val a = Math.toRadians(k * 45.0)
            c.drawCircle(Math.cos(a).toFloat() * 92f, Math.sin(a).toFloat() * 92f, 2.6f, p)
        }
        c.restore()
    }

    fun sparkle(c: Canvas, p: Paint, cx: Float, cy: Float, size: Float, color: Int, alpha: Int = 255, fat: Boolean = false) {
        p.reset(); p.isAntiAlias = true; p.style = Paint.Style.FILL; p.color = color; p.alpha = alpha
        draw(c, cx, cy, size, 0f) { c.drawPath(if (fat) sparkleFat else sparkle, p) }
    }

    fun cloud(c: Canvas, p: Paint, cx: Float, cy: Float, width: Float, color: Int) {
        p.reset(); p.isAntiAlias = true; p.style = Paint.Style.FILL; p.color = color
        c.save(); c.translate(cx, cy)
        val s = width / 64f
        c.scale(s, s); c.translate(-32f, -22f)
        c.drawPath(cloud, p)
        c.restore()
    }

    /** Fills [shape] (authored in [box] units) at the given place. */
    fun fill(c: Canvas, p: Paint, shape: Path, cx: Float, cy: Float, size: Float, rot: Float, color: Int, box: Float = 100f, alpha: Int = 255) {
        p.reset(); p.isAntiAlias = true; p.style = Paint.Style.FILL; p.color = color; p.alpha = alpha
        draw(c, cx, cy, size, rot, box) { c.drawPath(shape, p) }
    }

    private fun stroke(p: Paint, width: Float) { p.style = Paint.Style.STROKE; p.strokeWidth = width; p.pathEffect = null }

    private inline fun clipped(c: Canvas, p: Paint, width: Float, body: () -> Unit) {
        c.save(); c.clipPath(heart); stroke(p, width); body(); c.restore()
    }

    private inline fun inner(c: Canvas, scale: Float, body: () -> Unit) {
        c.save(); c.translate(50f, 50f); c.scale(scale, scale); c.translate(-50f, -50f); body(); c.restore()
    }

    @Suppress("RestrictedApi")
    private fun path(d: String): Path = PathParser.createPathFromPathData(d)

    /** A colour with its alpha replaced. */
    fun withAlpha(color: Int, alpha: Int): Int = (color and 0x00FFFFFF) or (alpha shl 24)

    const val TRANSPARENT = Color.TRANSPARENT
}
