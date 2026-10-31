package com.acechase.kidclock

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.acechase.kidclock.alarm.AlarmDetailActivity
import com.acechase.kidclock.alarm.AlarmScheduler
import com.acechase.kidclock.alarm.AlarmSoundPlayer
import com.acechase.kidclock.alarm.GentleSound
import com.acechase.kidclock.faces.ClockFace
import com.acechase.kidclock.holiday.HolidayDesigns
import com.acechase.kidclock.holiday.HolidayManager
import com.acechase.kidclock.holiday.HolidayPlanner
import com.acechase.kidclock.looks.Look
import com.acechase.kidclock.looks.LookCatalog
import com.acechase.kidclock.looks.LookPickerOverlay
import com.acechase.kidclock.looks.LookPickerState
import com.acechase.kidclock.night.Dimmer
import com.acechase.kidclock.night.NightSchedule
import com.acechase.kidclock.settings.SettingsActivity
import com.acechase.kidclock.weather.WeatherService
import com.acechase.kidclock.weather.WeatherDetailActivity
import com.acechase.kidclock.quotes.QuoteRepository
import com.acechase.kidclock.voice.VoiceCommandListener
import android.widget.Button
import android.view.Gravity
import android.graphics.drawable.GradientDrawable
import java.util.Calendar
import kotlin.math.abs

/**
 * The HOME activity. Fullscreen clock face: swipe left/right to change the background, hold the
 * time for two seconds then swipe to change the look (font and layout), hold the top-right corner
 * for two seconds to reach settings. Never sleeps the screen.
 */
class ClockActivity : AppCompatActivity() {
    private lateinit var prefs: Prefs
    private lateinit var root: FrameLayout
    private lateinit var overlay: View
    private lateinit var dimmer: Dimmer
    private lateinit var schedule: NightSchedule
    private lateinit var holidays: HolidayManager
    /** What [applyDesign] last built, so a pref changed behind our back (the adb test hook) is noticed. */
    private var shownFace = -1
    private var shownLook = -1
    private var shownHoliday: String? = null
    private var face: ClockFace? = null
    @Suppress("DEPRECATION")
    private var keyguardLock: android.app.KeyguardManager.KeyguardLock? = null
    private val handler = Handler(Looper.getMainLooper())
    private var gentlePlayer: AlarmSoundPlayer? = null
    private lateinit var awakeButton: Button
    private var wakeVoice: VoiceCommandListener? = null
    private var wakeVoiceListening = false
    private lateinit var pickerOverlay: LookPickerOverlay
    private val picker = LookPickerState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        schedule = NightSchedule(prefs)
        holidays = HolidayManager(prefs)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        @Suppress("DEPRECATION")
        keyguardLock = (getSystemService(KEYGUARD_SERVICE) as android.app.KeyguardManager).newKeyguardLock("kidclock")

        root = FrameLayout(this)
        pickerOverlay = LookPickerOverlay(this).apply { visibility = View.GONE }
        overlay = View(this).apply { setBackgroundColor(Color.BLACK); alpha = 0f; isClickable = false }
        setContentView(root)
        root.addView(pickerOverlay, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        root.addView(overlay, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        dimmer = Dimmer(this, overlay)
        awakeButton = Button(this).apply {
            text = "I'm awake!  ☀️"
            textSize = 24f
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply { cornerRadius = 48f; setColor(0xFF2E9E5B.toInt()) }
            setPadding(48, 24, 48, 24)
            visibility = View.GONE
            setOnClickListener { markAwake() }
        }
        root.addView(awakeButton, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            bottomMargin = 28; rightMargin = 36
        })
        // Installs from before looks existed stored Analog as the last background.
        LookCatalog.migrateAnalogBackground(prefs.faceIndex, LookCatalog.BACKGROUNDS_BEFORE_LOOKS)?.let { look ->
            prefs.lookIndex = look
            prefs.faceIndex = 0
        }
        holidays.sync()
        applyDesign()

        val gestures = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent) = true
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (picker.tap()) { exitPicker(); return true }
                val f = face ?: return false
                if (f.alarmRect.contains(e.x, e.y)) {
                    val id = shownAlarmId() ?: return false
                    startActivity(Intent(this@ClockActivity, AlarmDetailActivity::class.java).putExtra(AlarmDetailActivity.EXTRA_ALARM_ID, id))
                    return true
                }
                if (f.quoteRect.contains(e.x, e.y)) {
                    QuoteRepository.get(this@ClockActivity).next(this@ClockActivity)
                    tick()
                    return true
                }
                return false
            }
            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (picker.picking) return true
                val f = face ?: return false
                if (prefs.weatherEnabled && f.weatherRect.contains(e.x, e.y)) {
                    startActivity(Intent(this@ClockActivity, WeatherDetailActivity::class.java))
                    return true
                }
                return false
            }
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vx: Float, vy: Float): Boolean {
                if (e1 == null) return false
                val dx = e2.x - e1.x
                if (abs(dx) > 150 && abs(vx) > 400 && abs(dx) > abs(e2.y - e1.y)) {
                    val step = picker.swipe(dx, System.currentTimeMillis())
                    if (step != null) {
                        userShowLook(step)
                        armIdleExit()
                    } else {
                        userShowFace(if (dx < 0) 1 else -1)
                    }
                    return true
                }
                return false
            }
        })
        root.setOnTouchListener { v, ev ->
            handleCornerHold(v, ev)
            handleTimeHold(ev)
            gestures.onTouchEvent(ev)
            true
        }
        AlarmScheduler.rescheduleAll(this)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    /** Gentle wake-up: a soft chime once; the wake window itself is driven by prefs in [tick]. */
    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_GENTLE_WAKE, false) != true) return
        intent.removeExtra(EXTRA_GENTLE_WAKE)
        gentlePlayer?.stop()
        gentlePlayer = AlarmSoundPlayer(
            context = this, gentleSound = GentleSound.byId(prefs.gentleSoundId),
            loops = 2, stopAfterMs = 20_000L, streamPct = prefs.gentleVolumePct,
        ).also { it.start() }
        tick()
    }

    /** The alarm the status line is talking about: the one waking up gently, else the next main alarm. */
    private fun shownAlarmId(): Int? {
        if (schedule.inGentleWake() && prefs.wakeAlarmId >= 0) return prefs.wakeAlarmId
        if (schedule.inWakeWindow() && prefs.skipAlarmId >= 0 && prefs.skipAtMs > System.currentTimeMillis()) return prefs.skipAlarmId
        return AlarmScheduler.nextMainEvent(this)?.alarm?.id
    }

    /** Builds the screen: the active holiday's background and look if there is one, else the chosen ones. */
    private fun applyDesign() {
        val holiday = holidays.active
        shownFace = prefs.faceIndex; shownLook = prefs.lookIndex; shownHoliday = holiday?.id
        face?.let { root.removeView(it) }
        face = (holiday?.let { HolidayDesigns.face(this, it) } ?: ClockFace.create(this, prefs.faceIndex)).also {
            it.look = holiday?.let { h -> HolidayDesigns.look(h) } ?: Look.create(prefs.lookIndex)
            it.picking = picker.picking
            root.addView(it, 0, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        }
        tick()
    }

    /** A swipe to the next or previous background. Swiping away from a holiday design dismisses it for this year. */
    private fun userShowFace(step: Int) {
        holidays.userChanged()
        prefs.faceIndex = Math.floorMod(prefs.faceIndex + step, ClockFace.COUNT)
        applyDesign()
    }

    /** A swipe in the look picker; leaving a holiday look dismisses the holiday the same way. */
    private fun userShowLook(step: Int) {
        val wasHoliday = holidays.active != null
        holidays.userChanged()
        val i = LookCatalog.wrap(prefs.lookIndex + step)
        prefs.lookIndex = i
        if (wasHoliday) applyDesign() else face?.look = Look.create(i)
        showPickerName()
    }

    private fun showPickerName() {
        val holiday = holidays.active
        pickerOverlay.lookName = holiday?.lookName ?: LookCatalog.names[prefs.lookIndex]
        pickerOverlay.index = if (holiday != null) -1 else prefs.lookIndex
        pickerOverlay.count = LookCatalog.count
    }

    // Hold the time for two seconds to open the look picker. The face shows a filling ring meanwhile.
    private val holdCheck = Runnable {
        if (picker.advance(System.currentTimeMillis()) == LookPickerState.Event.ENTERED) enterPicker()
    }
    private val idleCheck = Runnable {
        if (picker.advance(System.currentTimeMillis()) == LookPickerState.Event.EXITED) exitPicker()
    }
    private fun handleTimeHold(ev: MotionEvent) {
        val f = face ?: return
        val now = System.currentTimeMillis()
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (picker.picking) armIdleExit()
                if (picker.down(ev.x, ev.y, f.timeRect.contains(ev.x, ev.y), now)) {
                    f.beginHold(ev.x, ev.y, now)
                    handler.postDelayed(holdCheck, LookPickerState.HOLD_MS + 10)
                }
            }
            MotionEvent.ACTION_MOVE -> if (picker.move(ev.x, ev.y)) cancelHold()
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { picker.up(); cancelHold() }
        }
    }
    private fun cancelHold() {
        handler.removeCallbacks(holdCheck)
        face?.endHold()
    }
    private fun enterPicker() {
        face?.endHold()
        face?.picking = true
        showPickerName()
        pickerOverlay.night = schedule.phase(Calendar.getInstance()) == NightSchedule.Phase.NIGHT
        pickerOverlay.visibility = View.VISIBLE
        armIdleExit()
    }
    private fun exitPicker() {
        handler.removeCallbacks(idleCheck)
        pickerOverlay.visibility = View.GONE
        face?.picking = false
    }
    private fun armIdleExit() {
        handler.removeCallbacks(idleCheck)
        handler.postDelayed(idleCheck, LookPickerState.IDLE_MS + 10)
    }

    // Hold the top-right corner to open settings; a deliberate gesture so a stray tap never opens it.
    private val openSettings = Runnable { startActivity(Intent(this, SettingsActivity::class.java)) }
    private fun handleCornerHold(v: View, ev: MotionEvent) {
        val inCorner = ev.x > v.width - CORNER_PX && ev.y < CORNER_PX
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> if (inCorner) handler.postDelayed(openSettings, HOLD_MS)
            MotionEvent.ACTION_MOVE -> if (!inCorner) handler.removeCallbacks(openSettings)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> handler.removeCallbacks(openSettings)
        }
    }

    private val ticker = object : Runnable {
        override fun run() { tick(); handler.postDelayed(this, 1000L) }
    }

    private fun tick() {
        // Holiday windows open and close on their own schedule; rebuild the screen when one does.
        if (holidays.sync() != HolidayPlanner.Action.NONE) { applyDesign(); return }
        if (shownFace != prefs.faceIndex || shownLook != prefs.lookIndex || shownHoliday != holidays.active?.id) { applyDesign(); return }
        val f = face ?: return
        val phase = schedule.phase(Calendar.getInstance())
        f.phase = phase
        f.use24h = prefs.use24h
        val wakeWindow = schedule.inWakeWindow()
        val gentle = schedule.inGentleWake()
        f.wakeMode = wakeWindow
        val quote = if (prefs.quotesEnabled) QuoteRepository.get(this).current(this) else null
        f.quote = if (wakeWindow) quote else null
        f.clockQuote = if (prefs.quotesOnClock) quote else null
        awakeButton.visibility = if (gentle) View.VISIBLE else View.GONE
        syncWakeVoice(gentle)
        val nextMain = AlarmScheduler.nextMainEvent(this)
        fun fmt(ms: Long): String {
            val c = Calendar.getInstance().apply { timeInMillis = ms }
            val t = Prefs.fmtMinutes(c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE), prefs.use24h)
            val day = if (c.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) ""
            else java.text.SimpleDateFormat("EEE ", java.util.Locale.getDefault()).format(c.time)
            return "$day$t"
        }
        f.nextAlarmText = when {
            gentle -> "Good morning ☀️ Alarm at ${fmt(prefs.wakeUntilMs)}" +
                if (wakeVoiceListening) "  ·  say “I'm awake”" else "  ·  tap when you're up"
            wakeWindow -> "You're up! Alarm is off for today. Have a great day ☀️"
            nextMain == null -> ""
            nextMain.isSnooze -> "Snoozed until ${fmt(nextMain.atMs)}"
            else -> fmt(nextMain.atMs)
        }
        // Brightness follows the scheduled window only: "Always night" changes colours, not brightness.
        dimmer.apply(schedule.isScheduledNight(Calendar.getInstance()), forceBright = wakeWindow)
        pickerOverlay.night = phase == NightSchedule.Phase.NIGHT
        f.weather = if (prefs.weatherEnabled) WeatherService.cached(this) else null
        WeatherService.maybeRefresh(this) { w -> face?.weather = w }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        @Suppress("DEPRECATION")
        runCatching { keyguardLock?.disableKeyguard() }
        dimmer.start()
        handler.post(ticker)
    }

    /** The child said or tapped "I'm awake": cancel today's main alarm, stay bright until its time. */
    private fun markAwake() {
        gentlePlayer?.stop(); gentlePlayer = null
        AlarmScheduler.markAwake(this)
        syncWakeVoice(false)
        tick()
    }

    /** Microphone is open only while a gentle wake-up is pending, listening for "I'm awake". */
    private fun syncWakeVoice(shouldListen: Boolean) {
        val wantVoice = shouldListen && prefs.voiceEnabled && VoiceCommandListener.hasPermission(this)
        if (wantVoice && wakeVoice == null) {
            wakeVoice = VoiceCommandListener(
                this,
                onCommand = { cmd -> if (cmd == VoiceCommandListener.Command.DISMISS) markAwake() },
                onStatus = { s -> wakeVoiceListening = s == VoiceCommandListener.Status.LISTENING },
                wakeWindowMode = true,
            ).also { it.start() }
        } else if (!wantVoice && wakeVoice != null) {
            wakeVoice?.stop(); wakeVoice = null
            wakeVoiceListening = false
        }
    }

    override fun onPause() {
        super.onPause()
        syncWakeVoice(false)
        gentlePlayer?.stop(); gentlePlayer = null
        handler.removeCallbacks(ticker)
        handler.removeCallbacks(openSettings)
        cancelHold()
        picker.reset()
        exitPicker()
        dimmer.stop()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUi()
    }

    private fun hideSystemUi() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    @Suppress("DEPRECATION", "MissingSuperCall")
    override fun onBackPressed() { /* HOME activity: back does nothing */ }

    companion object {
        const val EXTRA_GENTLE_WAKE = "gentle_wake"
        private const val CORNER_PX = 180f
        private const val HOLD_MS = 2000L
    }
}
