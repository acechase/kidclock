package com.acechase.kidclock.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import com.acechase.kidclock.art.StuffiesArt
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * The five stuffed animals sitting on a pink blanket at the foot of a cream bedsheet printed with
 * pastel planets, ringed hearts, shooting stars and moons. The sheet and blanket are painted once;
 * the animals are drawn every frame because every so often one of them does something: the red
 * panda waves, the puppy flicks its ears, the elephant swings its trunk, the big fawn wiggles its
 * ears, the small fawn bobs its head. Their left-to-right order changes once a day.
 */
class StuffiesFace(context: Context) : ClockFace(context) {
    private val sheet = StaticLayer(::paintSheet)
    private val cal = Calendar.getInstance()
    private var dayKey = -1
    private var dayCheckedAt = 0L
    private val order = intArrayOf(0, 1, 2, 3, 4)
    private val rnd = Random(System.nanoTime())
    private var nextGestureAt = 0L
    private var gestureStart = -1L
    private var gestureWho = -1

    override val dayTextColor = 0xFF6B4A2E.toInt()
    override val dayShadowColor = 0x1A000000
    override fun dayBackgroundForMoonBite() = CREAM

    override fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean) {
        if (night) {
            c.drawColor(Color.BLACK)
            StuffiesArt.tinyStar(c, w * 0.30f, h * 0.30f, h * 0.03f, NIGHT_TEXT, 40)
            StuffiesArt.tinyStar(c, w * 0.68f, h * 0.62f, h * 0.025f, NIGHT_TEXT, 34)
            StuffiesArt.tinyStar(c, w * 0.12f, h * 0.70f, h * 0.022f, NIGHT_TEXT, 30)
            return
        }
        sheet.draw(c, w, h)
        updateOrder(nowMs)

        // One gesture every 20 to 60 seconds, lasting GESTURE_MS.
        var t = -1f
        if (gestureStart >= 0L) {
            val tt = (nowMs - gestureStart) / GESTURE_MS
            if (tt >= 1f) {
                gestureStart = -1L; gestureWho = -1
                nextGestureAt = nowMs + nextWait()
            } else t = tt
        } else {
            if (nextGestureAt == 0L) nextGestureAt = nowMs + nextWait()
            if (nowMs >= nextGestureAt) {
                gestureStart = nowMs; gestureWho = rnd.nextInt(5); t = 0f
            }
        }

        // Spread the animals along the blanket, feet at 91% of the height, heads clear of the quote card, a wider gap in the middle.
        val unit = h / 960f * 0.66f
        var total = 0f
        for (a in 0 until 5) {
            val box = StuffiesArt.BOXES[a]
            total += (box.right - box.left) * unit * StuffiesArt.SCALES[a]
        }
        val gap = ((w * 0.95f - total) / 4.5f).coerceAtLeast(-h * 0.02f)
        var x = w * 0.025f
        val baseY = h * 0.91f
        for (slot in 0 until 5) {
            val a = order[slot]
            val s = unit * StuffiesArt.SCALES[a]
            val box = StuffiesArt.BOXES[a]
            c.save()
            c.translate(x - box.left * s, baseY - box.base * s)
            c.scale(s, s)
            StuffiesArt.animal(c, a, gestureFor(a, t))
            c.restore()
            x += (box.right - box.left) * s + gap * (if (slot == 1) 1.5f else 1f)
        }
    }

    private fun nextWait(): Long = 20_000L + rnd.nextLong(40_001L)

    /** Degrees for the moving part of [animal] at gesture time [t] in 0..1, or 0 when it is idle. */
    private fun gestureFor(animal: Int, t: Float): Float {
        if (t < 0f || animal != gestureWho) return 0f
        val env = sin(PI * t).toFloat()
        val flick = sin(4.0 * PI * t).toFloat() * env
        return when (animal) {
            StuffiesArt.FAWN -> 12f * flick
            StuffiesArt.ELEPHANT -> 14f * flick
            StuffiesArt.PUPPY -> 16f * flick
            StuffiesArt.PANDA -> -(32f * env + 14f * flick)
            else -> 6f * flick
        }
    }

    /** Reshuffles the animals' order once a day, seeded by the date so a restart keeps it. */
    private fun updateOrder(nowMs: Long) {
        if (dayKey >= 0 && nowMs - dayCheckedAt < 60_000L) return
        dayCheckedAt = nowMs
        cal.timeInMillis = nowMs
        val key = cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
        if (key == dayKey) return
        dayKey = key
        val r = Random(key)
        for (i in 0 until 5) order[i] = i
        for (i in 4 downTo 1) {
            val j = r.nextInt(i + 1)
            val tmp = order[i]; order[i] = order[j]; order[j] = tmp
        }
    }

    /** The cream sheet with its pastel print and the pink blanket, painted once. */
    private fun paintSheet(c: Canvas, w: Float, h: Float) {
        c.drawColor(CREAM)
        val sx = w / 1440f; val sy = h / 960f
        StuffiesArt.planet(c, 60f * sx, 40f * sy, 110f * sy, StuffiesArt.LILAC)
        StuffiesArt.ringHeart(c, 260f * sx, 120f * sy, 100f * sy, StuffiesArt.PINK)
        StuffiesArt.shooting(c, 400f * sx, 20f * sy, 150f * sy)
        StuffiesArt.moon(c, 640f * sx, 40f * sy, 70f * sy, StuffiesArt.YELLOW)
        StuffiesArt.planet(c, 820f * sx, 90f * sy, 90f * sy, StuffiesArt.MINT)
        StuffiesArt.tinyStar(c, 980f * sx, 40f * sy, 40f * sy, StuffiesArt.PEACH)
        StuffiesArt.ringHeart(c, 1080f * sx, 30f * sy, 110f * sy, StuffiesArt.LILAC)
        StuffiesArt.shooting(c, 1240f * sx, 150f * sy, 150f * sy)
        StuffiesArt.tinyStar(c, 180f * sx, 250f * sy, 34f * sy, StuffiesArt.BLUE)
        StuffiesArt.planet(c, 40f * sx, 330f * sy, 90f * sy, StuffiesArt.PEACH)
        StuffiesArt.moon(c, 230f * sx, 420f * sy, 60f * sy, StuffiesArt.PINK)
        StuffiesArt.ringHeart(c, 60f * sx, 520f * sy, 90f * sy, StuffiesArt.MINT)
        StuffiesArt.tinyStar(c, 1300f * sx, 330f * sy, 36f * sy, StuffiesArt.YELLOW)
        StuffiesArt.planet(c, 1320f * sx, 420f * sy, 100f * sy, StuffiesArt.PINK)
        StuffiesArt.shooting(c, 1180f * sx, 520f * sy, 130f * sy)
        StuffiesArt.tinyStar(c, 700f * sx, 580f * sy, 30f * sy, StuffiesArt.LILAC)
        StuffiesArt.moon(c, 900f * sx, 560f * sy, 54f * sy, StuffiesArt.BLUE)
        StuffiesArt.tinyStar(c, 520f * sx, 600f * sy, 28f * sy, StuffiesArt.PEACH)
        StuffiesArt.dot(c, 340f * sx, 60f * sy, 7f * sy, StuffiesArt.MINT)
        StuffiesArt.dot(c, 760f * sx, 200f * sy, 6f * sy, StuffiesArt.PINK)
        StuffiesArt.dot(c, 1000f * sx, 280f * sy, 7f * sy, StuffiesArt.YELLOW)
        StuffiesArt.dot(c, 140f * sx, 640f * sy, 6f * sy, StuffiesArt.LILAC)
        StuffiesArt.dot(c, 1240f * sx, 640f * sy, 6f * sy, StuffiesArt.PEACH)
        StuffiesArt.dot(c, 600f * sx, 640f * sy, 5f * sy, StuffiesArt.BLUE)
        StuffiesArt.blanket(c, w, h)
    }

    companion object {
        const val CREAM = 0xFFFBF3EA.toInt()
        private const val GESTURE_MS = 1500f
    }
}
