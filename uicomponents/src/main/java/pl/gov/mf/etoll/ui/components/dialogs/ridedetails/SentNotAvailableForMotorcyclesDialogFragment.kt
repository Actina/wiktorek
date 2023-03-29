package pl.gov.mf.etoll.ui.components.dialogs.ridedetails

import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicOneActionDialogFragment

class SentNotAvailableForMotorcyclesDialogFragment : BasicOneActionDialogFragment() {
    companion object {
        private const val TAG = "SentNotAvailableForMotorcyclesDialogFragment"

        fun createAndShow(fm: FragmentManager) = if (fm.findFragmentByTag(TAG) == null)
            SentNotAvailableForMotorcyclesDialogFragment().apply {
                isCancelable = true
                this.viewModel.configure(
                    startHeaderText = "motorcycle_sent_error_header",
                    startContentText = "motorcycle_sent_error_message",
                    startButtonText = "motorcycle_sent_error_button",
                    startIconRes = R.drawable.ic_warning_light,
                    startButtonStyle = ButtonStyles.BLUE,
                    startHeaderColor = "dialogHeaderTextPrimary"
                )
                show(fm, TAG)
            }.dialogResult else null
    }
}