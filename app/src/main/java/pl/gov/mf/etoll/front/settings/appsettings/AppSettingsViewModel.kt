package pl.gov.mf.etoll.front.settings.appsettings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.appmode.AppMode
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.core.overlay.controller.OverlayController
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorUC
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.etoll.translations.AppLanguageManagerUC
import pl.gov.mf.mobile.utils.addSafe
import pl.gov.mf.mobile.utils.translate
import javax.inject.Inject

class AppSettingsViewModel : BaseDatabindingViewModel() {
    private val _selectedLanguage = MutableLiveData("")
    val selectedLanguage: LiveData<String>
        get() = _selectedLanguage

    private val _sentMode = MutableLiveData<Boolean>()
    val sentMode: LiveData<Boolean>
        get() = _sentMode

    private val _soundChecked = MutableLiveData<Boolean>()
    val soundChecked: LiveData<Boolean>
        get() = _soundChecked

    private val _vibrationChecked = MutableLiveData<Boolean>()
    val vibrationChecked: LiveData<Boolean>
        get() = _vibrationChecked

    private val _overlayEnabled = MutableLiveData<Boolean>()
    val overlayEnabled: LiveData<Boolean>
        get() = _overlayEnabled

    private val _requestedDestination =
        MutableLiveData(SettingsNavigationDestinations.NONE)
    val requestedDestinations: LiveData<SettingsNavigationDestinations>
        get() = _requestedDestination

    private val _appModeConfigValue = MutableLiveData<String>()
    val appModeConfigValue = _appModeConfigValue

    @Inject
    lateinit var getCurrentLanguageUseCase: AppLanguageManagerUC.GetCurrentLanguageUseCase

    @Inject
    lateinit var writeSettingsUseCase: SettingsUC.WriteSettingsUseCase

    @Inject
    lateinit var readSettingsUseCase: SettingsUC.ReadSettingsUseCase

    @Inject
    lateinit var getRideCoordinatorConfigurationUseCase: RideCoordinatorUC.GetRideCoordinatorConfigurationUseCase

    @Inject
    lateinit var overlayController: OverlayController

    private var context: Context? = null

    fun onToolbarBack() {
        _requestedDestination.postValue(SettingsNavigationDestinations.BACK)
    }

    fun onResume(context: Context) {
        super.onResume()
        _sentMode.postValue(readSettingsUseCase.executeForBoolean(Settings.SUPPORT_FOR_SENT_ENABLED))
        _vibrationChecked.postValue(readSettingsUseCase.executeForBoolean(Settings.VIBRATION_NOTIFICATIONS))
        _soundChecked.postValue(readSettingsUseCase.executeForBoolean(Settings.SOUND_NOTIFICATIONS))
        _overlayEnabled.postValue(overlayController.overlayIsEnabled(context))
        _selectedLanguage.value =
            getCurrentLanguageUseCase.execute().translationName.key.translate(context)
        _appModeConfigValue.value = when (getCurrentAppModeUseCase.executeForConfigurationValue()) {
            AppMode.DARK_MODE -> "dark_mode_dark_mode_option"
            AppMode.LIGHT_MODE -> "dark_mode_light_mode_option"
            null -> "dark_mode_system_option"
        }
        this.context = context
    }

    override fun onPause() {
        super.onPause()
        this.context = null
    }

    fun onSoundClicked() {
        val soundEnabled = !soundChecked.value!!
        compositeDisposable.addSafe(
            writeSettingsUseCase.execute(Settings.SOUND_NOTIFICATIONS, soundEnabled)
                .subscribe({
                    _soundChecked.postValue(soundEnabled)
                }, {})
        )
    }

    fun onVibrationClicked() {
        val vibrationEnabled = !vibrationChecked.value!!
        compositeDisposable.addSafe(
            writeSettingsUseCase.execute(Settings.VIBRATION_NOTIFICATIONS, vibrationEnabled)
                .subscribe({
                    _vibrationChecked.postValue(vibrationEnabled)
                }, {})
        )
    }

    fun onSentModeSwitched() {
        val sentEnabled = !_sentMode.value!!
        compositeDisposable.addSafe(
            writeSettingsUseCase.execute(Settings.SUPPORT_FOR_SENT_ENABLED, sentEnabled)
                .subscribe({
                    _sentMode.postValue(sentEnabled)
                }, {})
        )
    }

    fun onOverlayModeSwitchRequested() {
        context?.let {
            if (overlayController.overlayIsEnabled(it)) {
                overlayController.disableOverlay()
                _overlayEnabled.postValue(false)
            } else {
                if (overlayController.tryToEnableOverlay(it)) {
                    _overlayEnabled.postValue(true)
                } else {
                    _requestedDestination.postValue(SettingsNavigationDestinations.OVERLAY_PERMISSIONS)
                }
            }
        }
    }

    fun onDarkModeSwitchRequested() {
        _requestedDestination.postValue(SettingsNavigationDestinations.DARK_MODE)
    }

    fun onChangeLanguageClicked() {
        _requestedDestination.postValue(SettingsNavigationDestinations.LANGUAGE_SETTINGS)
    }

    fun resetRequestedDestination() {
        _requestedDestination.postValue(SettingsNavigationDestinations.NONE)
    }

    fun shouldShowSentSelector(): Boolean =
        !(getRideCoordinatorConfigurationUseCase.execute()?.duringRide
            ?: false)

    fun showOverlayPermissionsRequest() {
        context?.let {
            overlayController.askForPermissions(it)
        }
    }
}

enum class SettingsNavigationDestinations {
    NONE, BACK, DARK_MODE, LANGUAGE_SETTINGS, OVERLAY_PERMISSIONS
}