package com.example.teamcompass.ui

import com.example.teamcompass.core.CompassCalculator
import com.example.teamcompass.core.PrioritizedTarget
import com.example.teamcompass.core.TargetFilterPreset
import com.example.teamcompass.core.TargetFilterState
import com.example.teamcompass.core.TargetPrioritizer
import com.example.teamcompass.domain.TeamMemberPrefs

internal class TargetFilterCoordinator(
    private val tacticalFiltersEnabled: Boolean,
    private val calculator: CompassCalculator = CompassCalculator(),
) {
    fun buildTargetsForState(
        state: UiState,
        nowMs: Long,
    ): Pair<List<PrioritizedTarget>, List<com.example.teamcompass.core.CompassTarget>> {
        val me = state.me ?: return emptyList<PrioritizedTarget>() to emptyList()
        val heading = state.myHeadingDeg ?: 0.0
        val targets = calculator.buildTargets(me, heading, state.players, nowMs)
        if (!tacticalFiltersEnabled) {
            return targets.map { PrioritizedTarget(it, 0) } to targets
        }
        val prioritized = TargetPrioritizer.prioritize(targets)
        val filtered = TargetPrioritizer.applyFilters(prioritized, state.targetFilterState)
        return prioritized to filtered.map { it.target }
    }

    fun fromRemotePrefs(remotePrefs: TeamMemberPrefs?): TargetFilterState {
        if (remotePrefs == null) return TargetFilterState()
        return TargetFilterState(
            preset = TargetFilterPreset.fromRaw(remotePrefs.preset),
            nearRadiusM = remotePrefs.nearRadiusM.coerceIn(50, 500),
            showDead = remotePrefs.showDead,
            showStale = remotePrefs.showStale,
            focusMode = remotePrefs.focusMode,
        )
    }

    fun toMemberPrefs(filterState: TargetFilterState): TeamMemberPrefs {
        return TeamMemberPrefs(
            preset = filterState.preset.name,
            nearRadiusM = filterState.nearRadiusM.coerceIn(50, 500),
            showDead = filterState.showDead,
            showStale = filterState.showStale,
            focusMode = filterState.focusMode,
            updatedAtMs = System.currentTimeMillis(),
        )
    }
}
