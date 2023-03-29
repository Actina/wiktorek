package pl.gov.mf.etoll.ui.components.dialogs

import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicOneActionDialogFragment

class RideCantBeStartedGpsNotExistsDialogFragment : BasicOneActionDialogFragment() {

    companion object {
        private const val TAG = "RideCantBeStartedGpsNotExistsDialogFragment"

        fun createAndShow(fm: FragmentManager) = if (fm.findFragmentByTag(TAG) == null)
            RideCantBeStartedGpsNotExistsDialogFragment().apply {
                isCancelable = true
                this.viewModel.configure(
                    startHeaderText = "misc_error_title",
                    startContentText = "no_gps_module_start_route_info_content",
                    startButtonText = "misc_ok_button",
                    startIconRes = R.drawable.ic_warning_red_light,
                    startButtonStyle = ButtonStyles.BLUE,
                    startHeaderColor = "dialogHeaderTextPrimary"
                )
                show(fm, TAG)
            }.dialogResult else null
    }
}
