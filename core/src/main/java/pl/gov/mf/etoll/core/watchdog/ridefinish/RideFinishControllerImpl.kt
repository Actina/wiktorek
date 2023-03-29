package pl.gov.mf.etoll.core.watchdog.ridefinish

import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import javax.inject.Inject

class RideFinishControllerImpl @Inject constructor(private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase) :
    RideFinishController {

    private var callbacks: RideFinishCallbacks? = null

    override fun intervalCheck() {
        callbacks?.let { call ->
            if (readSettingsUseCase.executeForBoolean(Settings.RIDE_SUMMARY_SHOULD_BE_SHOWN))
                call.onShowRideFinishedRequested()
        }
    }

    override fun onAppGoesToBackground() {
        callbacks = null
    }

    override fun onAppGoesToForeground(callbacks: RideFinishCallbacks) {
        this.callbacks = callbacks
    }
}