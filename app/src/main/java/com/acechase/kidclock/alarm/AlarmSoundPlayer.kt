package com.acechase.kidclock.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import kotlin.math.roundToInt

/**
 * Plays a synthesized clip on the ALARM stream, so it cannot be muted by the music stream and no audio
 * assets are needed.
 *
 * - Main alarm (default): loops [alarmSound] forever and ramps the track volume from 15 % to 100 % over
 *   [rampSeconds]. RingActivity raises the stream to its floor itself.
 * - Gentle wake-up ([gentleSound] set): plays the clip [loops] extra times and stops after [stopAfterMs].
 * - [streamPct] >= 0 pins the ALARM stream to that percent of its maximum for the duration and restores it
 *   on [stop]. The Portal has no UI for the alarm stream (its volume keys move the media stream), so the app
 *   owns it: the alarm and gentle volumes in Settings are the only knobs, and the previews use the same ones.
 */
class AlarmSoundPlayer(
    private val context: Context? = null,
    private val alarmSound: AlarmSound = AlarmSound.DEFAULT,
    private val gentleSound: GentleSound? = null,
    private val rampSeconds: Int = 30,
    private val loops: Int = -1,
    private val stopAfterMs: Long = 0L,
    private val streamPct: Int = -1,
) {
    private val gentle get() = gentleSound != null
    private var audioManager: AudioManager? = null
    private var savedStreamVolume = -1
    private var track: AudioTrack? = null
    private val handler = Handler(Looper.getMainLooper())
    private var startMs = 0L

    val isPlaying get() = track != null

    fun start() {
        if (track != null) return
        if (streamPct >= 0) pinStream()
        val pcm = gentleSound?.synthesize() ?: alarmSound.synthesize()
        val t = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(pcm.size * 2)
            .build()
        t.write(pcm, 0, pcm.size)
        t.setLoopPoints(0, pcm.size, loops)
        t.setVolume(when { gentle -> 0.6f; rampSeconds <= 0 -> 1f; else -> 0.15f })
        t.play()
        track = t
        startMs = System.currentTimeMillis()
        if (!gentle && rampSeconds > 0) handler.post(ramp)
        if (stopAfterMs > 0) handler.postDelayed(stopper, stopAfterMs)
    }

    private val stopper = Runnable { stop() }

    private val ramp = object : Runnable {
        override fun run() {
            val t = track ?: return
            val frac = ((System.currentTimeMillis() - startMs) / 1000f / rampSeconds).coerceIn(0f, 1f)
            t.setVolume(0.15f + 0.85f * frac)
            if (frac < 1f) handler.postDelayed(this, 1000)
        }
    }

    fun stop() {
        handler.removeCallbacks(ramp)
        handler.removeCallbacks(stopper)
        track?.let { runCatching { it.stop() }; it.release() }
        track = null
        restoreStream()
    }

    private fun pinStream() {
        val am = (context ?: return).getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        val max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        val target = (max * streamPct / 100f).roundToInt().coerceIn(1, max)
        val cur = am.getStreamVolume(AudioManager.STREAM_ALARM)
        if (cur == target) return
        savedStreamVolume = cur
        audioManager = am
        runCatching { am.setStreamVolume(AudioManager.STREAM_ALARM, target, 0) }
    }

    private fun restoreStream() {
        val am = audioManager ?: return
        if (savedStreamVolume >= 0) runCatching { am.setStreamVolume(AudioManager.STREAM_ALARM, savedStreamVolume, 0) }
        savedStreamVolume = -1
        audioManager = null
    }

    companion object { const val SAMPLE_RATE = 44100 }
}
