package com.acechase.kidclock.alarm

import android.content.Context
import com.acechase.kidclock.Prefs
import org.json.JSONArray

/** Tiny JSON-in-SharedPreferences store; a kid's clock has a handful of alarms at most. */
class AlarmStore(context: Context) {
    private val prefs = Prefs(context)

    fun all(): MutableList<Alarm> {
        val arr = JSONArray(prefs.alarmsJson)
        val out = mutableListOf<Alarm>()
        for (i in 0 until arr.length()) out += Alarm.fromJson(arr.getJSONObject(i))
        return out.sortedWith(compareBy({ it.hour }, { it.minute })).toMutableList()
    }

    fun get(id: Int): Alarm? = all().firstOrNull { it.id == id }

    fun saveAll(alarms: List<Alarm>) {
        prefs.alarmsJson = JSONArray(alarms.map { it.toJson() }).toString()
    }

    fun upsert(alarm: Alarm) {
        val list = all().filter { it.id != alarm.id } + alarm
        saveAll(list)
    }

    fun delete(id: Int) = saveAll(all().filter { it.id != id })

    fun newAlarm(hour: Int = 7, minute: Int = 0): Alarm {
        val id = prefs.nextAlarmId
        prefs.nextAlarmId = id + 1
        return Alarm(id, hour, minute, Alarm.WEEKDAYS.toMutableSet())
    }
}
