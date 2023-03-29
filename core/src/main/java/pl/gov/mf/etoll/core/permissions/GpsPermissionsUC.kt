package pl.gov.mf.etoll.core.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC

sealed class GpsPermissionsUC {

    class CheckGpsPermissionsUseCase(private val context: Context) : GpsPermissionsUC() {
        fun execute(): Boolean =
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> checkPermissions(
                    listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                )
                else -> checkPermissions(
                    listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }

        private fun checkPermissions(permissions: List<String>) =
            permissions.all { permission ->
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
    }

    class AskForGpsPermissionsUseCase(
        private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase
    ) : GpsPermissionsUC() {

        fun execute(fragment: Fragment) {
            val permissions =
            //only android 10 can have this combination. Starting from android 11 we cannot
                //ask for foreground and background permissions simultaneously.
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q)
                    listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                else
                    listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )

            fragment.requestPermissions(
                permissions.toTypedArray(),
                PERMISSION_REQUEST
            )

            writeSettingsUseCase.execute(Settings.GPS_PERMISSIONS_ASKED, true)
                .subscribe()
        }
    }

    companion object {
        const val PERMISSION_REQUEST = 0xbeef
    }

    class CanAskForGpsPermissionsUseCase(
        private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase
    ) : GpsPermissionsUC() {

        fun execute(fragment: Fragment): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) return true

            if (!readSettingsUseCase.executeForBoolean(Settings.GPS_PERMISSIONS_ASKED))
                return true

            return shouldShowPermissionRationale(
                fragment,
                listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        private fun shouldShowPermissionRationale(fragment: Fragment, permissions: List<String>) =
            permissions.any { permission ->
                fragment.shouldShowRequestPermissionRationale(permission)
            }
    }
}