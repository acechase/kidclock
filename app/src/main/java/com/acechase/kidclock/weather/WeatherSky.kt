package com.acechase.kidclock.weather

/**
 * The weather page dresses for the sky right now: a painted background ([SkyView]) plus the colours the text and
 * cards use on it. Chosen from the current WMO weather code; pure so the choice is unit-tested.
 */
enum class WeatherSky(
    /** Top-to-bottom background gradient. */
    val gradient: IntArray,
    /** Body text. */
    val ink: Int,
    /** Card fill over the sky (translucent). */
    val card: Int,
    /** Labels, the heading, the sky sentence. */
    val muted: Int,
    /** The feel word next to the temperature, and rainy days in the week strip. */
    val accent: Int,
    /** The highlighted temperature and warm days. */
    val warm: Int,
) {
    SUNNY(intArrayOf(0xFF2E8BE6.toInt(), 0xFF5DB4F5.toInt(), 0xFFFFD98A.toInt()), 0xFFFFFFFF.toInt(), 0x4D0A1E3C, 0xFFE6F2FF.toInt(), 0xFFFFF3B0.toInt(), 0xFFFFD166.toInt()),
    CLOUDY(intArrayOf(0xFF5B6B82.toInt(), 0xFF8793A6.toInt()), 0xFFFFFFFF.toInt(), 0x52141C28, 0xFFE2E7EF.toInt(), 0xFFDCE7F5.toInt(), 0xFFFFC978.toInt()),
    RAINY(intArrayOf(0xFF27364D.toInt(), 0xFF3E5270.toInt()), 0xFFFFFFFF.toInt(), 0x610A121E, 0xFFC9D6E8.toInt(), 0xFF9BD1FF.toInt(), 0xFFFFB74D.toInt()),
    SNOWY(intArrayOf(0xFFA9C4DD.toInt(), 0xFFDDE9F3.toInt()), 0xFF16263A.toInt(), 0x80FFFFFF.toInt(), 0xFF3F5570.toInt(), 0xFF1F5FA8.toInt(), 0xFFB4541A.toInt()),
    STORMY(intArrayOf(0xFF1E1B2E.toInt(), 0xFF3A3550.toInt()), 0xFFFFFFFF.toInt(), 0x5C000000, 0xFFCFC9E6.toInt(), 0xFFD9C9FF.toInt(), 0xFFFFB74D.toInt()),
    NIGHT(intArrayOf(0xFF0B1636.toInt(), 0xFF1B2B5C.toInt()), 0xFFFFFFFF.toInt(), 0x17FFFFFF, 0xFFB8C4E6.toInt(), 0xFFCFE0FF.toInt(), 0xFFFFD166.toInt());

    companion object {
        /** Clear → Sunny (Night after dark); cloud, fog → Cloudy; drizzle, rain, showers → Rainy; snow → Snowy; thunder → Stormy. */
        fun of(code: Int, nightSky: Boolean): WeatherSky = when (code) {
            0, 1 -> if (nightSky) NIGHT else SUNNY
            in 95..99 -> STORMY
            in 51..67, in 80..82 -> RAINY
            else -> if (WeatherWords.isSnow(code)) SNOWY else CLOUDY
        }
    }
}
