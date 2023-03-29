package pl.gov.mf.etoll.core.security.unlock

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleSource
import pl.gov.mf.etoll.core.security.unlock.statuses.SecurityInputStatus
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface UnlockManager {

    fun checkInvalidAttemptNumber(settings: Settings, maxInvalidNumber: Int): Single<Int>
    fun incrementInvalidAttemptsNumberFor(
        settings: Settings,
        maxInvalidNumber: Int,
        lockTime: Long
    ): Single<SecurityInputStatus>

    fun checkInputLockStatus(settings: Settings, lockTime: Long): Observable<SecurityInputStatus>
    fun resetSecurity(): Completable
    fun unlockApp(): Completable
}

class UnlockManagerImpl @Inject constructor(
    private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase,
    private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase
) : UnlockManager {

    private companion object {
        private const val TIMER_INTERVAL_SEC = 1L
    }

    override fun checkInvalidAttemptNumber(settings: Settings, maxInvalidNumber: Int) =
        readSettingsUseCase.executeForIntAsync(settings)
            .map { invalidPinAttempts -> maxInvalidNumber - invalidPinAttempts }

    override fun incrementInvalidAttemptsNumberFor(
        settings: Settings,
        maxInvalidNumber: Int,
        lockTime: Long
    ): Single<SecurityInputStatus> = readSettingsUseCase.executeForIntAsync(settings)
        .flatMap { invalidAttemptsNumber ->
            writeSettingsUseCase.execute(settings, invalidAttemptsNumber + 1)
                .andThen<SecurityInputStatus>(SingleSource {
                    val currentInvalidAttempts = invalidAttemptsNumber + 1
                    val status =
                        if (currentInvalidAttempts < maxInvalidNumber)
                            SecurityInputStatus.InvalidAndUnlocked(maxInvalidNumber - currentInvalidAttempts)
                        else
                            SecurityInputStatus.InvalidAndLocked(lockTime)

                    it.onSuccess(status)
                })
        }

    override fun checkInputLockStatus(
        settings: Settings,
        lockTime: Long
    ): Observable<SecurityInputStatus> =
        Observable.interval(0, TIMER_INTERVAL_SEC, TimeUnit.SECONDS)
            .concatMapSingle { checkStatus(settings, lockTime) }

    override fun resetSecurity(): Completable =
        Completable.concat(
            listOf(
                writeSettingsUseCase.execute(Settings.INVALID_PASSWORD_ATTEMPTS, 0),
                writeSettingsUseCase.execute(Settings.INVALID_PIN_ATTEMPTS, 0),
                writeSettingsUseCase.execute(Settings.LOCK_PIN_UNLOCK_PROCESS_TIMESTAMP, 0L),
                writeSettingsUseCase.execute(Settings.MOVE_TO_BACKGROUND_TIMESTAMP, 0L),
                writeSettingsUseCase.execute(Settings.PIN, ""),
                writeSettingsUseCase.execute(Settings.PASSWORD, ""),
                writeSettingsUseCase.execute(Settings.IS_BIOMETRIC_AUTH_ON, false),
                writeSettingsUseCase.execute(Settings.IS_LOCKED, false)
            )
        )

    override fun unlockApp(): Completable =
        Completable.concat(
            listOf(
                writeSettingsUseCase.execute(Settings.MOVE_TO_BACKGROUND_TIMESTAMP, 0L),
                writeSettingsUseCase.execute(Settings.INVALID_PIN_ATTEMPTS, 0),
                writeSettingsUseCase.execute(Settings.IS_LOCKED, false)
            )
        )

    private fun checkStatus(settings: Settings, lockTime: Long): Single<SecurityInputStatus> =
        readSettingsUseCase.executeForLongAsync(settings)
            .map<SecurityInputStatus> { timestamp ->
                val remainingTime = lockTime - (System.currentTimeMillis() - timestamp)
                if (timestamp == 0L || remainingTime <= 0)
                    SecurityInputStatus.Unlocked
                else
                    SecurityInputStatus.Locked(remainingTime)
            }
}