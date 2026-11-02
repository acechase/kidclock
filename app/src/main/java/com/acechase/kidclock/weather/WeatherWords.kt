package com.acechase.kidclock.weather

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

/**
 * Turns a [Weather] into the sentences an 11-year-old wants in the morning: how it will feel, what to wear,
 * whether to pack a raincoat, and what the week is doing, in plain words. Pure Kotlin so the rules are
 * unit-tested on the JVM; the thresholds are written in Fahrenheit and converted when the forecast is Celsius.
 */
class WeatherWords(
    private val zone: TimeZone = TimeZone.getDefault(),
    private val locale: Locale = Locale.getDefault(),
    private val use24h: Boolean = false,
) {
    /** One row of "your day": a kid-sized label, the temperature and the sky at that hour. */
    data class Part(val label: String, val temp: Double, val code: Int, val hour: Int)

    /** One column of the week strip. */
    data class Day(val name: String, val high: Double, val code: Int, val word: String, val rainy: Boolean)

    data class Words(
        /** "Friday morning in Redding" */
        val heading: String,
        /** The feel word for right now: "chilly". */
        val nowWord: String,
        /** The sky, now and later: "Cloudy now, then sunny by lunchtime." */
        val skySentence: String,
        /** "It warms up to 67° this afternoon, which is nice." */
        val daySentence: String,
        /** The number in [daySentence] worth colouring, e.g. "67°", or null. */
        val dayHighlight: String?,
        /** "No rain today." or "Rain likely around 2 pm, so bring a raincoat." */
        val rainSentence: String,
        /** "Wear a hoodie or a light jacket. You can take it off later." */
        val dress: String,
        val parts: List<Part>,
        /** "Rain tomorrow, then it gets warmer every day. Wednesday is the warmest." */
        val weekSentence: String,
        val week: List<Day>,
    )

    fun describe(w: Weather, nowMs: Long): Words {
        val cal = Calendar.getInstance(zone, locale).apply { timeInMillis = nowMs }
        val nowHour = cal.get(Calendar.HOUR_OF_DAY)
        val today = cal.get(Calendar.DAY_OF_YEAR)
        val f = w.fahrenheit

        val dayName = SimpleDateFormat("EEEE", locale).apply { timeZone = zone }.format(Date(nowMs))
        val timeOfDay = when { nowHour < 12 -> "morning"; nowHour < 18 -> "afternoon"; else -> "evening" }
        val heading = if (w.place.isBlank()) "$dayName $timeOfDay" else "$dayName $timeOfDay in ${w.place}"

        val todayHours = w.hourly.filter { dayOfYear(it.timeMs) == today && it.timeMs >= nowMs - 3_600_000L }
        val tomorrow = w.daily.getOrNull(1)

        // The sky now, and whether it changes later on.
        val nightSky = nowHour < 6 || nowHour >= 19
        val nowSky = skyWord(w.code, nightSky)
        val laterHours = if (nowHour < 12) todayHours.filter { hourOf(it.timeMs) in 12..17 } else todayHours.filter { hourOf(it.timeMs) in (nowHour + 2)..21 }
        val laterSky = laterHours.groupingBy { skyWord(it.code, hourOf(it.timeMs) >= 19) }.eachCount().maxByOrNull { it.value }?.key
        val skySentence = when {
            nightSky && w.code <= 1 -> "Clear and starry tonight."
            laterSky != null && laterSky != nowSky -> "${nowSky.replaceFirstChar { it.uppercase() }} now, then $laterSky ${if (nowHour < 12) "by lunchtime" else "tonight"}."
            nowHour >= 18 -> "${nowSky.replaceFirstChar { it.uppercase() }} tonight."
            else -> "${nowSky.replaceFirstChar { it.uppercase() }} all day."
        }

        // Sentence 1: where the day is going.
        val nowBand = band(w.temp, f)
        val afterSchool = nowHour >= 15
        val daySentence: String; val dayHighlight: String?
        if (afterSchool) {
            val low = tomorrow?.low ?: w.low
            dayHighlight = deg(low)
            daySentence = "It cools down to $dayHighlight tonight."
        } else if (w.high - w.temp >= degrees(6.0, f)) {
            dayHighlight = deg(w.high)
            daySentence = "It warms up to $dayHighlight this afternoon, which is ${word(band(w.high, f))}."
        } else if (w.temp - w.high >= degrees(6.0, f) || (w.temp - w.low >= degrees(6.0, f) && w.high - w.temp < degrees(3.0, f) && nowHour >= 12)) {
            dayHighlight = deg(w.low)
            daySentence = "It cools down to $dayHighlight tonight."
        } else {
            dayHighlight = deg(w.temp)
            daySentence = "It stays around $dayHighlight all day."
        }

        // Rain: the hours left until 8 pm, else today's daily chance.
        val wet = todayHours.filter { hourOf(it.timeMs) <= 20 }
        val maxP = wet.maxOfOrNull { it.precipPct } ?: w.daily.firstOrNull()?.precipPct ?: 0
        val wettest = wet.maxByOrNull { it.precipPct }
        val firstLikely = wet.firstOrNull { it.precipPct >= 50 }
        val snowy = (firstLikely ?: wettest)?.let { isSnow(it.code) } ?: w.daily.firstOrNull()?.let { isSnow(it.code) } ?: false
        val rainLikely = maxP >= 60
        val rainSentence = when {
            rainLikely && snowy -> firstLikely?.let { "Snow likely around ${hourText(it.timeMs)}, so wear boots and gloves." } ?: "Snow likely today, so wear boots and gloves."
            rainLikely -> firstLikely?.let { "Rain likely around ${hourText(it.timeMs)}, so bring a raincoat." } ?: "Rain likely today, so bring a raincoat."
            maxP >= 30 -> {
                val when_ = wettest?.let { partOfDay(hourOf(it.timeMs)) } ?: "today"
                if (snowy) "It might snow $when_." else "It might rain $when_, so a raincoat wouldn't hurt."
            }
            tomorrow != null && tomorrow.precipPct >= 50 -> "No rain today, but rain comes tomorrow."
            else -> "No rain today."
        }

        // What to wear: judged at 7 am (or now if the morning is under way), with layers if the afternoon is much warmer.
        val sevenAm = todayHours.firstOrNull { hourOf(it.timeMs) == 7 }
        val dressTemp = if (nowHour < 7 && sevenAm != null) sevenAm.temp else w.temp
        val dressBand = band(dressTemp, f)
        val dress = StringBuilder(DRESS[dressBand])
        if (!afterSchool && dressBand <= 3 && band(w.high, f) - dressBand >= 2) dress.append(" You can take it off later.")
        if (rainLikely) dress.append(if (snowy) " Snow boots too." else " And bring a raincoat.")

        // Your day.
        val parts = mutableListOf<Part>()
        if (nowHour >= 7) parts += Part("Now", w.temp, w.code, nowHour)
        for ((label, h) in PARTS) if (h > nowHour) at(todayHours, h)?.let { parts += Part(label, it.temp, it.code, h) }
        if (afterSchool) {
            val tomorrowHours = w.hourly.filter { dayOfYear(it.timeMs) == dayOfYear(nowMs + 86_400_000L) }
            at(tomorrowHours, 7)?.let { parts += Part("Tomorrow morning", it.temp, it.code, 7) }
        }

        // The week.
        val fmt = SimpleDateFormat("EEE", locale).apply { timeZone = zone }
        val fmtLong = SimpleDateFormat("EEEE", locale).apply { timeZone = zone }
        val days = w.daily.drop(1).take(6)
        val week = days.map { Day(fmt.format(Date(it.dateMs)), it.high, it.code, word(band(it.high, f)), isRainy(it)) }
        val weekSentence = if (days.isEmpty()) "" else weekSentence(days, f) { i ->
            if (i == 0) "Tomorrow" else fmtLong.format(Date(days[i].dateMs))
        }

        return Words(heading, word(nowBand), skySentence, daySentence, dayHighlight, rainSentence, dress.toString(), parts, weekSentence, week)
    }

    private fun weekSentence(days: List<DayPoint>, f: Boolean, name: (Int) -> String): String {
        val highs = days.map { it.high }
        val first = highs.take(3).average(); val last = highs.takeLast(3).average()
        val step = degrees(6.0, f)
        val steady = highs.zipWithNext().all { (a, b) -> b >= a - degrees(2.0, f) }
        val falling = highs.zipWithNext().all { (a, b) -> b <= a + degrees(2.0, f) }
        val trend = when {
            last - first >= step -> if (steady) "it gets warmer every day" else "it gets warmer later this week"
            first - last >= step -> if (falling) "it gets colder every day" else "it cools off later this week"
            else -> null
        }
        val rainy = days.indices.filter { isRainy(days[it]) }
        val snow = rainy.isNotEmpty() && rainy.all { isSnow(days[it].code) }
        val wet = if (snow) "Snow" else "Rain"
        val rainClause = when {
            rainy.isEmpty() -> "No rain all week"
            rainy.size >= 4 -> if (snow) "Snowy most of the week" else "Rainy most of the week"
            rainy[0] == 0 -> "$wet tomorrow"
            rainy.size >= 2 && rainy[1] == rainy[0] + 1 -> "$wet starts ${name(rainy[0])}"
            else -> "$wet on ${name(rainy[0])}"
        }
        val s1 = when {
            trend == null && rainy.isEmpty() -> "No rain all week, and it stays around ${deg(highs.average())} every day."
            trend == null -> "$rainClause, otherwise about the same all week."
            rainy.isEmpty() -> "No rain all week, and $trend."
            else -> "$rainClause, then $trend."
        }
        val spread = highs.max() - highs.min()
        val s2 = when {
            spread < step -> null
            first - last >= step -> "${name(highs.indexOf(highs.min()))} is the coldest."
            else -> "${name(highs.indexOf(highs.max()))} is the warmest."
        }
        return if (s2 == null) s1 else "$s1 $s2"
    }

    private fun at(hours: List<HourPoint>, h: Int): HourPoint? = hours.firstOrNull { hourOf(it.timeMs) == h }
    private fun hourOf(ms: Long) = Calendar.getInstance(zone, locale).apply { timeInMillis = ms }.get(Calendar.HOUR_OF_DAY)
    private fun dayOfYear(ms: Long) = Calendar.getInstance(zone, locale).apply { timeInMillis = ms }.get(Calendar.DAY_OF_YEAR)
    private fun hourText(ms: Long): String =
        SimpleDateFormat(if (use24h) "HH:mm" else "h a", locale).apply { timeZone = zone }.format(Date(ms)).lowercase(locale)
    private fun partOfDay(h: Int) = when { h < 12 -> "this morning"; h < 17 -> "this afternoon"; else -> "tonight" }

    companion object {
        /** Feel words by band; the bands are ≤32, 33–45, 46–55, 56–64, 65–77, 78–87, ≥88 °F. */
        val WORDS = listOf("freezing", "cold", "chilly", "cool", "nice", "warm", "hot")
        val DRESS = listOf(
            "Wear a big coat, a hat and gloves.",
            "Wear a warm coat.",
            "Wear a hoodie or a light jacket.",
            "Wear long sleeves.",
            "T-shirt weather.",
            "Shorts and a t-shirt.",
            "Shorts and a t-shirt, and bring water.",
        )
        private val PARTS = listOf("Leaving for school" to 7, "Lunch recess" to 12, "After school" to 15, "Bedtime" to 20)
        private val BAND_TOPS_F = doubleArrayOf(32.0, 45.0, 55.0, 64.0, 77.0, 87.0)

        fun band(temp: Double, fahrenheit: Boolean): Int {
            val tf = if (fahrenheit) temp else temp * 9 / 5 + 32
            return BAND_TOPS_F.indexOfFirst { tf <= it + 0.5 }.let { if (it < 0) 6 else it }
        }
        fun word(band: Int) = WORDS[band]
        fun deg(t: Double) = "${t.roundToInt()}°"
        /** A Fahrenheit difference in the forecast's unit. */
        fun degrees(f: Double, fahrenheit: Boolean) = if (fahrenheit) f else f * 5 / 9
        fun isSnow(code: Int) = code in 71..77 || code == 85 || code == 86
        /** WMO code to a sky word a kid would use. */
        fun skyWord(code: Int, nightSky: Boolean): String = when (code) {
            0, 1 -> if (nightSky) "clear" else "sunny"
            2 -> "partly cloudy"
            3 -> "cloudy"
            45, 48 -> "foggy"
            in 51..57 -> "drizzly"
            in 61..67, in 80..82 -> "rainy"
            in 95..99 -> "stormy"
            else -> if (isSnow(code)) "snowy" else "cloudy"
        }
        fun isWet(code: Int) = code in 51..67 || code in 80..82 || code in 95..99 || isSnow(code)
        fun isRainy(d: DayPoint) = d.precipPct >= 50 || isWet(d.code)
    }
}
