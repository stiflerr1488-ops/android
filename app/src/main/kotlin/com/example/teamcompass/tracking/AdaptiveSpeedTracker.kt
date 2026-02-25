package com.example.teamcompass.tracking

import android.location.Location
import com.example.teamcompass.core.MovementState
import kotlin.math.abs

/**
 * Адаптивный трекер скорости для определения режима движения.
 * 
 * Комбинирует GPS-скорость и дельту расстояния для классификации:
 * - STATIONARY: стоим на месте (интервал 30 сек)
 * - WALKING_SLOW: медленная ходьба (интервал 10 сек)
 * - WALKING_FAST: быстрая ходьба / бег (интервал 5 сек)
 * - VEHICLE: транспорт (интервал 2 сек)
 */
class AdaptiveSpeedTracker(
    private val nowMs: () -> Long = System::currentTimeMillis,
) {
    
    private var lastLocation: Location? = null
    private var lastUpdateTime: Long = 0L
    
    // Скользящее среднее для сглаживания (последние 5 измерений)
    private val speedBuffer = ArrayDeque<Float>(MAX_BUFFER_SIZE)
    
    // Текущее состояние с гистерезисом
    private var currentState: MovementState = MovementState.STATIONARY
    private var stateEnterTime: Long = 0L
    
    fun update(location: Location): MovementState {
        if (stateEnterTime <= 0L) {
            stateEnterTime = nowMs()
        }
        val rawSpeed = getSpeed(location)
        
        // Сглаживание скорости (скользящее среднее)
        speedBuffer.addLast(rawSpeed)
        if (speedBuffer.size > MAX_BUFFER_SIZE) {
            speedBuffer.removeFirst()
        }
        val avgSpeed = speedBuffer.average().toFloat()
        
        // Классификация с гистерезисом
        val newState = classifyMovement(avgSpeed)
        return applyHysteresis(newState, avgSpeed)
    }
    
    /**
     * Получение скорости: GPS или расчёт по дельте расстояния.
     */
    private fun getSpeed(location: Location): Float {
        // Если GPS отдаёт скорость (>= 0) и точность хорошая — используем её
        if (location.speed >= 0f && location.accuracy < ACCURACY_THRESHOLD) {
            return location.speed
        }
        
        // Иначе считаем по дельте расстояния
        return calculateSpeedFromDistance(location)
    }
    
    /**
     * Расчёт скорости по смещению между точками.
     */
    private fun calculateSpeedFromDistance(current: Location): Float {
        val last = lastLocation ?: run {
            // Первая точка — сохраняем и возвращаем 0
            lastLocation = Location(current)
            lastUpdateTime = current.time
            return 0f
        }
        
        val distance = last.distanceTo(current)  // метры
        val timeDelta = (current.time - lastUpdateTime) / 1000f  // секунды
        lastLocation = Location(current)
        lastUpdateTime = current.time
        
        return if (timeDelta > 0) distance / timeDelta else 0f
    }
    
    /**
     * Классификация движения по порогам скорости.
     */
    private fun classifyMovement(speed: Float): MovementState {
        return when {
            speed < SPEED_STATIONARY_THRESHOLD -> MovementState.STATIONARY
            speed < SPEED_WALKING_SLOW_THRESHOLD -> MovementState.WALKING_SLOW
            speed < SPEED_WALKING_FAST_THRESHOLD -> MovementState.WALKING_FAST
            else -> MovementState.VEHICLE
        }
    }
    
    /**
     * Гистерезис для предотвращения частых переключений.
     * Нужно превысить порог на 20% для перехода в следующее состояние.
     */
    private fun applyHysteresis(newState: MovementState, speed: Float): MovementState {
        if (newState != currentState) {
            val timeInState = nowMs() - stateEnterTime
            
            // Минимальное время в статусе (5 сек)
            if (timeInState < MIN_TIME_IN_STATE_MS) {
                return currentState
            }
            
            // Гистерезис: нужно превысить порог на 20% для повышения,
            // или упасть на 20% для понижения
            val shouldSwitch = when {
                newState.ordinal > currentState.ordinal -> {
                    // Переход в "быстрое" состояние — нужно превысить порог
                    speed > boundaryThresholdForState(currentState) * HYSTERESIS_FACTOR
                }
                newState.ordinal < currentState.ordinal -> {
                    // Переход в "медленное" состояние — нужно упасть ниже порога
                    speed < boundaryThresholdForState(newState) / HYSTERESIS_FACTOR
                }
                else -> true
            }
            
            if (!shouldSwitch) {
                return currentState
            }
            
            currentState = newState
            stateEnterTime = nowMs()
        }
        return currentState
    }
    
    private fun boundaryThresholdForState(state: MovementState): Float {
        return when (state) {
            MovementState.STATIONARY -> SPEED_STATIONARY_THRESHOLD
            MovementState.WALKING_SLOW -> SPEED_WALKING_SLOW_THRESHOLD
            MovementState.WALKING_FAST -> SPEED_WALKING_FAST_THRESHOLD
            MovementState.VEHICLE -> SPEED_WALKING_FAST_THRESHOLD
        }
    }
    
    /**
     * Сброс состояния (при остановке трекинга).
     */
    fun reset() {
        lastLocation = null
        lastUpdateTime = 0L
        speedBuffer.clear()
        currentState = MovementState.STATIONARY
        stateEnterTime = 0L
    }
    
    companion object {
        private const val MAX_BUFFER_SIZE = 5
        private const val ACCURACY_THRESHOLD = 50  // метров
        private const val MIN_TIME_IN_STATE_MS = 5_000L  // 5 секунд
        private const val HYSTERESIS_FACTOR = 1.2f  // 20% гистерезис
        
        // Пороги скорости (м/с)
        private const val SPEED_STATIONARY_THRESHOLD = 0.5f    // 0–2 км/ч
        private const val SPEED_WALKING_SLOW_THRESHOLD = 2.0f  // 2–7 км/ч
        private const val SPEED_WALKING_FAST_THRESHOLD = 5.0f  // 7–18 км/ч
    }
}
