package pl.gov.mf.etoll.base

import android.content.Context
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import pl.gov.mf.etoll.PendingError
import pl.gov.mf.etoll.ViewErrorCause
import pl.gov.mf.etoll.front.localization.localize
import pl.gov.mf.etoll.translations.AppLanguageManagerUC
import pl.gov.mf.etoll.translations.SupportedLanguages
import pl.gov.mf.etoll.translations.TranslationKeys
import pl.gov.mf.etoll.translations.TranslationsContainer
import javax.inject.Inject

abstract class BaseComposeViewModel(private val savedStateHandle: SavedStateHandle) :
    BaseViewModel(), LifecycleObserver {

    @Inject
    lateinit var getCurrentLocalizationUseCase: AppLanguageManagerUC.GetCurrentLanguageUseCase

    private var _loadingIndicator = MutableLiveData(false)
    val loadingIndicator: LiveData<Boolean>
        get() = _loadingIndicator

    private var _translations = MutableLiveData<TranslationsContainer>()
    val translations: LiveData<TranslationsContainer>
        get() = _translations

    private lateinit var errorTranslations: TranslationsContainer

    private var lastLanguage: SupportedLanguages? = null

    val pendingError: LiveData<PendingError> =
        savedStateHandle.getLiveData(PENDING_ERROR_DIALOG_TAG)

    override fun onCreate() {
        super.onCreate()
        _translations.value = generateTranslations()
    }

    abstract fun generateTranslations(): TranslationsContainer

    fun onStart(context: Context) {
        refreshTranslations(context)
    }

    fun onStop() {
        lastLanguage = null
    }

    private fun refreshTranslations(context: Context) {
        val language = getCurrentLocalizationUseCase.execute()
        if (lastLanguage == null || lastLanguage != language) {
            // refresh
            lastLanguage = language

            _translations.value?.copy()?.let {
                it.localize(context, language)
                _translations.value = it
            }
        }
    }

    fun showLoading() {
        _loadingIndicator.value = true
    }

    fun hideLoading() {
        _loadingIndicator.value = false
    }

    fun getErrorTranslations(context: Context): TranslationsContainer {
        if (::errorTranslations.isInitialized)
            return errorTranslations

        errorTranslations = generateErrorTranslations().apply {
            localize(context, getCurrentLocalizationUseCase.execute())
        }

        return errorTranslations
    }

    fun showError(cause: ViewErrorCause, customMessage: String? = null) {
        savedStateHandle[PENDING_ERROR_DIALOG_TAG] =
            PendingError(shouldBeShown = true, error = cause, customMessage = customMessage)
    }

    fun generateErrorTranslations() = TranslationsContainer(
        keys = mutableListOf(
            TranslationKeys.MISC_ERROR_TITLE,
            TranslationKeys.NETWORK_ERROR_INFO_CONTENT,
            TranslationKeys.MISC_OK_BUTTON,
            TranslationKeys.OLD_VERSION_ERROR_INFO_CONTENT,
            TranslationKeys.OLD_VERSION_ERROR_UPDATE_BUTTON,
            TranslationKeys.TIME_ERROR_TITLE
        )
    )

    fun onErrorShown(cause: ViewErrorCause, customText: String?) {
        savedStateHandle[PENDING_ERROR_DIALOG_TAG] = PendingError(
            shouldBeShown = false,
            error = cause,
            customMessage = customText
        )
    }

    companion object {
        const val PENDING_ERROR_DIALOG_TAG: String = "PENDING_ERROR"
    }
}