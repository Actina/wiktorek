package pl.gov.mf.etoll.ui.components.dialogs

import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicTwoActionsDialogFragment
import pl.gov.mf.mobile.utils.toDrawableIdInMode

class ConfirmRideFinishDialogFragment : BasicTwoActionsDialogFragment() {

    companion object {
        private const val TAG = "ConfirmRideFinishDialogFragment"

        fun createAndShow(fm: FragmentManager, tolled: Boolean, sent: Boolean) =
            if (fm.findFragmentByTag(TAG) == null)
                ConfirmRideFinishDialogFragment().apply {
                    isCancelable = true
                    this.viewModel.configure(
                        if (tolled && !sent) "dialog_end_ride_confirmation_header" else if (sent && !tolled) "dialog_end_sent_ride_confirmation_header" else "dialog_end_mix_ride_confirmation_header",
                        if (tolled && !sent) "dialog_end_ride_confirmation_content" else if (sent && !tolled) "dialog_end_sent_ride_confirmation_content" else "dialog_end_mix_ride_confirmation_content",
                        "dialog_end_ride_confirmation_confirm",
                        "dashboard_ride_control_cancel",
                        if (tolled)
                            "ic_confirm_finish_ride"
                        else
                            "ic_finish_sent",
                        ButtonStyles.RED,
                        ButtonStyles.BLUE,
                        "dialogHeaderTextPrimary"
                    )
                    show(fm, TAG)
                }.dialogResult else null
    }
}
