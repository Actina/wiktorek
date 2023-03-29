package pl.gov.mf.etoll.front.settings.languagesettingsv2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gov.mf.etoll.base.BaseComposeViewModel
import pl.gov.mf.etoll.front.localization.TranslationsGenerator
import pl.gov.mf.etoll.front.regulationsv2.RegulationsProvider
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.etoll.translations.AppLanguageManagerUC
import pl.gov.mf.etoll.translations.SupportedLanguages
import pl.gov.mf.etoll.translations.TranslationKeys
import pl.gov.mf.etoll.translations.TranslationsContainer
import pl.gov.mf.mobile.utils.addSafe
import java.io.Serializable
import javax.inject.Inject

class LanguageSettingsViewModelV2(private val savedStateHandle: SavedStateHandle) :
    BaseComposeViewModel(savedStateHandle = savedStateHandle), TranslationsGenerator {

    private val _viewData = savedStateHandle.getLiveData(
        SELECTED_LANGUAGE_VIEW_DATA,
        initialValue = LanguagesSettingsViewData()
    )
    val viewData: LiveData<LanguagesSettingsViewData>
        get() = _viewData

    private val _navigation = MutableLiveData(LanguageSettingsNavigation.NONE)
    val navigation: LiveData<LanguageSettingsNavigation> = _navigation

    private val _registrationMode = MutableLiveData(RegistrationMode.APP_REGISTERED)
    val registrationMode: LiveData<RegistrationMode> = _registrationMode

    @Inject
    lateinit var writeSettingsUseCase: SettingsUC.WriteSettingsUseCase

    @Inject
    lateinit var setCurrentLanguageUseCase: AppLanguageManagerUC.SetCurrentLanguageUseCase

    @Inject
    lateinit var getCurrentLanguageUseCase: AppLanguageManagerUC.GetCurrentLanguageUseCase

    @Inject
    lateinit var getAvailableLanguagesUseCase: AppLanguageManagerUC.GetAvailableLanguagesUseCase

    @Inject
    lateinit var regulationsProvider: RegulationsProvider

    private lateinit var selectedLanguage: SupportedLanguages
    private lateinit var currentRegulationsMode: RegulationsProvider.Mode
    private val languagesList = mutableListOf<SupportedLanguages>()

    override fun onCreate() {
        super.onCreate()
        compositeDisposable.addSafe(
            getAvailableLanguagesUseCase.execute().subscribe({ availableLanguages ->
                if (!::selectedLanguage.isInitialized) {
                    selectedLanguage = getCurrentLanguageUseCase.execute()
                }
                languagesList.addAll(availableLanguages)
                _viewData.value = LanguagesSettingsViewData(
                    selectedLanguage,
                    availableLanguages
                )
            }, {})
        )
        if (!::currentRegulationsMode.isInitialized) {
            viewModelScope.launch(Dispatchers.IO) {
                currentRegulationsMode = regulationsProvider.getCurrentRegulationsMode()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        savedStateHandle.get<String>(SELECTED_LANGUAGE)?.let {
            selectedLanguage = enumValueOf(removeQuotas(it))
            _viewData.value = LanguagesSettingsViewData(
                selectedLanguage,
                languagesList
            )
        }
    }

    override fun onPause() {
        super.onPause()
        savedStateHandle[SELECTED_LANGUAGE] = selectedLanguage.toJSON()
    }

    fun onApplyNewLanguageClicked() {
        compositeDisposable.addSafe(
            setCurrentLanguageUseCase.execute(selectedLanguage)
                .subscribe({
                    _navigation.value = LanguageSettingsNavigation.APPLY_NEW_LANGUAGE
                }, {})
        )
    }

    fun findNewDestination(startedFromSplash: Boolean) {
        if (startedFromSplash) {
            onIO {
                writeSettingsUseCase.executeV2(Settings.SELECTED_LANGUAGE_WEAK_SAVE, true)
                _navigation.postValue(LanguageSettingsNavigation.NEW_REGULATIONS_TO_ACCEPT)
            }
        } else {
            _navigation.value =
                LanguageSettingsNavigation.GO_BACK_TO_SETTINGS
        }
    }

    fun resetNavigation() {
        _navigation.value = LanguageSettingsNavigation.NONE
    }

    fun onLanguageSelected(newLanguage: SupportedLanguages) {
        selectedLanguage = newLanguage
        _viewData.value =
            LanguagesSettingsViewData(
                selectedLanguage,
                languagesList
            )
    }

    fun setRegistrationMode(isRegistrationMode: Boolean) {
        _registrationMode.value = if (isRegistrationMode)
            RegistrationMode.REGISTRATION
        else
            RegistrationMode.APP_REGISTERED
    }

    override fun generateTranslations(): TranslationsContainer = TranslationsContainer(
        keys = mutableListOf(
            TranslationKeys.LANGUAGE_SELECTION_HEADER,
            TranslationKeys.LANGUAGE_SELECTION_TITLE,
            TranslationKeys.MISC_SAVE_BUTTON,
            TranslationKeys.LANG_POLISH,
            TranslationKeys.LANG_ENGLISH,
            TranslationKeys.LANG_GERMAN,
            TranslationKeys.LANG_CZECH,
            TranslationKeys.LANG_SLOVAK,
            TranslationKeys.LANG_UKRAINIAN,
            TranslationKeys.LANG_BELARUSIAN,
            TranslationKeys.LANG_LITHUANIAN,
            TranslationKeys.LANG_RUSSIAN,
        )
    )

    private fun removeQuotas(it: String) = it.replace("\"", "")

    data class LanguagesSettingsViewData(
        val selectedLanguage: SupportedLanguages = SupportedLanguages.ENGLISH,
        val availableLanguages: List<SupportedLanguages> = emptyList(),
    ) : Serializable

    enum class RegistrationMode {
        REGISTRATION, APP_REGISTERED
    }

    enum class LanguageSettingsNavigation {
        NONE, APPLY_NEW_LANGUAGE, NEW_REGULATIONS_TO_ACCEPT, GO_BACK_TO_SETTINGS
    }

    companion object {
        private const val SELECTED_LANGUAGE = "selectedLanguage"
        private const val SELECTED_LANGUAGE_VIEW_DATA = "selectedLanguageViewData"
    }
}