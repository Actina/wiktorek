package pl.gov.mf.etoll.front.security.config.pin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.core.security.SecurityConfigUC
import pl.gov.mf.etoll.front.security.config.pin.models.ConfigPinData
import pl.gov.mf.etoll.front.security.config.pin.models.ConfigPinTranslations
import pl.gov.mf.etoll.front.security.config.types.SecurityConfigurationType
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.etoll.validations.ValidationManagerUC
import pl.gov.mf.etoll.validations.ValidationResult
import pl.gov.mf.mobile.utils.addSafe
import javax.inject.Inject

class ConfigPinViewModel : BaseDatabindingViewModel() {

    @Inject
    lateinit var writeSettingsUseCase: SettingsUC.WriteSettingsUseCase

    @Inject
    lateinit var readSettingsUseCase: SettingsUC.ReadSettingsUseCase

    @Inject
    lateinit var validatePinsUseCase: ValidationManagerUC.ValidatePinsUseCase

    @Inject
    lateinit var savePinUseCase: SecurityConfigUC.SavePinUseCase

    var configurationType = SecurityConfigurationType.NONE
        set(value) {
            field = value
            configureTranslations()
            configureBackOption()
        }

    private fun configureBackOption() =
        _backOptionAvailability.postValue(configurationType != SecurityConfigurationType.REGISTRATION)

    private fun configureTranslations() {
        _configPinTranslations.value = when (configurationType) {
            SecurityConfigurationType.NONE,
            SecurityConfigurationType.RESET_TO_UNLOCK,
            SecurityConfigurationType.REGISTRATION -> ConfigPinTranslations("security_config_pin_code_skip")
            SecurityConfigurationType.SETTINGS -> ConfigPinTranslations("security_config_pin_code_cancel")
        }
    }

    private val _navigate: MutableLiveData<NavigationTargets> =
        MutableLiveData(NavigationTargets.NONE)
    val navigate: LiveData<NavigationTargets>
        get() = _navigate

    private val _securityConfigData = MutableLiveData(ConfigPinData())
    val securityConfigData: LiveData<ConfigPinData>
        get() = _securityConfigData

    private val _validationResult = MutableLiveData(ValidationResult())
    val validationResult: LiveData<ValidationResult>
        get() = _validationResult

    private val _configPinTranslations = MutableLiveData<ConfigPinTranslations>()
    val configPinTranslations: LiveData<ConfigPinTranslations>
        get() = _configPinTranslations

    private val _backOptionAvailability = MutableLiveData<Boolean>()
    val backOptionAvailability: LiveData<Boolean>
        get() = _backOptionAvailability

    fun onSkipClicked() {
        when (configurationType) {
            SecurityConfigurationType.REGISTRATION -> saveSecurityTransSucceed()
            SecurityConfigurationType.SETTINGS ->
                _navigate.postValue(NavigationTargets.SECURITY_SETTINGS)
            SecurityConfigurationType.RESET_TO_UNLOCK ->
                _navigate.postValue(NavigationTargets.EXIT_RESET_PROCESS)
            else -> Unit
        }
    }

    fun onBackPressed() {
        _navigate.value = when (configurationType) {
            SecurityConfigurationType.NONE -> NavigationTargets.NONE
            SecurityConfigurationType.REGISTRATION -> NavigationTargets.CLOSE_APP
            SecurityConfigurationType.SETTINGS -> NavigationTargets.SECURITY_SETTINGS
            SecurityConfigurationType.RESET_TO_UNLOCK -> NavigationTargets.EXIT_RESET_PROCESS
        }
    }

    private fun saveSecurityTransSucceed() {
        compositeDisposable.addSafe(
            writeSettingsUseCase.execute(Settings.SECURITY_TRANS_SUCCEED, true)
                .subscribe({
                    _navigate.value =
                        NavigationTargets.DASHBOARD
                }, {})
        )
    }

    fun securityTransSucceedAndIsRegistration(): Boolean =
        readSettingsUseCase.executeForBoolean(Settings.SECURITY_TRANS_SUCCEED) && configurationType == SecurityConfigurationType.REGISTRATION

    fun clearValidationResult() = _validationResult.postValue(ValidationResult())

    fun savePin() {
        val validationResult = validatePins()
        if (!validationResult.isValid) {
            _validationResult.value = validationResult
            return
        }

        securityConfigData.value?.pin?.let { pin ->
            compositeDisposable.addSafe(
                savePinUseCase.execute(pin)
                    .subscribe({
                        _navigate.value =
                            NavigationTargets.CONFIG_PASSWORD
                    }, {})
            )
        }
    }

    fun resetNavigation() = _navigate.postValue(NavigationTargets.NONE)

    private fun validatePins(): ValidationResult {
        val pin = securityConfigData.value?.pin ?: return ValidationResult(isValid = false)
        val confirmationPin =
            securityConfigData.value?.confirmationPin ?: return ValidationResult(isValid = false)

        return validatePinsUseCase.execute(pin, confirmationPin)
    }

    enum class NavigationTargets {
        NONE,
        CONFIG_PASSWORD,
        DASHBOARD,
        SECURITY_SETTINGS,
        EXIT_RESET_PROCESS,
        CLOSE_APP
    }
}