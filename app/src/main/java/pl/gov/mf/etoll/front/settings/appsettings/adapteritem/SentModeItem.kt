package pl.gov.mf.etoll.front.settings.appsettings.adapteritem

import androidx.lifecycle.LiveData
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.utils.DynamicBindingAdapter

class SentModeItem(isChecked: LiveData<Boolean>, clicked: () -> Unit) :
    DynamicBindingAdapter.Item(
        R.layout.item_app_settings_sent_mode,
        ViewModel(isChecked, clicked)
    ) {
    data class ViewModel(val checked: LiveData<Boolean>, val clicked: () -> Unit) {
        fun onSentModeSwitched() {
            clicked()
        }
    }
}