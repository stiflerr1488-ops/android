package com.example.teamcompass.core

enum class TargetFilterPreset {
    ALL,
    SOS,
    NEAR,
    ACTIVE,
    ;

    companion object {
        fun fromRaw(value: String?): TargetFilterPreset {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ALL
        }
    }
}

data class TargetFilterState(
    val preset: TargetFilterPreset = TargetFilterPreset.ALL,
    val nearRadiusM: Int = 150,
    val showDead: Boolean = true,
    val showStale: Boolean = true,
    val focusMode: Boolean = false,
)

data class PrioritizedTarget(
    val target: CompassTarget,
    val priorityScore: Int,
)

object TargetPrioritizer {
    private const val SOS_BONUS = 1_000
    private const val FRESH_BONUS = 300
    private const val SUSPECT_BONUS = 120
    private const val STALE_BONUS = 30
    private const val HIDDEN_BONUS = 0
    private const val LOW_ACCURACY_PENALTY = -80
    private const val DEAD_PENALTY = -150
    private const val DISTANCE_BONUS_NEAR = 120
    private const val DISTANCE_BONUS_MEDIUM = 60

    fun prioritize(targets: List<CompassTarget>): List<PrioritizedTarget> {
        return targets
            .map { target ->
                PrioritizedTarget(
                    target = target,
                    priorityScore = score(target),
                )
            }
            .sortedWith(
                compareByDescending<PrioritizedTarget> { it.priorityScore }
                    .thenBy { it.target.distanceMeters }
                    .thenBy { it.target.lastSeenSec }
                    .thenBy { it.target.nick.lowercase() }
            )
    }

    fun applyFilters(targets: List<PrioritizedTarget>, state: TargetFilterState): List<PrioritizedTarget> {
        val nearRadius = state.nearRadiusM.coerceIn(50, 500)

        val filtered = targets
            .asSequence()
            .filter { prioritized ->
                val target = prioritized.target
                if (!state.showDead && target.mode == PlayerMode.DEAD) return@filter false
                if (!state.showStale && target.staleness in setOf(Staleness.STALE, Staleness.HIDDEN)) return@filter false
                when (state.preset) {
                    TargetFilterPreset.ALL -> true
                    TargetFilterPreset.SOS -> target.sosActive
                    TargetFilterPreset.NEAR -> target.distanceMeters <= nearRadius
                    TargetFilterPreset.ACTIVE -> target.mode == PlayerMode.GAME && target.staleness != Staleness.HIDDEN
                }
            }
            .toList()

        if (!state.focusMode) return filtered

        val alwaysVisibleSos = filtered.filter { it.target.sosActive }
        if (alwaysVisibleSos.size >= 8) return alwaysVisibleSos.take(8)

        val sosIds = alwaysVisibleSos.mapTo(HashSet()) { it.target.uid }
        val topNonSos = filtered
            .asSequence()
            .filterNot { it.target.uid in sosIds }
            .take(8 - alwaysVisibleSos.size)
            .toList()
        return alwaysVisibleSos + topNonSos
    }

    private fun score(target: CompassTarget): Int {
        val stalenessScore = when (target.staleness) {
            Staleness.FRESH -> FRESH_BONUS
            Staleness.SUSPECT -> SUSPECT_BONUS
            Staleness.STALE -> STALE_BONUS
            Staleness.HIDDEN -> HIDDEN_BONUS
        }
        val distanceScore = when {
            target.distanceMeters <= 100.0 -> DISTANCE_BONUS_NEAR
            target.distanceMeters <= 250.0 -> DISTANCE_BONUS_MEDIUM
            else -> 0
        }
        val sosScore = if (target.sosActive) SOS_BONUS else 0
        val accuracyPenalty = if (target.lowAccuracy) LOW_ACCURACY_PENALTY else 0
        val deadPenalty = if (target.mode == PlayerMode.DEAD) DEAD_PENALTY else 0
        return sosScore + stalenessScore + distanceScore + accuracyPenalty + deadPenalty
    }
}
