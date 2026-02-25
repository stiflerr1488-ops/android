package com.example.teamcompass.domain

enum class TeamCommandRole(val rank: Int) {
    SIDE_COMMANDER(0),
    COMPANY_COMMANDER(1),
    PLATOON_COMMANDER(2),
    TEAM_COMMANDER(3),
    TEAM_DEPUTY(4),
    FIGHTER(5),
}

enum class CombatRole {
    NONE,
    ASSAULTER,
    SCOUT,
    SNIPER,
    MORTAR,
}

enum class VehicleRole {
    NONE,
    DRIVER,
    ASSISTANT_DRIVER,
    PASSENGER,
}

data class TeamOrgPath(
    val sideId: String = DEFAULT_SIDE_ID,
    val companyId: String? = null,
    val platoonId: String? = null,
    val teamId: String? = null,
    val vehicleId: String? = null,
) {
    fun normalized(): TeamOrgPath = copy(
        sideId = sideId.ifBlank { DEFAULT_SIDE_ID },
        companyId = companyId?.trim()?.ifBlank { null },
        platoonId = platoonId?.trim()?.ifBlank { null },
        teamId = teamId?.trim()?.ifBlank { null },
        vehicleId = vehicleId?.trim()?.ifBlank { null },
    )

    fun isCompatibleWith(role: TeamCommandRole): Boolean {
        if (sideId.isBlank()) return false
        return when (role) {
            TeamCommandRole.SIDE_COMMANDER -> true
            TeamCommandRole.COMPANY_COMMANDER -> !companyId.isNullOrBlank()
            TeamCommandRole.PLATOON_COMMANDER -> !companyId.isNullOrBlank() && !platoonId.isNullOrBlank()
            TeamCommandRole.TEAM_COMMANDER,
            TeamCommandRole.TEAM_DEPUTY,
            TeamCommandRole.FIGHTER,
            -> !companyId.isNullOrBlank() && !platoonId.isNullOrBlank() && !teamId.isNullOrBlank()
        }
    }

    companion object {
        const val DEFAULT_SIDE_ID = "SIDE-1"
    }
}

data class TeamMemberRoleProfile(
    val uid: String,
    val callsign: String? = null,
    val commandRole: TeamCommandRole = TeamCommandRole.FIGHTER,
    val combatRole: CombatRole = CombatRole.NONE,
    val vehicleRole: VehicleRole = VehicleRole.NONE,
    val orgPath: TeamOrgPath = TeamOrgPath(),
    val updatedAtMs: Long = 0L,
    val updatedBy: String? = null,
)

data class TeamRolePatch(
    val commandRole: TeamCommandRole? = null,
    val combatRole: CombatRole? = null,
    val vehicleRole: VehicleRole? = null,
    val orgPath: TeamOrgPath? = null,
)

internal fun TeamMemberRoleProfile.applyPatch(
    patch: TeamRolePatch,
    updatedAtMs: Long,
    updatedBy: String,
): TeamMemberRoleProfile {
    val nextPath = (patch.orgPath ?: orgPath).normalized()
    return copy(
        commandRole = patch.commandRole ?: commandRole,
        combatRole = patch.combatRole ?: combatRole,
        vehicleRole = patch.vehicleRole ?: vehicleRole,
        orgPath = nextPath,
        updatedAtMs = updatedAtMs,
        updatedBy = updatedBy,
    )
}

internal fun canAssignRole(
    actor: TeamMemberRoleProfile,
    nextProfile: TeamMemberRoleProfile,
    patch: TeamRolePatch,
): Boolean {
    val actorRole = actor.commandRole
    val actorPath = actor.orgPath.normalized()
    val targetPath = nextProfile.orgPath.normalized()

    if (!targetPath.isCompatibleWith(nextProfile.commandRole)) return false
    if (!isInActorScope(actorRole, actorPath, targetPath)) return false

    val triesToChangeCommandRole = patch.commandRole != null
    val triesToChangeOrgPath = patch.orgPath != null
    val triesToChangeVehicleRole = patch.vehicleRole != null
    val triesToChangeCombatRole = patch.combatRole != null

    if (actorRole == TeamCommandRole.TEAM_DEPUTY) {
        return triesToChangeCombatRole && !triesToChangeCommandRole && !triesToChangeOrgPath && !triesToChangeVehicleRole
    }

    if (triesToChangeCommandRole) {
        if (!actorRole.canAssignCommandHierarchy()) return false
        if (nextProfile.commandRole.rank < actorRole.rank) return false
    }

    return true
}

internal fun TeamCommandRole.canAssignCommandHierarchy(): Boolean {
    return when (this) {
        TeamCommandRole.SIDE_COMMANDER,
        TeamCommandRole.COMPANY_COMMANDER,
        TeamCommandRole.PLATOON_COMMANDER,
        TeamCommandRole.TEAM_COMMANDER,
        -> true

        TeamCommandRole.TEAM_DEPUTY,
        TeamCommandRole.FIGHTER,
        -> false
    }
}

internal fun isInActorScope(
    actorRole: TeamCommandRole,
    actorPath: TeamOrgPath,
    targetPath: TeamOrgPath,
): Boolean {
    if (actorPath.sideId != targetPath.sideId) return false
    return when (actorRole) {
        TeamCommandRole.SIDE_COMMANDER -> true
        TeamCommandRole.COMPANY_COMMANDER -> {
            !actorPath.companyId.isNullOrBlank() &&
                actorPath.companyId == targetPath.companyId
        }
        TeamCommandRole.PLATOON_COMMANDER -> {
            !actorPath.companyId.isNullOrBlank() &&
                !actorPath.platoonId.isNullOrBlank() &&
                actorPath.companyId == targetPath.companyId &&
                actorPath.platoonId == targetPath.platoonId
        }
        TeamCommandRole.TEAM_COMMANDER,
        TeamCommandRole.TEAM_DEPUTY,
        TeamCommandRole.FIGHTER,
        -> {
            !actorPath.companyId.isNullOrBlank() &&
                !actorPath.platoonId.isNullOrBlank() &&
                !actorPath.teamId.isNullOrBlank() &&
                actorPath.companyId == targetPath.companyId &&
                actorPath.platoonId == targetPath.platoonId &&
                actorPath.teamId == targetPath.teamId
        }
    }
}
