package com.acechase.kidclock.alarm

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.acechase.kidclock.Prefs
import com.acechase.kidclock.R
import com.acechase.kidclock.voice.VoiceCommandListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Ringing screen. Two giant buttons plus voice ("snooze" / "stop"). The microphone is opened
 * here and only here, and released the moment the alarm ends or after the 10 minute timeout.
 */
class RingActivity : AppCompatActivity() {
    /** Pins the alarm stream to the chosen alarm volume for the ring and restores it afterwards. */
    private val player by lazy {
        AlarmSoundPlayer(context = this, alarmSound = AlarmSound.byId(prefs.alarmSoundId), streamPct = prefs.alarmVolumePct)
    }
    private var voice: VoiceCommandListener? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var prefs: Prefs
    private var alarmId = -1
    private var finished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        alarmId = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
        )
        window.attributes = window.attributes.apply { screenBrightness = 1f }
        setContentView(R.layout.activity_ring)

        val fmt = if (prefs.use24h) "HH:mm" else "h:mm"
        findViewById<TextView>(R.id.ringTime).text = SimpleDateFormat(fmt, Locale.getDefault()).format(Date())
        showLabel(intent)
        findViewById<View>(R.id.btnSnooze).setOnClickListener { snooze() }
        findViewById<View>(R.id.btnDismiss).setOnClickListener { dismiss() }

        player.start()
        startVoice()
        handler.postDelayed({ dismiss() }, TIMEOUT_MS)
    }

    /**
     * A second alarm (or a snooze) firing while this screen is already up is folded into the
     * current ring: adopt its id and label, keep the sound and the timeout running.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        alarmId = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
        showLabel(intent)
    }

    private fun showLabel(intent: Intent) {
        val label = intent.getStringExtra(AlarmScheduler.EXTRA_LABEL).orEmpty()
        findViewById<TextView>(R.id.ringLabel).text = label.ifEmpty { "Good morning!" }
    }


    private fun startVoice() {
        val status = findViewById<TextView>(R.id.micStatus)
        if (!prefs.voiceEnabled) { status.visibility = View.GONE; return }
        voice = VoiceCommandListener(
            this,
            onCommand = { cmd ->
                when (cmd) {
                    VoiceCommandListener.Command.SNOOZE -> snooze()
                    VoiceCommandListener.Command.DISMISS -> dismiss()
                }
            },
            onStatus = { s ->
                status.text = when (s) {
                    VoiceCommandListener.Status.LOADING -> "Getting ready to listen…"
                    VoiceCommandListener.Status.LISTENING -> getString(R.string.listening)
                    VoiceCommandListener.Status.UNAVAILABLE -> getString(R.string.mic_unavailable)
                }
            },
        ).also { it.start() }
    }

    private fun snooze() {
        if (finished) return
        AlarmScheduler.snooze(this, alarmId, prefs.snoozeMinutes)
        end()
    }

    private fun dismiss() {
        if (finished) return
        AlarmScheduler.rescheduleAll(this)
        end()
    }

    private fun end() {
        finished = true
        handler.removeCallbacksAndMessages(null)
        voice?.stop(); voice = null
        player.stop()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        voice?.stop(); voice = null
        player.stop()
    }

    @Suppress("DEPRECATION", "MissingSuperCall")
    override fun onBackPressed() { /* must choose snooze or off */ }

    companion object { private const val TIMEOUT_MS = 10 * 60_000L }
}
