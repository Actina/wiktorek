package pl.gov.mf.etoll.front.settings.appsettings.adapteritem

import androidx.lifecycle.LiveData
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.utils.DynamicBindingAdapter

class NotificationsItem(
    soundChecked: LiveData<Boolean>,
    onSoundClicked: () -> Unit,
    vibrationChecked: LiveData<Boolean>,
    onVibrationClicked: () -> Unit
) :
    DynamicBindingAdapter.Item(
        R.layout.item_app_settings_notifications,
        ViewModel(
            soundChecked, onSoundClicked,
            vibrationChecked, onVibrationClicked
        )
    ) {
    data class ViewModel(
        val soundChecked: LiveData<Boolean>, val onSoundClicked: () -> Unit,
        val vibrationChecked: LiveData<Boolean>, val onVibrationClicked: () -> Unit
    ) {

        fun onSoundNotificationsSelected() {
            onSoundClicked()
        }

        fun onVibrationNotificationsSelected() {
            onVibrationClicked()
        }
    }
}
