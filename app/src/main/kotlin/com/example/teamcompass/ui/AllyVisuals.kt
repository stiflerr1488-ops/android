package com.example.teamcompass.ui

import com.example.teamcompass.core.PlayerState
import com.example.teamcompass.core.Role
import com.example.teamcompass.domain.CombatRole
import com.example.teamcompass.domain.TeamCommandRole
import com.example.teamcompass.domain.TeamMemberRoleProfile
import com.example.teamcompass.domain.TeamOrgPath
import com.example.teamcompass.domain.VehicleRole

enum class AllyRelation {
    TEAM,
    ALLY,
}

enum class AllyRoleIcon {
    COMMANDER,
    DEPUTY,
    ASSAULT,
    SCOUT,
    SNIPER,
    MORTAR,
    VEHICLE,
    FIGHTER,
}

data class AllyVisualDescriptor(
    val relation: AllyRelation,
    val roleIcon: AllyRoleIcon,
)

fun buildAllyVisualsByUid(
    players: List<PlayerState>,
    roleProfiles: List<TeamMemberRoleProfile>,
    selfUid: String?,
): Map<String, AllyVisualDescriptor> {
    if (players.isEmpty()) return emptyMap()

    val profilesByUid = roleProfiles.associateBy { it.uid }
    val selfPath = selfUid?.let { profilesByUid[it]?.orgPath?.normalized() }

    return players.associate { player ->
        val profile = profilesByUid[player.uid]
        player.uid to AllyVisualDescriptor(
            relation = resolveAllyRelation(selfPath, profile?.orgPath),
            roleIcon = resolveRoleIcon(profile, player.role),
        )
    }
}

private fun resolveAllyRelation(
    selfPathRaw: TeamOrgPath?,
    targetPathRaw: TeamOrgPath?,
): AllyRelation {
    if (selfPathRaw == null || targetPathRaw == null) return AllyRelation.TEAM

    val selfPath = selfPathRaw.normalized()
    val targetPath = targetPathRaw.normalized()

    if (selfPath.sideId != targetPath.sideId) return AllyRelation.ALLY

    val selfTeamId = selfPath.teamId?.trim().orEmpty().ifBlank { null }
    val targetTeamId = targetPath.teamId?.trim().orEmpty().ifBlank { null }
    if (selfTeamId != null && targetTeamId != null) {
        val sameTeam = selfPath.companyId == targetPath.companyId &&
            selfPath.platoonId == targetPath.platoonId &&
            selfTeamId == targetTeamId
        return if (sameTeam) AllyRelation.TEAM else AllyRelation.ALLY
    }

    // If one of teamIds is not set yet, keep teammate color as default.
    return AllyRelation.TEAM
}

private fun resolveRoleIcon(
    profile: TeamMemberRoleProfile?,
    fallbackRole: Role,
): AllyRoleIcon {
    val commandRole = profile?.commandRole ?: when (fallbackRole) {
        Role.COMMANDER -> TeamCommandRole.TEAM_COMMANDER
        Role.DEPUTY -> TeamCommandRole.TEAM_DEPUTY
        Role.FIGHTER -> TeamCommandRole.FIGHTER
    }
    val combatRole = profile?.combatRole ?: CombatRole.NONE
    val vehicleRole = profile?.vehicleRole ?: VehicleRole.NONE

    return when {
        vehicleRole == VehicleRole.DRIVER || vehicleRole == VehicleRole.ASSISTANT_DRIVER -> AllyRoleIcon.VEHICLE
        commandRole <= TeamCommandRole.TEAM_COMMANDER -> AllyRoleIcon.COMMANDER
        commandRole == TeamCommandRole.TEAM_DEPUTY -> AllyRoleIcon.DEPUTY
        combatRole == CombatRole.ASSAULTER -> AllyRoleIcon.ASSAULT
        combatRole == CombatRole.SCOUT -> AllyRoleIcon.SCOUT
        combatRole == CombatRole.SNIPER -> AllyRoleIcon.SNIPER
        combatRole == CombatRole.MORTAR -> AllyRoleIcon.MORTAR
        else -> AllyRoleIcon.FIGHTER
    }
}
