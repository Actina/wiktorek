package pl.gov.mf.etoll.core.watchdog.minversion

import android.util.Log
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.utils.CallbacksContainer
import javax.inject.Inject

class WatchdogMinVersionControllerImpl @Inject constructor(
    private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase,
    private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase,
    // we need to use app build version, not library, therefore it need to be externalized
    private val buildVersionProvider: BuildVersionProvider
) : WatchdogMinVersionController {

    private val callbacksContainer = CallbacksContainer<WatchdogMinVersionControllerCallbacks>()

    override fun intervalCheck() {
        // read last stored value
        val minVersion = readSettingsUseCase.executeForInt(Settings.MIN_SUPPORTED_VERSION_BY_API)
        // fallback option - ignore default and wrong values
        if (minVersion <= 0) return
        // get current build version
        if (minVersion > buildVersionProvider.getCurrentBuildVersion()) {
            // inform about detected problem
            Log.d("MIN_VERSION", "MIN VERSION IS HIGHER THAN CURRENT APP VERSION!")
            callbacksContainer.get().forEach { it.value.onVersionTooOldDetected() }
        } else {
            Log.d("MIN_VERSION", "APP VERSION IS ALLOWED")
        }
    }

    override fun setCallbacks(callbacks: WatchdogMinVersionControllerCallbacks, tag: String) {
        callbacksContainer.set(callbacks, tag)
    }

    override fun removeCallbacks(tag: String) {
        callbacksContainer.set(null, tag)
    }

    override fun onNewMinVersionDetected(version: Int) {
        // write new min version
        writeSettingsUseCase.execute(Settings.MIN_SUPPORTED_VERSION_BY_API, version).subscribe()
        Log.d("MIN_VERSION", "READ MIN VERSION: $version")
    }
}