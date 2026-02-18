package com.example.teamcompass.core

class CompassCalculator {
    fun buildTargets(
        me: LocationPoint,
        myHeadingDeg: Double,
        others: List<PlayerState>,
        nowMs: Long,
    ): List<CompassTarget> {
        return others.mapNotNull { player ->
            val ageSec = ((nowMs - player.point.timestampMs).coerceAtLeast(0) / 1000)
            val staleness = StalenessPolicy.classify(ageSec)
            val sosActive = player.sosUntilMs > nowMs
            if (staleness == Staleness.HIDDEN) {
                return@mapNotNull CompassTarget(
                    uid = player.uid,
                    nick = player.nick,
                    distanceMeters = GeoMath.distanceMeters(me, player.point),
                    relativeBearingDeg = 0.0,
                    staleness = staleness,
                    lowAccuracy = player.point.accMeters > 50,
                    lastSeenSec = ageSec,
                    mode = player.mode,
                    anchored = player.anchored,
                    sosActive = sosActive,
                )
            }

            val bearing = GeoMath.bearingDegrees(me, player.point)
            val relative = GeoMath.normalizeRelativeDegrees(bearing - myHeadingDeg)
            CompassTarget(
                uid = player.uid,
                nick = player.nick,
                distanceMeters = GeoMath.distanceMeters(me, player.point),
                relativeBearingDeg = relative,
                staleness = staleness,
                lowAccuracy = player.point.accMeters > 50,
                lastSeenSec = ageSec,
                mode = player.mode,
                anchored = player.anchored,
                sosActive = sosActive,
            )
        }
    }
}
