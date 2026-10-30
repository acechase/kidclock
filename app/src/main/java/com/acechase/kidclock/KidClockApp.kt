package com.acechase.kidclock

import android.app.Application
import com.acechase.kidclock.alarm.AlarmScheduler
import org.vosk.LibVosk
import org.vosk.LogLevel

class KidClockApp : Application() {
    override fun onCreate() {
        super.onCreate()
        LibVosk.setLogLevel(LogLevel.WARNINGS)
        AlarmScheduler.rescheduleAll(this)
    }
}
