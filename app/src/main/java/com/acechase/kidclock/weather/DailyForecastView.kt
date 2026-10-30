package com.acechase.kidclock.weather

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** One row per day: name, icon, rain chance, and a low-to-high bar on a shared scale. */
class DailyForecastView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var days: List<DayPoint> = emptyList()
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val track = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x22FFFFFF }
    private val bar = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    fun setData(d: List<DayPoint>) { days = d.take(6); invalidate() }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        if (days.isEmpty()) {
            text.textSize = 24f; text.alpha = 160; text.textAlign = Paint.Align.CENTER
            c.drawText("Daily forecast not loaded yet", w / 2, h / 2, text)
            text.alpha = 255; text.textAlign = Paint.Align.LEFT
            return
        }
        val rowH = h / days.size
        val lo = days.minOf { it.low }; val hi = days.maxOf { it.high }
        val span = maxOf(1.0, hi - lo)
        val fmt = SimpleDateFormat("EEE", Locale.getDefault())
        val cal = Calendar.getInstance()
        val today = cal.get(Calendar.DAY_OF_YEAR)
        val nameX = 0f; val iconX = w * 0.20f; val rainX = w * 0.30f
        val barL = w * 0.52f; val barR = w * 0.88f
        for ((i, d) in days.withIndex()) {
            val cy = rowH * (i + 0.5f)
            cal.timeInMillis = d.dateMs
            text.textAlign = Paint.Align.LEFT
            text.textSize = 24f
            c.drawText(if (cal.get(Calendar.DAY_OF_YEAR) == today) "Today" else fmt.format(cal.time), nameX, cy + 9f, text)
            text.textSize = 30f
            c.drawText(Weather.iconFor(d.code, false), iconX, cy + 11f, text)
            if (d.precipPct >= 10) {
                text.textSize = 19f; text.color = 0xFF9BD1FF.toInt()
                c.drawText("💧${d.precipPct}%", rainX, cy + 7f, text)
                text.color = Color.WHITE
            }
            text.textSize = 22f; text.textAlign = Paint.Align.RIGHT
            c.drawText("${Math.round(d.low)}°", barL - 12f, cy + 8f, text)
            text.textAlign = Paint.Align.LEFT
            c.drawText("${Math.round(d.high)}°", barR + 12f, cy + 8f, text)
            rect.set(barL, cy - 7f, barR, cy + 7f)
            c.drawRoundRect(rect, 7f, 7f, track)
            val x0 = barL + ((d.low - lo) / span).toFloat() * (barR - barL)
            val x1 = barL + ((d.high - lo) / span).toFloat() * (barR - barL)
            bar.shader = LinearGradient(x0, 0f, x1, 0f, 0xFF4FC3F7.toInt(), 0xFFFFB74D.toInt(), Shader.TileMode.CLAMP)
            rect.set(x0, cy - 7f, maxOf(x1, x0 + 14f), cy + 7f)
            c.drawRoundRect(rect, 7f, 7f, bar)
        }
    }
}
