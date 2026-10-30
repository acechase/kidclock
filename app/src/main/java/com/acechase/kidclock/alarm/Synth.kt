package com.acechase.kidclock.alarm

import com.acechase.kidclock.alarm.AlarmSoundPlayer.Companion.SAMPLE_RATE
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/** A sound the user can pick in Settings: both [AlarmSound] and [GentleSound] implement it. */
interface SoundChoice {
    val id: String
    val title: String
    val blurb: String
}

// Small additive-synthesis helpers shared by AlarmSound and GentleSound. Everything is mono doubles
// at SAMPLE_RATE until toPcm() normalises to 16-bit.

internal fun buffer(seconds: Double) = DoubleArray((SAMPLE_RATE * seconds).toInt())

/**
 * Add one struck/plucked note into [mix] at [startSec]. [partials] are (ratio, weight, decayPerSec)
 * triples: harmonic (integer ratios) for strings, inharmonic for bars, tubes, bells and tines.
 */
internal fun strike(
    mix: DoubleArray, startSec: Double, f: Double, durSec: Double, amp: Double,
    partials: List<Triple<Double, Double, Double>>, attackSamples: Int = 120,
) {
    val start = (startSec * SAMPLE_RATE).toInt()
    val n = (durSec * SAMPLE_RATE).toInt()
    for (i in 0 until n) {
        val idx = start + i
        if (idx >= mix.size) break
        val tt = i.toDouble() / SAMPLE_RATE
        val attack = if (i < attackSamples) i.toDouble() / attackSamples else 1.0
        var v = 0.0
        for ((ratio, w, decay) in partials) v += sin(2 * PI * f * ratio * tt) * w * exp(-tt * decay)
        mix[idx] += v * attack * amp
    }
}

/**
 * Add a sustained tone (organ/brass style) with an attack-sustain-release envelope. [harmonics] are
 * (ratio, weight) pairs; no per-partial decay, so the note holds until [durSec] then releases.
 */
internal fun tone(
    mix: DoubleArray, startSec: Double, f: Double, durSec: Double, amp: Double,
    harmonics: List<Pair<Double, Double>>, attackSec: Double = 0.02, releaseSec: Double = 0.06,
) {
    val start = (startSec * SAMPLE_RATE).toInt()
    val n = (durSec * SAMPLE_RATE).toInt()
    val a = (attackSec * SAMPLE_RATE).toInt().coerceAtLeast(1)
    val r = (releaseSec * SAMPLE_RATE).toInt().coerceAtLeast(1)
    for (i in 0 until n) {
        val idx = start + i
        if (idx >= mix.size) break
        val tt = i.toDouble() / SAMPLE_RATE
        val env = minOf(1.0, i.toDouble() / a, (n - 1 - i).toDouble() / r)
        var v = 0.0
        for ((ratio, w) in harmonics) v += sin(2 * PI * f * ratio * tt) * w
        mix[idx] += v * env * amp
    }
}

internal fun toPcm(mix: DoubleArray, peakTarget: Double = 0.85): ShortArray {
    var peak = 1e-9
    for (v in mix) peak = max(peak, abs(v))
    val gain = peakTarget / peak
    return ShortArray(mix.size) { (mix[it] * gain * Short.MAX_VALUE).toInt().toShort() }
}

/** One-pole low-passed white noise, for breeze and knocks. */
internal fun softNoise(n: Int, rnd: Random, cutoff: Double): DoubleArray {
    val a = cutoff.coerceIn(0.001, 0.999)
    var y = 0.0
    return DoubleArray(n) { y += a * ((rnd.nextDouble() * 2 - 1) - y); y }
}
