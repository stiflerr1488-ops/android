package com.example.teamcompass.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.teamcompass.R

internal val markerColorPalette: List<Long> = listOf(
    0xFFE53935L,
    0xFFFFB300L,
    0xFF43A047L,
    0xFF00ACC1L,
    0xFF1E88E5L,
    0xFF8E24AAL,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FullscreenMapEmptyStateScaffold(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.map_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.nav_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(R.string.map_not_loaded))
        }
    }
}

@Composable
internal fun MarkerIconPanel(
    selectedIconRaw: String,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .width(84.dp)
            .fillMaxHeight(0.78f),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(stringResource(R.string.map_icon_label), style = MaterialTheme.typography.labelSmall)
            TacticalIconId.entries.forEach { icon ->
                val selected = icon.raw.equals(selectedIconRaw, ignoreCase = true)
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent,
                        )
                        .border(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            },
                            shape = RoundedCornerShape(10.dp),
                        )
                        .clickable { onIconSelected(icon.raw) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon.vector, contentDescription = icon.label, tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
internal fun MarkerColorPanel(
    selectedColorArgb: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .width(84.dp)
            .fillMaxHeight(0.78f),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(stringResource(R.string.map_color_label), style = MaterialTheme.typography.labelSmall)
            markerColorPalette.forEach { colorArgb ->
                val selected = colorArgb == selectedColorArgb
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(argbToColor(colorArgb))
                        .border(
                            width = if (selected) 2.5.dp else 1.dp,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            shape = CircleShape,
                        )
                        .clickable { onColorSelected(colorArgb) },
                )
            }
        }
    }
}

@Composable
internal fun BoxScope.FullscreenMapEditHud(
    deleteMode: Boolean,
    editHintDeleteMode: String,
    editHintEditMode: String,
    selectedColorArgb: Long,
    onColorSelected: (Long) -> Unit,
    selectedIconRaw: String,
    onIconSelected: (String) -> Unit,
) {
    MarkerColorPanel(
        selectedColorArgb = selectedColorArgb,
        onColorSelected = onColorSelected,
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = 12.dp, top = 16.dp, bottom = 16.dp),
    )
    MarkerIconPanel(
        selectedIconRaw = selectedIconRaw,
        onIconSelected = onIconSelected,
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 12.dp, top = 16.dp, bottom = 16.dp),
    )
    Text(
        text = if (deleteMode) editHintDeleteMode else editHintEditMode,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FullscreenMapTopBar(
    title: String,
    onBack: () -> Unit,
    onOpenStructure: () -> Unit,
    editMode: Boolean,
    onToggleEditMode: () -> Unit,
    showEditMenu: Boolean,
    onShowEditMenu: () -> Unit,
    onDismissEditMenu: () -> Unit,
    deleteMode: Boolean,
    deleteModeOffLabel: String,
    deleteModeOnLabel: String,
    saveChangesEnabled: Boolean,
    saveAsEnabled: Boolean,
    discardChangesEnabled: Boolean,
    onToggleDeleteMode: () -> Unit,
    onSaveChanges: () -> Unit,
    onSaveAs: () -> Unit,
    onDiscardChanges: () -> Unit,
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.nav_back),
                )
            }
        },
        actions = {
            IconButton(onClick = onOpenStructure) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = stringResource(R.string.map_structure_roles_cd),
                )
            }
            IconButton(onClick = onToggleEditMode) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.button_edit),
                    tint = if (editMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (editMode) {
                Box {
                    IconButton(onClick = onShowEditMenu) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.map_edit_menu_cd),
                        )
                    }
                    DropdownMenu(
                        expanded = showEditMenu,
                        onDismissRequest = onDismissEditMenu,
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(if (deleteMode) deleteModeOffLabel else deleteModeOnLabel)
                            },
                            onClick = onToggleDeleteMode,
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.map_save_changes)) },
                            enabled = saveChangesEnabled,
                            onClick = onSaveChanges,
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.map_save_as)) },
                            enabled = saveAsEnabled,
                            onClick = onSaveAs,
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.map_discard_changes)) },
                            enabled = discardChangesEnabled,
                            onClick = onDiscardChanges,
                        )
                    }
                }
            }
        },
    )
}

@Composable
internal fun FullscreenMapAddMarkerDialog(
    pointName: String,
    onPointNameChange: (String) -> Unit,
    pointDescription: String,
    onPointDescriptionChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirmAdd: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.map_new_marker_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = pointName,
                    onValueChange = { onPointNameChange(it.take(48)) },
                    label = { Text(stringResource(R.string.map_marker_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = pointDescription,
                    onValueChange = { onPointDescriptionChange(it.take(256)) },
                    label = { Text(stringResource(R.string.map_marker_description_optional_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = pointName.trim().isNotEmpty(),
                onClick = onConfirmAdd,
            ) { Text(stringResource(R.string.button_add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.label_cancel))
            }
        },
    )
}
