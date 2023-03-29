package pl.gov.mf.etoll.ui.components.dialogs

import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicOneActionDialogFragment

class NoConditionsToStartConfigurationFragment : BasicOneActionDialogFragment() {

    companion object {
        private const val TAG = "NoConditionsToStartConfigurationFragment"

        fun createAndShow(fm: FragmentManager) = if (fm.findFragmentByTag(TAG) == null)
            NoConditionsToStartConfigurationFragment().apply {
                isCancelable = true
                this.viewModel.configure(
                    startHeaderText = "dialog_error_generic_error_title",
                    startContentText = "dashboard_no_conditions_to_start_configuration",
                    startButtonText = "dialog_ok",
                    startIconRes = R.drawable.ic_warning_light,
                    startButtonStyle = ButtonStyles.BLUE,
                    startHeaderColor = "dialogHeaderTextPrimary"
                )
                show(fm, TAG)
            }.dialogResult else null

    }
}