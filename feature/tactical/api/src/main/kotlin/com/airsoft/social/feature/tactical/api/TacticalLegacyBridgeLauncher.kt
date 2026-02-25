package com.airsoft.social.feature.tactical.api

fun interface TacticalLegacyBridgeLauncher {
    fun openLegacyTactical()
}

object NoopTacticalLegacyBridgeLauncher : TacticalLegacyBridgeLauncher {
    override fun openLegacyTactical() = Unit
}
