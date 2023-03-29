package pl.gov.mf.etoll.front.regulationsv2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.gov.mf.etoll.appmode.AppMode
import pl.gov.mf.etoll.base.BaseComposeViewModel
import pl.gov.mf.etoll.core.watchdog.CoreWatchdogUC
import pl.gov.mf.etoll.front.localization.TranslationsGenerator
import pl.gov.mf.etoll.interfaces.CommonInterfacesUC
import pl.gov.mf.etoll.interfaces.NotificationHistoryController
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.etoll.translations.AppLanguageManagerUC
import pl.gov.mf.etoll.translations.SupportedLanguages
import pl.gov.mf.etoll.translations.TranslationKeys
import pl.gov.mf.etoll.translations.TranslationsContainer
import pl.gov.mf.mobile.utils.addSafe
import javax.inject.Inject

class RegulationsFragmentV2ViewModel(savedStateHandle: SavedStateHandle) :
    BaseComposeViewModel(savedStateHandle = savedStateHandle), TranslationsGenerator {

    @Inject
    lateinit var regulationsProvider: RegulationsProvider

    @Inject
    lateinit var getCurrentLanguageUseCase: AppLanguageManagerUC.GetCurrentLanguageUseCase

    @Inject
    lateinit var changeCoreObservationModeUseCase: CoreWatchdogUC.ChangeCoreObservationModeUseCase

    @Inject
    lateinit var readSettingsUseCase: SettingsUC.ReadSettingsUseCase

    @Inject
    lateinit var addToHistoryUseCase: CommonInterfacesUC.AddNotificationToHistoryUseCase

    private var _regulationsMode: MutableLiveData<RegulationsProvider.Mode> = MutableLiveData()
    val regulationsMode: LiveData<RegulationsProvider.Mode>
        get() = _regulationsMode

    private var _filename: MutableLiveData<String> = MutableLiveData()
    val filename: LiveData<String>
        get() = _filename

    private val _navigate: MutableLiveData<NavigationTargets> =
        MutableLiveData(NavigationTargets.NONE)
    val navigate: LiveData<NavigationTargets>
        get() = _navigate

    private var firstScreenEntry: Boolean = true

    private var _shouldShowAcceptDialog: MutableLiveData<Boolean> = MutableLiveData(false)
    val shouldShowAcceptDialog: LiveData<Boolean> = _shouldShowAcceptDialog

    override fun generateTranslations(): TranslationsContainer =
        TranslationsContainer(
            keys = mutableListOf(
                TranslationKeys.REGISTRATION_REGULATIONS_TITLE,
                TranslationKeys.REGISTRATION_REGULATIONS_ACCEPT,
                TranslationKeys.NEW_REGULATIONS_WHATSNEW_DIALOG_HEADER,
                TranslationKeys.NEW_REGULATIONS_WHATSNEW_DIALOG_TITLE,
                TranslationKeys.NEW_REGULATIONS_WHATSNEW_DIALOG_CONTENT,
                TranslationKeys.MISC_OK_BUTTON
            )
        )

    override fun onCreate() {
        super.onCreate()
        firstScreenEntry = true
        viewModelScope.launch(Dispatchers.IO + executionJob) {
            withContext(Dispatchers.Main) {
                _regulationsMode.value = regulationsProvider.getCurrentRegulationsMode()
                _filename.value =
                    regulationsProvider.getRegulationsFileName() + fileNameLanguage() + fileNameEnd()

                if (firstScreenEntry && _regulationsMode.value == RegulationsProvider.Mode.NEW_REGULATIONS)
                    showAcceptDialog()
            }
        }
    }

    fun onNewRegulationsOkClicked() {
        hideAcceptDialog()
        firstScreenEntry = false
    }

    private fun showAcceptDialog() {
        _shouldShowAcceptDialog.postValue(true)
    }

    private fun hideAcceptDialog() {
        _shouldShowAcceptDialog.postValue(false)
    }

    fun acceptRegulations() {
        showLoading()
        firstScreenEntry = false

        viewModelScope.launch(executionJob + Dispatchers.IO) {
            regulationsProvider.acceptNewRegulations(
                job = executionJob,
                returnDelegate = {
                    // TODO: refactor later when db will be made in coroutines way:
                    if (regulationsMode.value == RegulationsProvider.Mode.REGISTRATION_REQUIRED) {
                        compositeDisposable.addSafe(
                            addToHistoryUseCase.execute(
                                NotificationHistoryController.Type.CRM,
                                "registered_mode_sent_header",
                                "registered_mode_sent_content"
                            ).subscribe({
                                hideLoading()
                                onRegistrationFinished()
                            }, {
                                hideLoading()
                                onRegistrationFinished()
                            })
                        )
                    } else {
                        hideLoading()
                        onRegistrationFinished()
                    }
                },
                errorDelegate = {
                    // error
                    hideLoading()
                    showError(it.map())
                })
        }
    }

    private fun onRegistrationFinished() {
        // regulations accepted
        changeCoreObservationModeUseCase.executeForDefault()
        regulationsMode.value?.let {
            onMain {
                when (it) {
                    RegulationsProvider.Mode.NEW_REGULATIONS -> {
                        _navigate.value = NavigationTargets.DRIVER_WARNING
                    }
                    RegulationsProvider.Mode.REGISTRATION_REQUIRED -> {
                        _navigate.value = NavigationTargets.REGISTERED
                    }
                    else -> {
                        throw RuntimeException("Wrong flow!")
                    }
                }
            }
        }
    }

    private fun fileNameLanguage(): String =
        if (getCurrentLanguageUseCase.execute() == SupportedLanguages.POLISH) "_pl_PL"
        else "_en_EN"

    private fun fileNameEnd(): String = when (appMode.value?.appMode) {
        AppMode.DARK_MODE -> "_dark.pdf"
        AppMode.LIGHT_MODE, null -> "_light.pdf"
    }

    fun resetNavigation() {
        _navigate.value = NavigationTargets.NONE
    }

    enum class NavigationTargets {
        NONE,
        REGISTERED,
        DRIVER_WARNING,
    }
}