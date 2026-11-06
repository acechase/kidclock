package com.acechase.kidclock.night

/**
 * The arithmetic behind the gentle wake-up sunrise: how far along the sunrise is, how bright the
 * screen should be at that point, and the colour keyframes the scene is painted from. Pure and
 * unit-tested; the painting itself is faces/SunriseScene.
 */
object Sunrise {
    /** Length assumed for a wake-up whose start was never recorded (installs from before 0.6). */
    const val DEFAULT_LENGTH_MS = 20 * 60_000L

    /**
     * 0 at the start of the gentle wake-up, 1 at the alarm's time, clamped. A missing or nonsensical
     * start (0, or not before the end) is taken as [DEFAULT_LENGTH_MS] before the end.
     */
    fun progress(nowMs: Long, startMs: Long, endMs: Long): Float {
        val start = if (startMs in 1 until endMs) startMs else endMs - DEFAULT_LENGTH_MS
        if (nowMs <= start) return 0f
        if (nowMs >= endMs) return 1f
        return (nowMs - start).toFloat() / (endMs - start).toFloat()
    }

    /**
     * Screen brightness for a sunrise at [progress]: a dim pre-dawn that grows slowly at first and
     * quickly at the end, the way the room lightens before a real sunrise. Full brightness at 1.
     */
    fun brightness(progress: Float): Float {
        val p = progress.coerceIn(0f, 1f)
        return MIN_BRIGHTNESS + (1f - MIN_BRIGHTNESS) * p * p
    }
    const val MIN_BRIGHTNESS = 0.15f

    /** Mix two ARGB colours; [t] is 0 for [a], 1 for [b]. */
    fun lerpColor(a: Int, b: Int, t: Float): Int {
        val k = t.coerceIn(0f, 1f)
        fun ch(shift: Int): Int {
            val x = (a ushr shift) and 0xFF
            val y = (b ushr shift) and 0xFF
            return (x + (y - x) * k + 0.5f).toInt().coerceIn(0, 255)
        }
        return (ch(24) shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
    }

    /** Colour at [progress] along a ramp of [colors] placed at [KEYS]. */
    fun keyColor(colors: IntArray, progress: Float): Int {
        val p = progress.coerceIn(0f, 1f)
        for (i in 1 until KEYS.size) {
            if (p <= KEYS[i]) return lerpColor(colors[i - 1], colors[i], (p - KEYS[i - 1]) / (KEYS[i] - KEYS[i - 1]))
        }
        return colors.last()
    }

    /** Same for a ramp of plain values (glow strength and the like). */
    fun keyValue(values: FloatArray, progress: Float): Float {
        val p = progress.coerceIn(0f, 1f)
        for (i in 1 until KEYS.size) {
            if (p <= KEYS[i]) {
                val t = (p - KEYS[i - 1]) / (KEYS[i] - KEYS[i - 1])
                return values[i - 1] + (values[i] - values[i - 1]) * t
            }
        }
        return values.last()
    }

    /** Where the keyframes sit: deep night, first light, the sun on the horizon, climbing, full morning. */
    val KEYS = floatArrayOf(0f, 0.3f, 0.55f, 0.8f, 1f)

    val SKY_TOP = intArrayOf(0xFF060C24.toInt(), 0xFF1A2050.toInt(), 0xFF3A4C96.toInt(), 0xFF4A95DC.toInt(), 0xFF3F8FE0.toInt())
    val SKY_MID = intArrayOf(0xFF101A3C.toInt(), 0xFF4A3A78.toInt(), 0xFFB86E86.toInt(), 0xFF8EC4F2.toInt(), 0xFF78BDF5.toInt())
    val SKY_LOW = intArrayOf(0xFF1C2244.toInt(), 0xFFB85A48.toInt(), 0xFFFFB253.toInt(), 0xFFFFD9A3.toInt(), 0xFFC9E8FF.toInt())
    val GLOW = intArrayOf(0xFF3A2A50.toInt(), 0xFFFF7F50.toInt(), 0xFFFFC46B.toInt(), 0xFFFFE6B8.toInt(), 0xFFFFFFFF.toInt())
    val GLOW_STRENGTH = floatArrayOf(0.15f, 0.55f, 0.70f, 0.40f, 0.18f)
    val HILL_FAR = intArrayOf(0xFF0B1230.toInt(), 0xFF2A2650.toInt(), 0xFF5B5A80.toInt(), 0xFF4E8A5E.toInt(), 0xFF62A862.toInt())
    val HILL_NEAR = intArrayOf(0xFF06091C.toInt(), 0xFF171735.toInt(), 0xFF2D3550.toInt(), 0xFF2F6D3E.toInt(), 0xFF3E8C4A.toInt())
    val CLOUD = intArrayOf(0xFF1B2448.toInt(), 0xFF6D4A72.toInt(), 0xFFFFB39B.toInt(), 0xFFFFE9DA.toInt(), 0xFFF6F9FD.toInt())
    val SUN = intArrayOf(0xFFFF6A3D.toInt(), 0xFFFF7A3C.toInt(), 0xFFFFA43C.toInt(), 0xFFFFD36B.toInt(), 0xFFFFE88A.toInt())

    /** How high the sun has climbed, 0 (behind the hills) to 1 (up in the sky): slow to show, then steady. */
    fun sunHeight(progress: Float): Float {
        val p = progress.coerceIn(0f, 1f)
        return Math.pow(p.toDouble(), 1.6).toFloat()
    }

    /** Stars are gone by first light. */
    fun starAlpha(progress: Float): Float = (1f - progress / 0.45f).coerceIn(0f, 1f)

    /** Birds come out once the sun is well up. */
    fun birdAlpha(progress: Float): Float = ((progress - 0.72f) / 0.18f).coerceIn(0f, 1f)
}
