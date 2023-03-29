package pl.gov.mf.etoll.ui.components.dialogs.ridedetails

import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicOneActionDialogFragment

class TecsSessionExpiredDialogFragment : BasicOneActionDialogFragment() {
    companion object {
        private const val TAG = "TecsSessionExpiredDialogFragment"

        fun createAndShow(fm: FragmentManager) = if (fm.findFragmentByTag(TAG) == null)
            TecsSessionExpiredDialogFragment().apply {
                isCancelable = true
                this.viewModel.configure(
                    startHeaderText = "misc_warning_header",
                    startContentText = "session_expired_error_info",
                    startButtonText = "misc_ok_button",
                    startIconRes = R.drawable.ic_warning_light,
                    startButtonStyle = ButtonStyles.BLUE,
                    startHeaderColor = "dialogHeaderTextPrimary"
                )
                show(fm, TAG)
            }.dialogResult else null
    }
}