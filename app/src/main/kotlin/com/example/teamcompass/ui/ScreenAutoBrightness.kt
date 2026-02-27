package com.example.teamcompass.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.Window
import kotlin.math.abs

/**
 * Automatic screen brightness dimming based on phone posture.
 *
 * Current behavior:
 * - Flat on table (|z| ~ gravity): do NOT dim.
 * - Standing on portrait edge (|y| dominates): dim after a delay.
 * - Returning to any other posture restores brightness.
 */
class ScreenAutoBrightness(
    private val context: Context,
    private val window: Window,
) {
    private val sensorManager = context.getSystemService(SensorManager::class.java)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var isScreenDimmed = false
    private var normalBrightness = 0.8f

    // Delay to avoid false triggers from transient movements.
    private var verticalStartTime = 0L
    private val verticalDelayMs = 2000L

    private var isActive = false

    fun start() {
        if (isActive) {
            Log.w(TAG, "Already started")
            return
        }

        if (accelerometer == null) {
            Log.w(TAG, "Accelerometer not available on this device")
            return
        }

        sensorManager.registerListener(
            sensorListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI,
        )

        isActive = true
        Log.d(TAG, "Screen auto-brightness started")
    }

    fun stop() {
        if (!isActive) return

        sensorManager.unregisterListener(sensorListener)
        setScreenBrightness(normalBrightness)
        isScreenDimmed = false
        verticalStartTime = 0L
        isActive = false

        Log.d(TAG, "Screen auto-brightness stopped")
    }

    fun setNormalBrightness(brightness: Float) {
        normalBrightness = brightness.coerceIn(0.1f, 1.0f)
        if (!isScreenDimmed) {
            setScreenBrightness(normalBrightness)
        }
    }

    fun isScreenDimmed(): Boolean = isScreenDimmed

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

            val x = event.values[0] // left/right
            val y = event.values[1] // portrait top/bottom axis
            val z = event.values[2] // screen normal (flat on table if abs(z) ~ 9.8)

            // Flat on table (screen face-up/down): keep bright.
            val isFlatFaceUpOrDown = abs(z) > 8.0f && abs(x) < 4.0f && abs(y) < 4.0f

            // Phone stands on portrait edge ("на борту" / on bottom edge).
            // Y is dominant, Z is near zero. We avoid X-dominant posture to keep landscape radar bright.
            val isStandingOnPortraitEdge = abs(y) > 8.0f && abs(z) < 4.0f && abs(x) < 5.0f

            val now = System.currentTimeMillis()

            if (isStandingOnPortraitEdge && !isScreenDimmed) {
                if (verticalStartTime == 0L) {
                    verticalStartTime = now
                }

                if (now - verticalStartTime > verticalDelayMs) {
                    setScreenBrightness(0.0f)
                    isScreenDimmed = true
                    Log.d(
                        TAG,
                        "Screen dimmed (portrait-edge posture: x=${"%.2f".format(x)}, y=${"%.2f".format(y)}, z=${"%.2f".format(z)})",
                    )
                }
            } else if (!isStandingOnPortraitEdge && isScreenDimmed) {
                setScreenBrightness(normalBrightness)
                isScreenDimmed = false
                verticalStartTime = 0L
                val posture = if (isFlatFaceUpOrDown) "flat" else "active"
                Log.d(
                    TAG,
                    "Screen restored ($posture posture: x=${"%.2f".format(x)}, y=${"%.2f".format(y)}, z=${"%.2f".format(z)})",
                )
            } else if (!isStandingOnPortraitEdge) {
                verticalStartTime = 0L
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            Log.d(TAG, "Accelerometer accuracy changed: $accuracy")
        }
    }

    private fun setScreenBrightness(brightness: Float) {
        try {
            val layoutParams = window.attributes
            layoutParams.screenBrightness = brightness
            window.attributes = layoutParams
            Log.d(TAG, "Screen brightness set to: ${(brightness * 100).toInt()}%")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set screen brightness", e)
        }
    }

    companion object {
        private const val TAG = "ScreenAutoBrightness"
    }
}
