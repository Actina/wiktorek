package pl.gov.mf.etoll.front.security.config.password

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.core.devicecompatibility.DeviceCompatibilityUC
import pl.gov.mf.etoll.core.devicecompatibility.types.BiometricState
import pl.gov.mf.etoll.core.security.SecurityConfigUC
import pl.gov.mf.etoll.front.security.config.password.models.ConfigPasswordData
import pl.gov.mf.etoll.front.security.config.types.SecurityConfigurationType
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.etoll.validations.ValidationManagerUC
import pl.gov.mf.etoll.validations.ValidationResult
import pl.gov.mf.mobile.utils.addSafe
import javax.inject.Inject

class ConfigPasswordViewModel : BaseDatabindingViewModel() {

    @Inject
    lateinit var writeSettingsUseCase: SettingsUC.WriteSettingsUseCase

    @Inject
    lateinit var passwordsValidationUseCase: ValidationManagerUC.ValidatePasswordsUseCase

    @Inject
    lateinit var getBiometricStateUseCase: DeviceCompatibilityUC.GetBiometricStateUseCase

    @Inject
    lateinit var savePasswordUseCase: SecurityConfigUC.SavePasswordUseCase

    @Inject
    lateinit var saveBiometricAuthOptionUseCase: SecurityConfigUC.SaveBiometricAuthOptionUseCase

    private var _configurationType = SecurityConfigurationType.NONE

    private val _navigate: MutableLiveData<NavigationTargets> =
        MutableLiveData(NavigationTargets.NONE)
    val navigate: LiveData<NavigationTargets>
        get() = _navigate

    private val _securityConfigData = MutableLiveData(ConfigPasswordData())
    val securityConfigData: LiveData<ConfigPasswordData>
        get() = _securityConfigData

    private val _validationResult = MutableLiveData(ValidationResult())
    val validationResult: LiveData<ValidationResult>
        get() = _validationResult

    private val _biometricState = MutableLiveData<BiometricState>()
    val biometricState: LiveData<BiometricState>
        get() = _biometricState

    private val _isCancelAvailable = MutableLiveData<Boolean>()
    val isCancelAvailable: LiveData<Boolean>
        get() = _isCancelAvailable

    fun onBiometricSettingsChanged(isSwitchedOn: Boolean) {
        if (!isSwitchedOn) {
            saveBiometricValueToSettings(false)
            return
        }

        when (_biometricState.value) {
            BiometricState.AVAILABLE_BUT_SWITCHED_OFF -> _navigate.postValue(NavigationTargets.BIOMETRIC_SETTINGS)
            BiometricState.AVAILABLE_AND_SWITCHED_ON -> _navigate.postValue(NavigationTargets.BIOMETRIC_PROMPT)
            else -> Unit
        }
    }

    fun onSaveClicked() {
        val validationResult = validatePasswords()
        if (!validationResult.isValid) {
            _validationResult.value = validationResult
            return
        }

        securityConfigData.value?.password?.let { password ->
            compositeDisposable.addSafe(
                savePasswordUseCase.execute(password)
                    .subscribe(::handleForwardNavigation, {})
            )
        }
    }

    fun clearValidationResult() = _validationResult.postValue(ValidationResult())

    fun checkBiometricState() = _biometricState.postValue(getBiometricStateUseCase.execute())

    fun saveBiometricValueToSettings(isSwitchedOn: Boolean) {
        compositeDisposable.addSafe(
            saveBiometricAuthOptionUseCase.execute(isSwitchedOn)
                .subscribe({}, {})
        )
    }

    fun setConfigurationType(configurationType: SecurityConfigurationType) {
        _configurationType = configurationType
        _isCancelAvailable.postValue(configurationType != SecurityConfigurationType.REGISTRATION)
    }

    fun cancelProcess() {
        _navigate.value = when (_configurationType) {
            SecurityConfigurationType.SETTINGS -> NavigationTargets.SECURITY_SETTINGS
            SecurityConfigurationType.RESET_TO_UNLOCK -> NavigationTargets.EXIT_RESET_PROCESS
            else -> NavigationTargets.NONE
        }
    }

    private fun handleForwardNavigation() {
        _navigate.value = when (_configurationType) {
            SecurityConfigurationType.REGISTRATION -> NavigationTargets.DASHBOARD
            SecurityConfigurationType.SETTINGS -> NavigationTargets.SECURITY_SETTINGS
            SecurityConfigurationType.RESET_TO_UNLOCK -> NavigationTargets.EXIT_RESET_PROCESS
            else -> NavigationTargets.NONE
        }
    }

    private fun validatePasswords(): ValidationResult {
        val password =
            securityConfigData.value?.password ?: return ValidationResult(isValid = false)
        val confirmationPassword =
            securityConfigData.value?.confirmationPassword
                ?: return ValidationResult(isValid = false)

        return passwordsValidationUseCase.execute(password, confirmationPassword)
    }

    enum class NavigationTargets {
        NONE,
        DASHBOARD,
        SECURITY_SETTINGS,
        EXIT_RESET_PROCESS,
        BIOMETRIC_SETTINGS,
        BIOMETRIC_PROMPT
    }
}