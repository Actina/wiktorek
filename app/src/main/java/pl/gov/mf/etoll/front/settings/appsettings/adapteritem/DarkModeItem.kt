package pl.gov.mf.etoll.front.settings.appsettings.adapteritem

import androidx.lifecycle.LiveData
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.utils.DynamicBindingAdapter

class DarkModeItem(val header: String, val selectedMode: LiveData<String>, clicked: () -> Unit) :
    DynamicBindingAdapter.Item(
        R.layout.item_app_settings_dark_mode,
        ViewModel(header, selectedMode, clicked)
    ) {

    data class ViewModel(
        val header: String,
        val selectedMode: LiveData<String>,
        val clicked: () -> Unit,
    ) {
        fun onDarkModeSwitchClicked() {
            clicked()
        }
    }
}