package com.acechase.kidclock.looks

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Everything a look needs to draw one frame. The face fills it in; the look reads it and writes
 * back [alarmRect] and [timeRect] so taps and the hold-to-change gesture land in the right place.
 */
class LookContext(
    val w: Float,
    val h: Float,
    val nowMs: Long,
    val cal: Calendar,
    /** "7:15", or "07:15" in 24-hour mode. */
    val timeText: String,
    /** "AM" or "PM"; null in 24-hour mode. */
    val ampm: String?,
    val night: Boolean,
    /** Text colour: amber at night, otherwise the background's day colour. */
    val ink: Int,
    /** Shadow colour under text by day (ignored at night). */
    val shadow: Int,
    /** The next-alarm line, "" when there is none. */
    val nextAlarmText: String,
    /** False while the look picker is open: the alarm line and weather get out of the way. */
    val showChrome: Boolean,
    val fonts: Fonts,
    val alarmRect: RectF,
    val timeRect: RectF,
)

/**
 * A look draws the time, AM/PM, date and next-alarm line over whatever background the face
 * painted. One subclass per look; each must also read well at night in amber on black, when
 * [LookContext.night] is true and [LookContext.ink] is the amber.
 */
abstract class Look {
    abstract val name: String
    /** Typeface for the small shared labels the face draws itself (weather, quote attribution). */
    open fun labelTypeface(fonts: Fonts): Typeface = fonts.regular
    /** Colour for the small clock quote by day, or null to use the background's text colour. */
    open val quoteInk: Int? get() = null
    /** Box the small clock quote is fitted into; looks whose layout fills that area move it. */
    open fun quoteBox(w: Float, h: Float): RectF = RectF(w * 0.14f, h * 0.735f, w * 0.86f, h * 0.855f)

    abstract fun draw(c: Canvas, x: LookContext)

    protected val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val rect = RectF()
    private val longDateFmt = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    private val dayNameFmt = SimpleDateFormat("EEEE", Locale.getDefault())
    private val monthDayFmt = SimpleDateFormat("MMMM d", Locale.getDefault())
    private val shortDateFmt = SimpleDateFormat("EEEE MMM d", Locale.getDefault())

    protected fun longDate(x: LookContext): String = longDateFmt.format(x.cal.time)
    protected fun dayName(x: LookContext): String = dayNameFmt.format(x.cal.time)
    protected fun monthDay(x: LookContext): String = monthDayFmt.format(x.cal.time)
    protected fun shortDate(x: LookContext): String = shortDateFmt.format(x.cal.time)

    /** Soft shadow by day, none at night (amber on black needs no lift). */
    protected fun shadow(p: Paint, x: LookContext, radius: Float, dy: Float) {
        if (x.night) p.clearShadowLayer() else p.setShadowLayer(radius, 0f, dy, x.shadow)
    }

    /** Shrinks the paint's text size until [s] fits in [maxW]. */
    protected fun fit(p: Paint, s: String, maxW: Float) {
        while (p.measureText(s) > maxW && p.textSize > 8f) p.textSize *= 0.95f
    }

    protected fun withAlpha(color: Int, alpha: Int): Int = (color and 0x00FFFFFF) or (alpha shl 24)

    /**
     * The "⏰ next alarm" line, or nothing while the picker is open. Fills [LookContext.alarmRect]
     * so a tap on it opens the alarm.
     */
    protected fun drawAlarmLine(
        c: Canvas, x: LookContext, cx: Float, baseline: Float, size: Float, tf: Typeface,
        align: Paint.Align = Paint.Align.CENTER, color: Int = x.ink,
        alpha: Int = if (x.night) 110 else 220, shadowed: Boolean = true,
    ) {
        if (!x.showChrome || x.nextAlarmText.isEmpty()) { x.alarmRect.setEmpty(); return }
        paint.style = Paint.Style.FILL
        paint.typeface = tf
        paint.textSize = size
        paint.color = color
        paint.alpha = alpha
        paint.textAlign = align
        if (shadowed) shadow(paint, x, 8f, 3f) else paint.clearShadowLayer()
        val s = "⏰ ${x.nextAlarmText}"
        c.drawText(s, cx, baseline, paint)
        val tw = paint.measureText(s)
        val left = when (align) {
            Paint.Align.LEFT -> cx
            Paint.Align.CENTER -> cx - tw / 2f
            Paint.Align.RIGHT -> cx - tw
        }
        x.alarmRect.set(left - x.h * 0.08f, baseline - size * 1.5f, left + tw + x.h * 0.08f, baseline + size * 0.8f)
        paint.alpha = 255
        paint.textAlign = Paint.Align.CENTER
        paint.clearShadowLayer()
    }

    companion object {
        fun create(index: Int): Look = when (LookCatalog.wrap(index)) {
            0 -> ClassicLook()
            1 -> BoldLook()
            2 -> StackedLook()
            3 -> AnalogLook()
            4 -> SketchLook()
            5 -> WhackyLook()
            6 -> PixelLook()
            7 -> MagnetsLook()
            8 -> TiaraLook()
            9 -> MarkerLook()
            else -> MarkerStackedLook()
        }
    }
}
