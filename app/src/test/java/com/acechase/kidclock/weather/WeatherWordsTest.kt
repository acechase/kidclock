package com.acechase.kidclock.weather

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * The weather page speaks to an 11-year-old: what to wear, whether it will rain, and what the week is doing.
 * These tests pin the wording rules in [WeatherWords] against hand-built forecasts in a fixed time zone.
 */
class WeatherWordsTest {
    private val zone = TimeZone.getTimeZone("America/Los_Angeles")
    private val words = WeatherWords(zone, Locale.US)

    /** Friday 2026-11-06 at [hour]:20 local. */
    private fun at(hour: Int, minute: Int = 20, dayOffset: Int = 0): Long =
        Calendar.getInstance(zone, Locale.US).apply {
            clear(); set(2026, Calendar.NOVEMBER, 6 + dayOffset, hour, minute)
        }.timeInMillis

    /** Hourly points for today from [fromHour] to 23, plus tomorrow 0..23, with a bell-shaped day. */
    private fun hours(fromHour: Int, morning: Double, peak: Double, code: Int = 3, rain: (Int) -> Int = { 0 }): List<HourPoint> {
        val out = mutableListOf<HourPoint>()
        for (day in 0..1) for (h in (if (day == 0) fromHour else 0)..23) {
            val t = if (h <= 14) morning + (peak - morning) * (h / 14.0) else peak - (peak - morning) * ((h - 14) / 9.0)
            out += HourPoint(at(h, 0, day), Math.round(t).toDouble(), code, if (day == 0) rain(h) else 0)
        }
        return out
    }

    private fun days(vararg highs: Double, rain: (Int) -> Int = { 0 }, code: (Int) -> Int = { 3 }) =
        highs.mapIndexed { i, hi -> DayPoint(at(0, 0, i), hi, hi - 20, code(i), rain(i)) }

    private fun weather(
        temp: Double = 48.0, high: Double = 67.0, low: Double = 44.0, code: Int = 3, fahrenheit: Boolean = true,
        hourly: List<HourPoint> = hours(0, 44.0, 67.0),
        daily: List<DayPoint> = days(67.0, 61.0, 64.0, 69.0, 72.0, 74.0, 73.0, rain = { if (it == 1) 73 else 10 }),
    ) = Weather(temp, high, low, code, "Redding", fahrenheit, at(7), hourly, daily)

    @Test
    fun theSampleMorningReadsLikeTheDesign() {
        val w = words.describe(weather(), at(7))
        assertEquals("Friday morning in Redding", w.heading)
        assertEquals("chilly", w.nowWord)
        assertEquals("Cloudy all day.", w.skySentence)
        assertEquals("It warms up to 67° this afternoon, which is nice.", w.daySentence)
        assertEquals("67°", w.dayHighlight)
        assertEquals("No rain today, but rain comes tomorrow.", w.rainSentence)
        assertEquals("Wear a hoodie or a light jacket. You can take it off later.", w.dress)
        assertEquals(listOf("Now", "Lunch recess", "After school", "Bedtime"), w.parts.map { it.label })
        assertEquals("Rain tomorrow, then it gets warmer every day. Wednesday is the warmest.", w.weekSentence)
        assertEquals(listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu"), w.week.map { it.name })
        assertTrue(w.week[0].rainy); assertFalse(w.week[1].rainy)
        assertEquals("nice", w.week[4].word)   // 74° is still "nice"; warm starts at 78°
    }

    @Test
    fun temperatureBandsInFahrenheitAndCelsius() {
        val f = listOf(20.0 to "freezing", 40.0 to "cold", 50.0 to "chilly", 60.0 to "cool", 70.0 to "nice", 80.0 to "warm", 95.0 to "hot")
        for ((t, word) in f) assertEquals("$t°F", word, WeatherWords.word(WeatherWords.band(t, true)))
        assertEquals("freezing", WeatherWords.word(WeatherWords.band(0.0, false)))
        assertEquals("chilly", WeatherWords.word(WeatherWords.band(10.0, false)))
        assertEquals("nice", WeatherWords.word(WeatherWords.band(22.0, false)))
        assertEquals("hot", WeatherWords.word(WeatherWords.band(33.0, false)))
        // Boundaries: 32 is still freezing, 33 is cold; 45/46, 55/56, 64/65, 77/78, 87/88.
        assertEquals(0, WeatherWords.band(32.0, true)); assertEquals(1, WeatherWords.band(33.0, true))
        assertEquals(4, WeatherWords.band(77.0, true)); assertEquals(5, WeatherWords.band(78.0, true))
    }

    @Test
    fun dressFollowsTheMorningTemperatureAndAddsLayersOnlyWhenTheAfternoonIsMuchWarmer() {
        assertEquals("Wear a big coat, a hat and gloves.", words.describe(weather(temp = 25.0, high = 30.0), at(7)).dress)
        assertEquals("Wear a warm coat. You can take it off later.", words.describe(weather(temp = 40.0, high = 62.0), at(7)).dress)
        assertEquals("Wear long sleeves.", words.describe(weather(temp = 60.0, high = 66.0), at(7)).dress)
        assertEquals("T-shirt weather.", words.describe(weather(temp = 70.0, high = 85.0), at(7)).dress)
        assertEquals("Shorts and a t-shirt, and bring water.", words.describe(weather(temp = 90.0, high = 102.0), at(9)).dress)
    }

    @Test
    fun beforeSevenTheDressLineUsesTheSevenAmForecastNotTheCurrentTemperature() {
        // 5 am and 38° now, but 7 am will be 50°: a hoodie, not a coat.
        val hourly = hours(0, 44.0, 67.0).map { if (it.timeMs == at(7, 0)) it.copy(temp = 50.0) else it }
        val w = words.describe(weather(temp = 38.0, hourly = hourly), at(5))
        assertTrue(w.dress, w.dress.startsWith("Wear a hoodie"))
        assertEquals(listOf("Leaving for school", "Lunch recess", "After school", "Bedtime"), w.parts.map { it.label })
    }

    @Test
    fun rainLikelyNamesTheHourAndAddsARaincoatToTheDressLine() {
        val hourly = hours(0, 50.0, 60.0, code = 61, rain = { h -> if (h >= 14) 75 else 10 })
        val w = words.describe(weather(temp = 50.0, high = 60.0, hourly = hourly), at(7))
        assertEquals("Rain likely around 2 pm, so bring a raincoat.", w.rainSentence)
        assertEquals("Wear a hoodie or a light jacket. And bring a raincoat.", w.dress)
    }

    @Test
    fun rainLikelyInTwentyFourHourTime() {
        val hourly = hours(0, 50.0, 60.0, code = 61, rain = { h -> if (h >= 14) 75 else 10 })
        val w = WeatherWords(zone, Locale.US, use24h = true).describe(weather(temp = 50.0, high = 60.0, hourly = hourly), at(7))
        assertEquals("Rain likely around 14:00, so bring a raincoat.", w.rainSentence)
    }

    @Test
    fun aMaybeRainIsHedgedAndNamedByPartOfDay() {
        val hourly = hours(0, 50.0, 60.0, rain = { h -> if (h == 16) 45 else 5 })
        assertEquals("It might rain this afternoon, so a raincoat wouldn't hurt.", words.describe(weather(hourly = hourly), at(7)).rainSentence)
        val morning = hours(0, 50.0, 60.0, rain = { h -> if (h == 9) 40 else 5 })
        assertEquals("It might rain this morning, so a raincoat wouldn't hurt.", words.describe(weather(hourly = morning), at(7)).rainSentence)
    }

    @Test
    fun rainAfterBedtimeDoesNotCount() {
        val hourly = hours(0, 50.0, 60.0, rain = { h -> if (h >= 22) 90 else 0 })
        val w = words.describe(weather(hourly = hourly, daily = days(60.0, 62.0, 63.0)), at(7))
        assertEquals("No rain today.", w.rainSentence)
    }

    @Test
    fun snowGetsItsOwnWords() {
        val hourly = hours(0, 28.0, 33.0, code = 71, rain = { h -> if (h >= 8) 80 else 20 })
        val w = words.describe(weather(temp = 28.0, high = 33.0, low = 20.0, code = 71, hourly = hourly), at(7))
        assertEquals("Snow likely around 8 am, so wear boots and gloves.", w.rainSentence)
        assertEquals("Wear a big coat, a hat and gloves. Snow boots too.", w.dress)
    }

    @Test
    fun withoutHourlyDataTheDailyChanceIsUsed() {
        val w = words.describe(weather(hourly = emptyList(), daily = days(67.0, 61.0, rain = { if (it == 0) 70 else 0 })), at(7))
        assertEquals("Rain likely today, so bring a raincoat.", w.rainSentence)
        assertEquals(listOf("Now"), w.parts.map { it.label })
    }

    @Test
    fun aFlatDaySaysSo() {
        val w = words.describe(weather(temp = 62.0, high = 65.0, low = 58.0), at(8))
        assertEquals("It stays around 62° all day.", w.daySentence)
        assertEquals("62°", w.dayHighlight)
    }

    @Test
    fun afterSchoolTheDayLooksAheadToTonightAndTomorrowMorning() {
        val w = words.describe(weather(temp = 66.0), at(16))
        assertEquals("Friday afternoon in Redding", w.heading)
        assertEquals("It cools down to 41° tonight.", w.daySentence)          // tomorrow's low: 61 - 20
        assertEquals(listOf("Now", "Bedtime", "Tomorrow morning"), w.parts.map { it.label })
        assertEquals(7, w.parts.last().hour)
        // Layers are only advice for the morning; the current temperature decides what to wear now.
        assertEquals("T-shirt weather.", w.dress)
        val late = words.describe(weather(temp = 55.0), at(21))
        assertEquals("Friday evening in Redding", late.heading)
        assertEquals(listOf("Now", "Tomorrow morning"), late.parts.map { it.label })
    }

    @Test
    fun weekTrendCoolingAndSteady() {
        val cooling = words.describe(weather(daily = days(70.0, 68.0, 64.0, 60.0, 55.0, 52.0, 50.0)), at(7))
        assertEquals("No rain all week, and it gets colder every day. Thursday is the coldest.", cooling.weekSentence)
        val steady = words.describe(weather(daily = days(66.0, 65.0, 67.0, 66.0, 65.0, 66.0, 67.0)), at(7))
        assertEquals("No rain all week, and it stays around 66° every day.", steady.weekSentence)
        val bumpy = words.describe(weather(daily = days(60.0, 60.0, 70.0, 58.0, 62.0, 74.0, 75.0)), at(7))
        assertEquals("No rain all week, and it gets warmer later this week. Thursday is the warmest.", bumpy.weekSentence)
    }

    @Test
    fun weekRainClauses() {
        val wed = words.describe(weather(daily = days(66.0, 65.0, 67.0, 66.0, 65.0, 66.0, 67.0, rain = { if (it == 5) 80 else 0 })), at(7))
        assertEquals("Rain on Wednesday, otherwise about the same all week.", wed.weekSentence)
        val starts = words.describe(weather(daily = days(66.0, 65.0, 67.0, 66.0, 65.0, 66.0, 67.0, rain = { if (it >= 5) 80 else 0 })), at(7))
        assertEquals("Rain starts Wednesday, otherwise about the same all week.", starts.weekSentence)
        val most = words.describe(weather(daily = days(66.0, 65.0, 67.0, 66.0, 65.0, 66.0, 67.0, rain = { if (it in 1..5) 80 else 0 })), at(7))
        assertTrue(most.weekSentence, most.weekSentence.startsWith("Rainy most of the week"))
        // A rainy weather code counts even when the percentage is low.
        val byCode = words.describe(weather(daily = days(66.0, 65.0, 67.0, 66.0, 65.0, 66.0, 67.0, code = { if (it == 1) 61 else 3 })), at(7))
        assertTrue(byCode.weekSentence, byCode.weekSentence.startsWith("Rain tomorrow"))
    }

    @Test
    fun aSnowyWeekSaysSnow() {
        val w = words.describe(weather(daily = days(30.0, 31.0, 33.0, 36.0, 42.0, 44.0, 46.0, code = { if (it == 1) 71 else 3 })), at(7))
        assertEquals("Snow tomorrow, then it gets warmer every day. Thursday is the warmest.", w.weekSentence)
        val most = words.describe(weather(daily = days(30.0, 31.0, 30.0, 32.0, 31.0, 30.0, 31.0, code = { if (it in 1..4) 73 else 3 })), at(7))
        assertTrue(most.weekSentence, most.weekSentence.startsWith("Snowy most of the week"))
    }

    @Test
    fun theSkyPicksTheTheme() {
        assertEquals(WeatherSky.SUNNY, WeatherSky.of(0, false)); assertEquals(WeatherSky.NIGHT, WeatherSky.of(1, true))
        assertEquals(WeatherSky.CLOUDY, WeatherSky.of(2, false)); assertEquals(WeatherSky.CLOUDY, WeatherSky.of(3, true))
        assertEquals(WeatherSky.CLOUDY, WeatherSky.of(45, false)); assertEquals(WeatherSky.RAINY, WeatherSky.of(53, false))
        assertEquals(WeatherSky.RAINY, WeatherSky.of(61, true)); assertEquals(WeatherSky.RAINY, WeatherSky.of(81, false))
        assertEquals(WeatherSky.SNOWY, WeatherSky.of(71, false)); assertEquals(WeatherSky.SNOWY, WeatherSky.of(86, false))
        assertEquals(WeatherSky.STORMY, WeatherSky.of(95, false)); assertEquals(WeatherSky.STORMY, WeatherSky.of(99, true))
    }

    @Test
    fun aShortForecastStillMakesASentence() {
        val w = words.describe(weather(daily = days(67.0, 61.0, 64.0, rain = { if (it == 1) 73 else 0 })), at(7))
        assertEquals(2, w.week.size)
        assertEquals("Rain tomorrow, otherwise about the same all week.", w.weekSentence)
        val only = words.describe(weather(daily = days(67.0)), at(7))
        assertEquals("", only.weekSentence)
        assertTrue(only.week.isEmpty())
    }

    @Test
    fun celsiusThresholdsScale() {
        // 9 °C now, 19 °C high: chilly morning, nice afternoon, a 10 °C rise means layers.
        val hourly = hours(0, 8.0, 19.0)
        val w = words.describe(weather(temp = 9.0, high = 19.0, low = 7.0, fahrenheit = false, hourly = hourly,
            daily = days(19.0, 16.0, 17.0, 20.0, 22.0, 24.0, 23.0)), at(7))
        assertEquals("chilly", w.nowWord)
        assertEquals("It warms up to 19° this afternoon, which is nice.", w.daySentence)
        assertEquals("Wear a hoodie or a light jacket. You can take it off later.", w.dress)
        assertEquals("No rain all week, and it gets warmer every day. Wednesday is the warmest.", w.weekSentence)
    }

    @Test
    fun theSkySentenceSaysWhenTheSkyChanges() {
        val clearing = hours(0, 44.0, 67.0).map { if (hourOf(it.timeMs) >= 11 && it.timeMs < at(0, 0, 1)) it.copy(code = 0) else it }
        assertEquals("Cloudy now, then sunny by lunchtime.", words.describe(weather(hourly = clearing), at(7)).skySentence)
        val rainLater = hours(0, 44.0, 67.0).map { if (hourOf(it.timeMs) >= 17 && it.timeMs < at(0, 0, 1)) it.copy(code = 61) else it }
        assertEquals("Cloudy now, then rainy tonight.", words.describe(weather(hourly = rainLater), at(14)).skySentence)
        assertEquals("Clear and starry tonight.", words.describe(weather(code = 0), at(21)).skySentence)
        assertEquals("Snowy all day.", words.describe(weather(code = 71, hourly = hours(0, 28.0, 33.0, code = 71)), at(7)).skySentence)
        assertEquals("Stormy tonight.", words.describe(weather(code = 95, hourly = hours(0, 60.0, 70.0, code = 95)), at(19)).skySentence)
    }

    private fun hourOf(ms: Long) = Calendar.getInstance(zone, Locale.US).apply { timeInMillis = ms }.get(Calendar.HOUR_OF_DAY)

    @Test
    fun noPlaceMeansAPlainHeading() {
        val w = words.describe(weather().copy(place = ""), at(7))
        assertEquals("Friday morning", w.heading)
    }
}
