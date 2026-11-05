package com.acechase.kidclock.art

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import androidx.core.graphics.PathParser

/**
 * The five stuffed animals and the bedsheet pattern of the Stuffies background, traced from the
 * design canvas. Each animal is authored in its own box (the fawns 340x320, the elephant and the
 * red panda 320x340, the puppy 320x320) with its feet on [Box.base]; the caller translates and
 * scales the canvas to place it. Fur is a radial gradient per plush part (lighter up and to the
 * left, as the canvas does), built once in the part's own units, with a soft outline in a darker
 * tone of the same fur. Every animal takes one gesture angle in degrees for the part that moves.
 */
object StuffiesArt {
    const val FAWN = 0
    const val ELEPHANT = 1
    const val PUPPY = 2
    const val PANDA = 3
    const val SMALL_FAWN = 4

    /** Extent of an animal's shadow (left..right) in its box and the y its feet rest on. */
    class Box(val left: Float, val right: Float, val base: Float)
    val BOXES = arrayOf(
        Box(30f, 330f, 302f), // big fawn
        Box(10f, 310f, 322f), // elephant
        Box(30f, 290f, 306f), // puppy
        Box(20f, 300f, 324f), // red panda
        Box(30f, 330f, 302f), // small fawn (draw at 0.62 of the big fawn's scale)
    )
    /** Scale an animal is drawn at relative to the others. */
    val SCALES = floatArrayOf(1f, 1f, 1f, 1f, 0.62f)

    // Sheet pattern colours
    const val LILAC = 0xFFC9A6E8.toInt()
    const val PINK = 0xFFF4A6C0.toInt()
    const val MINT = 0xFF8FD3C7.toInt()
    const val YELLOW = 0xFFFFD98A.toInt()
    const val PEACH = 0xFFF8B27A.toInt()
    const val BLUE = 0xFFA8C8F0.toInt()
    const val BLANKET = 0xFFF6D8E0.toInt()
    const val FOLD = 0xFFEFC3D0.toInt()
    private const val SHADOW = 0xFFE3B8C6.toInt()

    // Fur tones
    private const val FAWN_LIGHT = 0xFFDD9452.toInt(); private const val FAWN_DARK = 0xFFB4642A.toInt(); private const val FAWN_LINE = 0xFF9A5A26.toInt()
    private const val GREY_LIGHT = 0xFFBDB8B3.toInt(); private const val GREY_DARK = 0xFF8E8883.toInt(); private const val GREY_LINE = 0xFF7D7873.toInt()
    private const val TAN_LIGHT = 0xFFE9C99A.toInt(); private const val TAN_DARK = 0xFFC49A62.toInt(); private const val TAN_LINE = 0xFFA07C48.toInt()
    private const val ORANGE_LIGHT = 0xFFE69050.toInt(); private const val ORANGE_DARK = 0xFFBF6528.toInt(); private const val ORANGE_LINE = 0xFF8A4A1C.toInt()
    private const val BLACK_LIGHT = 0xFF3A3A3A.toInt(); private const val BLACK_DARK = 0xFF101010.toInt(); private const val BLACK_LINE = 0xFF000000.toInt()
    private const val INK = 0xFF1A1A1A.toInt()
    private const val WHITE = 0xFFFFFFFF.toInt()
    private const val IVORY = 0xFFFFF4E6.toInt()
    private const val IVORY2 = 0xFFFFF8EE.toInt()
    private const val CREAM_TAN = 0xFFEFD9A8.toInt()
    private const val CREAM_PANDA = 0xFFF6EAD8.toInt()

    private val r = RectF()
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }

    /** A plush part's fur: radial gradient, highlight up and left of the part's centre, built once. */
    private fun fur(cx: Float, cy: Float, rx: Float, ry: Float, light: Int, dark: Int): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(cx - 0.24f * rx, cy - 0.36f * ry, 1.6f * maxOf(rx, ry), light, dark, Shader.TileMode.CLAMP)
        }

    // Big fawn parts
    private val fawnBody = fur(176f, 221f, 136f, 71f, FAWN_LIGHT, FAWN_DARK)
    private val fawnNeck = fur(112f, 190f, 42f, 54f, FAWN_LIGHT, FAWN_DARK)
    private val fawnEarL = fur(44f, 84f, 26f, 50f, FAWN_LIGHT, FAWN_DARK)
    private val fawnEarR = fur(158f, 80f, 26f, 50f, FAWN_LIGHT, FAWN_DARK)
    private val fawnHead = fur(100f, 122f, 64f, 64f, FAWN_LIGHT, FAWN_DARK)
    // Small fawn parts
    private val tanBody = fur(176f, 221f, 136f, 71f, TAN_LIGHT, TAN_DARK)
    private val tanNeck = fur(112f, 190f, 42f, 54f, TAN_LIGHT, TAN_DARK)
    private val tanEarL = fur(48f, 92f, 24f, 44f, TAN_LIGHT, TAN_DARK)
    private val tanEarR = fur(154f, 88f, 24f, 44f, TAN_LIGHT, TAN_DARK)
    private val tanHead = fur(100f, 122f, 64f, 64f, TAN_LIGHT, TAN_DARK)
    // Elephant parts
    private val greyBody = fur(160f, 236f, 122f, 82f, GREY_LIGHT, GREY_DARK)
    private val greyEarL = fur(66f, 158f, 50f, 70f, GREY_LIGHT, GREY_DARK)
    private val greyEarR = fur(254f, 158f, 50f, 70f, GREY_LIGHT, GREY_DARK)
    private val greyFootL = fur(96f, 296f, 34f, 24f, GREY_LIGHT, GREY_DARK)
    private val greyFootR = fur(224f, 296f, 34f, 24f, GREY_LIGHT, GREY_DARK)
    private val greyHead = fur(160f, 142f, 86f, 86f, GREY_LIGHT, GREY_DARK)
    private val greyTrunk = fur(150f, 252f, 34f, 62f, GREY_LIGHT, GREY_DARK)
    // Puppy parts
    private val pupBody = fur(160f, 250f, 102f, 62f, TAN_LIGHT, TAN_DARK)
    private val pupLegL = fur(133f, 260f, 21f, 42f, TAN_LIGHT, TAN_DARK)
    private val pupLegR = fur(187f, 260f, 21f, 42f, TAN_LIGHT, TAN_DARK)
    private val pupHead = fur(160f, 132f, 82f, 74f, TAN_LIGHT, TAN_DARK)
    // Red panda parts
    private val pandaBody = fur(160f, 242f, 94f, 88f, BLACK_LIGHT, BLACK_DARK)
    private val pandaArmL = fur(68f, 232f, 28f, 50f, BLACK_LIGHT, BLACK_DARK)
    private val pandaArmR = fur(252f, 232f, 28f, 50f, BLACK_LIGHT, BLACK_DARK)
    private val pandaFootL = fur(108f, 316f, 42f, 22f, BLACK_LIGHT, BLACK_DARK)
    private val pandaFootR = fur(212f, 316f, 42f, 22f, BLACK_LIGHT, BLACK_DARK)
    private val pandaHead = fur(160f, 126f, 90f, 90f, ORANGE_LIGHT, ORANGE_DARK)

    // Traced outlines
    private val fawnBodyPath = path("M60 250 C40 190 90 150 170 150 C260 150 312 190 302 250 C298 282 262 292 170 292 C90 292 68 282 60 250 Z")
    private val hoofL = path("M72 270 C70 288 86 300 108 298 C130 296 138 282 132 270 Z")
    private val hoofR = path("M150 274 C148 292 164 304 186 302 C208 300 216 286 210 274 Z")
    private val fawnTipL = path("M30 42 C36 30 52 30 58 42 C54 50 34 50 30 42 Z")
    private val fawnTipR = path("M144 38 C150 26 166 26 172 38 C168 46 148 46 144 38 Z")
    private val fawnMouth = path("M92 154 V162 M80 164 Q92 174 104 164")
    private val fawnBrows = path("M62 100 C66 94 74 92 80 94 M118 94 C124 92 132 94 136 100")
    private val tuskL = path("M134 206 C118 216 108 234 114 250")
    private val tuskR = path("M186 206 C202 216 212 234 206 250")
    private val trunk = path("M136 190 C138 236 132 262 116 290 C110 302 124 314 136 302 C154 276 176 246 184 190 Z")
    private val wrinkles = path("M138 214 Q160 222 180 214 M134 238 Q156 246 174 236 M126 262 Q148 270 164 258")
    private val elephantBrows = path("M112 116 C118 110 130 110 138 114 M182 114 C190 110 202 110 208 116")
    private val toes = path("M118 304 V294 M133 306 V296 M148 304 V294 M172 304 V294 M187 306 V296 M202 304 V294")
    private val pupEarL = path("M92 96 C58 104 52 164 66 196 C76 216 110 208 112 176 C113 144 110 112 92 96 Z")
    private val pupEarR = path("M228 96 C262 104 268 164 254 196 C244 216 210 208 208 176 C207 144 210 112 228 96 Z")
    private val pupMouth = path("M160 160 V172 M140 174 Q160 192 180 174")
    private val tongue = path("M160 176 Q162 190 172 186")
    private val pandaEarL = path("M78 74 C64 44 76 22 104 40 C110 54 100 72 78 74 Z")
    private val pandaEarR = path("M242 74 C256 44 244 22 216 40 C210 54 220 72 242 74 Z")
    private val tufts = path("M70 60 L54 50 M72 48 L60 34 M78 40 L72 26 M250 60 L266 50 M248 48 L260 34 M242 40 L248 26")
    private val pandaMouth = path("M160 162 V172 M144 174 Q160 188 176 174")
    private val pandaBrows = path("M100 84 Q122 76 134 86 M186 86 Q198 76 220 84")

    // Sheet pattern, each in a 100x100 box (the shooting star in 120x100)
    private val heart = path("M50 82 C20 60 10 44 14 30 C18 16 36 12 50 28 C64 12 82 16 86 30 C90 44 80 60 50 82 Z")
    private val streak1 = path("M10 70 C40 50 70 42 100 34")
    private val streak2 = path("M12 82 C42 62 72 54 102 46")
    private val streak3 = path("M14 94 C44 74 74 66 104 58")
    private val star = path("M104 28 L108 40 L120 44 L108 48 L104 60 L100 48 L88 44 L100 40 Z")
    private val moonPath = path("M70 14 A40 40 0 1 0 70 86 A30 30 0 1 1 70 14 Z")
    private val tinyStarPath = path("M50 10 L58 42 L90 50 L58 58 L50 90 L42 58 L10 50 L42 42 Z")
    /** The blanket and its fold, in the 1440x960 canvas units. */
    private val blanketPath = path("M0 740 C120 700 240 760 360 730 C480 700 600 760 720 730 C840 700 960 760 1080 730 C1200 700 1320 760 1440 730 L1440 960 L0 960 Z")
    private val foldPath = path("M0 790 C240 770 480 810 720 790 C960 770 1200 810 1440 790")

    // ---- drawing helpers, all in the animal's own units ----

    private fun oval(c: Canvas, cx: Float, cy: Float, rx: Float, ry: Float, paint: Paint) {
        r.set(cx - rx, cy - ry, cx + rx, cy + ry); c.drawOval(r, paint)
    }

    private fun dot(c: Canvas, cx: Float, cy: Float, rx: Float, ry: Float, color: Int, alpha: Int = 255) {
        p.color = color; p.alpha = alpha; oval(c, cx, cy, rx, ry, p)
    }

    private fun outline(color: Int, width: Float, alpha: Int = 255) {
        line.color = color; line.alpha = alpha; line.strokeWidth = width
    }

    private fun furOval(c: Canvas, cx: Float, cy: Float, rx: Float, ry: Float, fur: Paint, edge: Int, width: Float = 3f) {
        oval(c, cx, cy, rx, ry, fur); outline(edge, width); oval(c, cx, cy, rx, ry, line)
    }

    private fun furPath(c: Canvas, path: Path, fur: Paint, edge: Int, width: Float = 3f) {
        c.drawPath(path, fur); outline(edge, width); c.drawPath(path, line)
    }

    private fun furRect(c: Canvas, x: Float, y: Float, w: Float, h: Float, rad: Float, fur: Paint, edge: Int, width: Float = 3f) {
        r.set(x, y, x + w, y + h); c.drawRoundRect(r, rad, rad, fur); outline(edge, width); c.drawRoundRect(r, rad, rad, line)
    }

    private fun strokes(c: Canvas, path: Path, color: Int, width: Float, alpha: Int = 255) {
        outline(color, width, alpha); c.drawPath(path, line)
    }

    private fun fillPath(c: Canvas, path: Path, color: Int, alpha: Int = 255) {
        p.color = color; p.alpha = alpha; c.drawPath(path, p)
    }

    private fun shadow(c: Canvas, cx: Float, cy: Float, rx: Float) = dot(c, cx, cy, rx, 14f, SHADOW, 178)

    // ---- the animals ----

    /** Draws animal [which] (see the constants) in its own box at the canvas origin. */
    fun animal(c: Canvas, which: Int, gesture: Float) {
        when (which) {
            FAWN -> bigFawn(c, gesture)
            ELEPHANT -> elephant(c, gesture)
            PUPPY -> puppy(c, gesture)
            PANDA -> redPanda(c, gesture)
            else -> smallFawn(c, gesture)
        }
    }

    /** The spotted orange fawn lying down, hooves folded. [earWiggle] turns the ears about their bases. */
    fun bigFawn(c: Canvas, earWiggle: Float) {
        shadow(c, 180f, 302f, 150f)
        furPath(c, fawnBodyPath, fawnBody, FAWN_LINE)
        dot(c, 236f, 262f, 60f, 22f, FAWN_LINE, 46)
        dot(c, 150f, 166f, 7f, 9f, IVORY); dot(c, 186f, 158f, 8f, 10f, IVORY); dot(c, 224f, 166f, 8f, 10f, IVORY)
        dot(c, 258f, 184f, 7f, 9f, IVORY); dot(c, 206f, 186f, 6f, 8f, IVORY); dot(c, 244f, 206f, 6f, 8f, IVORY); dot(c, 170f, 190f, 5f, 7f, IVORY)
        dot(c, 124f, 264f, 54f, 22f, IVORY, 230)
        fillPath(c, hoofL, INK); fillPath(c, hoofR, INK)
        furOval(c, 112f, 190f, 42f, 54f, fawnNeck, FAWN_LINE)
        dot(c, 112f, 204f, 22f, 34f, IVORY, 217)
        // Ears, each turned about its base
        c.save(); c.rotate(earWiggle, 70f, 126f); c.rotate(-32f, 44f, 84f)
        furOval(c, 44f, 84f, 26f, 50f, fawnEarL, FAWN_LINE)
        dot(c, 44f, 90f, 13f, 34f, IVORY)
        fillPath(c, fawnTipL, INK)
        c.restore()
        c.save(); c.rotate(-earWiggle, 132f, 122f); c.rotate(32f, 158f, 80f)
        furOval(c, 158f, 80f, 26f, 50f, fawnEarR, FAWN_LINE)
        dot(c, 158f, 86f, 13f, 34f, IVORY)
        fillPath(c, fawnTipR, INK)
        c.restore()
        furOval(c, 100f, 122f, 64f, 64f, fawnHead, FAWN_LINE)
        dot(c, 82f, 88f, 6f, 8f, IVORY); dot(c, 104f, 80f, 5f, 7f, IVORY); dot(c, 124f, 92f, 5f, 7f, IVORY)
        dot(c, 96f, 154f, 36f, 26f, 0xFFF8EAD6.toInt())
        dot(c, 92f, 144f, 14f, 10f, INK)
        dot(c, 88f, 141f, 4f, 2.5f, WHITE, 178)
        strokes(c, fawnMouth, ORANGE_LINE, 3f)
        dot(c, 74f, 114f, 9f, 11f, INK); dot(c, 124f, 112f, 9f, 11f, INK)
        dot(c, 77f, 110f, 3.5f, 3.5f, WHITE); dot(c, 127f, 108f, 3.5f, 3.5f, WHITE)
        strokes(c, fawnBrows, FAWN_LINE, 2.5f)
    }

    /** The grey elephant facing front, trunk hanging. [trunkSwing] turns the trunk about its root. */
    fun elephant(c: Canvas, trunkSwing: Float) {
        shadow(c, 160f, 322f, 150f)
        furOval(c, 160f, 236f, 122f, 82f, greyBody, GREY_LINE)
        c.save(); c.rotate(-10f, 66f, 158f)
        furOval(c, 66f, 158f, 50f, 70f, greyEarL, GREY_LINE)
        dot(c, 70f, 162f, 34f, 52f, 0xFFCFCAC5.toInt())
        c.restore()
        c.save(); c.rotate(10f, 254f, 158f)
        furOval(c, 254f, 158f, 50f, 70f, greyEarR, GREY_LINE)
        dot(c, 250f, 162f, 34f, 52f, 0xFFCFCAC5.toInt())
        c.restore()
        furRect(c, 62f, 272f, 68f, 48f, 22f, greyFootL, GREY_LINE)
        furRect(c, 190f, 272f, 68f, 48f, 22f, greyFootR, GREY_LINE)
        val pad = 0xFFF2B8C6.toInt()
        dot(c, 80f, 308f, 5f, 5f, pad); dot(c, 96f, 312f, 5f, 5f, pad); dot(c, 112f, 308f, 5f, 5f, pad)
        dot(c, 208f, 308f, 5f, 5f, pad); dot(c, 224f, 312f, 5f, 5f, pad); dot(c, 240f, 308f, 5f, 5f, pad)
        furOval(c, 160f, 142f, 86f, 86f, greyHead, GREY_LINE)
        dot(c, 140f, 96f, 34f, 20f, 0xFFCFCAC5.toInt(), 115)
        strokes(c, tuskL, IVORY2, 11f); strokes(c, tuskR, IVORY2, 11f)
        c.save(); c.rotate(trunkSwing, 160f, 194f)
        furPath(c, trunk, greyTrunk, GREY_LINE)
        strokes(c, wrinkles, GREY_LINE, 2.5f, 204)
        c.restore()
        val eye = 0xFF2B2B2B.toInt()
        dot(c, 126f, 130f, 9f, 10f, eye); dot(c, 194f, 130f, 9f, 10f, eye)
        dot(c, 129f, 126f, 3f, 3f, WHITE); dot(c, 197f, 126f, 3f, 3f, WHITE)
        strokes(c, elephantBrows, GREY_LINE, 2.5f)
    }

    /** The golden-retriever puppy sitting up. [earFlick] turns the ears outward about their tops. */
    fun puppy(c: Canvas, earFlick: Float) {
        shadow(c, 160f, 306f, 130f)
        furOval(c, 160f, 250f, 102f, 62f, pupBody, TAN_LINE)
        dot(c, 160f, 236f, 64f, 70f, CREAM_TAN); outline(TAN_LINE, 3f); oval(c, 160f, 236f, 64f, 70f, line)
        furRect(c, 112f, 218f, 42f, 84f, 21f, pupLegL, TAN_LINE)
        furRect(c, 166f, 218f, 42f, 84f, 21f, pupLegR, TAN_LINE)
        dot(c, 133f, 300f, 30f, 14f, CREAM_TAN); outline(TAN_LINE, 3f); oval(c, 133f, 300f, 30f, 14f, line)
        dot(c, 187f, 300f, 30f, 14f, CREAM_TAN); oval(c, 187f, 300f, 30f, 14f, line)
        strokes(c, toes, TAN_LINE, 2f)
        c.save(); c.rotate(earFlick, 100f, 100f)
        fillPath(c, pupEarL, TAN_DARK); strokes(c, pupEarL, TAN_LINE, 3f)
        c.restore()
        c.save(); c.rotate(-earFlick, 220f, 100f)
        fillPath(c, pupEarR, TAN_DARK); strokes(c, pupEarR, TAN_LINE, 3f)
        c.restore()
        furOval(c, 160f, 132f, 82f, 74f, pupHead, TAN_LINE)
        dot(c, 128f, 98f, 22f, 14f, CREAM_TAN, 128); dot(c, 192f, 98f, 22f, 14f, CREAM_TAN, 128)
        dot(c, 160f, 164f, 48f, 34f, CREAM_TAN)
        dot(c, 160f, 148f, 18f, 13f, INK)
        dot(c, 154f, 144f, 5f, 3f, WHITE, 178)
        strokes(c, pupMouth, 0xFF8A6A3C.toInt(), 3.5f)
        strokes(c, tongue, 0xFFD9414F.toInt(), 6f)
        val eye = 0xFF2B1D10.toInt()
        dot(c, 128f, 114f, 10f, 12f, eye); dot(c, 192f, 114f, 10f, 12f, eye)
        dot(c, 131f, 110f, 3.5f, 3.5f, WHITE); dot(c, 195f, 110f, 3.5f, 3.5f, WHITE)
    }

    /** The red panda, arms out, big yellow eyes. [armWave] lifts the right arm about its shoulder (negative = up). */
    fun redPanda(c: Canvas, armWave: Float) {
        shadow(c, 160f, 324f, 140f)
        furOval(c, 160f, 242f, 94f, 88f, pandaBody, BLACK_LINE)
        c.save(); c.rotate(26f, 68f, 232f)
        furOval(c, 68f, 232f, 28f, 50f, pandaArmL, BLACK_LINE)
        c.restore()
        c.save(); c.rotate(armWave, 232f, 190f); c.rotate(-26f, 252f, 232f)
        furOval(c, 252f, 232f, 28f, 50f, pandaArmR, BLACK_LINE)
        c.restore()
        furOval(c, 108f, 316f, 42f, 22f, pandaFootL, BLACK_LINE)
        furOval(c, 212f, 316f, 42f, 22f, pandaFootR, BLACK_LINE)
        dot(c, 160f, 250f, 40f, 46f, 0xFF2E2E2E.toInt(), 153)
        fillPath(c, pandaEarL, 0xFF1C1C1C.toInt()); strokes(c, pandaEarL, BLACK_LINE, 3f)
        fillPath(c, pandaEarR, 0xFF1C1C1C.toInt()); strokes(c, pandaEarR, BLACK_LINE, 3f)
        strokes(c, tufts, IVORY2, 4f)
        furOval(c, 160f, 126f, 90f, 90f, pandaHead, ORANGE_LINE)
        dot(c, 96f, 150f, 22f, 30f, ORANGE_DARK, 115); dot(c, 224f, 150f, 22f, 30f, ORANGE_DARK, 115)
        dot(c, 84f, 164f, 22f, 34f, CREAM_PANDA); dot(c, 236f, 164f, 22f, 34f, CREAM_PANDA)
        dot(c, 160f, 166f, 40f, 30f, CREAM_PANDA)
        dot(c, 160f, 152f, 13f, 10f, INK)
        strokes(c, pandaMouth, 0xFF6B3A16.toInt(), 3.5f)
        strokes(c, pandaBrows, CREAM_PANDA, 6f)
        dot(c, 122f, 118f, 27f, 25f, WHITE); dot(c, 198f, 118f, 27f, 25f, WHITE)
        outline(ORANGE_LINE, 2.5f); oval(c, 122f, 118f, 27f, 25f, line); oval(c, 198f, 118f, 27f, 25f, line)
        val iris = 0xFFE3A52E.toInt()
        dot(c, 124f, 120f, 19f, 19f, iris); dot(c, 196f, 120f, 19f, 19f, iris)
        dot(c, 126f, 122f, 12f, 12f, 0xFF111111.toInt()); dot(c, 194f, 122f, 12f, 12f, 0xFF111111.toInt())
        dot(c, 118f, 112f, 6f, 6f, WHITE); dot(c, 188f, 112f, 6f, 6f, WHITE)
        dot(c, 132f, 130f, 2.5f, 2.5f, WHITE); dot(c, 202f, 130f, 2.5f, 2.5f, WHITE)
    }

    /** The small tan fawn with antler nubs and a butterfly patch. [headBob] turns the head and neck about the shoulders. */
    fun smallFawn(c: Canvas, headBob: Float) {
        shadow(c, 180f, 302f, 150f)
        furPath(c, fawnBodyPath, tanBody, TAN_LINE, 4f)
        dot(c, 236f, 262f, 60f, 22f, TAN_LINE, 38)
        dot(c, 160f, 166f, 7f, 9f, IVORY2); dot(c, 200f, 160f, 8f, 10f, IVORY2); dot(c, 236f, 172f, 7f, 9f, IVORY2); dot(c, 186f, 188f, 5f, 7f, IVORY2)
        dot(c, 124f, 264f, 54f, 22f, IVORY2, 230)
        val hoof = 0xFF8A6A3C.toInt()
        fillPath(c, hoofL, hoof); fillPath(c, hoofR, hoof)
        // Teal patch with a white butterfly
        dot(c, 262f, 232f, 20f, 20f, 0xFF5FB3D9.toInt())
        outline(0xFF2C7FA6.toInt(), 3f); oval(c, 262f, 232f, 20f, 20f, line)
        c.save(); c.rotate(-20f, 256f, 228f); dot(c, 256f, 228f, 6f, 8f, WHITE); c.restore()
        c.save(); c.rotate(20f, 268f, 228f); dot(c, 268f, 228f, 6f, 8f, WHITE); c.restore()
        dot(c, 257f, 238f, 4f, 5f, WHITE); dot(c, 267f, 238f, 4f, 5f, WHITE)
        // Head and neck, bobbing together
        c.save(); c.rotate(headBob, 112f, 244f)
        furOval(c, 112f, 190f, 42f, 54f, tanNeck, TAN_LINE, 4f)
        dot(c, 112f, 204f, 22f, 34f, IVORY2, 217)
        c.save(); c.rotate(-32f, 48f, 92f)
        furOval(c, 48f, 92f, 24f, 44f, tanEarL, TAN_LINE, 4f)
        dot(c, 48f, 98f, 12f, 30f, IVORY2)
        c.restore()
        c.save(); c.rotate(32f, 154f, 88f)
        furOval(c, 154f, 88f, 24f, 44f, tanEarR, TAN_LINE, 4f)
        dot(c, 154f, 94f, 12f, 30f, IVORY2)
        c.restore()
        p.color = hoof
        r.set(76f, 46f, 90f, 74f); c.drawRoundRect(r, 7f, 7f, p)
        r.set(112f, 42f, 126f, 70f); c.drawRoundRect(r, 7f, 7f, p)
        furOval(c, 100f, 122f, 64f, 64f, tanHead, TAN_LINE, 4f)
        dot(c, 96f, 154f, 36f, 26f, IVORY2)
        dot(c, 92f, 144f, 13f, 9f, 0xFF2B1D10.toInt())
        dot(c, 88f, 141f, 4f, 2.5f, WHITE, 178)
        strokes(c, fawnMouth, hoof, 3f)
        dot(c, 74f, 114f, 9f, 11f, 0xFF2B1D10.toInt()); dot(c, 124f, 112f, 9f, 11f, 0xFF2B1D10.toInt())
        dot(c, 77f, 110f, 3.5f, 3.5f, WHITE); dot(c, 127f, 108f, 3.5f, 3.5f, WHITE)
        c.restore()
    }

    // ---- the bedsheet pattern; (x, y) is the top-left corner, [size] the box width in pixels ----

    private inline fun symbol(c: Canvas, x: Float, y: Float, size: Float, box: Float, body: () -> Unit) {
        c.save(); c.translate(x, y)
        val s = size / box
        c.scale(s, s)
        body()
        c.restore()
    }

    /** A ringed planet. */
    fun planet(c: Canvas, x: Float, y: Float, size: Float, color: Int) = symbol(c, x, y, size, 100f) {
        p.color = color; c.drawCircle(50f, 50f, 26f, p)
        outline(color, 5f)
        c.rotate(-18f, 50f, 52f); oval(c, 50f, 52f, 46f, 12f, line)
    }

    /** A heart with a ring round it. */
    fun ringHeart(c: Canvas, x: Float, y: Float, size: Float, color: Int) = symbol(c, x, y, size, 100f) {
        fillPath(c, heart, color)
        outline(color, 5f)
        c.rotate(-18f, 50f, 54f); oval(c, 50f, 54f, 46f, 12f, line)
    }

    /** A shooting star: three rainbow streaks and a yellow star, in a 120-wide box. */
    fun shooting(c: Canvas, x: Float, y: Float, size: Float) = symbol(c, x, y, size, 120f) {
        strokes(c, streak1, PINK, 7f); strokes(c, streak2, MINT, 7f); strokes(c, streak3, LILAC, 7f)
        fillPath(c, star, YELLOW)
    }

    fun moon(c: Canvas, x: Float, y: Float, size: Float, color: Int) = symbol(c, x, y, size, 100f) {
        fillPath(c, moonPath, color)
    }

    fun tinyStar(c: Canvas, x: Float, y: Float, size: Float, color: Int, alpha: Int = 255) = symbol(c, x, y, size, 100f) {
        fillPath(c, tinyStarPath, color, alpha)
    }

    fun dot(c: Canvas, cx: Float, cy: Float, radius: Float, color: Int) {
        p.color = color; c.drawCircle(cx, cy, radius, p)
    }

    /** The pink blanket over the bottom of the sheet, wavy top edge and a fold line, fitted to [w] x [h]. */
    fun blanket(c: Canvas, w: Float, h: Float) {
        c.save(); c.scale(w / 1440f, h / 960f)
        fillPath(c, blanketPath, BLANKET)
        strokes(c, foldPath, FOLD, 6f)
        c.restore()
    }

    @Suppress("RestrictedApi")
    private fun path(d: String): Path = PathParser.createPathFromPathData(d)
}
