package com.example.teamcompass.tracking

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.teamcompass.core.TrackingMode
import com.example.teamcompass.core.TrackingPolicy
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

internal fun trackingRuntimePolicyFor(
    mode: TrackingMode,
    game: TrackingPolicy,
    silent: TrackingPolicy,
): TrackingPolicy {
    return when (mode) {
        TrackingMode.GAME -> game
        TrackingMode.SILENT -> silent
    }
}

internal fun trackingRuntimeHasLocationPermission(app: Application): Boolean {
    val fine =
        ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    val coarse =
        ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

internal fun trackingRuntimeIsLocationEnabled(app: Application): Boolean {
    val locationManager = app.getSystemService(LocationManager::class.java) ?: return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        locationManager.isLocationEnabled
    } else {
        runCatching {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }.getOrDefault(false)
    }
}

internal fun trackingRuntimeNewScope(
    coroutineExceptionHandler: CoroutineExceptionHandler,
): CoroutineScope {
    return CoroutineScope(SupervisorJob() + Dispatchers.Default + coroutineExceptionHandler)
}
