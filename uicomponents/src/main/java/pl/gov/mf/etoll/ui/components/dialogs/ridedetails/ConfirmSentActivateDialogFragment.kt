package pl.gov.mf.etoll.ui.components.dialogs.ridedetails

import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicTwoActionsDialogFragment

class ConfirmSentActivateDialogFragment : BasicTwoActionsDialogFragment() {

    companion object {
        private const val TAG = "ConfirmSentActivateDialogFragment"

        fun createAndShow(fm: FragmentManager) = if (fm.findFragmentByTag(TAG) == null)
            ConfirmSentActivateDialogFragment().apply {
                isCancelable = true
                this.viewModel.configure(
                    "ride_details_sent_selection_active_dialog_title",
                    "ride_details_sent_selection_active_dialog_content",
                    "ride_details_sent_selection_dialog_confirm",
                    "ride_details_sent_selection_dialog_cancel",
                    null,
                    ButtonStyles.BLUE,
                    ButtonStyles.RED,
                    "dialogHeaderTextPrimary"
                )
                show(fm, TAG)
            }.dialogResult else null
    }
}
