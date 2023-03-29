package pl.gov.mf.etoll.ui.components.dialogs

import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicTwoActionsDialogFragment

class CancelRideConfigurationDialogFragment : BasicTwoActionsDialogFragment() {

    companion object {
        private const val TAG = "ConfirmRideFinishDialogFragment"

        fun createAndShow(fm: FragmentManager) = if (fm.findFragmentByTag(TAG) == null)
            CancelRideConfigurationDialogFragment().apply {
                isCancelable = true
                this.viewModel.configure(
                    "dashboard_dialog_drop_configuration_title",
                    "dashboard_dialog_drop_configuration_content",
                    "dashboard_dialog_drop_configuration_yes",
                    "dashboard_dialog_drop_configuration_no",
                    "ic_warning",
                    ButtonStyles.BLUE,
                    ButtonStyles.RED,
                    "dialogHeaderTextPrimary"
                )
                show(fm, TAG)
            }.dialogResult else null
    }
}
