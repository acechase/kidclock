package com.acechase.kidclock.weather

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/** Twelve hourly columns: hour, icon, temperature on a curve, precipitation chance. */
class HourlyStripView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    var use24h = false
    private var points: List<HourPoint> = emptyList()
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textAlign = Paint.Align.CENTER }
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF7AD3FF.toInt(); strokeWidth = 4f; style = Paint.Style.STROKE }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x337AD3FF; style = Paint.Style.FILL }
    private val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val path = Path()

    fun setData(p: List<HourPoint>) { points = p.take(12); invalidate() }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        if (points.isEmpty()) {
            text.textSize = 24f; text.alpha = 160
            c.drawText("Hourly forecast not loaded yet", w / 2, h / 2, text); text.alpha = 255
            return
        }
        val n = points.size
        val colW = w / n
        val tMin = points.minOf { it.temp }; val tMax = points.maxOf { it.temp }
        val span = max(1.0, tMax - tMin)
        val curveTop = h * 0.40f; val curveBottom = h * 0.72f
        val fmt = SimpleDateFormat(if (use24h) "HH" else "ha", Locale.getDefault())
        val cal = Calendar.getInstance()

        // Temperature curve
        path.reset()
        val ys = FloatArray(n)
        for (i in 0 until n) {
            val x = colW * (i + 0.5f)
            val y = (curveBottom - ((points[i].temp - tMin) / span).toFloat() * (curveBottom - curveTop))
            ys[i] = y
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        c.drawPath(path, line)
        path.lineTo(colW * (n - 0.5f), curveBottom + 20f); path.lineTo(colW * 0.5f, curveBottom + 20f); path.close()
        c.drawPath(path, fill)

        for (i in 0 until n) {
            val p = points[i]
            val x = colW * (i + 0.5f)
            cal.timeInMillis = p.timeMs
            text.textSize = 20f; text.alpha = 200
            c.drawText(if (i == 0) "Now" else fmt.format(cal.time).lowercase(Locale.getDefault()), x, h * 0.12f, text)
            text.textSize = 30f; text.alpha = 255
            val hr = cal.get(Calendar.HOUR_OF_DAY)
            c.drawText(Weather.iconFor(p.code, hr < 6 || hr >= 19), x, h * 0.30f, text)
            c.drawCircle(x, ys[i], 6f, dot)
            text.textSize = 24f
            c.drawText("${Math.round(p.temp)}°", x, ys[i] - 14f, text)
            if (p.precipPct >= 10) {
                text.textSize = 19f; text.color = 0xFF9BD1FF.toInt()
                c.drawText("💧${p.precipPct}%", x, h * 0.92f, text)
                text.color = Color.WHITE
            }
        }
    }
}
