package pl.gov.mf.etoll.networking.manager.netswitch

import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import javax.inject.Inject

class NetworkSwitchConditionsCheckImpl @Inject constructor(
    private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase,
    private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase
) :
    NetworkSwitchConditionsCheck {
    override fun shouldSelectRealManager(): Boolean =
        !readSettingsUseCase.executeForBoolean(Settings.NETWORK_COMMUNICATION_LOCKED)

    override fun lockToDummy() {
        writeSettingsUseCase.execute(Settings.NETWORK_COMMUNICATION_LOCKED, true).subscribe()
    }
}