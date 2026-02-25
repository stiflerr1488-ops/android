package com.example.teamcompass.core.p2p

class SequenceGenerator(
    initialValue: Int = 0,
) {
    private var current: Int = initialValue.coerceAtLeast(0)

    @Synchronized
    fun next(): Int {
        val value = current
        current = if (current == Int.MAX_VALUE) 0 else current + 1
        return value
    }
}
