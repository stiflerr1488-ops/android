package com.example.teamcompass.ui

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.util.Log
import android.view.Display
import android.view.Surface

internal class HeadingSensorCoordinator(
    private val sensorManager: SensorManager?,
    private val displayManager: DisplayManager?,
    private val rotationSensor: Sensor?,
    private val onHeadingChanged: (Double?) -> Unit,
) {
    private var sensorListener: SensorEventListener? = null
    private var headingContinuous: Double? = null

    fun start() {
        if (rotationSensor == null || sensorManager == null) {
            onHeadingChanged(null)
            return
        }
        if (sensorListener != null) return

        headingContinuous = null
        val listener = object : SensorEventListener {
            private val rotation = FloatArray(9)
            private val orientation = FloatArray(3)
            private val remapped = FloatArray(9)

            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return
                SensorManager.getRotationMatrixFromVector(rotation, event.values)

                val displayRotation = try {
                    displayManager?.getDisplay(Display.DEFAULT_DISPLAY)?.rotation
                } catch (err: Throwable) {
                    Log.w(TAG, "Failed to read display rotation, fallback to ROTATION_0", err)
                    null
                } ?: Surface.ROTATION_0

                when (displayRotation) {
                    Surface.ROTATION_0 -> SensorManager.remapCoordinateSystem(
                        rotation,
                        SensorManager.AXIS_X,
                        SensorManager.AXIS_Y,
                        remapped,
                    )

                    Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(
                        rotation,
                        SensorManager.AXIS_Y,
                        SensorManager.AXIS_MINUS_X,
                        remapped,
                    )

                    Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(
                        rotation,
                        SensorManager.AXIS_MINUS_X,
                        SensorManager.AXIS_MINUS_Y,
                        remapped,
                    )

                    Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(
                        rotation,
                        SensorManager.AXIS_MINUS_Y,
                        SensorManager.AXIS_X,
                        remapped,
                    )

                    else -> System.arraycopy(rotation, 0, remapped, 0, 9)
                }

                SensorManager.getOrientation(remapped, orientation)
                var raw = Math.toDegrees(orientation[0].toDouble())
                if (raw < 0.0) raw += 360.0

                val previous = headingContinuous
                val alpha = 0.18
                val next = if (previous == null) {
                    raw
                } else {
                    previous + shortestDelta(normalize360(previous), raw) * alpha
                }

                headingContinuous = next
                onHeadingChanged(next)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorListener = listener
        sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        sensorListener?.let { listener ->
            sensorManager?.unregisterListener(listener)
        }
        sensorListener = null
        headingContinuous = null
        onHeadingChanged(null)
    }

    private fun normalize360(value: Double): Double {
        var normalized = value % 360.0
        if (normalized < 0.0) normalized += 360.0
        return normalized
    }

    private fun shortestDelta(fromDeg: Double, toDeg: Double): Double {
        return (toDeg - fromDeg + 540.0) % 360.0 - 180.0
    }

    private companion object {
        private const val TAG = "HeadingSensorCoord"
    }
}
