package pl.gov.mf.etoll.front.about.adapteritem

import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.utils.DynamicBindingAdapter

class AppUpdateItem(appVersion: String, onAppUpdateClicked: () -> Unit) :
    DynamicBindingAdapter.Item(
        R.layout.item_about_update_app,
        ViewModel(appVersion, onAppUpdateClicked)
    ) {
    data class ViewModel(
        val appVersion: String,
        val onAppUpdateClicked: () -> Unit
    )
}
