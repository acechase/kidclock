package com.acechase.kidclock.alarm

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.acechase.kidclock.Prefs
import com.acechase.kidclock.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Read-only card describing one alarm: everything shown on the edit page (time, on/off,
 * days, gentle wake-up lead) plus when it will ring next and its current state (snoozed, skipped
 * for today, gentle wake-up in progress). Opened by tapping the next-alarm line on the clock face.
 * Closes on any tap, back, or after a timeout so the clock is never left hidden.
 */
class AlarmDetailActivity : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_KidClock_Settings)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val prefs = Prefs(this)
        val alarm = AlarmStore(this).get(intent.getIntExtra(EXTRA_ALARM_ID, -1))
        val root = ScrollView(this).apply { isFillViewport = true; setOnClickListener { finish() } }
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 40, 64, 40)
            setOnClickListener { finish() }
        }
        root.addView(column, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setContentView(root)
        if (alarm == null) { column.addView(text("That alarm is gone.", 28f)); scheduleClose(); return }
        build(column, alarm, prefs)
        scheduleClose()
    }

    private fun build(col: LinearLayout, a: Alarm, prefs: Prefs) {
        val now = System.currentTimeMillis()
        val use24h = prefs.use24h
        val snoozed = prefs.snoozeAtMs > now && prefs.snoozeAlarmId == a.id
        val gentle = prefs.wakeUntilMs > now && prefs.wakeAlarmId == a.id
        val skipped = a.id == prefs.skipAlarmId && prefs.skipAtMs > now
        val nextMs = a.nextTriggerMs(now)?.let { t -> if (skipped && t == prefs.skipAtMs) a.nextTriggerMs(t) else t }

        // Header: label, big time, state pill.
        val title = if (a.label.isNotEmpty() && a.label != Alarm.TEST_LABEL) a.label else "Alarm"
        col.addView(text(title, 22f).apply { alpha = 0.7f })
        col.addView(LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(text(Prefs.fmtMinutes(a.hour * 60 + a.minute, use24h), 84f).apply {
                typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            val (stateText, stateColor) = when {
                !a.enabled -> "Off" to 0xFF6B6F80.toInt()
                snoozed -> "Snoozed" to 0xFF4A6FA5.toInt()
                gentle -> "Waking up gently" to 0xFFC98A1E.toInt()
                skipped -> "Done for today" to 0xFF2E9E5B.toInt()
                else -> "On" to 0xFF2E9E5B.toInt()
            }
            addView(pill(stateText, stateColor))
        })

        // Days of the week, like the toggles on the edit page.
        col.addView(section("Repeats"))
        col.addView(LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            val names = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            for (d in Calendar.SUNDAY..Calendar.SATURDAY) {
                val on = d in a.days
                addView(TextView(context).apply {
                    text = names[d - 1]; textSize = 18f; gravity = Gravity.CENTER
                    setTextColor(if (on) Color.WHITE else 0xFF8A8FA3.toInt())
                    background = GradientDrawable().apply {
                        cornerRadius = 20f
                        setColor(if (on) getColor(R.color.accent) and 0x55FFFFFF or 0x22000000 else getColor(R.color.settings_card))
                        if (on) setStroke(3, getColor(R.color.accent))
                    }
                }, LinearLayout.LayoutParams(0, 64, 1f).apply { marginEnd = if (d < Calendar.SATURDAY) 10 else 0 })
            }
        })
        col.addView(text(daysSummary(a.days), 18f).apply { alpha = 0.75f; setPadding(0, 8, 0, 0) })

        // When it rings next, with countdown.
        col.addView(section("Rings next"))
        if (!a.enabled) {
            col.addView(text("Not scheduled: this alarm is switched off.", 20f))
        } else if (nextMs == null) {
            col.addView(text("Not scheduled.", 20f))
        } else {
            val cal = Calendar.getInstance().apply { timeInMillis = nextMs }
            val day = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(cal.time)
            col.addView(text("$day at ${Prefs.fmtMinutes(cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE), use24h)}", 22f))
            col.addView(text(relative(nextMs - now), 18f).apply { alpha = 0.7f })
            if (snoozed) col.addView(text("Snoozed: rings again at ${fmt(prefs.snoozeAtMs, use24h)}", 18f).apply { setTextColor(0xFF9DBBE8.toInt()) })
            if (skipped) col.addView(text("Today's ring was turned off with “I'm awake”.", 18f).apply { setTextColor(0xFF8ED9A6.toInt()) })
        }

        // Gentle wake-up.
        col.addView(section("Gentle wake-up"))
        if (a.preMinutes == 0) {
            col.addView(text("Off. The alarm rings with no warm-up.", 20f))
        } else {
            val preAt = nextMs?.let { it - a.preMinutes * 60_000L }
            val at = if (preAt != null && a.enabled) " (at ${fmt(preAt, use24h)})" else ""
            col.addView(text("${a.preMinutes} minutes before the alarm$at", 20f))
            col.addView(text("Soft harp, the screen lights up, and the morning quote appears. Say “I'm awake” or tap the button to skip the main alarm.", 16f).apply { alpha = 0.7f })
            if (gentle) col.addView(text("Happening now. Main alarm at ${fmt(prefs.wakeUntilMs, use24h)}.", 18f).apply { setTextColor(0xFFF3C46B.toInt()) })
        }

        // What happens when it rings.
        col.addView(section("When it rings"))
        col.addView(text("Snooze lasts ${prefs.snoozeMinutes} minutes. " +
            if (prefs.voiceEnabled) "Say “snooze” or “stop”, or tap the buttons." else "Tap Snooze or Off.", 18f))

        col.addView(text("Tap anywhere to go back. To change this alarm, hold the top-right corner of the clock for two seconds.", 14f)
            .apply { alpha = 0.5f; setPadding(0, 36, 0, 0) })
    }

    private fun fmt(ms: Long, use24h: Boolean): String {
        val c = Calendar.getInstance().apply { timeInMillis = ms }
        return Prefs.fmtMinutes(c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE), use24h)
    }

    private fun relative(deltaMs: Long): String {
        val totalMin = (deltaMs / 60_000L).coerceAtLeast(0)
        val days = totalMin / (24 * 60)
        val hours = totalMin % (24 * 60) / 60
        val mins = totalMin % 60
        val parts = mutableListOf<String>()
        if (days > 0) parts += "$days day${if (days == 1L) "" else "s"}"
        if (hours > 0) parts += "$hours hour${if (hours == 1L) "" else "s"}"
        if (days == 0L && (mins > 0 || parts.isEmpty())) parts += "$mins minute${if (mins == 1L) "" else "s"}"
        return "That's in ${parts.joinToString(" ")}."
    }

    private fun daysSummary(days: Set<Int>): String = when {
        days.isEmpty() -> "Just once, then it turns itself off."
        days.size == 7 -> "Every day."
        days == Alarm.WEEKDAYS -> "School days (Monday to Friday)."
        days == setOf(Calendar.SATURDAY, Calendar.SUNDAY) -> "Weekends."
        else -> {
            val names = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
            "Every " + days.sorted().joinToString(", ") { names[it - 1] } + "."
        }
    }

    private fun text(s: String, size: Float) = TextView(this).apply { text = s; textSize = size }

    private fun section(s: String) = TextView(this).apply {
        text = s.uppercase(Locale.getDefault()); textSize = 13f; letterSpacing = 0.12f
        setTextColor(getColor(R.color.accent)); setPadding(0, 28, 0, 8)
    }

    private fun pill(s: String, color: Int) = TextView(this).apply {
        text = s; textSize = 18f; setTextColor(Color.WHITE)
        background = GradientDrawable().apply { cornerRadius = 40f; setColor(color) }
        setPadding(28, 10, 28, 10)
    }

    private fun scheduleClose() { handler.postDelayed({ finish() }, TIMEOUT_MS) }

    override fun onResume() {
        super.onResume()
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onDestroy() { super.onDestroy(); handler.removeCallbacksAndMessages(null) }

    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
        private const val TIMEOUT_MS = 60_000L
    }
}
