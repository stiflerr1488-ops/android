package com.example.teamcompass.core

class CompassCalculator {
    fun buildTargets(
        me: LocationPoint,
        myHeadingDeg: Double,
        others: List<PlayerState>,
        nowMs: Long,
    ): List<CompassTarget> {
        return others.map { player ->
            val ageSec = ((nowMs - player.point.timestampMs).coerceAtLeast(0) / 1000)
            val staleness = StalenessPolicy.classify(ageSec)
            val distanceMeters = GeoMath.distanceMeters(me, player.point)
            val bearing = GeoMath.bearingDegrees(me, player.point)
            val relative = GeoMath.normalizeRelativeDegrees(bearing - myHeadingDeg)

            CompassTarget(
                uid = player.uid,
                nick = player.nick,
                distanceMeters = distanceMeters,
                relativeBearingDeg = relative,
                staleness = staleness,
                lowAccuracy = player.point.accMeters > 50,
                lastSeenSec = ageSec,
            )
        }
    }
}
