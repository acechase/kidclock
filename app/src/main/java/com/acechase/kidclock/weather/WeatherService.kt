package com.acechase.kidclock.weather

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.acechase.kidclock.Prefs
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Android glue around [WeatherCore]: Open-Meteo current conditions and forecast, no API key. Location comes
 * from settings; until one is set, a coarse IP-based guess is used. Refreshes every 30 min on a background
 * thread; failures are logged and otherwise invisible. Nothing else in the app touches the network.
 */
object WeatherService {
    private const val TAG = "Weather"
    private val core = WeatherCore(http = ::httpGet)
    private val inFlight = AtomicBoolean(false)
    private val main = Handler(Looper.getMainLooper())

    private class PrefsStore(private val p: Prefs) : WeatherCore.Store {
        override val enabled get() = p.weatherEnabled
        override val fahrenheit get() = p.weatherFahrenheit
        override val lat get() = p.weatherLat
        override val lon get() = p.weatherLon
        override val place get() = p.weatherPlace
        override var weatherJson: String?
            get() = p.weatherJson
            set(v) { p.weatherJson = v }
    }

    /** The forecast to show, or null when there is none or it is too old to trust (see [WeatherCore.STALE_MS]). */
    fun cached(context: Context): Weather? = core.usable(PrefsStore(Prefs(context)))

    /** The last forecast we ever got, however old; for "last updated" messaging only. */
    fun lastKnown(context: Context): Weather? = core.cachedAny(PrefsStore(Prefs(context)))

    /** Returns quickly; calls [onUpdate] on the main thread when fresh data arrives. */
    fun maybeRefresh(context: Context, onUpdate: (Weather) -> Unit) {
        val store = PrefsStore(Prefs(context.applicationContext))
        if (!core.needsRefresh(store) || !inFlight.compareAndSet(false, true)) return
        Thread({
            try {
                core.refresh(store)
                    .onSuccess { w -> main.post { onUpdate(w) } }
                    .onFailure { e -> Log.w(TAG, "weather refresh failed: ${e.message}") }
            } finally {
                inFlight.set(false)
            }
        }, "weather").start()
    }

    /** Resolve a typed place name to coordinates. Blocking; call off the main thread. Throws when offline. */
    fun geocode(name: String): Triple<Double, Double, String>? = core.geocode(name)

    private fun httpGet(url: String): String {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 8000; c.readTimeout = 8000
        c.setRequestProperty("User-Agent", "KidClock/0.1")
        try {
            if (c.responseCode != 200) throw IllegalStateException("HTTP ${c.responseCode} for $url")
            return c.inputStream.bufferedReader().use { it.readText() }
        } finally { c.disconnect() }
    }
}
