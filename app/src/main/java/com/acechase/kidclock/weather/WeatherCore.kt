package com.acechase.kidclock.weather

import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * The weather feature's logic with no Android in it, so the failure paths can be unit-tested on the JVM:
 * refresh policy (how often to try, how long to back off after a failure), the staleness rule (how old a
 * cached forecast may be before we stop showing it), the Open-Meteo and ipinfo parsers, and the refresh
 * itself. Networking is injected as [http], which must return the body for a URL or throw.
 *
 * Every failure is contained: [refresh] never throws and never touches the cached forecast on failure, so a
 * dead network or a vanished service only ever means "no weather on the clock", never a broken clock.
 */
class WeatherCore(
    private val http: (String) -> String,
    private val now: () -> Long = System::currentTimeMillis,
) {
    /** What the core needs from persistent settings. [Prefs] is adapted to this in WeatherService. */
    interface Store {
        val enabled: Boolean
        val fahrenheit: Boolean
        val lat: Double
        val lon: Double
        val place: String
        var weatherJson: String?
    }

    /** When the last refresh was attempted (success or failure), 0 when never. */
    var lastAttemptMs = 0L
        private set

    /** Whatever is cached, however old. */
    fun cachedAny(store: Store): Weather? = Weather.fromJson(store.weatherJson)

    /** The cached forecast if it is recent enough to show, else null. Stale data is worse than none. */
    fun usable(store: Store): Weather? = cachedAny(store)?.takeIf { now() - it.fetchedAtMs <= STALE_MS }

    fun isStale(w: Weather): Boolean = now() - w.fetchedAtMs > STALE_MS

    /** True when a fetch should be attempted now: enabled, cache missing/old/wrong unit, and past the backoff. */
    fun needsRefresh(store: Store): Boolean {
        if (!store.enabled) return false
        val t = now()
        val c = cachedAny(store)
        val fresh = c != null && t - c.fetchedAtMs < REFRESH_MS && c.fahrenheit == store.fahrenheit && c.daily.isNotEmpty()
        if (fresh) return false
        return t - lastAttemptMs >= RETRY_MS
    }

    /**
     * Blocking fetch. On success the cache is replaced and the forecast returned; on any failure the cache is
     * left exactly as it was and the failure is returned, never thrown. Marks the attempt for backoff either way.
     */
    fun refresh(store: Store): Result<Weather> {
        lastAttemptMs = now()
        return runCatching {
            var lat = store.lat
            var lon = store.lon
            var place = store.place
            if (lat.isNaN() || lon.isNaN()) {
                val loc = parseIpLocation(http(IP_URL))
                lat = loc.lat; lon = loc.lon; place = loc.place
            }
            val w = parseForecast(http(forecastUrl(lat, lon, store.fahrenheit)), place, store.fahrenheit, now())
            store.weatherJson = w.toJson()
            w
        }
    }

    /** Resolve a typed place name to coordinates; null when nothing matches. Throws on network failure. */
    fun geocode(name: String): Triple<Double, Double, String>? {
        val q = URLEncoder.encode(name.trim(), "UTF-8")
        val o = JSONObject(http("https://geocoding-api.open-meteo.com/v1/search?name=$q&count=1&language=en&format=json"))
        val r = o.optJSONArray("results")?.optJSONObject(0) ?: return null
        val label = listOfNotNull(r.optString("name").ifEmpty { null }, r.optString("admin1").ifEmpty { null }).joinToString(", ")
        return Triple(r.getDouble("latitude"), r.getDouble("longitude"), label)
    }

    data class IpLocation(val lat: Double, val lon: Double, val place: String)

    companion object {
        /** Re-fetch when the cache is older than this. */
        const val REFRESH_MS = 30 * 60_000L
        /** After an attempt (either outcome) wait at least this long before trying again. */
        const val RETRY_MS = 5 * 60_000L
        /** Stop showing a forecast that could not be refreshed for this long. */
        const val STALE_MS = 3 * 60 * 60_000L
        const val IP_URL = "https://ipinfo.io/json"

        fun forecastUrl(lat: Double, lon: Double, fahrenheit: Boolean): String =
            "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                "&current=temperature_2m,weather_code" +
                "&hourly=temperature_2m,weather_code,precipitation_probability" +
                "&daily=temperature_2m_max,temperature_2m_min,weather_code,precipitation_probability_max" +
                "&timezone=auto&forecast_days=7&temperature_unit=" + if (fahrenheit) "fahrenheit" else "celsius"

        fun parseIpLocation(body: String): IpLocation {
            val ip = JSONObject(body)
            val loc = ip.getString("loc").split(",")
            return IpLocation(loc[0].toDouble(), loc[1].toDouble(), ip.optString("city", ""))
        }

        /** Open-Meteo forecast JSON to [Weather]; throws on anything missing so a half-broken payload is rejected whole. */
        fun parseForecast(body: String, place: String, fahrenheit: Boolean, nowMs: Long): Weather {
            val o = JSONObject(body)
            val cur = o.getJSONObject("current")
            val daily = o.getJSONObject("daily")
            val hourly = o.getJSONObject("hourly")
            val fmtHour = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
            val fmtDay = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val hours = mutableListOf<HourPoint>()
            val ht = hourly.getJSONArray("time"); val htemp = hourly.getJSONArray("temperature_2m")
            val hcode = hourly.getJSONArray("weather_code"); val hp = hourly.optJSONArray("precipitation_probability")
            for (i in 0 until ht.length()) {
                val t = fmtHour.parse(ht.getString(i))?.time ?: continue
                if (t < nowMs - 3_600_000L) continue            // keep the current hour and onward
                if (hours.size >= 36) break
                hours += HourPoint(t, htemp.getDouble(i), hcode.getInt(i), hp?.optInt(i) ?: 0)
            }
            val days = mutableListOf<DayPoint>()
            val dt = daily.getJSONArray("time"); val dmax = daily.getJSONArray("temperature_2m_max"); val dmin = daily.getJSONArray("temperature_2m_min")
            val dcode = daily.getJSONArray("weather_code"); val dp = daily.optJSONArray("precipitation_probability_max")
            for (i in 0 until dt.length()) {
                val t = fmtDay.parse(dt.getString(i))?.time ?: continue
                days += DayPoint(t, dmax.getDouble(i), dmin.getDouble(i), dcode.getInt(i), dp?.optInt(i) ?: 0)
            }
            if (days.isEmpty()) throw IllegalStateException("forecast has no daily data")
            return Weather(
                temp = cur.getDouble("temperature_2m"),
                high = dmax.getDouble(0),
                low = dmin.getDouble(0),
                code = cur.getInt("weather_code"),
                place = place, fahrenheit = fahrenheit, fetchedAtMs = nowMs,
                hourly = hours, daily = days,
            )
        }
    }
}
