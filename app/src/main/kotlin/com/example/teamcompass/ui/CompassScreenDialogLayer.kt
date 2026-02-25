package com.example.teamcompass.ui

import androidx.compose.runtime.Composable

@Composable
internal fun CompassScreenDialogLayer(
    state: UiState,
    rangeMeters: Float,
    showMarkerPalette: Boolean,
    showStatusDialog: Boolean,
    onShowStatusDialogChange: (Boolean) -> Unit,
    showHelpDialog: Boolean,
    onShowHelpDialogChange: (Boolean) -> Unit,
    onMarkHelpSeen: () -> Unit,
    pointAction: PointActionState?,
    onPointActionChange: (PointActionState?) -> Unit,
    pointDialog: PointDialogState?,
    onPointDialogChange: (PointDialogState?) -> Unit,
    defaultPointLabel: String,
    defaultPointIconRaw: String,
    defaultPointForTeam: Boolean,
    onDefaultPointPrefsChanged: (iconRaw: String, forTeam: Boolean) -> Unit,
    showMapsDialog: Boolean,
    onShowMapsDialogChange: (Boolean) -> Unit,
    onLaunchMapImport: () -> Unit,
    onCopyCode: () -> Unit,
    onAddPointAt: (lat: Double, lon: Double, label: String, icon: String, forTeam: Boolean) -> Unit,
    onUpdatePoint: (id: String, lat: Double, lon: Double, label: String, icon: String, isTeam: Boolean) -> Unit,
    onDeletePoint: (id: String, isTeam: Boolean) -> Unit,
    onMapEnabled: (Boolean) -> Unit,
    onOpenFullscreenMap: () -> Unit,
    onMapOpacity: (Float) -> Unit,
    onClearMap: () -> Unit,
) {
    if (showStatusDialog) {
        CompassStatusDialog(
            state = state,
            rangeMeters = rangeMeters,
            showMarkerPalette = showMarkerPalette,
            onDismiss = { onShowStatusDialogChange(false) },
            onCopyCode = onCopyCode,
        )
    }

    if (showHelpDialog) {
        CompassHelpDialog(
            onDismissAndAcknowledge = {
                onShowHelpDialogChange(false)
                onMarkHelpSeen()
            },
        )
    }

    pointAction?.let { action ->
        val marker = action.marker
        CompassPointActionDialog(
            action = action,
            currentUid = state.uid,
            onDismiss = { onPointActionChange(null) },
            onCopyToMine = {
                onAddPointAt(marker.lat, marker.lon, marker.label, marker.iconRaw, false)
                onPointActionChange(null)
            },
            onEdit = {
                onPointDialogChange(
                    PointDialogState(
                        id = marker.id,
                        isTeam = marker.isTeam,
                        createdBy = marker.createdBy,
                        lat = marker.lat,
                        lon = marker.lon,
                        initialLabel = marker.label,
                        initialIconRaw = marker.iconRaw,
                    )
                )
                onPointActionChange(null)
            },
            onMove = {
                onUpdatePoint(marker.id, action.pressLat, action.pressLon, marker.label, marker.iconRaw, marker.isTeam)
                onPointActionChange(null)
            },
            onDelete = {
                onDeletePoint(marker.id, marker.isTeam)
                onPointActionChange(null)
            },
        )
    }

    pointDialog?.let { dialog ->
        CompassPointEditDialog(
            dialog = dialog,
            defaultPointLabel = defaultPointLabel,
            defaultPointIconRaw = defaultPointIconRaw,
            defaultPointForTeam = defaultPointForTeam,
            onDismiss = { onPointDialogChange(null) },
            onAddPointAt = onAddPointAt,
            onUpdatePoint = onUpdatePoint,
            onDefaultPointPrefsChanged = onDefaultPointPrefsChanged,
        )
    }

    if (showMapsDialog) {
        CompassMapsDialog(
            state = state,
            onDismiss = { onShowMapsDialogChange(false) },
            onImportClick = onLaunchMapImport,
            onMapEnabled = onMapEnabled,
            onOpenFullscreenMap = onOpenFullscreenMap,
            onMapOpacity = onMapOpacity,
            onClearMap = onClearMap,
        )
    }
}
