package com.example.teamcompass.ui

import android.app.Application
import android.view.Window

/**
 * Lifecycle-safe wrapper around [ScreenAutoBrightness] that keeps all Window binding logic
 * outside ViewModel business code.
 */
internal class AutoBrightnessBinding(
    private val app: Application,
    private val onInitError: (Throwable) -> Unit,
) {
    private var controller: ScreenAutoBrightness? = null
    private var boundWindow: Window? = null
    private var lastBrightness: Float = DEFAULT_BRIGHTNESS
    private var enabled: Boolean = false

    fun bindWindow(window: Window?, normalBrightness: Float, enabled: Boolean) {
        lastBrightness = normalBrightness
        this.enabled = enabled
        if (boundWindow === window) {
            controller?.setNormalBrightness(lastBrightness)
            if (enabled) controller?.start() else controller?.stop()
            return
        }

        controller?.stop()
        controller = null
        boundWindow = window
        if (window == null) return

        val created = try {
            ScreenAutoBrightness(app, window)
        } catch (err: Throwable) {
            onInitError(err)
            null
        }
        controller = created
        created?.setNormalBrightness(lastBrightness)
        if (enabled) {
            created?.start()
        }
    }

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        if (enabled) controller?.start() else controller?.stop()
    }

    fun setBrightness(brightness: Float) {
        lastBrightness = brightness
        controller?.setNormalBrightness(brightness)
    }

    fun clear() {
        controller?.stop()
        controller = null
        boundWindow = null
    }

    companion object {
        private const val DEFAULT_BRIGHTNESS = 0.8f
    }
}

