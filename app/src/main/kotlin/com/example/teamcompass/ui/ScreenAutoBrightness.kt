package com.example.teamcompass.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.Window

/**
 * Автоматическое управление яркостью экрана на основе положения телефона.
 * 
 * Принцип работы:
 * - Телефон вертикально (в подсумке/кармане) → экран гаснет (0% яркости)
 * - Телефон горизонтально (в руке) → экран включается (нормальная яркость)
 * 
 * Используется акселерометр для определения положения телефона относительно гравитации.
 */
class ScreenAutoBrightness(
    private val context: Context,
    private val window: Window
) {
    private val sensorManager = context.getSystemService(SensorManager::class.java)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    private var isScreenDimmed = false
    private var normalBrightness = 0.8f  // 80% яркость по умолчанию
    
    // Задержка для предотвращения ложных срабатываний (2 секунды)
    private var verticalStartTime = 0L
    private val verticalDelayMs = 2000L
    
    private var isActive = false
    
    /**
     * Запуск отслеживания положения телефона
     */
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
            SensorManager.SENSOR_DELAY_UI  // Достаточно быстро для реакции
        )
        
        isActive = true
        Log.d(TAG, "Screen auto-brightness started")
    }
    
    /**
     * Остановка отслеживания и восстановление нормальной яркости
     */
    fun stop() {
        if (!isActive) {
            return
        }
        
        sensorManager.unregisterListener(sensorListener)
        setScreenBrightness(normalBrightness)
        isScreenDimmed = false
        verticalStartTime = 0L
        isActive = false
        
        Log.d(TAG, "Screen auto-brightness stopped")
    }
    
    /**
     * Установка нормальной яркости (когда экран активен)
     */
    fun setNormalBrightness(brightness: Float) {
        normalBrightness = brightness.coerceIn(0.1f, 1.0f)
        if (!isScreenDimmed) {
            setScreenBrightness(normalBrightness)
        }
    }
    
    /**
     * Проверка состояния: экран затемнён или нет
     */
    fun isScreenDimmed(): Boolean = isScreenDimmed
    
    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
            
            val x = event.values[0]  // Влево/вправо
            val y = event.values[1]  // Вперёд/назад
            val z = event.values[2]  // Вертикально
            
            // Вертикальное положение (телефон в подсумке/кармане)
            // Z ≈ 9.8 м/² (гравитация), X и Y ≈ 0
            val isVertical = kotlin.math.abs(z) > 8.0 && 
                            kotlin.math.abs(x) < 4.0 && 
                            kotlin.math.abs(y) < 4.0
            
            // Горизонтальное положение (телефон в руке, смотрим на экран)
            // X или Y ≈ 9.8 м/², Z ≈ 0
            val isHorizontal = kotlin.math.abs(x) > 5.0 || kotlin.math.abs(y) > 5.0
            
            val now = System.currentTimeMillis()
            
            if (isVertical && !isScreenDimmed) {
                // Телефон в вертикальном положении
                if (verticalStartTime == 0L) {
                    verticalStartTime = now
                }
                
                // Затемнить только если вертикально > 2 секунд (защита от ложных срабатываний)
                if (now - verticalStartTime > verticalDelayMs) {
                    setScreenBrightness(0.0f)
                    isScreenDimmed = true
                    Log.d(TAG, "Screen dimmed (vertical position: z=${"%.2f".format(z)})")
                }
            } else if (!isVertical && isScreenDimmed) {
                // Телефон в горизонтальном положении → включить экран
                setScreenBrightness(normalBrightness)
                isScreenDimmed = false
                verticalStartTime = 0L
                Log.d(TAG, "Screen restored (horizontal position: z=${"%.2f".format(z)})")
            } else if (!isVertical) {
                // Сброс таймера если телефон не вертикально
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
