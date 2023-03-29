package pl.gov.mf.etoll.ui.components.dialogs.ridedetails

import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicTwoActionsDialogFragment

class ConfirmSentEndDialogFragment : BasicTwoActionsDialogFragment() {

    companion object {
        private const val TAG = "ConfirmSentEndDialogFragment"

        fun createAndShow(fm: FragmentManager, lastSent: Boolean) =
            if (fm.findFragmentByTag(TAG) == null)
                ConfirmSentEndDialogFragment().apply {
                    isCancelable = true
                    this.viewModel.configure(
                        if (lastSent) "ride_details_sent_selection_and_ride_stop_dialog_title" else "ride_details_sent_selection_stop_dialog_title",
                        if (lastSent) "ride_details_sent_selection_and_ride_stop_dialog_content" else "ride_details_sent_selection_stop_dialog_content",
                        "ride_details_sent_selection_dialog_confirm",
                        "ride_details_sent_selection_dialog_cancel",
                        "ic_warning_red",
                        ButtonStyles.RED,
                        ButtonStyles.BLUE,
                        "dialogHeaderTextPrimary"
                    )
                    show(fm, TAG)
                }.dialogResult else null
    }
}
