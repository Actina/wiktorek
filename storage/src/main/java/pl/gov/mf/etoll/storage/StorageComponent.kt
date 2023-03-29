package pl.gov.mf.etoll.storage

import pl.gov.mf.etoll.storage.settings.SettingsUC.ReadSettingsUseCase
import pl.gov.mf.etoll.storage.settings.SettingsUC.WriteSettingsUseCase

interface StorageComponent {

    fun useCaseReadSettings(): ReadSettingsUseCase

    fun useCaseWriteSettings(): WriteSettingsUseCase

}
