package pl.gov.mf.mobile.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

fun hasFineLocationPermission(context: Context): Boolean =
    hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)

private fun hasPermission(context: Context, permission: String): Boolean =
    PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, permission)
