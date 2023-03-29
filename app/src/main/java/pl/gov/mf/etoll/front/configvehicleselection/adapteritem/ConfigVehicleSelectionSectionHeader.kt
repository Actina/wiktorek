package pl.gov.mf.etoll.front.configvehicleselection.adapteritem

import android.view.Gravity
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.utils.DynamicBindingAdapter

class ConfigVehicleSelectionSectionHeader(
    untranslated: String,
    isPaddingTop: Boolean = false,
    gravity: Int = Gravity.LEFT
) :
    DynamicBindingAdapter.Item(
        R.layout.item_config_vehicle_selection_section_header,
        ViewModel(untranslated, isPaddingTop, gravity)
    ) {
    data class ViewModel(
        val untranslated: String,
        val isPaddingTop: Boolean,
        val gravity: Int
    )
}