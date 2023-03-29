package pl.gov.mf.etoll.ui.components.dialogs.crm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.translations.AppLanguageManagerUC
import pl.gov.mf.etoll.translations.TranslationKeys
import pl.gov.mf.etoll.translations.TranslationsContainer
import pl.gov.mf.etoll.ui.components.dialogs.base.BaseComposeDialogFragmentViewModel

class CrmDialogViewModel(
    private val getCurrentLanguageUseCase: AppLanguageManagerUC.GetCurrentLanguageUseCase,
) : BaseComposeDialogFragmentViewModel(getCurrentLanguageUseCase) {
    private val _contentText: MutableLiveData<String> = MutableLiveData("")
    val contentText: LiveData<String> = _contentText
    private val _headerText: MutableLiveData<String> = MutableLiveData("")
    val headerText: LiveData<String> = _headerText
    private val _messageType =
        MutableLiveData<CrmDialogFragment.MessageType>(CrmDialogFragment.MessageType.INFO)
    val messageType: LiveData<CrmDialogFragment.MessageType> = _messageType

    override fun generateTranslations(): TranslationsContainer = TranslationsContainer(
        keys = mutableListOf(TranslationKeys.MISC_OK_BUTTON)
    )

    fun refreshUiState(
        data: CrmDialogFragment.CrmMessageModel,
    ) {
        _contentText.value = data.contents?.getForLanguage(getCurrentLanguageUseCase.execute())
        _headerText.value =
            data.headers?.getForLanguage(getCurrentLanguageUseCase.execute())
        _messageType.value = data.type
    }

    enum class DialogResult {
        CONFIRMED
    }
}