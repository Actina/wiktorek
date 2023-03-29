package pl.gov.mf.etoll.front.security.unlock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import io.reactivex.SingleSource
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.core.biometric.BiometricStatus
import pl.gov.mf.etoll.core.devicecompatibility.DeviceCompatibilityUC
import pl.gov.mf.etoll.core.devicecompatibility.types.BiometricState
import pl.gov.mf.etoll.core.security.SecurityConfigUC
import pl.gov.mf.etoll.core.security.unlock.PinUnlockUC
import pl.gov.mf.etoll.core.security.unlock.statuses.SecurityInputStatus
import pl.gov.mf.etoll.front.security.unlock.models.SecurityUnlockWithPinCodeConfiguration
import pl.gov.mf.etoll.front.security.unlock.models.UnlockWithPinCode
import pl.gov.mf.mobile.utils.addSafe
import javax.inject.Inject

class SecurityUnlockWithPinCodeViewModel : BaseDatabindingViewModel() {

    @Inject
    lateinit var markAppAsLockedUseCase: PinUnlockUC.MarkAppAsLockedUseCase

    @Inject
    lateinit var checkPinUseCase: SecurityConfigUC.CheckPinUseCase

    @Inject
    lateinit var invalidPinEnteredUseCase: PinUnlockUC.InvalidPinEnteredUseCase

    @Inject
    lateinit var clearPinInvalidAttemptsNumberUC: PinUnlockUC.ClearPinInvalidAttemptsNumberUC

    @Inject
    lateinit var unlockAppUseCase: PinUnlockUC.UnlockAppUseCase

    @Inject
    lateinit var lockPinInputUC: PinUnlockUC.LockPinInputUC

    @Inject
    lateinit var checkPinInputLockStatusUC: PinUnlockUC.CheckPinInputLockStatusUC

    @Inject
    lateinit var checkAttemptsNumberLeftUC: PinUnlockUC.CheckAttemptsNumberLeftUC

    @Inject
    lateinit var getBiometricStateUseCase: DeviceCompatibilityUC.GetBiometricStateUseCase

    @Inject
    lateinit var getBiometricAuthOptionUseCase: SecurityConfigUC.GetBiometricAuthOptionUseCase

    @Inject
    lateinit var updateUiUseCase: SecurityUnlockWithPinCodeUC.UpdateUiUseCase

    private val _unlockWithPinCode = MutableLiveData(UnlockWithPinCode())
    val unlockWithPinCode: LiveData<UnlockWithPinCode>
        get() = _unlockWithPinCode

    private val _navigate = MutableLiveData(NavigationTargets.NONE)
    val navigate: LiveData<NavigationTargets>
        get() = _navigate

    private val _biometricEnabled = MutableLiveData<Boolean>()
    val biometricEnabled: LiveData<Boolean>
        get() = _biometricEnabled

    private val _securityInputStatus =
        MutableLiveData<SecurityInputStatus>(SecurityInputStatus.None)
    val configuration: LiveData<SecurityUnlockWithPinCodeConfiguration> =
        Transformations.map(_securityInputStatus) { updateUiUseCase.execute(it) }

    override fun onResume() {
        super.onResume()

        observeLockProcess()
        checkInvalidPinAttempts()
        checkBiometricSignInOption()
    }

    fun markAppAsLocked() {
        compositeDisposable.addSafe(
            markAppAsLockedUseCase.execute()
                .subscribe({}, {})
        )
    }

    fun checkPin() {
        val pin = unlockWithPinCode.value?.pin ?: return

        val disposable = checkPinUseCase.execute(pin)
            .flatMap { isValid ->
                if (isValid) unlockAppCleanup()
                else invalidPinEnteredUseCase.execute().map {
                    if (it is SecurityInputStatus.InvalidAndLocked)
                        lockPinProcess()
                    _securityInputStatus.postValue(it)
                    _unlockWithPinCode.value = UnlockWithPinCode()
                    NavigationTargets.NONE
                }
            }.subscribe({
                _navigate.postValue(it)
            }, {})

        compositeDisposable.addSafe(disposable)
    }

    private fun observeLockProcess() {
        compositeDisposable.addSafe(
            checkPinInputLockStatusUC.execute()
                .subscribe({ pinInputStatus ->
                    when {
                        isInitial(pinInputStatus) || hasProcessChangedStatusToUnlocked(
                            pinInputStatus
                        ) -> {
                            clearInvalidAttemptsNumber()
                            _securityInputStatus.postValue(pinInputStatus)
                        }
                        isLocked(pinInputStatus) -> _securityInputStatus.postValue(pinInputStatus)
                    }
                }, {})
        )
    }

    fun handleBiometricResult(result: BiometricStatus?) {
        when (result) {
            BiometricStatus.SUCCESS -> unlockAppCleanup().subscribe({
                _navigate.value = it
            }, {})
            else -> Unit
        }
    }

    private fun lockPinProcess() {
        compositeDisposable.addSafe(
            lockPinInputUC.execute()
                .subscribe({}, {})
        )
    }

    private fun clearInvalidAttemptsNumber() {
        compositeDisposable.addSafe(
            clearPinInvalidAttemptsNumberUC.execute()
                .subscribe({}, {})
        )
    }

    private fun unlockAppCleanup() = unlockAppUseCase.execute()
        .andThen(SingleSource<NavigationTargets> {
            _securityInputStatus.postValue(SecurityInputStatus.Unlocked)
            it.onSuccess(NavigationTargets.BACK)
        })

    private fun checkBiometricSignInOption() {
        compositeDisposable.addSafe(
            getBiometricAuthOptionUseCase.execute()
                .subscribe({ isEnabled ->
                    _biometricEnabled.postValue(isEnabled && getBiometricStateUseCase.execute() == BiometricState.AVAILABLE_AND_SWITCHED_ON)
                }, {})
        )
    }

    private fun checkInvalidPinAttempts() {
        compositeDisposable.addSafe(
            checkAttemptsNumberLeftUC.execute()
                .subscribe({ invalidAttemptsNumber ->
                    if (invalidAttemptsNumber in 1..2)
                        _securityInputStatus.value =
                            SecurityInputStatus.InvalidAndUnlocked(invalidAttemptsNumber)
                }, {})
        )
    }

    private fun isInitial(securityInputStatus: SecurityInputStatus) =
        securityInputStatus == SecurityInputStatus.Unlocked && _securityInputStatus.value is SecurityInputStatus.None

    private fun hasProcessChangedStatusToUnlocked(securityInputStatus: SecurityInputStatus) =
        securityInputStatus is SecurityInputStatus.Unlocked && _securityInputStatus.value?.isLockedStatus() == true

    private fun isLocked(securityInputStatus: SecurityInputStatus) =
        securityInputStatus is SecurityInputStatus.Locked

    enum class NavigationTargets {
        NONE, BACK
    }
}