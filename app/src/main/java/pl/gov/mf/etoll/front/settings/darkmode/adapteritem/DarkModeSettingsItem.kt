package pl.gov.mf.etoll.front.settings.darkmode.adapteritem

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.appmode.AppMode
import pl.gov.mf.etoll.utils.DynamicBindingAdapter

class DarkModeSettingsItem(
    modeName: String,
    selected: Boolean,
    onClicked: () -> Unit,
) :
    DynamicBindingAdapter.Item(
        R.layout.item_dark_mode_settings, ViewModel(
            mode = modeName,
            selected = selected,
            onClicked = onClicked
        )
    ) {

    data class ViewModel(
        val mode: String,
        val selected: Boolean,
        val onClicked: () -> Unit,
    ) {
        private var _radioButtonChecked = MutableLiveData(false)
        val radioButtonChecked: LiveData<Boolean> get() = _radioButtonChecked

        init {
            _radioButtonChecked.value = selected
        }

        fun onItemBodySelected() {
            _radioButtonChecked.value = true
            onClicked()
        }
    }
}