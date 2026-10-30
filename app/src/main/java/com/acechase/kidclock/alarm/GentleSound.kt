package com.acechase.kidclock.alarm

import com.acechase.kidclock.alarm.AlarmSoundPlayer.Companion.SAMPLE_RATE
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * The gentle wake-up sounds. Each one is synthesized on demand into a short mono clip that ends in
 * silence so it can be looped a couple of times by [AlarmSoundPlayer]; nothing is stored as an asset.
 * The clips are deliberately soft and sparse: this plays before the real alarm, not instead of it.
 */
enum class GentleSound(override val id: String, override val title: String, override val blurb: String) : SoundChoice {
    HARP("harp", "Harp", "A quick glissando rising through two octaves, then a pause."),
    MUSIC_BOX("musicbox", "Music box", "A slow, glassy lullaby phrase with long ring-out, pitched low and warm."),
    WIND_CHIMES("chimes", "Wind chimes", "Tuned tubes stirred by a breeze, a little different every time."),
    DAWN_CHORUS("birds", "Dawn chorus", "Soft birdsong over a faint warm hum, like a window left open."),
    KALIMBA("kalimba", "Kalimba", "Warm thumb-piano plucks in a calm repeating pattern.");

    fun synthesize(): ShortArray = when (this) {
        HARP -> harp()
        MUSIC_BOX -> musicBox()
        WIND_CHIMES -> windChimes()
        DAWN_CHORUS -> dawnChorus()
        KALIMBA -> kalimba()
    }

    companion object {
        val DEFAULT = HARP
        fun byId(id: String?): GentleSound = values().firstOrNull { it.id == id } ?: DEFAULT

        // ---- the sounds -----------------------------------------------------------------------

        /** Plucked harp: harmonics that die faster than the fundamental, C pentatonic over two octaves. */
        private fun harp(): ShortArray {
            val freqs = doubleArrayOf(261.63, 293.66, 329.63, 392.00, 440.00, 523.25, 587.33, 659.25, 783.99, 880.00, 1046.50, 1174.66)
            val spacing = 0.11
            val noteDur = 2.4
            val mix = buffer(spacing * freqs.size + noteDur + 2.5)
            val partials = (1..6).map { h -> Triple(h.toDouble(), 1.0 / (h * h), 1.6 + 1.1 * h) }
            for ((k, f) in freqs.withIndex()) strike(mix, k * spacing, f, noteDur, 1.0, partials)
            return toPcm(mix)
        }

        /**
         * Music box: a tine has a pure, glassy tone with a faint high "tink" partial and a very long
         * decay. High register, slow and sparse, with rests so it feels patient.
         */
        private fun musicBox(): ShortArray {
            // (frequency, beat offset) in an original G-major phrase; 0 = rest handled by gaps.
            // One octave below a toy music box: the research on children favours lower-pitched alarms.
            val g5 = 392.00; val a5 = 440.00; val b5 = 493.88; val d6 = 587.33; val e6 = 659.25; val g6 = 783.99; val e5 = 329.63
            val notes = listOf(
                g5 to 0.0, b5 to 0.5, d6 to 1.0, b5 to 1.5,
                a5 to 2.25, g5 to 2.75, e5 to 3.25,
                g5 to 4.25, d6 to 4.75, e6 to 5.25, d6 to 5.75, b5 to 6.25,
                a5 to 7.0, g6 to 7.5, g5 to 8.25,
            )
            val mix = buffer(8.25 + 3.5 + 2.5)
            val partials = listOf(Triple(1.0, 1.0, 1.1), Triple(2.0, 0.18, 2.4), Triple(5.43, 0.06, 7.0))
            for ((f, beat) in notes) strike(mix, beat * 0.62, f, 3.5, if (f > 600) 0.8 else 1.0, partials, attackSamples = 60)
            return toPcm(mix)
        }

        /**
         * Wind chimes: six aluminium tubes in A pentatonic, inharmonic partials, struck in little gusts
         * at random times and strengths over a quiet breeze of filtered noise. Seeded by the clock so
         * each morning's pattern differs.
         */
        private fun windChimes(): ShortArray {
            val rnd = Random(System.currentTimeMillis())
            val tubes = doubleArrayOf(880.00, 987.77, 1108.73, 1318.51, 1479.98, 1760.00)
            val length = 11.0
            val mix = buffer(length)
            val partials = listOf(Triple(1.0, 1.0, 0.9), Triple(2.76, 0.35, 2.2), Triple(5.40, 0.12, 4.5))
            var t = 0.2
            while (t < length - 4.0) {
                val gust = 2 + rnd.nextInt(4)
                var tt = t
                repeat(gust) {
                    val f = tubes[rnd.nextInt(tubes.size)]
                    strike(mix, tt, f, 4.0, 0.35 + rnd.nextDouble() * 0.65, partials, attackSamples = 40)
                    tt += 0.08 + rnd.nextDouble() * 0.35
                }
                t = tt + 0.8 + rnd.nextDouble() * 1.8
            }
            val breeze = softNoise(mix.size, rnd, 0.015)
            for (i in mix.indices) {
                val s = i.toDouble() / SAMPLE_RATE
                val swell = 0.5 + 0.5 * sin(2 * PI * 0.12 * s + 1.0)
                val fade = if (s > length - 2.0) (length - s) / 2.0 else 1.0
                mix[i] += breeze[i] * 0.25 * swell * fade
            }
            return toPcm(mix)
        }

        /**
         * Dawn chorus: three "birds" with different pitch ranges sing short chirp trains (fast sine
         * sweeps) at random moments over a faint warm pad, so it sounds like outside rather than a device.
         */
        private fun dawnChorus(): ShortArray {
            val rnd = Random(System.currentTimeMillis())
            val length = 12.0
            val mix = buffer(length)
            fun chirp(startSec: Double, f0: Double, f1: Double, dur: Double, amp: Double) {
                val start = (startSec * SAMPLE_RATE).toInt()
                val n = (dur * SAMPLE_RATE).toInt()
                var phase = 0.0
                for (i in 0 until n) {
                    val idx = start + i
                    if (idx >= mix.size) break
                    val u = i.toDouble() / n
                    val f = f0 + (f1 - f0) * u
                    phase += 2 * PI * f / SAMPLE_RATE
                    val env = sin(PI * u).let { it * it }
                    mix[idx] += sin(phase) * env * amp
                }
            }
            data class Bird(val lo: Double, val hi: Double, val amp: Double)
            val birds = listOf(Bird(2200.0, 3400.0, 0.55), Bird(3000.0, 4600.0, 0.4), Bird(1700.0, 2600.0, 0.5))
            for ((bi, b) in birds.withIndex()) {
                var t = rnd.nextDouble() * (if (bi == 0) 0.3 else 1.5)   // the first bird sings right away
                while (t < length - 1.5) {
                    val chirps = 2 + rnd.nextInt(5)
                    val base = b.lo + rnd.nextDouble() * (b.hi - b.lo) * 0.5
                    val span = (b.hi - b.lo) * (0.2 + rnd.nextDouble() * 0.5) * (if (rnd.nextBoolean()) 1 else -1)
                    var tt = t
                    repeat(chirps) {
                        val dur = 0.05 + rnd.nextDouble() * 0.1
                        chirp(tt, base, base + span, dur, b.amp * (0.6 + rnd.nextDouble() * 0.4))
                        tt += dur + 0.04 + rnd.nextDouble() * 0.16
                    }
                    t = tt + 0.6 + rnd.nextDouble() * 2.2
                }
            }
            // Warm pad: a low C major triad, very quiet, slow tremolo, faded at both ends for a clean loop.
            val pad = doubleArrayOf(130.81, 196.00, 329.63)
            for (i in mix.indices) {
                val s = i.toDouble() / SAMPLE_RATE
                val edge = minOf(1.0, s / 1.5, (length - s) / 1.5).coerceAtLeast(0.0)
                val trem = 0.85 + 0.15 * sin(2 * PI * 0.2 * s)
                var v = 0.0
                for (f in pad) v += sin(2 * PI * f * s)
                mix[i] += v * 0.06 * trem * edge
            }
            return toPcm(mix)
        }

        /**
         * Kalimba: tines ring with a strong fundamental, a bright inharmonic partial that dies quickly,
         * and a tiny wooden knock at the attack. An F pentatonic pattern played twice, then rest.
         */
        private fun kalimba(): ShortArray {
            val rnd = Random(7)
            val f4 = 349.23; val g4 = 392.00; val a4 = 440.00; val c5 = 523.25; val d5 = 587.33; val f5 = 698.46
            val pattern = listOf(f4, a4, c5, a4, d5, c5, a4, f4, g4, a4, c5, f5, c5, a4, g4, f4)
            val step = 0.32
            val mix = buffer(step * pattern.size * 2 + 2.0 + 2.6)
            val partials = listOf(Triple(1.0, 1.0, 2.0), Triple(2.0, 0.08, 4.0), Triple(6.27, 0.22, 14.0))
            val knock = softNoise((0.012 * SAMPLE_RATE).toInt(), rnd, 0.12)
            for (rep in 0 until 2) for ((k, f) in pattern.withIndex()) {
                val t = (rep * pattern.size + k) * step + if (rep == 1) 0.4 else 0.0
                strike(mix, t, f, 1.8, 1.0, partials, attackSamples = 30)
                val start = (t * SAMPLE_RATE).toInt()
                for (i in knock.indices) if (start + i < mix.size) mix[start + i] += knock[i] * 0.35 * (1.0 - i.toDouble() / knock.size)
            }
            return toPcm(mix)
        }
    }
}
