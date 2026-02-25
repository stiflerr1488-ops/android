package com.example.teamcompass.ui

import android.content.Context
import android.net.Uri
import com.example.teamcompass.core.CompassTarget
import com.example.teamcompass.core.TargetFilterPreset

internal data class CompassScreenState(
    val ui: UiState,
    val context: Context,
    val controlPositions: Map<CompassControlId, ControlPosition>,
    val controlLayoutEditEnabled: Boolean,
)

internal data class CompassScreenActions(
    val onRequestPermission: (Boolean) -> Unit,
    val onRefreshLocation: () -> Unit,
    val onStartTracking: () -> Unit,
    val onLeave: () -> Unit,
    val targetsProvider: (Long) -> List<CompassTarget>,
    val onCopyCode: () -> Unit,
    val onOpenSettings: () -> Unit,
    val onTogglePlayerMode: () -> Unit,
    val onStartBluetoothScan: () -> Unit,
    val onSos: () -> Unit,
    val onAddPointAt: (lat: Double, lon: Double, label: String, icon: String, forTeam: Boolean) -> Unit,
    val onUpdatePoint: (id: String, lat: Double, lon: Double, label: String, icon: String, isTeam: Boolean) -> Unit,
    val onDeletePoint: (id: String, isTeam: Boolean) -> Unit,
    val onQuickCommand: (QuickCommandType) -> Unit,
    val onEnemyPing: (lat: Double, lon: Double, type: QuickCommandType) -> Unit,
    val onEnemyMarkEnabled: (Boolean) -> Unit,
    val onImportMap: (Uri) -> Unit,
    val onMapEnabled: (Boolean) -> Unit,
    val onMapOpacity: (Float) -> Unit,
    val onClearMap: () -> Unit,
    val onOpenMapFullscreen: () -> Unit,
    val onControlPositionChange: (CompassControlId, ControlPosition) -> Unit,
    val onSetTargetPreset: (TargetFilterPreset) -> Unit,
    val onSetNearRadius: (Int) -> Unit,
    val onSetShowDead: (Boolean) -> Unit,
    val onSetShowStale: (Boolean) -> Unit,
    val onSetFocusMode: (Boolean) -> Unit,
    val onMarkHelpSeen: () -> Unit,
    val onMarkOnboardingSeen: () -> Unit,
    val onError: (String) -> Unit,
)
