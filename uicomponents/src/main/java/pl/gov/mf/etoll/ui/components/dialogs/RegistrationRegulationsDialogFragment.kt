package pl.gov.mf.etoll.ui.components.dialogs

import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicTwoActionsDialogFragment
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicTwoActionsDialogFragment.ButtonStyles.BLUE
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicTwoActionsDialogFragment.ButtonStyles.RED_OUTLINED

class RegistrationRegulationsDialogFragment : BasicTwoActionsDialogFragment() {

    companion object {
        private const val TAG = "TecsCancelTransactionDialogFragment"

        fun createAndShow(fm: FragmentManager) =
            if (fm.findFragmentByTag(TAG) == null)
                RegistrationRegulationsDialogFragment().apply {
                    isCancelable = false
                    viewModel.configure(
                        "dialog_registration_regulations_header",
                        "dialog_registration_regulations_content",
                        "dialog_registration_regulations_accept",
                        "dialog_registration_regulations_disagree",
                        "ic_warning",
                        BLUE,
                        RED_OUTLINED,
                        "dialogHeaderTextPrimary",
                        horizontalButtonsVisible = false,
                        verticalButtonsVisible = true
                    )
                    show(fm, TAG)
                }.dialogResult else null
    }
}