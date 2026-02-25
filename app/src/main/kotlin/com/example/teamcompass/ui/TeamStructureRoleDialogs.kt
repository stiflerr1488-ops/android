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
internal fun TeamRoleBulkDialog(
    title: String,
    editableCount: Int,
    actorCommandRole: TeamCommandRole,
    onDismiss: () -> Unit,
    onApply: (TeamRolePatch) -> Unit,
) {
    val resources = LocalContext.current.resources
    var commandRole by remember { mutableStateOf<TeamCommandRole?>(null) }
    var combatRole by remember { mutableStateOf<CombatRole?>(null) }
    var vehicleRole by remember { mutableStateOf<VehicleRole?>(null) }

    val canChangeCommand = actorCommandRole != TeamCommandRole.FIGHTER &&
        actorCommandRole != TeamCommandRole.TEAM_DEPUTY
    val canChangeCombat = actorCommandRole != TeamCommandRole.FIGHTER
    val canChangeVehicle = actorCommandRole != TeamCommandRole.FIGHTER &&
        actorCommandRole != TeamCommandRole.TEAM_DEPUTY

    val patch = TeamRolePatch(
        commandRole = commandRole.takeIf { canChangeCommand },
        combatRole = combatRole.takeIf { canChangeCombat },
        vehicleRole = vehicleRole.takeIf { canChangeVehicle },
        orgPath = null,
    )
    val hasChanges = patch.commandRole != null || patch.combatRole != null || patch.vehicleRole != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.team_structure_bulk_title), fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.team_structure_bulk_subtitle_format, title, editableCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OptionalRoleDropdown(
                    title = stringResource(R.string.team_role_command_title),
                    enabled = canChangeCommand,
                    value = commandRole,
                    options = TeamCommandRole.entries,
                    optionLabel = { it.toUiLabel(resources) },
                    onValueChange = { commandRole = it },
                )
                OptionalRoleDropdown(
                    title = stringResource(R.string.team_role_combat_title),
                    enabled = canChangeCombat,
                    value = combatRole,
                    options = CombatRole.entries,
                    optionLabel = { it.toUiLabel(resources) },
                    onValueChange = { combatRole = it },
                )
                OptionalRoleDropdown(
                    title = stringResource(R.string.team_role_vehicle_title),
                    enabled = canChangeVehicle,
                    value = vehicleRole,
                    options = VehicleRole.entries,
                    optionLabel = { it.toUiLabel(resources) },
                    onValueChange = { vehicleRole = it },
                )
                if (!canChangeCommand && canChangeCombat) {
                    Text(
                        stringResource(R.string.team_structure_bulk_deputy_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = { onApply(patch) },
                enabled = hasChanges,
            ) {
                Text(stringResource(R.string.team_structure_apply))
            }
        },
        dismissButton = {
            FilledTonalButton(onClick = onDismiss) {
                Text(stringResource(R.string.label_cancel))
            }
        },
    )
}

@Composable
private fun <T> OptionalRoleDropdown(
    title: String,
    enabled: Boolean,
    value: T?,
    options: List<T>,
    optionLabel: (T) -> String,
    onValueChange: (T?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { expanded = true },
            enabled = enabled,
        ) {
            Text(
                text = value?.let(optionLabel) ?: stringResource(R.string.team_structure_do_not_change),
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.team_structure_do_not_change)) },
                onClick = {
                    expanded = false
                    onValueChange(null)
                },
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        expanded = false
                        onValueChange(option)
                    },
                )
            }
        }
    }
}
