package pl.gov.mf.etoll.core.security.unlock

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import pl.gov.mf.etoll.commons.Constants
import pl.gov.mf.etoll.core.security.unlock.statuses.SecurityInputStatus
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC

sealed class ResetToUnlockUC {

    class CheckAttemptsNumberLeftUC(
        private val unlockManager: UnlockManager
    ) : ResetToUnlockUC() {

        fun execute() = unlockManager.checkInvalidAttemptNumber(
            Settings.INVALID_PASSWORD_ATTEMPTS,
            Constants.PASSWORD_MAX_INVALID_ATTEMPTS
        )
    }

    class InvalidPasswordEnteredUseCase(
        private val unlockManager: UnlockManager
    ) : ResetToUnlockUC() {

        fun execute(): Single<SecurityInputStatus> =
            unlockManager.incrementInvalidAttemptsNumberFor(
                Settings.INVALID_PASSWORD_ATTEMPTS,
                Constants.PASSWORD_MAX_INVALID_ATTEMPTS,
                Constants.LOCK_TIME_OF_PASSWORD_ATTEMPTS_MILLIS
            )
    }

    class ResetSecurityConfigUseCase(
        private val unlockManager: UnlockManager
    ) : ResetToUnlockUC() {

        fun execute(): Completable = unlockManager.resetSecurity()
    }

    class ClearPasswordInvalidAttemptsNumberUC(
        private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase
    ) : ResetToUnlockUC() {

        fun execute() = writeSettingsUseCase.execute(Settings.INVALID_PASSWORD_ATTEMPTS, 0)
    }

    class LockPasswordInputUC(
        private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase
    ) : ResetToUnlockUC() {

        fun execute() = writeSettingsUseCase.execute(
            Settings.LOCK_RESET_PIN_PROCESS_TIMESTAMP,
            System.currentTimeMillis()
        )
    }

    class CheckPasswordInputLockStatusUC(
        private val unlockManager: UnlockManager
    ) : ResetToUnlockUC() {

        fun execute(): Observable<SecurityInputStatus> =
            unlockManager.checkInputLockStatus(
                Settings.LOCK_RESET_PIN_PROCESS_TIMESTAMP,
                Constants.LOCK_TIME_OF_PASSWORD_ATTEMPTS_MILLIS
            )
    }
}