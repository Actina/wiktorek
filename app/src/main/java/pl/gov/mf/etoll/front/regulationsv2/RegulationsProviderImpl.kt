package pl.gov.mf.etoll.front.regulationsv2

import kotlinx.coroutines.CoroutineScope
import pl.gov.mf.etoll.core.devicecompatibility.DeviceCompatibilityUC
import pl.gov.mf.etoll.networking.RegistrationProvider
import pl.gov.mf.etoll.storage.settings.SettingsUC
import javax.inject.Inject

class RegulationsProviderImpl @Inject constructor(
    readSettingsUseCase: SettingsUC.ReadSettingsUseCase,
    writeSettingsUseCase: SettingsUC.WriteSettingsUseCase,
    registrationProvider: RegistrationProvider,
    checkAutoTimeEnabledUseCase: DeviceCompatibilityUC.CheckAutoTimeEnabledUseCase,
    scope: CoroutineScope,
) : RegulationsProvider(
    readSettingsUseCase = readSettingsUseCase,
    writeSettingsUseCase = writeSettingsUseCase,
    registrationProvider = registrationProvider,
    checkAutoTimeEnabledUseCase = checkAutoTimeEnabledUseCase,
    scope = scope
) {
    override fun getRegulationsFileName(): String = "regulations_1.4"
}