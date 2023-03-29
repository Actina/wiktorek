package pl.gov.mf.etoll.ui.components.dialogs

import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicOneActionDialogFragment

class RideCantBeStartedDialogFragment : BasicOneActionDialogFragment() {

    companion object {
        private const val TAG = "RideCantBeStartedDialogFragment"

        fun createAndShow(fm: FragmentManager) = if (fm.findFragmentByTag(TAG) == null)
            RideCantBeStartedDialogFragment().apply {
                isCancelable = true
                this.viewModel.configure(
                    startHeaderText = "ride_cant_be_started_header_android",
                    startContentText = "ride_cant_be_started_content_android",
                    startButtonText = "ride_cant_be_started_button_android",
                    startIconRes = R.drawable.ic_confirm_finish_ride_light,
                    startButtonStyle = ButtonStyles.BLUE,
                    startHeaderColor = "dialogHeaderTextPrimary"
                )
                show(fm, TAG)
            }.dialogResult else null
    }
}
