package com.cyberdiviner.engine

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * ShakeDetector — accelerometer-based shake detection for Liuyao.
 *
 * Uses TYPE_ACCELEROMETER to detect physical phone shaking.
 * When acceleration exceeds the threshold (12 m/s²), fires onShake callback.
 *
 * Usage:
 *   val detector = ShakeDetector(context) { /* shake detected */ }
 *   detector.start()
 *   // ... later
 *   detector.stop()
 */
class ShakeDetector(
    context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Threshold: 12 m/s² (roughly 1.2g, excluding gravity)
    private val shakeThreshold = 12.0f

    // Minimum interval between shakes to avoid rapid-fire (500ms)
    private val minIntervalMs = 500L
    private var lastShakeTime = 0L

    // Smoothing: use a small window to avoid noise
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var initialized = false

    val isAvailable: Boolean get() = accelerometer != null

    fun start() {
        accelerometer?.let { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        if (!initialized) {
            lastX = x
            lastY = y
            lastZ = z
            initialized = true
            return
        }

        // Delta acceleration (removes gravity component roughly)
        val deltaX = x - lastX
        val deltaY = y - lastY
        val deltaZ = z - lastZ

        val acceleration = sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)

        lastX = x
        lastY = y
        lastZ = z

        if (acceleration > shakeThreshold) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTime > minIntervalMs) {
                lastShakeTime = now
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
