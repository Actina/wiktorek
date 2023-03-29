package pl.gov.mf.etoll.ui.components.dialogs.base

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pl.gov.mf.etoll.front.localization.localize
import pl.gov.mf.etoll.translations.AppLanguageManagerUC
import pl.gov.mf.etoll.translations.SupportedLanguages
import pl.gov.mf.etoll.translations.TranslationsContainer

abstract class BaseComposeDialogFragmentViewModel(
    private val getCurrentLocalizationUseCase: AppLanguageManagerUC.GetCurrentLanguageUseCase
) : ViewModel() {

    private var lastLanguage: SupportedLanguages? = null

    private var _translations = MutableLiveData<TranslationsContainer>()
    val translations: LiveData<TranslationsContainer>
        get() = _translations

    fun onCreate() {
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
}