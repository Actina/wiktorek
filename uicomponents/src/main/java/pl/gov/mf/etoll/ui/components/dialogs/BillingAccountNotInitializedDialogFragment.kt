package pl.gov.mf.etoll.ui.components.dialogs

import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicOneActionDialogFragment

class BillingAccountNotInitializedDialogFragment : BasicOneActionDialogFragment() {
    companion object {
        private const val TAG = "NkspoBillingAccountIssuesDialogFragment"

        fun createAndShow(fm: FragmentManager) = if (fm.findFragmentByTag(TAG) == null)
            BillingAccountNotInitializedDialogFragment().apply {
                isCancelable = true
                this.viewModel.configure(
                    startHeaderText = "dialog_billing_account_not_initialized_title",
                    startContentText = "dialog_billing_account_not_initialized_content",
                    startButtonText = "dialog_billing_account_not_initialized_button",
                    startIconRes = R.drawable.ic_error_light,
                    startButtonStyle = ButtonStyles.BLUE,
                    startHeaderColor = "dialogHeaderTextPrimary"
                )
                show(fm, TAG)
            }.dialogResult else null
    }
}