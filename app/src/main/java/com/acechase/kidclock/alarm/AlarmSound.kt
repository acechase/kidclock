package com.acechase.kidclock.alarm

import com.acechase.kidclock.alarm.AlarmSoundPlayer.Companion.SAMPLE_RATE
import kotlin.math.exp

/**
 * The main alarm sounds. Each is a short synthesized phrase ending in a rest; [AlarmSoundPlayer] loops it
 * for as long as the alarm rings and ramps the volume up over the first half minute.
 *
 * Tuned to what the sleep-inertia research suggests (McFarlane et al. 2020, summarised by Healthline and
 * Startle): a melody you could hum rather than a mechanical beep, a dominant pitch around C5 (~500 Hz, and
 * lower rather than higher for children), a tempo of 100-120 BPM, and loudness that rises gradually.
 * Everything here sits at 112 BPM in C major between G4 and A5.
 */
enum class AlarmSound(override val id: String, override val title: String, override val blurb: String) : SoundChoice {
    CHIME("chime", "Chime", "A soft seven-note chime you can hum, centred on C5 at 112 BPM."),
    BELLS("bells", "Bells", "Two warm bells tolling a slow descending tune. Low and hard to sleep through."),
    MARIMBA("marimba", "Marimba", "A bouncy wooden tune at 112 BPM that bubbles along cheerfully."),
    PULSE("beeps", "Pulse", "A rounded two-note pulse, steady and plain, without the harsh beep."),
    FANFARE("fanfare", "Fanfare", "A mellow brass ta-ta-ta-taa, like the start of something good.");

    fun synthesize(): ShortArray = when (this) {
        CHIME -> chime()
        BELLS -> bells()
        MARIMBA -> marimba()
        PULSE -> pulse()
        FANFARE -> fanfare()
    }

    companion object {
        val DEFAULT = CHIME
        fun byId(id: String?): AlarmSound = values().firstOrNull { it.id == id } ?: DEFAULT

        private const val BPM = 112.0
        private const val QUARTER = 60.0 / BPM          // 0.536 s
        private const val EIGHTH = QUARTER / 2
        private const val G4 = 392.00; private const val A4 = 440.00; private const val C5 = 523.25
        private const val D5 = 587.33; private const val E5 = 659.25; private const val G5 = 783.99; private const val A5 = 880.00

        /** Seven legato chime notes: C E G E G A G, then a rest. The original three notes, grown into a tune. */
        private fun chime(): ShortArray {
            val tune = doubleArrayOf(C5, E5, G5, E5, G5, A5, G5)
            val mix = buffer(QUARTER * 8 + 0.8)
            val partials = listOf(Triple(1.0, 0.7, 4.0), Triple(2.0, 0.2, 5.0), Triple(3.0, 0.08, 6.0))
            for ((k, f) in tune.withIndex()) strike(mix, k * QUARTER, f, 0.9, 1.0, partials, attackSamples = 200)
            return toPcm(mix)
        }

        /** Bells tolling E C D G, G D E C (the old Westminster fragment) with a lingering low hum. */
        private fun bells(): ShortArray {
            val tune = doubleArrayOf(E5, C5, D5, G4, G4, D5, E5, C5)
            val mix = buffer(QUARTER * 8 + 1.6 + 0.6)
            val partials = listOf(
                Triple(0.5, 0.3, 1.2), Triple(1.0, 1.0, 1.8), Triple(2.0, 0.4, 3.0),
                Triple(2.4, 0.2, 4.0), Triple(3.0, 0.15, 5.0), Triple(4.2, 0.06, 7.0),
            )
            for ((k, f) in tune.withIndex()) strike(mix, k * QUARTER, f, 1.6, if (k == 4) 0.9 else 1.0, partials, attackSamples = 40)
            return toPcm(mix)
        }

        /** Marimba eighth notes: C E G A G E D C, C D E G E D C, then a rest. */
        private fun marimba(): ShortArray {
            val tune = doubleArrayOf(C5, E5, G5, A5, G5, E5, D5, C5, C5, D5, E5, G5, E5, D5, C5)
            val mix = buffer(EIGHTH * 16 + 0.9)
            val partials = listOf(Triple(1.0, 1.0, 6.0), Triple(4.0, 0.3, 14.0), Triple(9.2, 0.08, 30.0))
            for ((k, f) in tune.withIndex()) strike(mix, k * EIGHTH, f, 0.7, if (k % 4 == 0) 1.0 else 0.8, partials, attackSamples = 20)
            return toPcm(mix)
        }

        /** A rounded tone (soft attack, few harmonics) alternating C5 and G4 on the beat, then a rest. */
        private fun pulse(): ShortArray {
            val tune = doubleArrayOf(C5, G4, C5, G4, C5)
            val mix = buffer(QUARTER * 8)
            val harmonics = listOf(1.0 to 1.0, 2.0 to 0.25, 3.0 to 0.08)
            for ((k, f) in tune.withIndex()) tone(mix, k * QUARTER, f, 0.3, 1.0, harmonics, attackSec = 0.04, releaseSec = 0.08)
            return toPcm(mix)
        }

        /** Mellow brass: G G G (eighths) then C held with a fifth above, softly voiced, then a rest. */
        private fun fanfare(): ShortArray {
            val mix = buffer(EIGHTH * 3 + QUARTER * 2 + 1.3)
            val harmonics = (1..7).map { h -> h.toDouble() to (1.0 / h) * exp(-0.3 * h) }
            for (k in 0 until 3) tone(mix, k * EIGHTH, G4, EIGHTH * 0.8, 0.9, harmonics, attackSec = 0.04, releaseSec = 0.05)
            tone(mix, EIGHTH * 3, C5, QUARTER * 2, 1.0, harmonics, attackSec = 0.05, releaseSec = 0.3)
            tone(mix, EIGHTH * 3, G5, QUARTER * 2, 0.35, harmonics, attackSec = 0.05, releaseSec = 0.3)
            return toPcm(mix)
        }
    }
}
