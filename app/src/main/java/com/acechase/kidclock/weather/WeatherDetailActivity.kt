package com.acechase.kidclock.weather

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.content.res.ColorStateList
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.acechase.kidclock.Prefs
import com.acechase.kidclock.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * The weather page, written for an 11-year-old and dressed for the sky: the temperature now and where the day is
 * going, whether to pack a raincoat, what to wear, the temperature at the moments of their day, and the week in a
 * sentence, over a background painted for the current weather ([SkyView], colours from [WeatherSky]). The wording
 * comes from [WeatherWords]. For an early reader ([Prefs.weatherPictures]) the two answers that matter most, what
 * to wear and whether to take a raincoat, also appear as picture tiles above the sentences. Opened by tapping the
 * weather on the clock face; closes on any tap, back, or after a timeout so the clock is never left hidden.
 *
 * For screenshots, `adb shell am start -n com.acechase.kidclock/.weather.WeatherDetailActivity --es sky RAINY`
 * forces a sky (any [WeatherSky] name); the forecast itself is still the real one.
 */
class WeatherDetailActivity : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var sky: WeatherSky

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_KidClock_Settings)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_weather)
        val prefs = Prefs(this)
        val w = WeatherService.cached(this)
        val now = System.currentTimeMillis()
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val nightSky = hour < 6 || hour >= 19
        sky = intent.getStringExtra(EXTRA_SKY)?.let { s -> WeatherSky.values().firstOrNull { it.name.equals(s, ignoreCase = true) } }
            ?: if (w != null) WeatherSky.of(w.code, nightSky) else WeatherSky.CLOUDY
        dress()
        if (w != null) show(w, WeatherWords(use24h = prefs.use24h).describe(w, now), nightSky, prefs.weatherPictures)
        else showUnavailable(prefs)
        findViewById<View>(R.id.wxRoot).setOnClickListener { finish() }
        handler.postDelayed({ finish() }, TIMEOUT_MS)
        // Ask for fresh data if the cache is stale; redraw when it arrives.
        WeatherService.maybeRefresh(this) { if (!isFinishing) recreate() }
    }

    /** Paint the sky and give every fixed view the sky's colours. */
    private fun dress() {
        findViewById<SkyView>(R.id.wxSkyView).sky = sky
        for (id in intArrayOf(R.id.wxDressCard, R.id.wxParts, R.id.wxWeekCard, R.id.wxWearTile, R.id.wxRainTile)) findViewById<View>(id).background = card()
        for (id in intArrayOf(R.id.wxHeading, R.id.wxSky, R.id.wxPartsLabel, R.id.wxWeekLabel, R.id.wxFooter, R.id.wxWearSub, R.id.wxRainSub)) findViewById<TextView>(id).setTextColor(sky.muted)
        for (id in intArrayOf(R.id.wxNow, R.id.wxDay, R.id.wxRain, R.id.wxDress, R.id.wxWeek, R.id.wxWearWord, R.id.wxRainWord)) findViewById<TextView>(id).setTextColor(sky.ink)
        for (id in intArrayOf(R.id.wxWearIcon, R.id.wxRainIcon)) findViewById<ImageView>(id).imageTintList = ColorStateList.valueOf(sky.ink)
        findViewById<TextView>(R.id.wxNowWord).setTextColor(sky.accent)
    }

    private fun card() = GradientDrawable().apply { setColor(sky.card); cornerRadius = 20f * resources.displayMetrics.density }

    private fun show(w: Weather, words: WeatherWords.Words, nightSky: Boolean, pictures: Boolean) {
        if (pictures) tiles(words)
        text(R.id.wxHeading, words.heading)
        text(R.id.wxIcon, w.icon(nightSky))
        text(R.id.wxNow, w.tempText)
        text(R.id.wxNowWord, words.nowWord)
        text(R.id.wxSky, words.skySentence)
        findViewById<TextView>(R.id.wxDay).text = highlight(words.daySentence, words.dayHighlight, sky.warm)
        text(R.id.wxRain, words.rainSentence)
        text(R.id.wxDress, words.dress)
        text(R.id.wxDressIcon, dressIcon(words))

        val parts = findViewById<LinearLayout>(R.id.wxParts)
        for (p in words.parts) parts.addView(partRow(p, nightSky))

        text(R.id.wxWeek, words.weekSentence)
        val strip = findViewById<LinearLayout>(R.id.wxWeekStrip)
        for (d in words.week) strip.addView(dayColumn(d), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
    }

    /** No usable forecast: say whether we never had one or lost the connection, so stale data is never implied. */
    private fun showUnavailable(prefs: Prefs) {
        text(R.id.wxHeading, "Weather")
        text(R.id.wxNow, "—")
        val last = WeatherService.lastKnown(this)
        text(R.id.wxDay, if (last == null) "No forecast yet." else {
            val fmt = SimpleDateFormat(if (prefs.use24h) "EEEE HH:mm" else "EEEE h:mm a", Locale.getDefault())
            "The forecast isn't available right now. Last update ${fmt.format(Date(last.fetchedAtMs))}."
        })
        text(R.id.wxRain, "Check back in a few minutes.")
        findViewById<View>(R.id.wxDressCard).visibility = View.INVISIBLE
    }

    private fun partRow(p: WeatherWords.Part, nightSky: Boolean): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 10, 0, 10)
        }
        row.addView(TextView(this).apply {
            text = p.label; textSize = 22f; setTypeface(typeface, Typeface.BOLD); setTextColor(sky.ink)
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(TextView(this).apply {
            text = Weather.iconFor(p.code, nightSky && (p.hour < 6 || p.hour >= 19)); textSize = 28f
            setPadding(0, 0, 14, 0)
        })
        row.addView(TextView(this).apply {
            text = WeatherWords.deg(p.temp); textSize = 30f; setTypeface(typeface, Typeface.BOLD); setTextColor(sky.ink)
            gravity = Gravity.END; minWidth = 90
        })
        return row
    }

    private fun dayColumn(d: WeatherWords.Day): View {
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL }
        col.addView(TextView(this).apply { text = d.name; textSize = 15f; setTypeface(typeface, Typeface.BOLD); setTextColor(sky.muted) })
        col.addView(TextView(this).apply { text = Weather.iconFor(d.code, false); textSize = 26f; setPadding(0, 2, 0, 2) })
        col.addView(TextView(this).apply { text = WeatherWords.deg(d.high); textSize = 22f; setTypeface(typeface, Typeface.BOLD); setTextColor(sky.ink) })
        col.addView(TextView(this).apply {
            text = if (d.rainy) (if (WeatherWords.isSnow(d.code)) "snowy" else "rainy") else d.word
            textSize = 14f; setTypeface(typeface, Typeface.BOLD)
            setTextColor(if (d.rainy) sky.accent else if (d.word == "warm" || d.word == "hot") sky.warm else sky.ink)
        })
        return col
    }

    /**
     * The picture tiles: what to wear and whether to take a raincoat, read off the same sentences the page
     * says below them, so the pictures and the words never disagree. The sky sentence steps aside for room.
     */
    private fun tiles(words: WeatherWords.Words) {
        findViewById<View>(R.id.wxTiles).visibility = View.VISIBLE
        findViewById<View>(R.id.wxSky).visibility = View.GONE
        val d = words.dress
        val (wearIcon, wearWord) = when {
            d.startsWith("Wear a big coat") -> R.drawable.ic_wear_coat to "Big coat"
            d.startsWith("Wear a warm coat") -> R.drawable.ic_wear_coat to "Warm coat"
            d.startsWith("Wear a hoodie") -> R.drawable.ic_wear_hoodie to "Hoodie"
            d.startsWith("Wear long sleeves") -> R.drawable.ic_wear_long to "Long sleeves"
            d.startsWith("Shorts") -> R.drawable.ic_wear_tshirt to "Shorts"
            else -> R.drawable.ic_wear_tshirt to "T-shirt"
        }
        findViewById<ImageView>(R.id.wxWearIcon).setImageResource(wearIcon)
        text(R.id.wxWearWord, wearWord)
        text(R.id.wxWearSub, when {
            d.contains("take it off later") -> "take it off later"
            d.contains("bring water") -> "and bring water"
            else -> "all day"
        })
        val r = words.rainSentence
        val (rainIcon, rainWord, rainSub) = when {
            d.contains("Snow boots") -> Triple(R.drawable.ic_wear_boots, "Snow boots", "snow today")
            r.startsWith("Rain likely") -> {
                val when_ = r.removePrefix("Rain likely ").substringBefore(",")
                Triple(R.drawable.ic_rain_umbrella, "Raincoat", if (when_.startsWith("around")) "rain $when_" else "rain today")
            }
            r.startsWith("It might rain") -> Triple(R.drawable.ic_rain_umbrella, "Maybe raincoat", "it might rain")
            r.startsWith("It might snow") -> Triple(R.drawable.ic_wear_boots, "Maybe boots", "it might snow")
            else -> Triple(R.drawable.ic_rain_none, "No raincoat", "dry all day")
        }
        findViewById<ImageView>(R.id.wxRainIcon).setImageResource(rainIcon)
        text(R.id.wxRainWord, rainWord)
        text(R.id.wxRainSub, rainSub)
    }

    private fun dressIcon(words: WeatherWords.Words): String {
        val d = words.dress
        return when {
            d.contains("Snow boots") -> "🥾"
            d.contains("raincoat") -> "🧥"
            d.startsWith("Shorts") -> "🩳"
            d.startsWith("T-shirt") -> "👕"
            d.startsWith("Wear long sleeves") -> "👕"
            else -> "🧥"
        }
    }

    private fun highlight(s: String, part: String?, color: Int): CharSequence {
        val i = if (part == null) -1 else s.indexOf(part)
        if (i < 0) return s
        return SpannableString(s).apply { setSpan(ForegroundColorSpan(color), i, i + part!!.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
    }

    private fun text(id: Int, s: String) { findViewById<TextView>(id).text = s }

    override fun onResume() {
        super.onResume()
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onDestroy() { super.onDestroy(); handler.removeCallbacksAndMessages(null) }

    companion object {
        private const val TIMEOUT_MS = 90_000L
        const val EXTRA_SKY = "sky"
    }
}
