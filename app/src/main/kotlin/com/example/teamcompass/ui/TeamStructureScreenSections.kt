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
@Composable
internal fun SideTreeSection(
    sideId: String,
    members: List<TeamRosterItemUi>,
    canEditMember: (TeamRosterItemUi) -> Boolean,
    onMemberClick: (TeamRosterItemUi) -> Unit,
    onBulkEdit: (title: String, targetMembers: List<TeamRosterItemUi>) -> Unit,
) {
    val resources = LocalContext.current.resources
    val sideTitle = stringResource(R.string.team_structure_side_format, sideId)
    val editableSideMembers = remember(members) { members.filter(canEditMember) }
    Card(
        shape = RoundedCornerShape(Spacing.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Text(
                    sideTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (editableSideMembers.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { onBulkEdit(sideTitle, editableSideMembers) },
                    ) {
                        Text(stringResource(R.string.team_structure_bulk))
                    }
                }
            }
            Text(
                stringResource(R.string.team_structure_members_count_format, members.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            val byCompany = remember(members) {
                members.groupBy { normalizeLevelId(it.orgPath.normalized().companyId) }
            }
            orderedNullableIds(byCompany.keys).forEach { companyId ->
                val companyMembers = byCompany[companyId].orEmpty()
                LevelSection(
                    title = if (companyId == null) {
                        stringResource(R.string.team_structure_side_hq)
                    } else {
                        resources.getString(R.string.team_structure_company_format, companyId)
                    },
                    members = companyMembers,
                    nextLevelTitle = { platoonId ->
                        if (platoonId == null) {
                            resources.getString(R.string.team_structure_no_platoon)
                        } else {
                            resources.getString(R.string.team_structure_platoon_format, platoonId)
                        }
                    },
                    nextLevelId = { it.orgPath.normalized().platoonId },
                    canEditMember = canEditMember,
                    onMemberClick = onMemberClick,
                    onBulkEdit = onBulkEdit,
                    ) { platoonMembers, canEdit, onClick ->
                        LevelSection(
                            title = "",
                            members = platoonMembers,
                            nextLevelTitle = { teamId ->
                                if (teamId == null) {
                                    resources.getString(R.string.team_structure_no_team)
                                } else {
                                    resources.getString(R.string.team_structure_team_format, teamId)
                                }
                            },
                            nextLevelId = { it.orgPath.normalized().teamId },
                            canEditMember = canEdit,
                        onMemberClick = onClick,
                        onBulkEdit = onBulkEdit,
                    ) { teamMembers, canEditInTeam, onClickInTeam ->
                        LevelSection(
                            title = "",
                            members = teamMembers,
                            nextLevelTitle = { vehicleId ->
                                if (vehicleId == null) {
                                    resources.getString(R.string.team_structure_foot_group)
                                } else {
                                    resources.getString(R.string.team_structure_vehicle_format, vehicleId)
                                }
                            },
                            nextLevelId = { it.orgPath.normalized().vehicleId },
                            canEditMember = canEditInTeam,
                            onMemberClick = onClickInTeam,
                            onBulkEdit = onBulkEdit,
                            nestedContent = { leafMembers, canEditLeaf, onClickLeaf ->
                                MembersBlock(
                                    members = leafMembers,
                                    canEditMember = canEditLeaf,
                                    onMemberClick = onClickLeaf,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelSection(
    title: String,
    members: List<TeamRosterItemUi>,
    nextLevelTitle: (String?) -> String,
    nextLevelId: (TeamRosterItemUi) -> String?,
    canEditMember: (TeamRosterItemUi) -> Boolean,
    onMemberClick: (TeamRosterItemUi) -> Unit,
    onBulkEdit: (title: String, targetMembers: List<TeamRosterItemUi>) -> Unit,
    nestedContent: @Composable (List<TeamRosterItemUi>, (TeamRosterItemUi) -> Boolean, (TeamRosterItemUi) -> Unit) -> Unit,
) {
    if (members.isEmpty()) return

    val blockModifier = Modifier
        .fillMaxWidth()
        .padding(start = 8.dp)

    Column(modifier = blockModifier, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        val byLevel = remember(members) {
            members.groupBy { normalizeLevelId(nextLevelId(it)) }
        }
        orderedNullableIds(byLevel.keys).forEach { levelId ->
            val levelMembers = byLevel[levelId].orEmpty()
            val editableLevelMembers = remember(levelMembers) { levelMembers.filter(canEditMember) }
            val currentLevelTitle = nextLevelTitle(levelId)
            Card(
                shape = RoundedCornerShape(Spacing.md),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f),
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        Text(
                            text = "$currentLevelTitle • ${levelMembers.size}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        if (editableLevelMembers.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { onBulkEdit(currentLevelTitle, editableLevelMembers) },
                            ) {
                                Text(stringResource(R.string.team_structure_bulk))
                            }
                        }
                    }
                    nestedContent(levelMembers, canEditMember, onMemberClick)
                }
            }
        }
    }
}

@Composable
private fun MembersBlock(
    members: List<TeamRosterItemUi>,
    canEditMember: (TeamRosterItemUi) -> Boolean,
    onMemberClick: (TeamRosterItemUi) -> Unit,
) {
    val sortedMembers = remember(members) { members.sortedWith(memberSortComparator()) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        sortedMembers.forEach { member ->
            val canEditRoles = canEditMember(member)
            MemberRow(
                member = member,
                canEditRoles = canEditRoles,
                onClick = {
                    if (canEditRoles) onMemberClick(member)
                },
            )
        }
    }
}

@Composable
private fun MemberRow(
    member: TeamRosterItemUi,
    canEditRoles: Boolean,
    onClick: () -> Unit,
) {
    val resources = LocalContext.current.resources
    val distanceLabel = member.distanceMeters?.roundToInt()
        ?.let { stringResource(R.string.distance_m_format, it) }
        ?: stringResource(R.string.placeholder_dash)
    val status = when {
        member.sosActive -> stringResource(R.string.label_sos)
        member.isDead -> stringResource(R.string.status_dead_short)
        else -> stringResource(R.string.team_structure_status_ok)
    }
    val roles = buildString {
        append(member.commandRole.toUiLabel(resources))
        if (member.combatRole != CombatRole.NONE) append(" / ${member.combatRole.toUiLabel(resources)}")
        if (member.vehicleRole != VehicleRole.NONE) append(" / ${member.vehicleRole.toUiLabel(resources)}")
    }
    val modifier = if (canEditRoles) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = member.callsign,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "$roles • $distanceLabel • $status",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (canEditRoles) {
            FilledTonalButton(onClick = onClick) { Text(stringResource(R.string.team_structure_role_button)) }
        }
    }
}

