package pl.gov.mf.etoll.front.settings.appsettings.adapteritem

import androidx.lifecycle.LiveData
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.utils.DynamicBindingAdapter

class OverlayItem(isChecked: LiveData<Boolean>, clicked: () -> Unit) :
    DynamicBindingAdapter.Item(
        R.layout.item_app_settings_overlay,
        ViewModel(isChecked, clicked)
    ) {

    data class ViewModel(val checked: LiveData<Boolean>, val clicked: () -> Unit) {
        fun onOverlaySwitchClicked() {
            clicked()
        }
    }
}