package pl.gov.mf.etoll.front.security.resettounlock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import io.reactivex.SingleSource
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.core.security.SecurityConfigUC
import pl.gov.mf.etoll.core.security.unlock.ResetToUnlockUC
import pl.gov.mf.etoll.core.security.unlock.statuses.SecurityInputStatus
import pl.gov.mf.etoll.front.security.resettounlock.models.ResetPinCode
import pl.gov.mf.etoll.front.security.resettounlock.models.SecurityResetPinCodeToUnlockConfiguration
import pl.gov.mf.mobile.utils.addSafe
import javax.inject.Inject

class SecurityResetPinCodeToUnlockViewModel : BaseDatabindingViewModel() {

    @Inject
    lateinit var checkPasswordUseCase: SecurityConfigUC.CheckPasswordUseCase

    @Inject
    lateinit var checkAttemptsNumberLeftUC: ResetToUnlockUC.CheckAttemptsNumberLeftUC

    @Inject
    lateinit var invalidPasswordEnteredUseCase: ResetToUnlockUC.InvalidPasswordEnteredUseCase

    @Inject
    lateinit var resetSecurityConfigUseCase: ResetToUnlockUC.ResetSecurityConfigUseCase

    @Inject
    lateinit var clearPasswordInvalidAttemptsNumberUC: ResetToUnlockUC.ClearPasswordInvalidAttemptsNumberUC

    @Inject
    lateinit var lockPasswordInputUC: ResetToUnlockUC.LockPasswordInputUC

    @Inject
    lateinit var checkPasswordInputLockStatusUC: ResetToUnlockUC.CheckPasswordInputLockStatusUC

    @Inject
    lateinit var updateUiUseCase: SecurityResetPinCodeToUnlockUC.UpdateUiUseCase

    private val _securityInputStatus =
        MutableLiveData<SecurityInputStatus>(SecurityInputStatus.None)
    val configuration: LiveData<SecurityResetPinCodeToUnlockConfiguration> =
        Transformations.map(_securityInputStatus) { updateUiUseCase.execute(it) }

    private val _resetPinCode = MutableLiveData(ResetPinCode())
    val resetPinCode: LiveData<ResetPinCode>
        get() = _resetPinCode

    private val _navigate: MutableLiveData<NavigationTargets> =
        MutableLiveData(NavigationTargets.NONE)
    val navigate: LiveData<NavigationTargets>
        get() = _navigate

    override fun onResume() {
        super.onResume()

        observeLockProcess()
        checkInvalidPasswordAttempts()
    }

    fun checkPassword() {
        //if it is locked - don't validate
        if (_securityInputStatus.value?.isLockedStatus() == true) return
        val password = resetPinCode.value?.password ?: return

        val disposable = checkPasswordUseCase.execute(password)
            .flatMap { isValid ->
                if (isValid) resetSecurityConfig()
                else invalidPasswordEnteredUseCase.execute().map {
                    if (it is SecurityInputStatus.InvalidAndLocked)
                        lockPasswordProcess()
                    _securityInputStatus.postValue(it)
                    _resetPinCode.value = ResetPinCode()
                    NavigationTargets.NONE
                }
            }.subscribe({
                _navigate.value = it
            }, {})

        compositeDisposable.addSafe(disposable)
    }

    fun isProcessLocked(): Boolean = _securityInputStatus.value?.isLockedStatus() == true

    private fun clearInvalidAttemptsNumber() =
        compositeDisposable.addSafe(
            clearPasswordInvalidAttemptsNumberUC.execute()
                .subscribe({}, {})
        )

    private fun lockPasswordProcess() =
        compositeDisposable.addSafe(
            lockPasswordInputUC.execute()
                .subscribe({}, {})
        )

    private fun resetSecurityConfig() = resetSecurityConfigUseCase.execute()
        .andThen(SingleSource<NavigationTargets> {
            _securityInputStatus.postValue(SecurityInputStatus.Unlocked)
            it.onSuccess(NavigationTargets.CONFIG_SECURITY)
        })

    private fun observeLockProcess() {
        compositeDisposable.addSafe(
            checkPasswordInputLockStatusUC.execute()
                .subscribe({ passwordInputStatus ->
                    when {
                        isInitial(passwordInputStatus) || hasProcessChangedStatusToUnlocked(
                            passwordInputStatus
                        ) -> {
                            clearInvalidAttemptsNumber()
                            _securityInputStatus.postValue(passwordInputStatus)
                        }
                        isLocked(passwordInputStatus) -> _securityInputStatus.postValue(
                            passwordInputStatus
                        )
                    }
                }, {})
        )
    }

    private fun checkInvalidPasswordAttempts() =
        compositeDisposable.addSafe(
            checkAttemptsNumberLeftUC.execute()
                .subscribe({ invalidAttemptsNumber ->
                    if (invalidAttemptsNumber in 1..2)
                        _securityInputStatus.value =
                            SecurityInputStatus.InvalidAndUnlocked(invalidAttemptsNumber)
                }, {})
        )

    private fun isInitial(securityInputStatus: SecurityInputStatus) =
        securityInputStatus == SecurityInputStatus.Unlocked && _securityInputStatus.value is SecurityInputStatus.None

    private fun hasProcessChangedStatusToUnlocked(securityInputStatus: SecurityInputStatus) =
        securityInputStatus is SecurityInputStatus.Unlocked && _securityInputStatus.value?.isLockedStatus() == true

    private fun isLocked(securityInputStatus: SecurityInputStatus) =
        securityInputStatus is SecurityInputStatus.Locked

    enum class NavigationTargets {
        NONE, CONFIG_SECURITY
    }
}