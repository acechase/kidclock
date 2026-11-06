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
    private val onCommand: (VoiceMatcher.Command) -> Unit,
    private val onStatus: (Status) -> Unit,
    /** Wake-window mode: snooze is meaningless and the mic stays open longer, so a one-word phrase must be very clear. */
    private val wakeWindowMode: Boolean = false,
) {
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
        val words = ArrayList<VoiceMatcher.Word>()
        obj.optJSONArray("result")?.let { arr ->
            for (i in 0 until arr.length()) {
                val w = arr.getJSONObject(i)
                words.add(VoiceMatcher.Word(w.optString("word"), w.optDouble("conf", 0.0)))
            }
        }
        val sinceStart = System.currentTimeMillis() - listeningSinceMs
        Log.d(TAG, "heard '$text' " + words.joinToString(" ") { "%s=%.2f".format(it.word, it.conf) } + " t=+${sinceStart}ms")
        val cmd = VoiceMatcher.match(words, sinceStart, wakeWindowMode) ?: return
        if (fired.compareAndSet(false, true)) main.post { onCommand(cmd) }
    }

    companion object {
        private const val TAG = "VoiceCommand"
        private const val SAMPLE_RATE = 16000f
        private const val GRAMMAR = VoiceMatcher.GRAMMAR

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
