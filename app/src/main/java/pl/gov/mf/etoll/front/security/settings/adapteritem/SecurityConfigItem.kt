package pl.gov.mf.etoll.front.security.settings.adapteritem

import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.utils.DynamicBindingAdapter

class SecurityConfigItem(
    onConfigureSecurityListener: () -> Unit
) : DynamicBindingAdapter.Item(
    R.layout.item_security_overview_security_config,
    ViewModel(onConfigureSecurityListener)
) {

    data class ViewModel(private val onConfigureSecurityListener: () -> Unit) {

        fun onConfigureSecurityClicked() = onConfigureSecurityListener()
    }
}