package pl.gov.mf.etoll.front.security.resettounlock

import android.content.Context
import pl.gov.mf.etoll.commons.formatMillisToMinutesAndSeconds
import pl.gov.mf.etoll.core.security.unlock.statuses.SecurityInputStatus
import pl.gov.mf.etoll.front.security.resettounlock.models.SecurityResetPinCodeToUnlockConfiguration
import pl.gov.mf.mobile.utils.translate

sealed class SecurityResetPinCodeToUnlockUC {

    class UpdateUiUseCase(
        private val context: Context
    ) {

        fun execute(inputStatus: SecurityInputStatus) =
            when (inputStatus) {
                is SecurityInputStatus.None,
                is SecurityInputStatus.Valid,
                is SecurityInputStatus.Unlocked -> SecurityResetPinCodeToUnlockConfiguration()
                is SecurityInputStatus.Locked -> thirdInvalidAttemptInfo(inputStatus.timeLeftInMillis)
                is SecurityInputStatus.InvalidAndLocked -> thirdInvalidAttemptInfo(inputStatus.timeLeftInMillis)
                is SecurityInputStatus.InvalidAndUnlocked -> invalidAttemptInfo(inputStatus.attemptsLeft)
            }

        private fun thirdInvalidAttemptInfo(timeLeftInMillis: Long) =
            SecurityResetPinCodeToUnlockConfiguration(
                errorText = "security_reset_pin_code_to_unlock_third_invalid_attempt_android".translate(
                    context,
                    timeLeftInMillis.formatMillisToMinutesAndSeconds()
                ),
                isErrorVisible = true,
                isInputLocked = true,
                isNavigationBackAvailable = false
            )

        private fun invalidAttemptInfo(attemptsLeft: Int) =
            SecurityResetPinCodeToUnlockConfiguration(
                errorText = if (attemptsLeft == 2) "security_reset_pin_code_to_unlock_two_attempts_left"
                    .translate(context)
                else "security_reset_pin_code_to_unlock_one_attempt_left"
                    .translate(context),
                isErrorVisible = true,
                isInputLocked = false,
                isNavigationBackAvailable = true
            )
    }
}