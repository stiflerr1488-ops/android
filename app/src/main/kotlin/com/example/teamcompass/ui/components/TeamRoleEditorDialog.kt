@file:android.annotation.SuppressLint("LocalContextResourcesRead")

package com.example.teamcompass.ui.components

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teamcompass.R
import com.example.teamcompass.domain.CombatRole
import com.example.teamcompass.domain.TeamCommandRole
import com.example.teamcompass.domain.TeamOrgPath
import com.example.teamcompass.domain.TeamRolePatch
import com.example.teamcompass.domain.VehicleRole
import com.example.teamcompass.ui.theme.Spacing

@StringRes
fun TeamCommandRole.toUiLabelRes(): Int = when (this) {
    TeamCommandRole.SIDE_COMMANDER -> R.string.role_side_commander
    TeamCommandRole.COMPANY_COMMANDER -> R.string.role_company_commander
    TeamCommandRole.PLATOON_COMMANDER -> R.string.role_platoon_commander
    TeamCommandRole.TEAM_COMMANDER -> R.string.role_team_commander
    TeamCommandRole.TEAM_DEPUTY -> R.string.role_team_deputy
    TeamCommandRole.FIGHTER -> R.string.role_fighter
}

fun TeamCommandRole.toUiLabel(resources: Resources): String = resources.getString(toUiLabelRes())

@StringRes
fun CombatRole.toUiLabelRes(): Int = when (this) {
    CombatRole.NONE -> R.string.combat_role_none
    CombatRole.ASSAULTER -> R.string.combat_role_assaulter
    CombatRole.SCOUT -> R.string.combat_role_scout
    CombatRole.SNIPER -> R.string.combat_role_sniper
    CombatRole.MORTAR -> R.string.combat_role_mortar
}

fun CombatRole.toUiLabel(resources: Resources): String = resources.getString(toUiLabelRes())

@StringRes
fun VehicleRole.toUiLabelRes(): Int = when (this) {
    VehicleRole.NONE -> R.string.vehicle_role_none
    VehicleRole.DRIVER -> R.string.vehicle_role_driver
    VehicleRole.ASSISTANT_DRIVER -> R.string.vehicle_role_assistant_driver
    VehicleRole.PASSENGER -> R.string.vehicle_role_passenger
}

fun VehicleRole.toUiLabel(resources: Resources): String = resources.getString(toUiLabelRes())

fun TeamOrgPath.toShortUiLabel(resources: Resources): String {
    val normalized = normalized()
    val parts = mutableListOf(resources.getString(R.string.org_side_format, normalized.sideId))
    normalized.companyId?.let { parts += resources.getString(R.string.org_company_format, it) }
    normalized.platoonId?.let { parts += resources.getString(R.string.org_platoon_format, it) }
    normalized.teamId?.let { parts += resources.getString(R.string.org_team_format, it) }
    normalized.vehicleId?.let { parts += resources.getString(R.string.org_vehicle_format, it) }
    return parts.joinToString(separator = " • ")
}

@Composable
fun TeamRoleEditorDialog(
    member: TeamRosterItemUi,
    actorCommandRole: TeamCommandRole,
    onDismiss: () -> Unit,
    onSave: (TeamRolePatch) -> Unit,
) {
    val resources = LocalContext.current.resources
    var commandRole by remember(member.uid) { mutableStateOf(member.commandRole) }
    var combatRole by remember(member.uid) { mutableStateOf(member.combatRole) }
    var vehicleRole by remember(member.uid) { mutableStateOf(member.vehicleRole) }

    val normalizedPath = remember(member.uid, member.orgPath) { member.orgPath.normalized() }
    var sideId by remember(member.uid) { mutableStateOf(normalizedPath.sideId) }
    var companyId by remember(member.uid) { mutableStateOf(normalizedPath.companyId.orEmpty()) }
    var platoonId by remember(member.uid) { mutableStateOf(normalizedPath.platoonId.orEmpty()) }
    var teamId by remember(member.uid) { mutableStateOf(normalizedPath.teamId.orEmpty()) }
    var vehicleId by remember(member.uid) { mutableStateOf(normalizedPath.vehicleId.orEmpty()) }

    val draftPath = TeamOrgPath(
        sideId = sideId.trim().ifBlank { TeamOrgPath.DEFAULT_SIDE_ID },
        companyId = companyId.trim().ifBlank { null },
        platoonId = platoonId.trim().ifBlank { null },
        teamId = teamId.trim().ifBlank { null },
        vehicleId = vehicleId.trim().ifBlank { null },
    ).normalized()

    val canChangeCommandAndPath = actorCommandRole != TeamCommandRole.FIGHTER &&
        actorCommandRole != TeamCommandRole.TEAM_DEPUTY
    val canChangeCombatRole = actorCommandRole != TeamCommandRole.FIGHTER
    val canChangeVehicleRole = actorCommandRole != TeamCommandRole.FIGHTER &&
        actorCommandRole != TeamCommandRole.TEAM_DEPUTY

    val patch = TeamRolePatch(
        commandRole = commandRole.takeIf { canChangeCommandAndPath && it != member.commandRole },
        combatRole = combatRole.takeIf { canChangeCombatRole && it != member.combatRole },
        vehicleRole = vehicleRole.takeIf { canChangeVehicleRole && it != member.vehicleRole },
        orgPath = draftPath.takeIf { canChangeCommandAndPath && it != normalizedPath },
    )

    val hasChanges = patch.commandRole != null ||
        patch.combatRole != null ||
        patch.vehicleRole != null ||
        patch.orgPath != null
    val isPathCompatible = draftPath.isCompatibleWith(commandRole)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.team_role_editor_title_format, member.callsign), fontWeight = FontWeight.SemiBold)
                Text(
                    member.orgPath.toShortUiLabel(resources),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = { onSave(patch) },
                enabled = hasChanges && isPathCompatible,
            ) {
                Text(stringResource(R.string.button_save))
            }
        },
        dismissButton = {
            FilledTonalButton(onClick = onDismiss) {
                Text(stringResource(R.string.label_cancel))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                RoleDropdown(
                    title = stringResource(R.string.team_role_command_title),
                    selectedLabel = commandRole.toUiLabel(resources),
                    options = TeamCommandRole.entries,
                    optionLabel = { it.toUiLabel(resources) },
                    enabled = canChangeCommandAndPath,
                    onSelected = { commandRole = it },
                )
                RoleDropdown(
                    title = stringResource(R.string.team_role_combat_title),
                    selectedLabel = combatRole.toUiLabel(resources),
                    options = CombatRole.entries,
                    optionLabel = { it.toUiLabel(resources) },
                    enabled = canChangeCombatRole,
                    onSelected = { combatRole = it },
                )
                RoleDropdown(
                    title = stringResource(R.string.team_role_vehicle_title),
                    selectedLabel = vehicleRole.toUiLabel(resources),
                    options = VehicleRole.entries,
                    optionLabel = { it.toUiLabel(resources) },
                    enabled = canChangeVehicleRole,
                    onSelected = { vehicleRole = it },
                )

                Text(
                    stringResource(R.string.team_role_org_section),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = sideId,
                    onValueChange = { sideId = it.take(20) },
                    label = { Text(stringResource(R.string.team_role_side_id_label)) },
                    singleLine = true,
                    enabled = canChangeCommandAndPath,
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = companyId,
                    onValueChange = { companyId = it.take(20) },
                    label = { Text(stringResource(R.string.team_role_company_id_label)) },
                    singleLine = true,
                    enabled = canChangeCommandAndPath,
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = platoonId,
                    onValueChange = { platoonId = it.take(20) },
                    label = { Text(stringResource(R.string.team_role_platoon_id_label)) },
                    singleLine = true,
                    enabled = canChangeCommandAndPath,
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = teamId,
                    onValueChange = { teamId = it.take(20) },
                    label = { Text(stringResource(R.string.team_role_team_id_label)) },
                    singleLine = true,
                    enabled = canChangeCommandAndPath,
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vehicleId,
                    onValueChange = { vehicleId = it.take(20) },
                    label = { Text(stringResource(R.string.team_role_vehicle_id_label)) },
                    singleLine = true,
                    enabled = canChangeCommandAndPath,
                )

                if (!isPathCompatible) {
                    Text(
                        stringResource(R.string.team_role_path_invalid),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (!canChangeCommandAndPath && canChangeCombatRole) {
                    Text(
                        stringResource(R.string.team_role_deputy_limited_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    )
}

@Composable
private fun <T> RoleDropdown(
    title: String,
    selectedLabel: String,
    options: List<T>,
    optionLabel: (T) -> String,
    enabled: Boolean = true,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { expanded = true },
            enabled = enabled,
        ) {
            Text(
                selectedLabel,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    },
                )
            }
        }
    }
}
