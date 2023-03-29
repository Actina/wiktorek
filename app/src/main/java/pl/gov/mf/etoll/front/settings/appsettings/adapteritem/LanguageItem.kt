package pl.gov.mf.etoll.front.settings.appsettings.adapteritem

import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.utils.DynamicBindingAdapter

class LanguageItem(selectedLanguage: String, onChangeLanguageClicked: () -> Unit) :
    DynamicBindingAdapter.Item(
        R.layout.item_app_settings_language,
        ViewModel(selectedLanguage, onChangeLanguageClicked)
    ) {
    data class ViewModel(val selectedLanguage: String, val onChangeLanguageClicked: () -> Unit)
}
