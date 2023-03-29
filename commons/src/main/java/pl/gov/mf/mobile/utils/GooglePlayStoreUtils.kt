package pl.gov.mf.mobile.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

class GooglePlayStoreUtils {
    companion object {
        fun showStoreForCurrentApp(activity: Activity) {
            val packageName = activity.applicationContext.packageName
            showStoreForApp(activity = activity, packageName = packageName)
        }

        fun showStoreForApp(activity: Activity, packageName: String) {
            try {
                activity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                activity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            }
        }

        fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean =
            try {
                packageManager.getPackageInfo(packageName, 0)
                true
            } catch (ex: PackageManager.NameNotFoundException) {
                false
            }
    }
}

fun Context.isGooglePlayServicesAvailable() = GoogleApiAvailability.getInstance()
    .isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS
