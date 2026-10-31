package com.acechase.kidclock

import android.content.Context
import android.content.SharedPreferences

/** All user-facing settings. Times are stored as minutes after midnight. */
class Prefs(context: Context) {
    private val sp: SharedPreferences =
        context.applicationContext.getSharedPreferences("kidclock", Context.MODE_PRIVATE)

    var use24h: Boolean
        get() = sp.getBoolean("use24h", false)
        set(v) = sp.edit().putBoolean("use24h", v).apply()

    var nightStartMin: Int
        get() = sp.getInt("nightStart", 20 * 60 + 30)
        set(v) = sp.edit().putInt("nightStart", v).apply()

    var nightEndMin: Int
        get() = sp.getInt("nightEnd", 6 * 60 + 30)
        set(v) = sp.edit().putInt("nightEnd", v).apply()

    /** Night mode all day long (dark screen, amber digits) regardless of the night window. */
    var alwaysNight: Boolean
        get() = sp.getBoolean("alwaysNight", false)
        set(v) = sp.edit().putBoolean("alwaysNight", v).apply()

    var snoozeMinutes: Int
        get() = sp.getInt("snoozeMin", 9)
        set(v) = sp.edit().putInt("snoozeMin", v.coerceIn(1, 30)).apply()

    var voiceEnabled: Boolean
        get() = sp.getBoolean("voice", true)
        set(v) = sp.edit().putBoolean("voice", v).apply()

    var faceIndex: Int
        get() = sp.getInt("face", 0)
        set(v) = sp.edit().putInt("face", v).apply()

    /** Which look (font and layout) draws the time over the background; see looks/LookCatalog. */
    var lookIndex: Int
        get() = sp.getInt("look", 0)
        set(v) = sp.edit().putInt("look", v).apply()

    /** Holiday designs: a special background and look from three days before each holiday until the day after. */
    var holidaysEnabled: Boolean
        get() = sp.getBoolean("holidaysOn", true)
        set(v) = sp.edit().putBoolean("holidaysOn", v).apply()

    /** The holiday whose design is overriding the chosen background and look, or null; see holiday/HolidayPlanner. */
    var holidayActive: String?
        get() = sp.getString("holidayActive", null)
        set(v) = sp.edit().putString("holidayActive", v).apply()

    /** A holiday the child opted out of by changing the design during its window. */
    var holidayDismissed: String?
        get() = sp.getString("holidayDismissed", null)
        set(v) = sp.edit().putString("holidayDismissed", v).apply()

    /** Developer hook: pretend today is inside this holiday's window (tools/test-holiday.sh). */
    var holidayForce: String?
        get() = sp.getString("holidayForce", null)
        set(v) = sp.edit().putString("holidayForce", v).apply()

    /** Pending snooze, 0 when none. */
    var snoozeAtMs: Long
        get() = sp.getLong("snoozeAt", 0L)
        set(v) = sp.edit().putLong("snoozeAt", v).apply()

    var snoozeAlarmId: Int
        get() = sp.getInt("snoozeAlarmId", -1)
        set(v) = sp.edit().putInt("snoozeAlarmId", v).apply()

    /** End of the current gentle wake-up window (the main alarm's time), 0 when none. */
    var wakeUntilMs: Long
        get() = sp.getLong("wakeUntil", 0L)
        set(v) = sp.edit().putLong("wakeUntil", v).apply()

    /** Alarm whose gentle pre-alarm opened the current wake window. */
    var wakeAlarmId: Int
        get() = sp.getInt("wakeAlarmId", -1)
        set(v) = sp.edit().putInt("wakeAlarmId", v).apply()

    /** After "I'm awake": keep the bright okay-to-wake display until this time (the cancelled alarm's time). */
    var awakeUntilMs: Long
        get() = sp.getLong("awakeUntil", 0L)
        set(v) = sp.edit().putLong("awakeUntil", v).apply()

    /** Skip exactly one occurrence of an alarm (the one at [skipAtMs]). */
    var skipAlarmId: Int
        get() = sp.getInt("skipAlarmId", -1)
        set(v) = sp.edit().putInt("skipAlarmId", v).apply()

    var skipAtMs: Long
        get() = sp.getLong("skipAt", 0L)
        set(v) = sp.edit().putLong("skipAt", v).apply()

    var quotesEnabled: Boolean
        get() = sp.getBoolean("quotesOn", true)
        set(v) = sp.edit().putBoolean("quotesOn", v).apply()

    var quoteDayKey: Int
        get() = sp.getInt("quoteDay", 0)
        set(v) = sp.edit().putInt("quoteDay", v).apply()

    var quoteOfDayId: Int
        get() = sp.getInt("quoteId", -1)
        set(v) = sp.edit().putInt("quoteId", v).apply()

    /** Show the current quote, small and quiet, on the main clock face (not only during gentle wake-up). */
    var quotesOnClock: Boolean
        get() = sp.getBoolean("quotesOnClock", true)
        set(v) = sp.edit().putBoolean("quotesOnClock", v).apply()

    /** Today's batch of quote ids and which one is showing; tapping the quote advances the index. */
    var quoteBatchIds: List<Int>
        get() = (sp.getString("quoteBatch", "") ?: "").split(',').mapNotNull { it.trim().toIntOrNull() }
        set(v) = sp.edit().putString("quoteBatch", v.joinToString(",")).apply()

    var quoteIndex: Int
        get() = sp.getInt("quoteIndex", 0)
        set(v) = sp.edit().putInt("quoteIndex", v).apply()

    var quoteHistory: List<Int>
        get() = (sp.getString("quoteHistory", "") ?: "").split(',').mapNotNull { it.trim().toIntOrNull() }
        set(v) = sp.edit().putString("quoteHistory", v.joinToString(",")).apply()

    /** Which main alarm sound rings; see alarm/AlarmSound. */
    var alarmSoundId: String
        get() = sp.getString("alarmSound", "chime") ?: "chime"
        set(v) = sp.edit().putString("alarmSound", v).apply()

    /** Gentle wake-up (and its preview) pins the alarm stream to this percent of maximum while it plays. */
    var gentleVolumePct: Int
        get() = sp.getInt("gentleVol", 25)
        set(v) = sp.edit().putInt("gentleVol", v.coerceIn(5, 100)).apply()

    /** Which gentle wake-up sound plays; see alarm/GentleSound. */
    var gentleSoundId: String
        get() = sp.getString("gentleSound", "harp") ?: "harp"
        set(v) = sp.edit().putString("gentleSound", v).apply()

    var alarmsJson: String
        get() = sp.getString("alarms", "[]") ?: "[]"
        set(v) = sp.edit().putString("alarms", v).apply()

    var weatherEnabled: Boolean
        get() = sp.getBoolean("weatherOn", true)
        set(v) = sp.edit().putBoolean("weatherOn", v).apply()

    var weatherFahrenheit: Boolean
        get() = sp.getBoolean("weatherF", true)
        set(v) = sp.edit().putBoolean("weatherF", v).apply()

    var weatherLat: Double
        get() = Double.fromBits(sp.getLong("wLat", Double.NaN.toBits()))
        set(v) = sp.edit().putLong("wLat", v.toBits()).apply()

    var weatherLon: Double
        get() = Double.fromBits(sp.getLong("wLon", Double.NaN.toBits()))
        set(v) = sp.edit().putLong("wLon", v.toBits()).apply()

    var weatherPlace: String
        get() = sp.getString("wPlace", "") ?: ""
        set(v) = sp.edit().putString("wPlace", v).apply()

    var weatherJson: String?
        get() = sp.getString("weather", null)
        set(v) = sp.edit().putString("weather", v).apply()

    /** What AlarmScheduler last registered with AlarmManager; 0 when nothing. See AlarmPlanner. */
    var lastScheduledAtMs: Long
        get() = sp.getLong("lastSchedAt", 0L)
        set(v) = sp.edit().putLong("lastSchedAt", v).apply()

    var lastScheduledAlarmId: Int
        get() = sp.getInt("lastSchedId", -1)
        set(v) = sp.edit().putInt("lastSchedId", v).apply()

    var lastScheduledIsPre: Boolean
        get() = sp.getBoolean("lastSchedPre", false)
        set(v) = sp.edit().putBoolean("lastSchedPre", v).apply()

    var lastScheduledIsSnooze: Boolean
        get() = sp.getBoolean("lastSchedSnooze", false)
        set(v) = sp.edit().putBoolean("lastSchedSnooze", v).apply()

    /** While ringing (and in the preview) the alarm stream is pinned to this percent of its maximum. */
    var alarmVolumePct: Int
        get() = sp.getInt("minAlarmVol", 50)
        set(v) = sp.edit().putInt("minAlarmVol", v.coerceIn(5, 100)).apply()

    var nextAlarmId: Int
        get() = sp.getInt("nextAlarmId", 1)
        set(v) = sp.edit().putInt("nextAlarmId", v).apply()

    companion object {
        fun fmtMinutes(min: Int, use24h: Boolean): String {
            val h = min / 60
            val m = min % 60
            if (use24h) return "%02d:%02d".format(h, m)
            val h12 = when (h % 12) { 0 -> 12; else -> h % 12 }
            return "%d:%02d %s".format(h12, m, if (h < 12) "AM" else "PM")
        }
    }
}
