package pl.gov.mf.etoll.core.watchdog.netlock

import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.utils.CallbacksContainer
import javax.inject.Inject

class CoreNetlockCheckerImpl @Inject constructor(private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase) :
    CoreNetlockChecker {

    private var networkCommunicationIsLocked = true
    private var lastStateWasShown = true
    private val callbacks = CallbacksContainer<CoreNetlockCheckerCallbacks>()

    override fun intervalCheck() {
        val newValue = readSettingsUseCase.executeForBoolean(Settings.NETWORK_COMMUNICATION_LOCKED)
        if (newValue != networkCommunicationIsLocked) {
            if (!networkCommunicationIsLocked)
            // we care only for switch false->true, as default was true
                lastStateWasShown = false
            networkCommunicationIsLocked = newValue
        }

        if (networkCommunicationIsLocked && !lastStateWasShown)
            callbacks.get().forEach {
                it.value.onShowNetworkLock()
            }
    }

    override fun markStateAsShowed() {
        lastStateWasShown = true
    }

    override fun markStateAsNotShowed() {
        lastStateWasShown = false
    }

    override fun setCallbacks(callback: CoreNetlockCheckerCallbacks, tag: String) {
        callbacks.set(callback, tag)
    }

    override fun removeCallbacks(tag: String) {
        callbacks.set(null, tag)
    }
}