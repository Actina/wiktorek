package pl.gov.mf.etoll.core.security.unlock

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import pl.gov.mf.etoll.commons.Constants.LOCK_TIME_OF_PIN_ATTEMPTS_MILLIS
import pl.gov.mf.etoll.commons.Constants.PIN_MAX_INVALID_ATTEMPTS
import pl.gov.mf.etoll.core.security.unlock.statuses.SecurityInputStatus
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC

sealed class PinUnlockUC {

    class MarkAppAsLockedUseCase(
        private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase
    ) : PinUnlockUC() {

        fun execute() = writeSettingsUseCase.execute(Settings.IS_LOCKED, true)
    }

    class CheckAttemptsNumberLeftUC(
        private val unlockManager: UnlockManager
    ) : PinUnlockUC() {

        fun execute() = unlockManager.checkInvalidAttemptNumber(
            Settings.INVALID_PIN_ATTEMPTS,
            PIN_MAX_INVALID_ATTEMPTS
        )
    }

    class InvalidPinEnteredUseCase(
        private val unlockManager: UnlockManager
    ) : PinUnlockUC() {

        fun execute(): Single<SecurityInputStatus> =
            unlockManager.incrementInvalidAttemptsNumberFor(
                Settings.INVALID_PIN_ATTEMPTS,
                PIN_MAX_INVALID_ATTEMPTS,
                LOCK_TIME_OF_PIN_ATTEMPTS_MILLIS
            )
    }

    class ClearPinInvalidAttemptsNumberUC(
        private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase
    ) : PinUnlockUC() {

        fun execute() = writeSettingsUseCase.execute(Settings.INVALID_PIN_ATTEMPTS, 0)
    }

    class UnlockAppUseCase(
        private val unlockManager: UnlockManager
    ) : PinUnlockUC() {

        fun execute(): Completable = unlockManager.unlockApp()
    }

    class LockPinInputUC(
        private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase
    ) : PinUnlockUC() {

        fun execute() = writeSettingsUseCase.execute(
            Settings.LOCK_PIN_UNLOCK_PROCESS_TIMESTAMP,
            System.currentTimeMillis()
        )
    }

    class CheckPinInputLockStatusUC(
        private val unlockManager: UnlockManager
    ) : PinUnlockUC() {

        fun execute(): Observable<SecurityInputStatus> =
            unlockManager.checkInputLockStatus(
                Settings.LOCK_PIN_UNLOCK_PROCESS_TIMESTAMP,
                LOCK_TIME_OF_PIN_ATTEMPTS_MILLIS
            )
    }
}