package pl.gov.mf.etoll.ui.components.dialogs

import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicTwoActionsDialogFragment

class CancellableTwoButtonDialog : BasicTwoActionsDialogFragment() {

    companion object {
        private const val TAG = "CancellableTwoButtonDialog"

        fun createAndShow(
            fm: FragmentManager,
            title: String,
            body: String,
            button: String,
            iconRes: String? = null
        ) =
            if (fm.findFragmentByTag(TAG) == null)
                CancellableTwoButtonDialog().apply {
                    isCancelable = true
                    this.viewModel.configure(
                        title,
                        body,
                        button,
                        "dialog_permissions_cancel_android",
                        iconRes,
                        ButtonStyles.BLUE,
                        ButtonStyles.RED,
                        startHeaderColorName = "dialogHeaderTextPrimary"
                    )
                    show(fm, TAG + System.currentTimeMillis())
                }.dialogResult else null
    }
}
