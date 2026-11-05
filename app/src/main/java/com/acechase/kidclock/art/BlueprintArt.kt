package com.acechase.kidclock.art

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.PathParser

/**
 * The line drawings on the Blueprint background: six figures traced from the design canvas, each
 * authored in a 100x100 box as a handful of pen strokes (a few filled dots, two dashed guide
 * lines), inked in one colour with round caps the way a draughtsman would.
 */
object BlueprintArt {
    /** One pen stroke of a figure: its path, width in box units, and whether it is filled or dashed. */
    class Stroke(val path: Path, val width: Float, val fill: Boolean = false, val dash: DashPathEffect? = null)

    private val dash44 = DashPathEffect(floatArrayOf(4f, 4f), 0f)
    private val dash45 = DashPathEffect(floatArrayOf(4f, 5f), 0f)

    /** FIG. 1: a rocket with fins, a porthole, a flame and a dashed centre line. */
    val rocket: List<Stroke> = listOf(
        Stroke(path("M50 6 C66 22 68 50 60 74 H40 C32 50 34 22 50 6 Z"), 3f),
        Stroke(path("M40 58 L24 80 L40 76 Z M60 58 L76 80 L60 76 Z"), 3f),
        Stroke(circle(50f, 38f, 8f), 3f),
        Stroke(path("M42 76 L50 96 L58 76"), 3f),
        Stroke(path("M50 14 V70"), 1.5f, dash = dash44),
    )

    /** FIG. 2: a tank, turret and gun over a track with four wheels. */
    val tank: List<Stroke> = listOf(
        Stroke(rrect(12f, 50f, 84f, 72f, 8f), 3f),
        Stroke(rrect(34f, 36f, 66f, 52f, 4f), 3f),
        Stroke(path("M66 44 H96"), 3f),
        Stroke(rrect(8f, 66f, 92f, 88f, 11f), 3f),
        Stroke(circle(22f, 77f, 5f), 2.5f),
        Stroke(circle(40f, 77f, 5f), 2.5f),
        Stroke(circle(58f, 77f, 5f), 2.5f),
        Stroke(circle(76f, 77f, 5f), 2.5f),
    )

    /** FIG. 3: a one-eyed monster with horns, two fangs and little legs. */
    val monster: List<Stroke> = listOf(
        Stroke(oval(20f, 26f, 80f, 90f), 3f),
        Stroke(path("M32 34 L22 10 L42 30 M68 34 L78 10 L58 30"), 3f),
        Stroke(circle(50f, 48f, 12f), 3f),
        Stroke(circle(50f, 48f, 5f), 0f, fill = true),
        Stroke(path("M32 70 Q50 86 68 70 M40 72 L44 80 L48 72 M52 72 L56 80 L60 72"), 2.5f),
        Stroke(path("M40 90 V98 M60 90 V98"), 3f),
    )

    /** FIG. 4: a boxy robot with an antenna, square eyes, arms, legs and a dial. */
    val robot: List<Stroke> = listOf(
        Stroke(rrect(30f, 16f, 70f, 48f, 4f), 3f),
        Stroke(rect(38f, 26f, 46f, 34f), 0f, fill = true),
        Stroke(rect(54f, 26f, 62f, 34f), 0f, fill = true),
        Stroke(path("M42 40 H58 M50 16 V6"), 2.5f),
        Stroke(circle(50f, 5f, 3f), 0f, fill = true),
        Stroke(rrect(26f, 52f, 74f, 86f, 4f), 3f),
        Stroke(path("M26 62 H10 V80 M74 62 H90 V80 M36 86 V98 M64 86 V98"), 3f),
        Stroke(circle(50f, 69f, 6f), 2.5f),
    )

    /** FIG. 5: a T-rex in profile, tail to the left, with a dot for an eye. */
    val trex: List<Stroke> = listOf(
        Stroke(path("M22 72 C10 52 28 32 54 36 C68 38 78 28 84 16 L92 28 C88 44 78 52 66 54 C72 66 62 82 44 84 Z"), 3f),
        Stroke(path("M22 72 L4 90 M44 84 L40 98 M58 80 L62 96 M62 54 L70 62"), 3f),
        Stroke(circle(82f, 24f, 3f), 0f, fill = true),
    )

    /** FIG. 6: a flying saucer with a dome, three lights and dashed beam lines. */
    val ufo: List<Stroke> = listOf(
        Stroke(oval(8f, 44f, 92f, 68f), 3f),
        Stroke(path("M28 50 A22 18 0 0 1 72 50"), 3f),
        Stroke(circle(24f, 58f, 3f), 0f, fill = true),
        Stroke(circle(50f, 62f, 3f), 0f, fill = true),
        Stroke(circle(76f, 58f, 3f), 0f, fill = true),
        Stroke(path("M40 70 L30 94 M60 70 L70 94"), 2f, dash = dash45),
    )

    /**
     * Inks [figure] in [color], its 100x100 box centred on ([cx],[cy]) at [size] px and turned by
     * [rot] degrees. Stroke widths and dashes scale with the figure.
     */
    fun draw(c: Canvas, p: Paint, figure: List<Stroke>, cx: Float, cy: Float, size: Float, rot: Float, color: Int) {
        p.reset(); p.isAntiAlias = true; p.color = color
        p.strokeCap = Paint.Cap.ROUND; p.strokeJoin = Paint.Join.ROUND
        Doodles.draw(c, cx, cy, size, rot) {
            for (s in figure) {
                if (s.fill) {
                    p.style = Paint.Style.FILL; p.pathEffect = null
                } else {
                    p.style = Paint.Style.STROKE; p.strokeWidth = s.width; p.pathEffect = s.dash
                }
                c.drawPath(s.path, p)
            }
        }
        p.pathEffect = null
    }

    private fun circle(cx: Float, cy: Float, r: Float): Path = Path().apply { addCircle(cx, cy, r, Path.Direction.CW) }
    private fun oval(l: Float, t: Float, r: Float, b: Float): Path = Path().apply { addOval(RectF(l, t, r, b), Path.Direction.CW) }
    private fun rect(l: Float, t: Float, r: Float, b: Float): Path = Path().apply { addRect(l, t, r, b, Path.Direction.CW) }
    private fun rrect(l: Float, t: Float, r: Float, b: Float, rx: Float): Path =
        Path().apply { addRoundRect(RectF(l, t, r, b), rx, rx, Path.Direction.CW) }

    @Suppress("RestrictedApi")
    private fun path(d: String): Path = PathParser.createPathFromPathData(d)
}
