package com.acechase.kidclock.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import com.acechase.kidclock.looks.Fonts
import com.acechase.kidclock.looks.Look
import com.acechase.kidclock.looks.LookContext
import com.acechase.kidclock.looks.HoldZone
import com.acechase.kidclock.looks.LookPickerState
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
 * One painted background plus the clock drawn over it. Subclasses paint the themed background in
 * [drawBackground]; the time, date and alarm line are drawn by the current [look] (see looks/),
 * which the user picks separately from the background. During a gentle wake-up every look gives
 * way to the classic layout so the quote card fits. At night every face goes dark with amber digits.
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
    /** A few easy words drawn big over the wake-up quote for an early reader (null = just the quote). */
    var littleLine: String? = null
        set(v) { if (field != v) { field = v; invalidate() } }
    /** Small, quiet quote on the ordinary clock layout (null = off). Hidden at night. */
    var clockQuote: Quote? = null
        set(v) { if (field != v) { field = v; invalidate() } }
    /** The font and layout the time is drawn in. */
    var look: Look = Look.create(0)
        set(v) { field = v; invalidate() }
    /** True while the look picker is open: the alarm line, weather and quote step aside. */
    var picking = false
        set(v) { if (field != v) { field = v; invalidate() } }
    /**
     * The gentle wake-up sunrise, drawn in place of the background while [wakeMode] is on: how far
     * it has come, 0 (deep night) to 1 (full morning), or null outside a wake-up. The scene eases
     * towards a jump (the sun finishes rising when the child says "I'm awake").
     */
    var sunrise: Float? = null
        set(v) { if (field != v) { field = v; invalidate() } }
    private var shownSunrise = Float.NaN
    private val sunriseScene by lazy { SunriseScene() }

    /** Screen area the weather occupies; tapping it opens the weather page. */
    val weatherRect = RectF()
    /** Screen area of the next-alarm line; tapping it opens the alarm's detail card. Empty when no line. */
    val alarmRect = RectF()
    /** Screen area of the quote (small line or wake-up card); tapping it shows the next quote. Empty when none. */
    val quoteRect = RectF()
    /** Screen area of the time as the look drew it; part of the hold zone that opens the look picker. */
    val timeRect = RectF()

    private val smallQuotePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("serif", Typeface.ITALIC) }
    private val quotePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("serif", Typeface.ITALIC) }
    private val littlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL) }
    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cardRect = RectF()
    private val fonts = Fonts.get(context)

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
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
    private val ringRect = RectF()
    private val dateFmt = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

    // Hold-to-change-look feedback: a ring that fills under the finger.
    private var holdStartMs = 0L
    private var holdX = 0f
    private var holdY = 0f

    /** Accent colour for digits in day mode; faces override. */
    protected open val dayTextColor = Color.WHITE
    protected open val dayShadowColor = 0x66000000
    /** The colours in use this frame: the face's own, amber at night, white over the sunrise. */
    private var activeInk = Color.WHITE
    private var activeShadow = 0x66000000

    abstract fun drawBackground(c: Canvas, w: Float, h: Float, nowMs: Long, night: Boolean)

    /**
     * May a finger at (x, y) start the hold that opens the look picker? The whole time/date section counts:
     * the middle of the screen, minus the settings corner, the weather, the next-alarm line and the quote.
     */
    fun inHoldZone(x: Float, y: Float, cornerPx: Float): Boolean = HoldZone.contains(
        x, y, width.toFloat(), height.toFloat(), timeRect.box(),
        listOf(weatherRect.box(), alarmRect.box(), quoteRect.box()), cornerPx,
    )
    private fun RectF.box() = HoldZone.Box(left, top, right, bottom)

    /** The finger came down in the hold zone: start showing the hold ring there. */
    fun beginHold(x: Float, y: Float, nowMs: Long) {
        holdStartMs = nowMs; holdX = x; holdY = y
        invalidate()
    }

    fun endHold() {
        if (holdStartMs != 0L) { holdStartMs = 0L; invalidate() }
    }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val nowMs = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = nowMs }

        val sunriseTarget = sunrise
        val sunriseOn = wakeMode && sunriseTarget != null
        activeInk = if (night) NIGHT_TEXT else if (sunriseOn) Color.WHITE else dayTextColor
        activeShadow = if (sunriseOn) SUNRISE_SHADOW else dayShadowColor
        if (sunriseOn) {
            // Follow the clock; a jump (say, to full morning) is eased over a few seconds.
            shownSunrise = if (shownSunrise.isNaN()) sunriseTarget!!
            else shownSunrise + (sunriseTarget!! - shownSunrise).coerceIn(-SUNRISE_STEP, SUNRISE_STEP)
            sunriseScene.draw(c, w, h, nowMs, shownSunrise)
        } else {
            shownSunrise = Float.NaN
            drawBackground(c, w, h, nowMs, night)
        }

        val textColor = activeInk
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val timeStr = if (use24h) "%02d:%02d".format(hour, minute)
        else "%d:%02d".format(if (hour % 12 == 0) 12 else hour % 12, minute)
        val ampm = if (use24h) null else if (hour < 12) "AM" else "PM"
        val showQuote = wakeMode && quote != null

        if (wakeMode) {
            drawWakeLayout(c, w, h, cal, timeStr, ampm, textColor, showQuote)
        } else {
            look.draw(c, LookContext(
                w = w, h = h, nowMs = nowMs, cal = cal, timeText = timeStr, ampm = ampm, night = night,
                ink = textColor, shadow = activeShadow, nextAlarmText = nextAlarmText, showChrome = !picking,
                fonts = fonts, alarmRect = alarmRect, timeRect = timeRect,
            ))
        }

        // Quote of the morning (card during wake-up, otherwise a quiet line if enabled)
        val cq = clockQuote
        if (showQuote) drawQuote(c, w, h, quote!!)
        else if (!wakeMode && cq != null && !night && !picking) drawSmallQuote(c, w, h, cq)
        else quoteRect.setEmpty()

        // Weather (top-right). Hidden at night so the room stays dark, and while picking a look.
        val wx = weather
        weatherRect.set(w * 0.70f, 0f, w, h * 0.24f)
        if (wx != null && !night && !picking) {
            smallPaint.color = textColor
            smallPaint.typeface = if (wakeMode) fonts.regular else look.labelTypeface(fonts)
            smallPaint.fontFeatureSettings = "lnum"
            smallPaint.setShadowLayer(8f, 0f, 3f, activeShadow)
            smallPaint.textAlign = Paint.Align.RIGHT
            smallPaint.textSize = h * 0.085f
            val tempW = smallPaint.measureText(wx.tempText)
            c.drawText(wx.tempText, w * 0.96f, h * 0.13f, smallPaint)
            emojiPaint.textSize = h * 0.075f
            c.drawText(wx.icon(nightSky = hour < 6 || hour >= 19), w * 0.96f - tempW - h * 0.06f, h * 0.13f, emojiPaint)
            smallPaint.textSize = h * 0.038f
            smallPaint.alpha = 210
            c.drawText(wx.rangeText, w * 0.96f, h * 0.185f, smallPaint)
            smallPaint.alpha = 255
            smallPaint.textAlign = Paint.Align.CENTER
            smallPaint.clearShadowLayer()
            smallPaint.fontFeatureSettings = null
            smallPaint.typeface = fonts.regular
        }

        // Stay-in-bed moon (top-left) at night. Morning has no lamp: the gentle wake-up sunrise is the signal.
        val lampColor = if (phase == NightSchedule.Phase.NIGHT) 0xFFE04040.toInt() else 0
        if (lampColor != 0) {
            val r = h * 0.045f
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

        val holding = holdStartMs != 0L
        if (holding) drawHoldRing(c, w, h, nowMs, textColor)

        // Animate: smooth by day, once a second at night (unless a hold ring is filling).
        if (isAttachedToWindow) postInvalidateDelayed(if (night && !holding) 1000L else 50L)
    }

    /** The classic layout used during a gentle wake-up, with the quote card when there is a quote. */
    private fun drawWakeLayout(c: Canvas, w: Float, h: Float, cal: Calendar, timeStr: String, ampm: String?, textColor: Int, showQuote: Boolean) {
        timePaint.color = textColor
        smallPaint.color = textColor
        if (night) timePaint.clearShadowLayer() else timePaint.setShadowLayer(12f, 0f, 6f, activeShadow)

        timePaint.textSize = if (showQuote) h * 0.30f else h * 0.42f
        while (timePaint.measureText(timeStr) > w * 0.85f) timePaint.textSize *= 0.95f
        val baseY = if (showQuote) h * 0.40f else h * 0.56f
        c.drawText(timeStr, w / 2f, baseY, timePaint)
        val timeW = timePaint.measureText(timeStr)
        timeRect.set(w / 2f - timeW / 2f - h * 0.05f, baseY - timePaint.textSize * 0.8f, w / 2f + timeW / 2f + h * 0.12f, baseY + timePaint.textSize * 0.15f)

        if (ampm != null) {
            smallPaint.textSize = h * 0.06f
            smallPaint.textAlign = Paint.Align.LEFT
            c.drawText(ampm, w / 2f + timeW / 2f + h * 0.02f, baseY - timePaint.textSize * 0.62f, smallPaint)
            smallPaint.textAlign = Paint.Align.CENTER
        }

        smallPaint.textSize = if (showQuote) h * 0.05f else h * 0.07f
        smallPaint.alpha = if (night) 140 else 230
        c.drawText(dateFmt.format(cal.time), w / 2f, if (showQuote) h * 0.49f else h * 0.70f, smallPaint)

        // Status line, left-aligned to leave room for the "I'm awake" button.
        if (nextAlarmText.isNotEmpty()) {
            smallPaint.textSize = h * 0.036f
            smallPaint.alpha = if (night) 110 else 220
            smallPaint.textAlign = Paint.Align.LEFT
            // Shrink a long line a little before cutting it short ("say “I'm awake” or “I'm up”" is long).
            var t = nextAlarmText
            while (smallPaint.measureText(t) > w * 0.66f && smallPaint.textSize > h * 0.028f) smallPaint.textSize *= 0.95f
            while (smallPaint.measureText(t) > w * 0.66f && t.length > 8) t = t.dropLast(4) + "…"
            c.drawText(t, w * 0.04f, h * 0.945f, smallPaint)
            smallPaint.textAlign = Paint.Align.CENTER
            alarmRect.set(0f, h * 0.88f, w * 0.72f, h)
        } else alarmRect.setEmpty()
        smallPaint.alpha = 255
    }

    /** A ring filling clockwise under the finger, and after a moment a hint about what the hold does. */
    private fun drawHoldRing(c: Canvas, w: Float, h: Float, nowMs: Long, textColor: Int) {
        val progress = ((nowMs - holdStartMs).toFloat() / LookPickerState.HOLD_MS).coerceIn(0f, 1f)
        val r = h * 0.0675f
        val cx = holdX.coerceIn(r, w - r); val cy = holdY.coerceIn(r, h - r)
        lampPaint.clearShadowLayer()
        lampPaint.color = textColor
        lampPaint.alpha = 56
        c.drawCircle(cx, cy, r * 0.92f, lampPaint)
        ringPaint.strokeWidth = h * 0.0075f
        ringPaint.color = textColor
        ringPaint.alpha = 90
        c.drawCircle(cx, cy, r, ringPaint)
        ringPaint.alpha = 255
        ringRect.set(cx - r, cy - r, cx + r, cy + r)
        c.drawArc(ringRect, -90f, 360f * progress, false, ringPaint)
        if (progress > 0.2f) {
            // Just under the ring, kept on screen, so it reads wherever the finger is.
            smallPaint.typeface = fonts.medium
            smallPaint.textSize = h * 0.0375f
            smallPaint.color = textColor
            if (night) smallPaint.clearShadowLayer() else smallPaint.setShadowLayer(8f, 0f, 3f, activeShadow)
            val hint = "Keep holding to change the look"
            val half = smallPaint.measureText(hint) / 2f + h * 0.02f
            val hx = cx.coerceIn(half, w - half)
            val hy = if (cy + r * 2.2f < h * 0.97f) cy + r * 2.2f else cy - r * 1.6f
            c.drawText(hint, hx, hy, smallPaint)
            smallPaint.clearShadowLayer()
            smallPaint.typeface = fonts.regular
        }
    }

    protected open fun dayBackgroundForMoonBite(): Int = 0xFF102040.toInt()

    /** Discreet quote for the everyday layout: small italic, low alpha, at most three lines plus attribution. */
    private fun drawSmallQuote(c: Canvas, w: Float, h: Float, q: Quote) {
        val box = look.quoteBox(w, h)
        val inner = box.width().toInt()
        val quoted = "“${q.text}”"
        val ink = look.quoteInk ?: activeInk
        smallQuotePaint.color = ink
        smallQuotePaint.setShadowLayer(6f, 0f, 2f, activeShadow)
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
        // A translucent panel, sized to the text, so the quote stays readable over busy backgrounds:
        // smoked glass under light ink, frosted glass under dark ink.
        var widest = 0f
        for (i in 0 until layout.lineCount) widest = maxOf(widest, layout.getLineWidth(i))
        // The attribution in the look's label face; wide faces (Rubik Mono) shrink to fit, and widen the card.
        val attr = "— ${q.attribution}"
        smallPaint.typeface = look.labelTypeface(fonts)
        smallPaint.textSize = attrSize
        while (smallPaint.measureText(attr) > inner && smallPaint.textSize > h * 0.016f) smallPaint.textSize *= 0.94f
        widest = maxOf(widest, smallPaint.measureText(attr))
        val padX = h * 0.03f; val padY = h * 0.018f
        cardRect.set(box.centerX() - widest / 2f - padX, top - padY, box.centerX() + widest / 2f + padX, top + total + padY * 0.6f)
        val lightInk = (0.299f * ((ink shr 16) and 0xFF) + 0.587f * ((ink shr 8) and 0xFF) + 0.114f * (ink and 0xFF)) > 128f
        cardPaint.color = if (lightInk) 0x5C000000 else 0x9EFFFFFF.toInt()
        c.drawRoundRect(cardRect, h * 0.02f, h * 0.02f, cardPaint)
        c.save()
        c.translate(box.centerX() - inner / 2f, top)
        smallQuotePaint.alpha = 220
        layout.draw(c)
        c.restore()
        smallPaint.color = ink
        smallPaint.alpha = 180
        smallPaint.setShadowLayer(6f, 0f, 2f, activeShadow)
        c.drawText(attr, box.centerX(), top + layout.height + attrSize * 1.1f, smallPaint)
        smallPaint.clearShadowLayer()
        smallPaint.alpha = 255
        smallPaint.typeface = fonts.regular
        quoteRect.set(cardRect.left - h * 0.01f, cardRect.top - h * 0.01f, cardRect.right + h * 0.01f, cardRect.bottom + h * 0.01f)
    }

    /**
     * The wake-up card: the quote in italics with its attribution, and for an early reader a few easy words
     * big on top with a short rule between, so the younger kid reads the line and the older one reads on.
     */
    private fun drawQuote(c: Canvas, w: Float, h: Float, q: Quote) {
        val little = littleLine
        // Over the sunrise the card keeps to the right, leaving the sun's column on the left.
        val left = if (wakeMode && sunrise != null) w * SunriseScene.CARD_LEFT else w * 0.07f
        cardRect.set(left, if (little != null) h * 0.545f else h * 0.555f, w * 0.93f, h * 0.885f)
        val inner = (cardRect.width() - h * 0.08f).toInt()
        val ink = activeInk
        var littleLayout: StaticLayout? = null
        if (little != null) {
            littlePaint.color = ink
            var ls = h * 0.075f
            while (true) {
                littlePaint.textSize = ls
                littleLayout = StaticLayout.Builder.obtain(little, 0, little.length, littlePaint, inner)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER).setLineSpacing(0f, 1.05f).build()
                if (littleLayout.lineCount <= 1 || ls < h * 0.05f) break
                ls *= 0.92f
            }
        }
        val littleH = littleLayout?.let { it.height + h * 0.045f } ?: 0f
        quotePaint.color = ink
        var size = if (little != null) h * 0.040f else h * 0.052f
        var layout: StaticLayout
        val quoted = "\u201c${q.text}\u201d"
        val room = cardRect.height() * 0.70f - littleH
        while (true) {
            quotePaint.textSize = size
            layout = StaticLayout.Builder.obtain(quoted, 0, quoted.length, quotePaint, inner)
                .setAlignment(Layout.Alignment.ALIGN_CENTER).setLineSpacing(0f, 1.12f).build()
            if (layout.height <= room || size < h * 0.026f) break
            size *= 0.93f
        }
        // Shrink the card to the text so short quotes do not float in an empty box.
        cardRect.bottom = minOf(cardRect.bottom, cardRect.top + littleH + layout.height + h * 0.12f)
        // Frosted glass under dark ink, smoked under light ink: busy scenes (stuffies, the tank) stay behind the words.
        val lightInk = (0.299f * ((ink shr 16) and 0xFF) + 0.587f * ((ink shr 8) and 0xFF) + 0.114f * (ink and 0xFF)) > 128f
        cardPaint.color = if (night) 0x33FF8A3D else if (lightInk) 0x7A000000 else 0xA6FFFFFF.toInt()
        c.drawRoundRect(cardRect, h * 0.03f, h * 0.03f, cardPaint)
        var y = cardRect.top + h * 0.035f
        if (littleLayout != null) {
            c.save(); c.translate(cardRect.left + h * 0.04f, y); littleLayout.draw(c); c.restore()
            y += littleLayout.height
            cardPaint.color = ink
            cardPaint.alpha = if (night) 120 else 150
            c.drawRoundRect(w / 2f - h * 0.06f, y + h * 0.018f, w / 2f + h * 0.06f, y + h * 0.024f, h * 0.003f, h * 0.003f, cardPaint)
            cardPaint.alpha = 255
            y += h * 0.045f
        }
        c.save(); c.translate(cardRect.left + h * 0.04f, y); layout.draw(c); c.restore()
        smallPaint.color = ink
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
        private const val SUNRISE_SHADOW = 0x66000000
        /** Largest change in the drawn sunrise per frame (20 fps): a jump to full morning takes about four seconds. */
        private const val SUNRISE_STEP = 0.012f

        /** One painted background per index; the look drawn over it is chosen separately. */
        fun create(context: Context, index: Int): ClockFace = when (Math.floorMod(index, COUNT)) {
            0 -> SkyFace(context)
            1 -> OceanFace(context)
            2 -> RainbowFace(context)
            3 -> GalaxyFace(context)
            4 -> FirefliesFace(context)
            5 -> AuroraFace(context)
            6 -> MeadowFace(context)
            7 -> DoodleStringsFace(context)
            8 -> NotebookFace(context)
            9 -> SoccerBoardFace(context)
            10 -> StadiumFace(context)
            11 -> CrabTankFace(context)
            12 -> NightReefFace(context)
            13 -> StuffiesFace(context)
            14 -> DoodleNotebookFace(context)
            else -> BlueprintFace(context)
        }
        const val COUNT = 16
    }
}
