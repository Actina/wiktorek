package pl.gov.mf.etoll.front.settings.darkmode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.appmode.AppMode
import pl.gov.mf.etoll.appmode.AppModeManagerUC
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.front.settings.darkmode.DarkModeSettingsFragment.ThemeSettingsViewData
import pl.gov.mf.mobile.utils.addSafe
import javax.inject.Inject

class DarkModeSettingsViewModel : BaseDatabindingViewModel() {

    private val _viewData = MutableLiveData<ThemeSettingsViewData>()
    val viewData: LiveData<ThemeSettingsViewData>
        get() = _viewData

    private val _navigation = MutableLiveData<DarkModeSettingsNavigation>()
    val navigation: LiveData<DarkModeSettingsNavigation>
        get() = _navigation

    private var selectedMode: AppMode? = null
    private var savedMode: AppMode? = null

    @Inject
    lateinit var setCurrentAppModeUseCase: AppModeManagerUC.SetCurrentAppModeUseCase

    fun onApplyClicked() {
        if (selectedMode != savedMode) {
            compositeDisposable.addSafe(
                setCurrentAppModeUseCase.execute(
                    selectedMode ?: AppMode.LIGHT_MODE, selectedMode == null
                ).subscribe()
            )
            savedMode = selectedMode
            _navigation.postValue(DarkModeSettingsNavigation.CHANGE_LAYOUT_BACK)
        }
    }

    fun resetNavigation() {
        _navigation.postValue(DarkModeSettingsNavigation.IDLE)
    }

    override fun onCreate() {
        savedMode = getCurrentAppModeUseCase.executeForConfigurationValue()
        selectedMode = savedMode

        _viewData.value =
            ThemeSettingsViewData(selectedMode)

    }

    fun onModeSelected(dark: Boolean, bySystem: Boolean) {
        selectedMode = if (bySystem) null else if (dark) AppMode.DARK_MODE else AppMode.LIGHT_MODE
        viewData.value?.let {
            _viewData.value = ThemeSettingsViewData(selectedMode)
        }
    }

    enum class DarkModeSettingsNavigation {
        IDLE, CHANGE_LAYOUT_BACK
    }
}
