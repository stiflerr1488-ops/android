package com.airsoft.social.feature.settings.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airsoft.social.core.data.DeviceDiagnosticsPort
import com.airsoft.social.core.data.PermissionStatusPort
import com.airsoft.social.core.data.SecurityStatusPort
import com.airsoft.social.core.model.DeviceDiagnosticsSnapshot
import com.airsoft.social.core.model.PermissionStatusSnapshot
import com.airsoft.social.core.model.SecurityStatusSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsSystemStatusViewModel @Inject constructor(
    private val permissionStatusPort: PermissionStatusPort,
    private val securityStatusPort: SecurityStatusPort,
    private val deviceDiagnosticsPort: DeviceDiagnosticsPort,
) : ViewModel() {

    val permissionStatus: StateFlow<PermissionStatusSnapshot> = permissionStatusPort
        .observeStatus()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = PermissionStatusSnapshot(),
        )

    val securityStatus: StateFlow<SecurityStatusSnapshot> = securityStatusPort
        .observeStatus()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SecurityStatusSnapshot(),
        )

    val deviceDiagnostics: StateFlow<DeviceDiagnosticsSnapshot> = deviceDiagnosticsPort
        .observeStatus()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = DeviceDiagnosticsSnapshot(),
        )

    fun refreshPermissions() {
        viewModelScope.launch { permissionStatusPort.refresh() }
    }

    fun refreshSecurity() {
        viewModelScope.launch { securityStatusPort.refresh() }
    }

    fun refreshDiagnostics() {
        viewModelScope.launch { deviceDiagnosticsPort.refresh() }
    }
}
