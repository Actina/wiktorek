package pl.gov.mf.etoll.front.security.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.core.devicecompatibility.DeviceCompatibilityUC
import pl.gov.mf.etoll.core.devicecompatibility.types.BiometricState
import pl.gov.mf.etoll.core.security.SecurityConfigUC
import pl.gov.mf.etoll.front.security.settings.models.SecuritySettingsConfig
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.utils.addSafe
import javax.inject.Inject

class SecuritySettingsViewModel : BaseDatabindingViewModel() {

    @Inject
    lateinit var isSecurityConfigValidUseCaseUseCase: SecurityConfigUC.IsSecurityConfigValidUseCase

    @Inject
    lateinit var getBiometricStateUseCase: DeviceCompatibilityUC.GetBiometricStateUseCase

    //TODO: remove save and get biometric auth useCases - overengineering
    @Inject
    lateinit var getBiometricAuthOptionUseCase: SecurityConfigUC.GetBiometricAuthOptionUseCase

    @Inject
    lateinit var saveBiometricAuthOptionUseCase: SecurityConfigUC.SaveBiometricAuthOptionUseCase

    @Inject
    lateinit var writeSettingsUseCase: SettingsUC.WriteSettingsUseCase

    @Inject
    lateinit var readSettingsUseCase: SettingsUC.ReadSettingsUseCase

    private val _isSecurityConfigValid = MutableLiveData<Boolean>()

    private val _biometricState = MutableLiveData<BiometricState>()
    val biometricState: LiveData<BiometricState>
        get() = _biometricState

    private val _isBiometricAuthOn = MutableLiveData<Boolean>()
    val isBiometricAuthOn: LiveData<Boolean>
        get() = _isBiometricAuthOn

    val securitySettingsConfig: LiveData<SecuritySettingsConfig> =
        MediatorLiveData<SecuritySettingsConfig>().apply {
            value = SecuritySettingsConfig()
            addSource(biometricState) { biometricState ->
                value =
                    value?.copy(isBiometricAvailable = biometricState != BiometricState.NOT_AVAILABLE)
            }
            addSource(_isSecurityConfigValid) { isValid ->
                value = value?.copy(isSecurityConfigValid = isValid)
            }
        }

    override fun onResume() {
        super.onResume()

        _biometricState.value =
            getBiometricStateUseCase.execute() //WARNING: DO NOT use .postValue here

        if (biometricState.value != BiometricState.AVAILABLE_AND_SWITCHED_ON) {
            //When we return to the app and to this screen and fingerprint was removed
            //- we should clear IS_BIOMETRIC_AUTH_ON flag in settings and update switch
            compositeDisposable.addSafe(
                //Disable biometric auth in settings
                writeSettingsUseCase.execute(Settings.IS_BIOMETRIC_AUTH_ON, false)
                    .subscribe({
                        //sync switch with auth disabled in settings
                        _isBiometricAuthOn.value =
                            readSettingsUseCase.executeForBoolean(Settings.IS_BIOMETRIC_AUTH_ON)
                    }, {

                    })
            )
        }
    }

    fun checkSecurityConfig() {
        compositeDisposable.addSafe(
            isSecurityConfigValidUseCaseUseCase.execute()
                .subscribe({ isValid ->
                    _isSecurityConfigValid.value = isValid
                }, {})
        )
    }

    fun fetchBiometricAuthOption() {
        compositeDisposable.addSafe(
            getBiometricAuthOptionUseCase.execute()
                .subscribe({ isEnabled ->
                    _isBiometricAuthOn.value = isEnabled
                }, {})
        )
    }

    fun checkBiometricState() = _biometricState.postValue(getBiometricStateUseCase.execute())

    fun saveBiometricValueToSettings(isSwitchedOn: Boolean) {
        compositeDisposable.addSafe(
            saveBiometricAuthOptionUseCase.execute(isSwitchedOn)
                .subscribe({
                    _isBiometricAuthOn.value = isSwitchedOn
                }, {})
        )
    }

    fun toggleBiometricAuthOption(): Boolean {
        val isEnabled = _isBiometricAuthOn.value!!.not()
        _isBiometricAuthOn.value = isEnabled
        return isEnabled
    }

    fun switchOffBiometricAuthOption() = _isBiometricAuthOn.postValue(false)
}