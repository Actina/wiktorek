package pl.gov.mf.etoll.core.security

import io.reactivex.Single
import io.reactivex.functions.BiFunction
import pl.gov.mf.etoll.core.security.unlock.UnlockManager
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.utils.checkSum

sealed class SecurityConfigUC {

    class SavePinUseCase(
        private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase
    ) : SecurityConfigUC() {

        fun execute(pin: String) = writeSettingsUseCase.execute(Settings.PIN, pin)
    }

    class SavePasswordUseCase(
        private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase
    ) : SecurityConfigUC() {

        fun execute(password: String) =
            writeSettingsUseCase.execute(Settings.PASSWORD, password.checkSum())
    }

    //TODO: Overenigneering - refactor and use settings useCases
    class SaveBiometricAuthOptionUseCase(
        private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase
    ) : SecurityConfigUC() {

        fun execute(isBiometricAuthOn: Boolean) =
            writeSettingsUseCase.execute(Settings.IS_BIOMETRIC_AUTH_ON, isBiometricAuthOn)
    }

    //TODO: Overenigneering - refactor and use settings useCases
    class GetBiometricAuthOptionUseCase(
        private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase
    ) : SecurityConfigUC() {

        fun execute() = readSettingsUseCase.executeForBooleanAsync(Settings.IS_BIOMETRIC_AUTH_ON)
    }

    class CheckPasswordUseCase(
        private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase
    ) : SecurityConfigUC() {

        fun execute(password: String) =
            readSettingsUseCase.executeForStringAsync(Settings.PASSWORD)
                .map { storedPassword -> password.checkSum() == storedPassword }
    }

    class CheckPinUseCase(
        private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase
    ) : SecurityConfigUC() {
        fun execute(pin: String) =
            readSettingsUseCase.executeForStringAsync(Settings.PIN)
                .map { storedPin -> pin == storedPin }
    }

    class IsSecurityConfigValidUseCase(
        private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase
    ) : SecurityConfigUC() {

        fun execute(): Single<Boolean> = Single.zip(
            readSettingsUseCase.executeForStringAsync(Settings.PIN),
            readSettingsUseCase.executeForStringAsync(Settings.PASSWORD),
            BiFunction { pin, password -> pin.isNotEmpty() && password.isNotEmpty() }
        )
    }

    class ClearSecurityConfigUseCase(
        private val unlockManager: UnlockManager
    ) : SecurityConfigUC() {

        fun execute() = unlockManager.resetSecurity()
    }
}
