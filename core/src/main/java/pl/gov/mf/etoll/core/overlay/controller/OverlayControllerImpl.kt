package pl.gov.mf.etoll.core.overlay.controller

import android.content.Context
import pl.gov.mf.etoll.core.devicecompatibility.DeviceInfoProvider
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import javax.inject.Inject

class OverlayControllerImpl @Inject constructor(
    private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase,
    private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase,
    private val deviceInfoProvider: DeviceInfoProvider
) : OverlayController {

    override fun tryToEnableOverlay(context: Context): Boolean {
        // we assume, user wants to permamently change switch, which actually would start working when permissions will be provided
        if (!readSettingsUseCase.executeForBoolean(Settings.OVERLAY_ENABLED_BY_USER))
            writeSettingsUseCase.execute(Settings.OVERLAY_ENABLED_BY_USER, true).subscribe()
        if (!isOverlayPermissionGranted(context)) return false
        return true
    }

    override fun disableOverlay() {
        writeSettingsUseCase.execute(Settings.OVERLAY_ENABLED_BY_USER, false).subscribe()
    }

    override fun overlayIsEnabled(context: Context): Boolean {
        val overlayIsEnabledInSettings =
            readSettingsUseCase.executeForBoolean(Settings.OVERLAY_ENABLED_BY_USER)
        return overlayIsEnabledInSettings && isOverlayPermissionGranted(context)
    }

    override fun askForPermissions(context: Context) {
        context.startActivity(deviceInfoProvider.getDeviceManufacturerOverlayPermissionIntent())
    }

    private fun isOverlayPermissionGranted(context: Context): Boolean =
        android.provider.Settings.canDrawOverlays(context)
}