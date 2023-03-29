package pl.gov.mf.etoll.core.inactivity

import android.annotation.SuppressLint
import io.reactivex.Single
import pl.gov.mf.etoll.commons.Constants
import pl.gov.mf.etoll.core.security.SecurityConfigUC
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class InactivityControllerImpl @Inject constructor(
    private val isSecurityConfigValidUseCase: SecurityConfigUC.IsSecurityConfigValidUseCase,
    private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase,
    private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase
) : InactivityController {

    private val isInForeground = AtomicBoolean(true)

    @SuppressLint("CheckResult")
    override fun onAppGoesToBackground() {
        isSecurityConfigValidUseCase.execute()
            .flatMapCompletable { isSecurityValid ->
                val timestamp = if (isSecurityValid) System.currentTimeMillis() else 0L

                // save background timestamp
                writeSettingsUseCase.execute(
                    Settings.MOVE_TO_BACKGROUND_TIMESTAMP,
                    timestamp
                )
            }
            .subscribe({}, {})

        isInForeground.set(false)
    }

    override fun onAppGoesToForeground() {
        isInForeground.set(true)
    }

    override fun onCheckRequested(): Single<InactivityState> =
        Single.zip(
            listOf(
                isSecurityConfigValidUseCase.execute(),
                readSettingsUseCase.executeForBooleanAsync(Settings.IS_LOCKED),
                readSettingsUseCase.executeForLongAsync(Settings.MOVE_TO_BACKGROUND_TIMESTAMP),
                readSettingsUseCase.executeForLongAsync(Settings.LOCK_RESET_PIN_PROCESS_TIMESTAMP)
            )
        ) { results ->
            val isSecurityValid = results[0] as Boolean
            val isLocked = results[1] as Boolean
            val moveToBackgroundTimestamp = results[2] as Long
            val lockResetPinProcessTimestamp = results[3] as Long

            if (!isSecurityValid || //security is not configured
                moveToBackgroundTimestamp == 0L //timestamp is set to 0
            ) {
                return@zip InactivityState.UNLOCKED
            }

            val currentTime = System.currentTimeMillis()

            // last unlock was unsuccessful
            if (isLocked) {
                //if password is still locked
                if (lockResetPinProcessTimestamp + Constants.LOCK_TIME_OF_PASSWORD_ATTEMPTS_MILLIS > currentTime) {
                    return@zip InactivityState.SHOW_PIN_RESET
                }
                return@zip InactivityState.SHOW_PIN_LOGIN
            }

            // if less than 180s - clear timestamp
            if (currentTime - moveToBackgroundTimestamp < Constants.MAX_INACTIVITY_TIME_IN_BACKGROUND_MILLIS) {
                writeSettingsUseCase.execute(Settings.MOVE_TO_BACKGROUND_TIMESTAMP, 0L).subscribe()
                return@zip InactivityState.UNLOCKED
            }

            return@zip InactivityState.SHOW_PIN_LOGIN
        }
}