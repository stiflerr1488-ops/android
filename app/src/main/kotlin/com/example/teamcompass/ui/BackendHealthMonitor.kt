package com.example.teamcompass.ui

import com.example.teamcompass.domain.TeamRepository
import kotlinx.coroutines.flow.collectLatest

internal class BackendHealthMonitor(
    private val teamRepository: TeamRepository,
) {
    suspend fun collect(
        onAvailabilitySample: suspend (available: Boolean, previous: Boolean?) -> Unit,
    ) {
        var previousAvailability: Boolean? = null
        teamRepository.observeBackendHealth().collectLatest { available ->
            onAvailabilitySample(available, previousAvailability)
            previousAvailability = available
        }
    }
}

