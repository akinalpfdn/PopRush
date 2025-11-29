package com.akinalpfdn.poprush.coop.presentation.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import timber.log.Timber

/**
 * Manages permissions required for coop multiplayer functionality.
 * Handles both legacy and new Android 12+ Bluetooth permissions.
 * Simplified version that only checks permissions without launching dialogs.
 */
class CoopPermissionManager(private val context: Context) {

    private var _hasPermissions by mutableStateOf(false)
    val hasPermissions: Boolean get() = _hasPermissions

    init {
        checkPermissions()
    }

    /**
     * Check if all required permissions are granted
     */
    private fun checkPermissions() {
        _hasPermissions = areAllPermissionsGranted()
    }

    /**
     * Get all required permissions based on Android version
     */
    private fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf<String>()

        // Legacy permissions (always required)
        permissions.addAll(listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        // Android 12+ permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.addAll(listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ))
        }

        return permissions.toTypedArray()
    }

    /**
     * Check if all required permissions are granted
     */
    private fun areAllPermissionsGranted(): Boolean {
        val requiredPermissions = getRequiredPermissions()
        Timber.d("Checking permissions for Android ${Build.VERSION.SDK_INT}")
        Timber.d("Required permissions: ${requiredPermissions.joinToString(", ")}")

        val grantedPermissions = mutableListOf<String>()
        val missingPermissions = mutableListOf<String>()

        requiredPermissions.forEach { permission ->
            val isGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            if (isGranted) {
                grantedPermissions.add(permission)
            } else {
                missingPermissions.add(permission)
            }
        }

        Timber.d("Granted permissions: ${grantedPermissions.joinToString(", ")}")
        Timber.d("Missing permissions: ${missingPermissions.joinToString(", ")}")

        return missingPermissions.isEmpty()
    }

    /**
     * Refresh permission status (call after user grants permissions)
     */
    fun refreshPermissions() {
        Timber.d("Refreshing permissions...")
        val oldStatus = _hasPermissions
        checkPermissions()
        Timber.d("Permission status changed: $oldStatus -> $_hasPermissions")
    }

    /**
     * Get missing permissions that need to be requested
     */
    fun getMissingPermissions(): Array<String> {
        val requiredPermissions = getRequiredPermissions()
        return requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
    }

    /**
     * Get missing permissions as human-readable names
     */
    fun getMissingPermissionsDisplay(): List<String> {
        val missingPermissions = getMissingPermissions()
        return missingPermissions.map { permission ->
            when (permission) {
                Manifest.permission.BLUETOOTH -> "Bluetooth"
                Manifest.permission.BLUETOOTH_ADMIN -> "Bluetooth Admin"
                Manifest.permission.ACCESS_WIFI_STATE -> "WiFi Access"
                Manifest.permission.CHANGE_WIFI_STATE -> "WiFi Control"
                Manifest.permission.ACCESS_FINE_LOCATION -> "Precise Location"
                Manifest.permission.ACCESS_COARSE_LOCATION -> "Approximate Location"
                Manifest.permission.BLUETOOTH_SCAN -> "Bluetooth Scan (Android 12+)"
                Manifest.permission.BLUETOOTH_ADVERTISE -> "Bluetooth Advertise (Android 12+)"
                Manifest.permission.BLUETOOTH_CONNECT -> "Bluetooth Connect (Android 12+)"
                Manifest.permission.NEARBY_WIFI_DEVICES -> "Nearby WiFi Devices (Android 12+)"
                else -> permission
            }
        }
    }
}

/**
 * Composable wrapper for CoopPermissionManager
 */
@Composable
fun rememberCoopPermissionManager(context: Context): CoopPermissionManager {
    return remember(context) { CoopPermissionManager(context) }
}

/**
 * Check if we should show permission rationale for location permission
 */
fun shouldShowLocationPermissionRationale(context: Context): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(
        context as android.app.Activity,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
}