package pl.gov.mf.etoll.front.security.settings.adapteritem

import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.utils.DynamicBindingAdapter

class PinConfigItem(
    onResetPinListener: () -> Unit,
    onTurnOffSecurityListener: () -> Unit
) : DynamicBindingAdapter.Item(
    R.layout.item_security_overview_pin_config,
    ViewModel(onResetPinListener, onTurnOffSecurityListener)
) {

    data class ViewModel(
        private val onResetPinListener: () -> Unit,
        private val onTurnOffSecurityListener: () -> Unit
    ) {
        fun onResetPinClicked() = onResetPinListener()

        fun onTurnOffSecurityClicked() = onTurnOffSecurityListener()
    }
}