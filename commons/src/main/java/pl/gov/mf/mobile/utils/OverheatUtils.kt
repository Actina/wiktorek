package pl.gov.mf.mobile.utils

import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.os.PowerManager.THERMAL_STATUS_LIGHT
import android.os.PowerManager.THERMAL_STATUS_MODERATE

object OverheatUtils {
    fun getCurrentLevel(context: Context): OverheatStatus =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val currentStatus = powerManager.currentThermalStatus
            when {
                currentStatus <= THERMAL_STATUS_LIGHT -> OverheatStatus.COLD
                currentStatus == THERMAL_STATUS_MODERATE -> {
                    OverheatStatus.WARNING
                }
                else -> OverheatStatus.CRITICAL
            }
        } else {
            OverheatStatus.COLD
        }

    enum class OverheatStatus {
        COLD, WARNING, CRITICAL
    }
}
