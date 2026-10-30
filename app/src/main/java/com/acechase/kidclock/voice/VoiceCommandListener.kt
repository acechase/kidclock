package com.acechase.kidclock.voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Offline keyword listener used only while an alarm rings. The recognizer is grammar-limited to a
 * few phrases so it reacts fast and ignores everything else. The microphone is released in [stop].
 */
class VoiceCommandListener(
    private val context: Context,
    private val onCommand: (Command) -> Unit,
    private val onStatus: (Status) -> Unit,
    /** Wake-window mode: snooze is meaningless and the mic stays open longer, so require a two-word phrase. */
    private val wakeWindowMode: Boolean = false,
) {
    enum class Command { SNOOZE, DISMISS }
    enum class Status { LOADING, LISTENING, UNAVAILABLE }

    private val main = Handler(Looper.getMainLooper())
    private val stopped = AtomicBoolean(false)
    private val fired = AtomicBoolean(false)
    private var speech: SpeechService? = null
    private var recognizer: Recognizer? = null
    private var listeningSinceMs = 0L

    fun start() {
        if (!hasPermission(context)) { onStatus(Status.UNAVAILABLE); return }
        onStatus(Status.LOADING)
        Thread({
            try {
                val model = loadModel(context)
                val rec = Recognizer(model, SAMPLE_RATE, GRAMMAR).apply { setWords(true) }
                val svc = SpeechService(rec, SAMPLE_RATE)
                main.post {
                    if (stopped.get()) { svc.shutdown(); rec.close(); return@post }
                    recognizer = rec
                    speech = svc
                    svc.startListening(listener)
                    listeningSinceMs = System.currentTimeMillis()
                    onStatus(Status.LISTENING)
                }
            } catch (e: Exception) {
                Log.e(TAG, "voice unavailable", e)
                main.post { onStatus(Status.UNAVAILABLE) }
            }
        }, "vosk-init").start()
    }

    fun stop() {
        stopped.set(true)
        speech?.let { runCatching { it.stop() }; runCatching { it.shutdown() } }
        speech = null
        recognizer?.let { runCatching { it.close() } }
        recognizer = null
    }

    private val listener = object : RecognitionListener {
        // Partials are ignored on purpose: the alarm chime itself produces confident-looking
        // partial guesses. Only a finished utterance with high per-word confidence counts.
        override fun onPartialResult(hypothesis: String?) {}
        override fun onResult(hypothesis: String?) = handle(hypothesis)
        override fun onFinalResult(hypothesis: String?) = handle(hypothesis)
        override fun onError(exception: Exception?) { Log.w(TAG, "recognizer error", exception); onStatus(Status.UNAVAILABLE) }
        override fun onTimeout() {}
    }

    private fun handle(json: String?) {
        if (json == null || fired.get()) return
        val obj = runCatching { JSONObject(json) }.getOrNull() ?: return
        val text = obj.optString("text", "").trim()
        if (text.isEmpty()) return
        val words = obj.optJSONArray("result")
        var minConf = 1.0
        var n = 0
        if (words != null) for (i in 0 until words.length()) {
            val w = words.getJSONObject(i)
            if (w.optString("word") == "[unk]") { minConf = 0.0; continue }
            minConf = minOf(minConf, w.optDouble("conf", 0.0)); n++
        }
        val sinceStart = System.currentTimeMillis() - listeningSinceMs
        Log.d(TAG, "heard '$text' minConf=%.2f words=$n t=+${sinceStart}ms".format(minConf))
        if (sinceStart < WARMUP_MS || n == 0 || minConf < MIN_CONF) return
        val cmd = COMMANDS[text] ?: return
        if (wakeWindowMode && (cmd == Command.SNOOZE || n < 2)) return
        if (fired.compareAndSet(false, true)) main.post { onCommand(cmd) }
    }

    companion object {
        private const val TAG = "VoiceCommand"
        private const val SAMPLE_RATE = 16000f
        private const val GRAMMAR = """["snooze", "snooze please", "stop", "stop alarm", "alarm off", "turn off", "turn off alarm", "i'm awake", "i am awake", "[unk]"]"""
        private val COMMANDS = mapOf(
            "snooze" to Command.SNOOZE, "snooze please" to Command.SNOOZE,
            "stop" to Command.DISMISS, "stop alarm" to Command.DISMISS, "alarm off" to Command.DISMISS,
            "turn off" to Command.DISMISS, "turn off alarm" to Command.DISMISS,
            "i'm awake" to Command.DISMISS, "i am awake" to Command.DISMISS,
        )
        private const val MIN_CONF = 0.70
        private const val WARMUP_MS = 1500L

        @Volatile private var cachedModel: Model? = null

        /** The model takes a couple of seconds to load, so keep it for the life of the process. */
        @Synchronized
        private fun loadModel(context: Context): Model {
            cachedModel?.let { return it }
            val dir = ModelInstaller.ensureInstalled(context)
            return Model(dir.absolutePath).also { cachedModel = it }
        }

        fun hasPermission(context: Context) =
            context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }
}
