package com.example.teamcompass

import com.example.teamcompass.core.CompassCalculator
import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.core.PlayerState

fun main() {
    val now = System.currentTimeMillis()
    val me = LocationPoint(55.7558, 37.6176, 7.0, 1.5, 25.0, now)
    val team = listOf(
        PlayerState("u1", "Скаут", LocationPoint(55.7562, 37.6188, 8.0, 0.8, null, now - 12_000)),
        PlayerState("u2", "Ведущий", LocationPoint(55.7520, 37.6200, 60.0, 0.4, null, now - 45_000)),
        PlayerState("u3", "Медик", LocationPoint(55.7500, 37.6100, 12.0, 0.6, null, now - 130_000)),
    )

    val targets = CompassCalculator().buildTargets(me, myHeadingDeg = 30.0, others = team, nowMs = now)

    println("=== Team Compass MVP prototype ===")
    for (t in targets) {
        println("${t.nick}: ${"%.0f".format(t.distanceMeters)}m, rel=${"%.0f".format(t.relativeBearingDeg)}°, status=${t.staleness}, lowAcc=${t.lowAccuracy}, lastSeen=${t.lastSeenSec}s")
    }
}
