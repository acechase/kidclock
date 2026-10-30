package com.acechase.kidclock.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import com.acechase.kidclock.night.NightSchedule
import com.acechase.kidclock.weather.Weather
import com.acechase.kidclock.quotes.Quote
import android.graphics.RectF
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Draws the time, date, next-alarm line and okay-to-wake lamp. Subclasses paint the themed
 * background in [drawBackground]. At night every face goes dark with amber digits.
 */
abstract class ClockFace(context: Context) : View(context) {
    var use24h = false
    var phase = NightSchedule.Phase.DAY
        set(v) { if (field != v) { field = v; invalidate() } }
    var nextAlarmText: String = ""
        set(v) { if (field != v) { field = v; invalidate() } }
    var weather: Weather? = null
        set(v) { if (field != v) { field = v; invalidate() } }
    /** Gentle wake-up layout: smaller time, quote card, status line. */
    var wakeMode = false
        set(v) { if (field != v) { field = v; invalidate() } }
    var quote: Quote? = null
        set(v) { if (field != v) { field = v; invalidate() } }
    /** Small, quiet quote on the ordinary clock layout (null = off). Hidden at night. */
    var clockQuote: Quote? = null
        set(v) { if (field != v) { field = v; invalidate() } }

    /** Screen area the weather occupies; double-tapping it opens the detail screen. */
    val weatherRect = RectF()
    /** Screen area of the next-alarm line; tapping it opens the alarm's detail card. Empty when no line. */
    val alarmRect = RectF()
    /** Screen area of the quote (small line or wake-up card); tapping it shows the next quote. Empty when none. */
    val quoteRect = RectF()
    private val smallQuotePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("serif", Typeface.ITALIC) }
    private val quotePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("serif", Typeface.ITALIC) }
    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cardRect = RectF()

    protected val night get() = phase == NightSchedule.Phase.NIGHT

    private val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }
    private val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }
    private val lampPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val dateFmt = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

    /** Faces that draw their own time (analog) return false to hide the big digits. */
    protected open val showDigitalTime: Boolean get() = true
    /** Where the date line is centred; faces override to move it. */
    protected open fun dateAnchor(w: Float, h: Float): Pair<Float, Float> = Pair(w / 2f, h * 0.70f)
    /** Box the small clock-face quote is fitted into; faces with a different layout override. */
    protected open fun clockQuoteBox(w: Float, h: Float): RectF = RectF(w * 0.14f, h * 0.735f, w * 0.86f, h * 0.855f)

    /** Accent colour for digits in day mode; faces override. */
    protected open val dayTextColor = Color.WHITE
    protected open val dayShadowColor = 0x66000000

    abstract fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean)

    override fun onDraw(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val nowMs = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = nowMs }

        drawBackground(c, w, h, nowMs, night)

        val textColor = if (night) NIGHT_TEXT else dayTextColor
        timePaint.color = textColor
        smallPaint.color = textColor
        if (night) timePaint.clearShadowLayer() else timePaint.setShadowLayer(12f, 0f, 6f, dayShadowColor)

        // Time
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val timeStr = if (use24h) "%02d:%02d".format(hour, minute)
        else "%d:%02d".format(if (hour % 12 == 0) 12 else hour % 12, minute)
        val showQuote = wakeMode && quote != null
        timePaint.textSize = if (showQuote) h * 0.30f else h * 0.42f
        while (timePaint.measureText(timeStr) > w * 0.85f) timePaint.textSize *= 0.95f
        val baseY = if (showQuote) h * 0.40f else h * 0.56f
        val digital = showDigitalTime
        if (digital) c.drawText(timeStr, w / 2f, baseY, timePaint)

        if (!use24h && digital) {
            smallPaint.textSize = h * 0.06f
            val ampm = if (hour < 12) "AM" else "PM"
            val timeW = timePaint.measureText(timeStr)
            smallPaint.textAlign = Paint.Align.LEFT
            c.drawText(ampm, w / 2f + timeW / 2f + h * 0.02f, baseY - timePaint.textSize * 0.62f, smallPaint)
            smallPaint.textAlign = Paint.Align.CENTER
        }

        // Date
        smallPaint.textSize = if (showQuote) h * 0.05f else h * 0.07f
        smallPaint.alpha = if (night) 140 else 230
        val (dateX, dateY) = if (showQuote) Pair(w / 2f, h * 0.49f) else dateAnchor(w, h)
        c.drawText(dateFmt.format(cal.time), dateX, dateY, smallPaint)

        // Quote of the morning (card during wake-up, otherwise a quiet line if enabled)
        val cq = clockQuote
        if (showQuote) drawQuote(c, w, h, quote!!)
        else if (!wakeMode && cq != null && !night) drawSmallQuote(c, w, h, cq)
        else quoteRect.setEmpty()

        // Next alarm / status line. In wake mode it is left-aligned to leave room for the button.
        if (nextAlarmText.isNotEmpty()) {
            smallPaint.textSize = if (wakeMode) h * 0.036f else h * 0.045f
            smallPaint.alpha = if (night) 110 else 220
            if (wakeMode) {
                smallPaint.textAlign = Paint.Align.LEFT
                var t = nextAlarmText
                while (smallPaint.measureText(t) > w * 0.66f && t.length > 8) t = t.dropLast(4) + "\u2026"
                c.drawText(t, w * 0.04f, h * 0.945f, smallPaint)
                smallPaint.textAlign = Paint.Align.CENTER
                alarmRect.set(0f, h * 0.88f, w * 0.72f, h)
            } else {
                val s = "⏰ $nextAlarmText"
                c.drawText(s, w / 2f, h * 0.92f, smallPaint)
                val half = smallPaint.measureText(s) / 2f + h * 0.08f
                alarmRect.set(w / 2f - half, h * 0.865f, w / 2f + half, h)
            }
        } else alarmRect.setEmpty()
        smallPaint.alpha = 255

        // Weather (top-right). Hidden at night so the room stays dark.
        val wx = weather
        weatherRect.set(w * 0.70f, 0f, w, h * 0.24f)
        if (wx != null && !night) {
            val hour24 = cal.get(Calendar.HOUR_OF_DAY)
            smallPaint.setShadowLayer(8f, 0f, 3f, dayShadowColor)
            smallPaint.textAlign = Paint.Align.RIGHT
            smallPaint.textSize = h * 0.085f
            val tempW = smallPaint.measureText(wx.tempText)
            c.drawText(wx.tempText, w * 0.96f, h * 0.13f, smallPaint)
            emojiPaint.textSize = h * 0.075f
            c.drawText(wx.icon(nightSky = hour24 < 6 || hour24 >= 19), w * 0.96f - tempW - h * 0.06f, h * 0.13f, emojiPaint)
            smallPaint.textSize = h * 0.038f
            smallPaint.alpha = 210
            c.drawText(wx.rangeText, w * 0.96f, h * 0.185f, smallPaint)
            smallPaint.alpha = 255
            smallPaint.textAlign = Paint.Align.CENTER
            smallPaint.clearShadowLayer()
        }

        // Okay-to-wake lamp (top-left): red moon at night, green sun in the wake window.
        val lampColor = when (phase) {
            NightSchedule.Phase.NIGHT -> 0xFFE04040.toInt()
            NightSchedule.Phase.WAKE_OK -> 0xFF4CD964.toInt()
            NightSchedule.Phase.DAY -> 0
        }
        if (lampColor != 0) {
            val pulse = if (phase == NightSchedule.Phase.WAKE_OK) 1f + 0.15f * kotlin.math.sin(nowMs / 400.0).toFloat() else 1f
            val r = h * 0.045f * pulse
            val cx = w * 0.06f
            val cy = h * 0.10f
            lampPaint.color = lampColor
            lampPaint.alpha = if (night) 150 else 255
            if (night) lampPaint.clearShadowLayer() else lampPaint.setShadowLayer(r, 0f, 0f, lampColor)
            c.drawCircle(cx, cy, r, lampPaint)
            if (phase == NightSchedule.Phase.NIGHT) {
                // Bite out of the circle to make a crescent.
                lampPaint.color = if (night) Color.BLACK else dayBackgroundForMoonBite()
                lampPaint.clearShadowLayer()
                c.drawCircle(cx + r * 0.45f, cy - r * 0.25f, r * 0.8f, lampPaint)
            }
        }

        // Animate: smooth by day, once a second at night.
        if (isAttachedToWindow) postInvalidateDelayed(if (night) 1000L else 50L)
    }

    protected open fun dayBackgroundForMoonBite(): Int = 0xFF102040.toInt()

    /** Discreet quote for the everyday layout: small italic, low alpha, at most three lines plus attribution. */
    private fun drawSmallQuote(c: Canvas, w: Float, h: Float, q: Quote) {
        val box = clockQuoteBox(w, h)
        val inner = box.width().toInt()
        val quoted = "\u201C${q.text}\u201D"
        smallQuotePaint.color = dayTextColor
        smallQuotePaint.setShadowLayer(6f, 0f, 2f, dayShadowColor)
        val attrSize = h * 0.028f
        var size = h * 0.036f
        var layout: StaticLayout
        while (true) {
            smallQuotePaint.textSize = size
            layout = StaticLayout.Builder.obtain(quoted, 0, quoted.length, smallQuotePaint, inner)
                .setAlignment(Layout.Alignment.ALIGN_CENTER).setLineSpacing(0f, 1.08f).build()
            if (layout.height + attrSize * 1.4f <= box.height() || size <= h * 0.024f) break
            size *= 0.92f
        }
        val total = layout.height + attrSize * 1.4f
        val top = box.top + (box.height() - total) / 2f
        c.save()
        c.translate(box.centerX() - inner / 2f, top)
        smallQuotePaint.alpha = 185
        layout.draw(c)
        c.restore()
        smallPaint.textSize = attrSize
        smallPaint.alpha = 150
        smallPaint.setShadowLayer(6f, 0f, 2f, dayShadowColor)
        c.drawText("\u2014 ${q.attribution}", box.centerX(), top + layout.height + attrSize * 1.1f, smallPaint)
        smallPaint.clearShadowLayer()
        smallPaint.alpha = 255
        quoteRect.set(box.left - h * 0.02f, top - h * 0.02f, box.right + h * 0.02f, top + total + h * 0.03f)
    }

    private fun drawQuote(c: Canvas, w: Float, h: Float, q: Quote) {
        cardRect.set(w * 0.07f, h * 0.555f, w * 0.93f, h * 0.885f)
        val inner = (cardRect.width() - h * 0.08f).toInt()
        quotePaint.color = if (night) NIGHT_TEXT else dayTextColor
        var size = h * 0.052f
        var layout: StaticLayout
        val quoted = "\u201C${q.text}\u201D"
        while (true) {
            quotePaint.textSize = size
            layout = StaticLayout.Builder.obtain(quoted, 0, quoted.length, quotePaint, inner)
                .setAlignment(Layout.Alignment.ALIGN_CENTER).setLineSpacing(0f, 1.12f).build()
            if (layout.height <= cardRect.height() * 0.70f || size < h * 0.03f) break
            size *= 0.93f
        }
        // Shrink the card to the text so short quotes do not float in an empty box.
        cardRect.bottom = minOf(cardRect.bottom, cardRect.top + layout.height + h * 0.12f)
        cardPaint.color = if (night) 0x33FF8A3D else 0x2E000000
        c.drawRoundRect(cardRect, h * 0.03f, h * 0.03f, cardPaint)
        c.save()
        c.translate(cardRect.left + h * 0.04f, cardRect.top + h * 0.035f)
        layout.draw(c)
        c.restore()
        smallPaint.textSize = h * 0.034f
        smallPaint.alpha = if (night) 150 else 215
        smallPaint.textAlign = Paint.Align.RIGHT
        c.drawText("\u2014 ${q.attribution}", cardRect.right - h * 0.05f, cardRect.bottom - h * 0.04f, smallPaint)
        smallPaint.textAlign = Paint.Align.CENTER
        smallPaint.alpha = 255
        quoteRect.set(cardRect)
    }

    companion object {
        const val NIGHT_TEXT = 0xFFFF8A3D.toInt() // warm amber, low blue content

        fun create(context: Context, index: Int): ClockFace = when (Math.floorMod(index, COUNT)) {
            0 -> SkyFace(context)
            1 -> OceanFace(context)
            2 -> RainbowFace(context)
            3 -> GalaxyFace(context)
            4 -> FirefliesFace(context)
            5 -> AuroraFace(context)
            6 -> MeadowFace(context)
            else -> AnalogFace(context)
        }
        const val COUNT = 8
    }
}
