package pl.gov.mf.etoll.core.androidsettings

sealed class AndroidSettingsManagerUC {

    class OpenSecuritySettingsUseCase(
        private val androidSettingsManager: AndroidSettingsManager
    ) : AndroidSettingsManagerUC() {

        fun execute() = androidSettingsManager.openSecuritySettings()
    }

    class OpenAppDetailsSettingsUseCase(
        private val androidSettingsManager: AndroidSettingsManager
    ) : AndroidSettingsManagerUC() {

        fun execute() = androidSettingsManager.openAppSettings()
    }
}