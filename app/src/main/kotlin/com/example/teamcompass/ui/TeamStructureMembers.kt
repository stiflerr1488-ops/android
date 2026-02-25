@file:android.annotation.SuppressLint("LocalContextResourcesRead")

package com.example.teamcompass.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teamcompass.R
import com.example.teamcompass.core.GeoMath
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.core.Role
import com.example.teamcompass.domain.CombatRole
import com.example.teamcompass.domain.TeamCommandRole
import com.example.teamcompass.domain.TeamMemberRoleProfile
import com.example.teamcompass.domain.TeamOrgPath
import com.example.teamcompass.domain.TeamRolePatch
import com.example.teamcompass.domain.VehicleRole
import com.example.teamcompass.ui.components.TeamRosterItemUi
import com.example.teamcompass.ui.components.toUiLabel
import com.example.teamcompass.ui.theme.Spacing
import java.util.Locale
import kotlin.math.roundToInt

internal fun buildStructureMembers(
    state: UiState,
    roleProfilesByUid: Map<String, TeamMemberRoleProfile>,
    now: Long,
): List<TeamRosterItemUi> {
    val me = state.me
    val playersByUid = state.players.associateBy { it.uid }
    val allUids = (playersByUid.keys + roleProfilesByUid.keys).toSortedSet()

    return allUids.map { uid ->
        val player = playersByUid[uid]
        val profile = roleProfilesByUid[uid]
        val commandRole = profile?.commandRole ?: when (player?.role) {
            Role.COMMANDER -> TeamCommandRole.TEAM_COMMANDER
            Role.DEPUTY -> TeamCommandRole.TEAM_DEPUTY
            else -> TeamCommandRole.FIGHTER
        }
        TeamRosterItemUi(
            uid = uid,
            callsign = player?.nick ?: profile?.callsign ?: uid.take(8),
            distanceMeters = if (player != null && me != null) {
                GeoMath.distanceMeters(me, player.point)
            } else {
                null
            },
            isCommander = commandRole <= TeamCommandRole.TEAM_COMMANDER,
            isDead = player?.mode == PlayerMode.DEAD,
            sosActive = (player?.sosUntilMs ?: 0L) > now,
            commandRole = commandRole,
            combatRole = profile?.combatRole ?: CombatRole.NONE,
            vehicleRole = profile?.vehicleRole ?: VehicleRole.NONE,
            orgPath = profile?.orgPath ?: TeamOrgPath(),
        )
    }.sortedWith(memberSortComparator())
}

internal fun memberSortComparator(): Comparator<TeamRosterItemUi> {
    return compareBy<TeamRosterItemUi> { it.commandRole.rank }
        .thenBy { it.orgPath.normalized().sideId }
        .thenBy { it.orgPath.normalized().companyId ?: "" }
        .thenBy { it.orgPath.normalized().platoonId ?: "" }
        .thenBy { it.orgPath.normalized().teamId ?: "" }
        .thenBy { it.orgPath.normalized().vehicleId ?: "" }
        .thenBy { it.callsign.lowercase(Locale.getDefault()) }
}

internal fun normalizeLevelId(raw: String?): String? = raw?.trim()?.ifBlank { null }

internal fun orderedNullableIds(keys: Set<String?>): List<String?> {
    return keys.sortedWith(compareBy<String?>({ it != null }, { it ?: "" }))
}
