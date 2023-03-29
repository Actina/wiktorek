package pl.gov.mf.etoll.ui.components.dialogs.ridedetails

import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicTwoActionsDialogFragment

class ConfirmSentCancelDialogFragment : BasicTwoActionsDialogFragment() {

    companion object {
        private const val TAG = "ConfirmSentCancelDialogFragment"

        fun createAndShow(fm: FragmentManager, lastSent: Boolean) =
            if (fm.findFragmentByTag(TAG) == null)
                ConfirmSentCancelDialogFragment().apply {
                    isCancelable = true
                    this.viewModel.configure(
                        if (lastSent) "ride_details_sent_selection_and_ride_cancel_dialog_title" else "ride_details_sent_selection_cancel_dialog_title",
                        if (lastSent) "ride_details_sent_selection_and_ride_cancel_dialog_content" else "ride_details_sent_selection_cancel_dialog_content",
                        "ride_details_sent_selection_dialog_confirm",
                        "ride_details_sent_selection_dialog_cancel",
                        "ic_warning",
                        ButtonStyles.BLUE,
                        ButtonStyles.RED,
                        "dialogHeaderTextPrimary"
                    )
                    show(fm, TAG)
                }.dialogResult else null
    }
}
