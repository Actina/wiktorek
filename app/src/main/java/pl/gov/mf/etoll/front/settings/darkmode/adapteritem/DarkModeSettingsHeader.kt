package pl.gov.mf.etoll.front.settings.darkmode.adapteritem

import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.utils.DynamicBindingAdapter

class DarkModeSettingsHeader : DynamicBindingAdapter.Item(
    R.layout.item_dark_mode_settings_header,
    ViewModel("")
) {
    data class ViewModel(val empty: String = "")
}