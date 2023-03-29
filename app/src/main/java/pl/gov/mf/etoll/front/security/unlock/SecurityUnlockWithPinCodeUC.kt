package pl.gov.mf.etoll.front.security.unlock

import android.content.Context
import pl.gov.mf.etoll.commons.formatMillisToMinutesAndSeconds
import pl.gov.mf.etoll.core.security.unlock.statuses.SecurityInputStatus
import pl.gov.mf.etoll.front.security.unlock.models.SecurityUnlockWithPinCodeConfiguration
import pl.gov.mf.mobile.utils.translate

sealed class SecurityUnlockWithPinCodeUC {

    class UpdateUiUseCase(
        private val context: Context
    ) {

        fun execute(inputState: SecurityInputStatus): SecurityUnlockWithPinCodeConfiguration =
            when (inputState) {
                is SecurityInputStatus.None,
                is SecurityInputStatus.Valid,
                is SecurityInputStatus.Unlocked -> SecurityUnlockWithPinCodeConfiguration()
                is SecurityInputStatus.Locked -> thirdInvalidAttemptInfo(inputState.timeLeftInMillis)
                is SecurityInputStatus.InvalidAndLocked -> thirdInvalidAttemptInfo(inputState.timeLeftInMillis)
                is SecurityInputStatus.InvalidAndUnlocked -> invalidAttemptInfo(inputState.attemptsLeft)
            }

        private fun thirdInvalidAttemptInfo(timeLeftInMillis: Long) =
            SecurityUnlockWithPinCodeConfiguration(
                errorText = "security_unlock_with_pin_code_third_invalid_attempt_android".translate(
                    context,
                    timeLeftInMillis.formatMillisToMinutesAndSeconds()
                ),
                isErrorVisible = true,
                isInputLocked = true
            )

        private fun invalidAttemptInfo(attemptsLeft: Int) =
            SecurityUnlockWithPinCodeConfiguration(
                errorText = if (attemptsLeft == 2) "security_unlock_with_pin_code_two_attempts_left"
                    .translate(context)
                else "security_unlock_with_pin_code_one_attempt_left"
                    .translate(context),
                isErrorVisible = true,
                isInputLocked = false
            )
    }
}