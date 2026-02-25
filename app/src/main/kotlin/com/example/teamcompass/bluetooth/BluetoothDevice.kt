package com.example.teamcompass.bluetooth

/**
 * Тип Bluetooth-устройства
 */
enum class DeviceType {
    PHONE,
    HEADPHONES,
    WATCH,
    TABLET,
    LAPTOP,
    UNKNOWN
}

/**
 * Bluetooth-устройство, обнаруженное при сканировании
 */
data class BluetoothDevice(
    val address: String,           // MAC-адрес
    val name: String,              // Название (может быть "Unknown")
    val rssi: Int,                 // Сила сигнала (dBm, обычно -100 до -20)
    val timestamp: Long,           // Время обнаружения (epoch ms)
    val type: DeviceType = DeviceType.UNKNOWN,  // Тип устройства
    val serviceUuids: List<String> = emptyList() // UUID сервисов (для классификации)
) {
    /**
     * Расчёт расстояния по RSSI (приблизительно)
     * Формула: distance = 10^((txPower - RSSI) / (10 * n))
     * где txPower ≈ -59 dBm (на 1 метре), n ≈ 2 (на открытом пространстве)
     */
    fun calculateDistanceMeters(): Double {
        val txPower = -59  // Калиброванное значение на 1 метре
        val n = 2.0  // Коэффициент среды (2 = открытый воздух)
        
        if (rssi == 0) return -1.0  // Неизвестно
        
        val ratio = rssi * 1.0 / txPower
        return if (ratio < 1.0) {
            // Близко (< 1 метра)
            Math.pow(ratio, 10.0)
        } else {
            // Дальше (> 1 метра)
            Math.pow(0.89976, ratio) * Math.pow(11.443, 1.0 / ratio)
        }
    }
    
    /**
     * Проверка: устройство устарело (прошло больше 30 секунд)
     */
    fun isExpired(now: Long = System.currentTimeMillis()): Boolean {
        return (now - timestamp) > 30_000L
    }
    
    /**
     * Возраст устройства в секундах
     */
    fun ageSeconds(now: Long = System.currentTimeMillis()): Long {
        return (now - timestamp) / 1000L
    }
}

/**
 * Результат сканирования Bluetooth
 */
data class BluetoothScanResult(
    val devices: List<BluetoothDevice>,
    val scannedAt: Long,
    val expiresAt: Long  // Когда результаты устареют
) {
    /**
     * Получить только актуальные устройства (не устаревшие)
     */
    fun getActiveDevices(now: Long = System.currentTimeMillis()): List<BluetoothDevice> {
        return devices.filter { !it.isExpired(now) }
    }
    
    /**
     * Проверка: результаты устарели
     */
    fun isExpired(now: Long = System.currentTimeMillis()): Boolean {
        return now > expiresAt
    }
    
    /**
     * Осталось времени до истечения (в секундах)
     */
    fun remainingSeconds(now: Long = System.currentTimeMillis()): Long {
        return ((expiresAt - now) / 1000L).coerceAtLeast(0)
    }
}
