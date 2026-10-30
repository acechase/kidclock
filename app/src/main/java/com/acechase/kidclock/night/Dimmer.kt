package com.acechase.kidclock.night

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.View
import android.view.WindowManager

/**
 * Night dimming without WRITE_SETTINGS: the clock is always the foreground window, so the
 * per-window brightness attribute plus a black overlay is enough. The ambient light sensor
 * nudges brightness within each mode's band.
 */
class Dimmer(private val activity: Activity, private val overlay: View) : SensorEventListener {
    private val sm = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val light = sm.getDefaultSensor(Sensor.TYPE_LIGHT)
    private var lux = 100f
    private var haveReading = false

    fun start() { light?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) } }
    fun stop() { sm.unregisterListener(this) }

    override fun onSensorChanged(e: SensorEvent) {
        val v = e.values[0]
        lux = if (haveReading) lux * 0.8f + v * 0.2f else v
        haveReading = true
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun apply(night: Boolean, forceBright: Boolean = false) {
        val brightness: Float
        val overlayAlpha: Float
        if (forceBright) {
            brightness = 1f
            overlayAlpha = 0f
        } else if (night) {
            brightness = 0.02f
            overlayAlpha = if (lux < 5f) 0.55f else 0.3f
        } else {
            brightness = (0.35f + 0.65f * (lux / 150f).coerceIn(0f, 1f))
            overlayAlpha = 0f
        }
        val lp = activity.window.attributes
        if (lp.screenBrightness != brightness) {
            lp.screenBrightness = brightness
            activity.window.attributes = lp
        }
        overlay.alpha = overlayAlpha
    }

    fun currentLux() = lux
}
