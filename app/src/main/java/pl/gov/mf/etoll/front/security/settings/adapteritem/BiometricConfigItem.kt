package pl.gov.mf.etoll.front.security.settings.adapteritem

import androidx.lifecycle.LiveData
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.utils.DynamicBindingAdapter

class BiometricConfigItem(isChecked: LiveData<Boolean>, clicked: () -> Unit) :
    DynamicBindingAdapter.Item(
        R.layout.item_security_overview_biometric_config,
        ViewModel(isChecked, clicked)
    ) {

    data class ViewModel(val checked: LiveData<Boolean>, private val clicked: () -> Unit) {

        fun onBiometricSwitched() = clicked()
    }
}