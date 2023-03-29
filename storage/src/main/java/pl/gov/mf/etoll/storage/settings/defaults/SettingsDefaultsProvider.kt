package pl.gov.mf.etoll.storage.settings.defaults

import pl.gov.mf.etoll.storage.settings.Settings

interface SettingsDefaultsProvider {
    fun getDefaultValueFor(settingsName: Settings): Any
}