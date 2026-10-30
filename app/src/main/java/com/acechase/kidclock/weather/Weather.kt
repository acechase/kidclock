package com.acechase.kidclock.weather

import org.json.JSONArray
import org.json.JSONObject

data class HourPoint(val timeMs: Long, val temp: Double, val code: Int, val precipPct: Int)
data class DayPoint(val dateMs: Long, val high: Double, val low: Double, val code: Int, val precipPct: Int)

data class Weather(
    val temp: Double,
    val high: Double,
    val low: Double,
    val code: Int,
    val place: String,
    val fahrenheit: Boolean,
    val fetchedAtMs: Long,
    val hourly: List<HourPoint> = emptyList(),
    val daily: List<DayPoint> = emptyList(),
) {
    val tempText get() = "${Math.round(temp)}°"
    val rangeText get() = "H ${Math.round(high)}°  L ${Math.round(low)}°"
    val conditionText get() = describe(code)

    /** WMO weather code to an emoji the Portal's colour emoji font can draw. */
    fun icon(nightSky: Boolean): String = iconFor(code, nightSky)

    fun toJson() = JSONObject().apply {
        put("temp", temp); put("high", high); put("low", low); put("code", code)
        put("place", place); put("f", fahrenheit); put("at", fetchedAtMs)
        put("hourly", JSONArray(hourly.map { JSONObject().apply { put("t", it.timeMs); put("temp", it.temp); put("code", it.code); put("p", it.precipPct) } }))
        put("daily", JSONArray(daily.map { JSONObject().apply { put("t", it.dateMs); put("hi", it.high); put("lo", it.low); put("code", it.code); put("p", it.precipPct) } }))
    }.toString()

    companion object {
        fun fromJson(s: String?): Weather? = runCatching {
            val o = JSONObject(s ?: return null)
            val hourly = o.optJSONArray("hourly")?.let { a -> List(a.length()) { i -> a.getJSONObject(i).let { HourPoint(it.getLong("t"), it.getDouble("temp"), it.getInt("code"), it.optInt("p")) } } } ?: emptyList()
            val daily = o.optJSONArray("daily")?.let { a -> List(a.length()) { i -> a.getJSONObject(i).let { DayPoint(it.getLong("t"), it.getDouble("hi"), it.getDouble("lo"), it.getInt("code"), it.optInt("p")) } } } ?: emptyList()
            Weather(o.getDouble("temp"), o.getDouble("high"), o.getDouble("low"), o.getInt("code"),
                o.optString("place"), o.optBoolean("f", true), o.optLong("at"), hourly, daily)
        }.getOrNull()

        fun iconFor(code: Int, nightSky: Boolean): String = when (code) {
            0 -> if (nightSky) "🌙" else "☀️"
            1 -> if (nightSky) "🌙" else "🌤️"
            2 -> "⛅"
            3 -> "☁️"
            45, 48 -> "🌫️"
            in 51..57 -> "🌦️"
            in 61..67, in 80..82 -> "🌧️"
            in 71..77, 85, 86 -> "🌨️"
            in 95..99 -> "⛈️"
            else -> "🌡️"
        }

        fun describe(code: Int): String = when (code) {
            0 -> "Clear"
            1 -> "Mostly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing drizzle"
            61, 63, 65 -> "Rain"
            66, 67 -> "Freezing rain"
            71, 73, 75, 77 -> "Snow"
            80, 81, 82 -> "Rain showers"
            85, 86 -> "Snow showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with hail"
            else -> ""
        }
    }
}
