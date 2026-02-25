@file:android.annotation.SuppressLint("LocalContextResourcesRead")

package com.example.teamcompass.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.teamcompass.domain.isInActorScope
import com.example.teamcompass.ui.components.TeamRoleEditorDialog
import com.example.teamcompass.ui.components.TeamRosterItemUi
import com.example.teamcompass.ui.components.toShortUiLabel
import com.example.teamcompass.ui.components.toUiLabel
import com.example.teamcompass.ui.theme.AlphaTokens
import com.example.teamcompass.ui.theme.Spacing
import java.util.Locale
import kotlin.math.roundToInt

private data class BulkRoleTarget(
    val title: String,
    val targetUids: List<String>,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamStructureScreen(
    state: UiState,
    onBack: () -> Unit,
    onAssignTeamMemberRole: (targetUid: String, patch: TeamRolePatch) -> Unit,
    onAssignTeamMemberRolesBulk: (targetUids: List<String>, patch: TeamRolePatch) -> Unit,
) {
    val resources = LocalContext.current.resources
    val now = System.currentTimeMillis()
    val roleProfilesByUid = remember(state.roleProfiles) {
        state.roleProfiles.associateBy { it.uid }
    }
    val allMembers = remember(state.players, state.roleProfiles, state.me, now) {
        buildStructureMembers(state, roleProfilesByUid, now)
    }
    val myCommandRole = remember(state.uid, roleProfilesByUid, state.players) {
        val uid = state.uid ?: return@remember TeamCommandRole.FIGHTER
        roleProfilesByUid[uid]?.commandRole ?: when (state.players.firstOrNull { it.uid == uid }?.role) {
            Role.COMMANDER -> TeamCommandRole.TEAM_COMMANDER
            Role.DEPUTY -> TeamCommandRole.TEAM_DEPUTY
            else -> TeamCommandRole.FIGHTER
        }
    }
    val canEditRoles = remember(myCommandRole) { myCommandRole != TeamCommandRole.FIGHTER }
    val actorProfile = remember(state.uid, roleProfilesByUid, myCommandRole) {
        val uid = state.uid ?: return@remember null
        roleProfilesByUid[uid] ?: TeamMemberRoleProfile(
            uid = uid,
            commandRole = myCommandRole,
            orgPath = TeamOrgPath(),
        )
    }
    val canEditMember: (TeamRosterItemUi) -> Boolean = remember(actorProfile, canEditRoles) {
        { member ->
            if (!canEditRoles) {
                false
            } else {
                val actor = actorProfile
                if (actor == null) {
                    false
                } else {
                    isInActorScope(
                        actorRole = actor.commandRole,
                        actorPath = actor.orgPath.normalized(),
                        targetPath = member.orgPath.normalized(),
                    )
                }
            }
        }
    }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedMember by remember { mutableStateOf<TeamRosterItemUi?>(null) }
    var bulkRoleTarget by remember { mutableStateOf<BulkRoleTarget?>(null) }

    val filteredMembers = remember(allMembers, query) {
        val q = query.trim().lowercase(Locale.getDefault())
        if (q.isBlank()) allMembers
        else allMembers.filter { member ->
            member.callsign.lowercase(Locale.getDefault()).contains(q) ||
                member.commandRole.toUiLabel(resources).lowercase(Locale.getDefault()).contains(q) ||
                member.orgPath.toShortUiLabel(resources).lowercase(Locale.getDefault()).contains(q)
        }
    }

    val membersBySide = remember(filteredMembers) {
        filteredMembers
            .groupBy { it.orgPath.normalized().sideId.ifBlank { TeamOrgPath.DEFAULT_SIDE_ID } }
            .toSortedMap()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.team_structure_title), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.nav_back))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            item("summary") {
                Card(
                    shape = RoundedCornerShape(Spacing.lg),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = AlphaTokens.overlayStrong),
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        Text(
                            stringResource(R.string.team_structure_summary_format, membersBySide.size, filteredMembers.size),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            stringResource(R.string.team_structure_my_role_format, myCommandRole.toUiLabel(resources)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = query,
                            onValueChange = { query = it.take(32) },
                            label = { Text(stringResource(R.string.team_structure_search_label)) },
                            singleLine = true,
                        )
                    }
                }
            }

            if (filteredMembers.isEmpty()) {
                item("empty") {
                    Card(
                        shape = RoundedCornerShape(Spacing.lg),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(modifier = Modifier.padding(Spacing.md)) {
                            Text(stringResource(R.string.team_structure_not_found_title), fontWeight = FontWeight.SemiBold)
                            Text(
                                stringResource(R.string.team_structure_not_found_message),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                membersBySide.forEach { (sideId, sideMembers) ->
                    item("side_$sideId") {
                        SideTreeSection(
                            sideId = sideId,
                            members = sideMembers,
                            canEditMember = canEditMember,
                            onMemberClick = { member -> selectedMember = member },
                            onBulkEdit = { title, targetMembers ->
                                val targetUids = targetMembers.map { it.uid }.distinct()
                                if (targetUids.isNotEmpty()) {
                                    bulkRoleTarget = BulkRoleTarget(
                                        title = title,
                                        targetUids = targetUids,
                                    )
                                }
                            },
                        )
                    }
                }
            }

            item("bottom_space") { Spacer(Modifier.height(Spacing.md)) }
        }
    }

    selectedMember?.let { member ->
        TeamRoleEditorDialog(
            member = member,
            actorCommandRole = myCommandRole,
            onDismiss = { selectedMember = null },
            onSave = { patch ->
                selectedMember = null
                if (patch.commandRole == null && patch.combatRole == null && patch.vehicleRole == null && patch.orgPath == null) {
                    return@TeamRoleEditorDialog
                }
                onAssignTeamMemberRole(member.uid, patch)
            },
        )
    }

    bulkRoleTarget?.let { target ->
        TeamRoleBulkDialog(
            title = target.title,
            editableCount = target.targetUids.size,
            actorCommandRole = myCommandRole,
            onDismiss = { bulkRoleTarget = null },
            onApply = { patch ->
                bulkRoleTarget = null
                if (patch.commandRole == null && patch.combatRole == null && patch.vehicleRole == null && patch.orgPath == null) {
                    return@TeamRoleBulkDialog
                }
                onAssignTeamMemberRolesBulk(target.targetUids, patch)
            },
        )
    }
}

