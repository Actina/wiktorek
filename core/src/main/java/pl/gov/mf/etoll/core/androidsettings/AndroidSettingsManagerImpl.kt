package pl.gov.mf.etoll.core.androidsettings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import javax.inject.Inject

class AndroidSettingsManagerImpl @Inject constructor(
    private val context: Context
) : AndroidSettingsManager {

    override fun openSecuritySettings() {
        val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    override fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }
}