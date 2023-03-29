package pl.gov.mf.etoll.front.about.adapteritem

import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.utils.DynamicBindingAdapter

class RegulationsAndConsentsItem(onShowRegulationsAndConsents: () -> Unit) :
    DynamicBindingAdapter.Item(
        R.layout.item_about_consents,
        ViewModel(onShowRegulationsAndConsents)
    ) {
    data class ViewModel(
        val onShowRegulationsAndConsents: () -> Unit
    )
}