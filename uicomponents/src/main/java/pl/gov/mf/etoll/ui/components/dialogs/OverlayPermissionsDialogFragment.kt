package pl.gov.mf.etoll.ui.components.dialogs

import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicTwoActionsDialogFragment

class OverlayPermissionsDialogFragment : BasicTwoActionsDialogFragment() {

    companion object {
        private const val TAG = "ConfirmSentEndDialogFragment"

        fun createAndShow(fm: FragmentManager) =
            if (fm.findFragmentByTag(TAG) == null)
                OverlayPermissionsDialogFragment().apply {
                    isCancelable = true
                    this.viewModel.configure(
                        startHeaderText = "bubble_warning_header_android",
                        startContentText = "bubble_permissions_text_android",
                        startButtonText = "bubble_permissions_settings_button_android",
                        startCancelButtonText = "bubble_permissions_cancel_button_android",
                        startIconResName = "ic_warning",
                        startButtonStyle = ButtonStyles.BLUE,
                        startCancelButtonStyle = ButtonStyles.RED,
                        startHeaderColorName = "dialogHeaderTextPrimary"
                    )
                    show(fm, TAG)
                }.dialogResult else null
    }
}
