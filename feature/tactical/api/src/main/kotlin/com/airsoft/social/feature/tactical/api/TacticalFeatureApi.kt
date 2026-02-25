package com.airsoft.social.feature.tactical.api
data class TacticalFeatureContract(
    val route: String,
    val title: String,
)
interface TacticalFeatureEntry {
    val contract: TacticalFeatureContract
}
object TacticalFeatureApi {
    const val ROUTE: String = "tactical"
    val contract: TacticalFeatureContract = TacticalFeatureContract(
        route = ROUTE,
        title = "Tactical",
    )
}
