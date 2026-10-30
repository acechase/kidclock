package com.acechase.kidclock.weather

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.acechase.kidclock.Prefs
import com.acechase.kidclock.R
import java.util.Calendar
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat

/**
 * Detailed forecast: the next 12 hours as a strip with a temperature curve, and the next days
 * as rows with high/low bars. Opened by double-tapping the weather on the clock face; closes on
 * any tap, back, or after a timeout so the clock is never left hidden.
 */
class WeatherDetailActivity : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_KidClock_Settings)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_weather)
        val prefs = Prefs(this)
        val w = WeatherService.cached(this)
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        findViewById<TextView>(R.id.wxPlace).text = w?.place?.ifEmpty { "Weather" } ?: "Weather"
        findViewById<TextView>(R.id.wxNow).text = w?.let { "${it.icon(hour < 6 || hour >= 19)}  ${it.tempText}" } ?: "—"
        findViewById<TextView>(R.id.wxCondition).text = w?.let { "${it.conditionText}  ·  ${it.rangeText}" } ?: unavailableText()
        findViewById<HourlyStripView>(R.id.wxHourly).apply { use24h = prefs.use24h; setData(w?.hourly ?: emptyList()) }
        findViewById<DailyForecastView>(R.id.wxDaily).setData(w?.daily ?: emptyList())
        findViewById<View>(R.id.wxRoot).setOnClickListener { finish() }
        handler.postDelayed({ finish() }, TIMEOUT_MS)
        // Ask for fresh data if the cache is stale; redraw when it arrives.
        WeatherService.maybeRefresh(this) { fresh -> if (!isFinishing) recreate() }
    }

    override fun onResume() {
        super.onResume()
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    /** No usable forecast: say whether we never had one or lost the connection, so stale data is never implied. */
    private fun unavailableText(): String {
        val last = WeatherService.lastKnown(this) ?: return "No forecast yet"
        val fmt = SimpleDateFormat(if (Prefs(this).use24h) "EEE HH:mm" else "EEE h:mm a", Locale.getDefault())
        return "Forecast unavailable  ·  last update ${fmt.format(Date(last.fetchedAtMs))}"
    }

    override fun onDestroy() { super.onDestroy(); handler.removeCallbacksAndMessages(null) }

    companion object { private const val TIMEOUT_MS = 90_000L }
}
