package com.acechase.kidclock.settings

import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.acechase.kidclock.Prefs
import com.acechase.kidclock.R
import com.acechase.kidclock.alarm.Alarm
import com.acechase.kidclock.alarm.AlarmScheduler
import com.acechase.kidclock.alarm.AlarmSound
import com.acechase.kidclock.alarm.SoundChoice
import com.acechase.kidclock.alarm.AlarmSoundPlayer
import com.acechase.kidclock.alarm.GentleSound
import com.acechase.kidclock.alarm.AlarmStore
import com.acechase.kidclock.voice.VoiceCommandListener
import com.acechase.kidclock.weather.WeatherService
import com.acechase.kidclock.quotes.QuoteRepository
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.widget.EditText
import android.widget.Toast
import java.util.Calendar

/** Plain, big-touch-target settings. Everything saves immediately and reschedules alarms. */
class SettingsActivity : AppCompatActivity() {
    private lateinit var prefs: Prefs
    private lateinit var store: AlarmStore
    private lateinit var alarmList: LinearLayout
    private lateinit var general: LinearLayout
    private var preview: AlarmSoundPlayer? = null
    private var previewKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_KidClock_Settings)
        setContentView(R.layout.activity_settings)
        prefs = Prefs(this)
        store = AlarmStore(this)
        alarmList = findViewById(R.id.alarmList)
        general = findViewById(R.id.general)

        findViewById<Button>(R.id.btnDone).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnTest).setOnClickListener { AlarmScheduler.scheduleTest(this, 10); finish() }
        findViewById<Button>(R.id.btnTestGentle).setOnClickListener {
            AlarmScheduler.scheduleGentleTest(this)
            Toast.makeText(this, "Gentle wake-up in ~1 min, alarm ~1 min later", Toast.LENGTH_LONG).show()
            finish()
        }
        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            val a = store.newAlarm()
            store.upsert(a)
            pickTime(a.hour, a.minute) { h, m -> a.hour = h; a.minute = m; save(a) }
            rebuild()
        }
        rebuild()
    }

    private fun save(a: Alarm) { store.upsert(a); AlarmScheduler.rescheduleAll(this); rebuild() }

    private fun rebuild() {
        alarmList.removeAllViews()
        for (a in store.all()) if (a.label != Alarm.TEST_LABEL) alarmList.addView(alarmRow(a))
        buildGeneral()
    }

    private fun alarmRow(a: Alarm): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(getColor(R.color.settings_card))
        setPadding(20, 14, 20, 14)
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 14 }

        // Line 1: time, on/off, delete
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(TextView(context).apply {
                text = Prefs.fmtMinutes(a.hour * 60 + a.minute, prefs.use24h)
                textSize = 40f
                typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
                setOnClickListener { pickTime(a.hour, a.minute) { h, m -> a.hour = h; a.minute = m; save(a) } }
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(Switch(context).apply {
                isChecked = a.enabled
                scaleX = 1.5f; scaleY = 1.5f
                setOnCheckedChangeListener { _, on -> a.enabled = on; save(a) }
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginEnd = 40 })
            addView(Button(context).apply {
                text = getString(R.string.delete)
                isAllCaps = false
                setOnClickListener { store.delete(a.id); AlarmScheduler.rescheduleAll(context); rebuild() }
            })
        })

        // Line 2: days of the week
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 10, 0, 0)
            val names = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            for (d in Calendar.SUNDAY..Calendar.SATURDAY) {
                addView(ToggleButton(context).apply {
                    textOn = names[d - 1]; textOff = names[d - 1]; text = names[d - 1]
                    textSize = 16f
                    isChecked = d in a.days
                    minWidth = 0; minimumWidth = 0; minHeight = 0; minimumHeight = 0
                    alpha = if (isChecked) 1f else 0.4f
                    setOnCheckedChangeListener { _, on -> if (on) a.days += d else a.days -= d; save(a) }
                }, LinearLayout.LayoutParams(0, 72, 1f).apply { marginEnd = if (d < Calendar.SATURDAY) 8 else 0 })
            }
        })

        // Line 3: gentle wake-up lead time
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 10, 0, 0)
            addView(TextView(context).apply {
                text = "Gentle wake-up before"
                textSize = 18f
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            val choices = Alarm.PRE_CHOICES
            val idx = choices.indexOf(a.preMinutes).coerceAtLeast(0)
            addView(Button(context).apply { text = "–"; setOnClickListener { a.preMinutes = choices[(idx - 1).coerceAtLeast(0)]; save(a) } })
            addView(TextView(context).apply {
                text = if (a.preMinutes == 0) "Off" else "${a.preMinutes} min"
                textSize = 22f; minWidth = 150; gravity = Gravity.CENTER
            })
            addView(Button(context).apply { text = "+"; setOnClickListener { a.preMinutes = choices[(idx + 1).coerceAtMost(choices.lastIndex)]; save(a) } })
        })
    }

    private fun buildGeneral() {
        general.removeAllViews()
        general.addView(switchRow(getString(R.string.use_24h), prefs.use24h) { prefs.use24h = it; rebuild() })
        general.addView(timeRow(getString(R.string.night_start), prefs.nightStartMin) { prefs.nightStartMin = it; buildGeneral() })
        general.addView(timeRow(getString(R.string.night_end), prefs.nightEndMin) { prefs.nightEndMin = it; buildGeneral() })
        general.addView(switchRow(getString(R.string.always_night), prefs.alwaysNight) { prefs.alwaysNight = it })
        general.addView(switchRow(getString(R.string.holiday_designs), prefs.holidaysEnabled) { prefs.holidaysEnabled = it })
        general.addView(stepperRow(getString(R.string.snooze_minutes), prefs.snoozeMinutes) { prefs.snoozeMinutes = it; buildGeneral() })
        general.addView(soundRow(ALARM_SOUND, AlarmSound.values().toList(), AlarmSound.byId(prefs.alarmSoundId),
            { prefs.alarmSoundId = it.id }, ::alarmPreview))
        general.addView(stepperRow("Alarm volume %", prefs.alarmVolumePct, step = 5) {
            prefs.alarmVolumePct = it; startPreview(ALARM_SOUND, alarmPreview(AlarmSound.byId(prefs.alarmSoundId)))
        })
        general.addView(soundRow(GENTLE_SOUND, GentleSound.values().toList(), GentleSound.byId(prefs.gentleSoundId),
            { prefs.gentleSoundId = it.id }, ::gentlePreview))
        general.addView(stepperRow("Gentle wake-up volume %", prefs.gentleVolumePct, step = 5) {
            prefs.gentleVolumePct = it; startPreview(GENTLE_SOUND, gentlePreview(GentleSound.byId(prefs.gentleSoundId)))
        })
        general.addView(switchRow(getString(R.string.voice_commands), prefs.voiceEnabled) { prefs.voiceEnabled = it })
        general.addView(switchRow("Daily quotes (shown during gentle wake-up)", prefs.quotesEnabled) { prefs.quotesEnabled = it; buildGeneral() })
        if (prefs.quotesEnabled) {
            general.addView(switchRow("Also show the quote on the clock face", prefs.quotesOnClock) { prefs.quotesOnClock = it })
            val repo = QuoteRepository.get(this)
            val q = repo.current(this)
            general.addView(LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 0, 0, 8)
                addView(TextView(context).apply {
                    text = if (q == null) "No quotes loaded" else "Today (${repo.position(context)}): \u201c${q.text}\u201d \u2014 ${q.attribution}"
                    textSize = 14f; alpha = 0.8f
                }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                addView(Button(context).apply {
                    text = "Next"; isAllCaps = false; textSize = 14f
                    setOnClickListener { repo.next(context); buildGeneral() }
                })
                addView(Button(context).apply {
                    text = "New set"; isAllCaps = false; textSize = 14f
                    setOnClickListener { repo.reroll(context); buildGeneral() }
                })
            })
            general.addView(TextView(this).apply {
                text = "Six quotes are picked each day; tapping the quote on the clock shows the next one. ${repo.all().size} quotes in the library (app/src/main/assets/quotes.json)."
                textSize = 12f; alpha = 0.5f; setPadding(0, 0, 0, 12)
            })
        }
        general.addView(switchRow("Show weather", prefs.weatherEnabled) { prefs.weatherEnabled = it; buildGeneral() })
        if (prefs.weatherEnabled) {
            general.addView(switchRow("Fahrenheit", prefs.weatherFahrenheit) { prefs.weatherFahrenheit = it })
            general.addView(row("Weather location").apply {
                addView(Button(context).apply {
                    text = prefs.weatherPlace.ifEmpty { "Auto (by IP)" }; isAllCaps = false; textSize = 18f
                    setOnClickListener { pickLocation() }
                })
            })
        }
        if (!VoiceCommandListener.hasPermission(this)) {
            general.addView(TextView(this).apply {
                text = "Microphone permission not granted. Run:\nadb shell pm grant com.acechase.kidclock android.permission.RECORD_AUDIO"
                textSize = 14f; setTextColor(Color.parseColor("#FFB347")); setPadding(0, 12, 0, 0)
            })
        }
        general.addView(TextView(this).apply {
            text = "Tip: swipe the clock left or right to change its look. Hold the top-right corner for two seconds to get back here."
            textSize = 15f; alpha = 0.7f; setPadding(0, 24, 0, 0)
        })
    }

    // ---- sound choosers -------------------------------------------------------------------------

    private fun alarmPreview(snd: SoundChoice) = AlarmSoundPlayer(
        context = this, alarmSound = snd as AlarmSound, rampSeconds = 0, stopAfterMs = PREVIEW_MS, streamPct = prefs.alarmVolumePct,
    )

    private fun gentlePreview(snd: SoundChoice) = AlarmSoundPlayer(
        context = this, gentleSound = snd as GentleSound, loops = 0, stopAfterMs = PREVIEW_MS, streamPct = prefs.gentleVolumePct,
    )

    /** A row showing the current sound (tap for the list) and a Play/Stop button for a short preview. */
    private fun soundRow(
        title: String, choices: List<SoundChoice>, current: SoundChoice,
        onPick: (SoundChoice) -> Unit, player: (SoundChoice) -> AlarmSoundPlayer,
    ): LinearLayout {
        val playing = preview?.isPlaying == true && previewKey == title
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(row(title).apply {
                addView(Button(context).apply {
                    text = "${current.title} \u25BE"; isAllCaps = false; textSize = 18f
                    setOnClickListener {
                        pickSound(title, choices, current) { picked -> onPick(picked); startPreview(title, player(picked)) }
                    }
                })
                addView(Button(context).apply {
                    text = if (playing) "\u25A0 Stop" else "\u25B6 Play"; isAllCaps = false; textSize = 16f
                    setOnClickListener { if (playing) { stopPreview(); buildGeneral() } else startPreview(title, player(current)) }
                })
            })
            addView(TextView(context).apply {
                text = current.blurb; textSize = 13f; alpha = 0.6f; setPadding(0, 0, 0, 10)
            })
        }
    }

    /** Our own dark chooser (framework AlertDialog theming is unreliable on this device). */
    private fun pickSound(title: String, choices: List<SoundChoice>, current: SoundChoice, onPick: (SoundChoice) -> Unit) {
        val dlg = Dialog(this)
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#2A2F38"))
            setPadding(40, 32, 40, 24)
            addView(TextView(context).apply {
                text = title; textSize = 22f; setTextColor(Color.WHITE); setPadding(0, 0, 0, 16)
            })
        }
        for (snd in choices) {
            val selected = snd == current
            col.addView(LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 16, 24, 16)
                if (selected) setBackgroundColor(Color.parseColor("#3A5A78"))
                addView(TextView(context).apply {
                    text = (if (selected) "\u25CF  " else "\u25CB  ") + snd.title
                    textSize = 19f; setTextColor(Color.WHITE)
                })
                addView(TextView(context).apply {
                    text = snd.blurb; textSize = 14f; setTextColor(Color.parseColor("#B8C0CC")); setPadding(44, 2, 0, 0)
                })
                setOnClickListener { dlg.dismiss(); onPick(snd) }
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = 6 })
        }
        col.addView(Button(this).apply {
            text = "Cancel"; isAllCaps = false
            setOnClickListener { dlg.dismiss() }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.END; topMargin = 12 })
        dlg.setContentView(col, ViewGroup.LayoutParams(700, ViewGroup.LayoutParams.WRAP_CONTENT))
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dlg.show()
    }

    private fun startPreview(key: String, p: AlarmSoundPlayer) {
        stopPreview()
        preview = p; previewKey = key
        p.start()
        general.postDelayed({ if (preview?.isPlaying != true) { preview = null; previewKey = null; buildGeneral() } }, PREVIEW_MS + 200)
        buildGeneral()
    }

    private fun stopPreview() { preview?.stop(); preview = null; previewKey = null }

    override fun onPause() { super.onPause(); stopPreview() }

    private fun label(text: String) = TextView(this).apply { this.text = text; textSize = 20f }

    private fun switchRow(title: String, value: Boolean, onChange: (Boolean) -> Unit) = row(title).apply {
        addView(Switch(context).apply { isChecked = value; scaleX = 1.3f; scaleY = 1.3f; setOnCheckedChangeListener { _, on -> onChange(on) } })
    }

    private fun timeRow(title: String, minutes: Int, onChange: (Int) -> Unit) = row(title).apply {
        addView(Button(context).apply {
            text = Prefs.fmtMinutes(minutes, prefs.use24h); isAllCaps = false; textSize = 20f
            setOnClickListener { pickTime(minutes / 60, minutes % 60) { h, m -> onChange(h * 60 + m) } }
        })
    }

    private fun stepperRow(title: String, value: Int, step: Int = 1, onChange: (Int) -> Unit) = row(title).apply {
        addView(Button(context).apply { text = "–"; setOnClickListener { onChange(value - step) } })
        addView(TextView(context).apply { text = value.toString(); textSize = 24f; setPadding(24, 0, 24, 0) })
        addView(Button(context).apply { text = "+"; setOnClickListener { onChange(value + step) } })
    }

    private fun row(title: String) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, 8, 0, 8)
        addView(label(title), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
    }

    private fun pickLocation() {
        val input = EditText(this).apply { hint = "City or ZIP, e.g. Portland OR"; setText(prefs.weatherPlace); setSingleLine() }
        AlertDialog.Builder(this)
            .setTitle("Weather location")
            .setView(input)
            .setNeutralButton("Auto (by IP)") { _, _ ->
                prefs.weatherLat = Double.NaN; prefs.weatherLon = Double.NaN; prefs.weatherPlace = ""; prefs.weatherJson = null
                buildGeneral()
            }
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val q = input.text.toString().trim()
                if (q.isEmpty()) return@setPositiveButton
                Thread {
                    val r = runCatching { WeatherService.geocode(q) }.getOrNull()
                    runOnUiThread {
                        if (r == null) { Toast.makeText(this, "Could not find \"$q\"", Toast.LENGTH_LONG).show(); return@runOnUiThread }
                        prefs.weatherLat = r.first; prefs.weatherLon = r.second; prefs.weatherPlace = r.third; prefs.weatherJson = null
                        Toast.makeText(this, "Weather set to ${r.third}", Toast.LENGTH_SHORT).show()
                        buildGeneral()
                    }
                }.start()
            }
            .show()
    }

    private fun pickTime(h: Int, m: Int, cb: (Int, Int) -> Unit) {
        TimePickerDialog(this, { _, hh, mm -> cb(hh, mm) }, h, m, prefs.use24h).show()
    }

    companion object {
        private const val ALARM_SOUND = "Alarm sound"
        private const val GENTLE_SOUND = "Gentle wake-up sound"
        private const val PREVIEW_MS = 10_000L
    }
}
