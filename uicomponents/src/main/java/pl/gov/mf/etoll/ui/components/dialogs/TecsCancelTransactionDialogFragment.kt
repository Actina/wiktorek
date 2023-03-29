package pl.gov.mf.etoll.ui.components.dialogs

import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicTwoActionsDialogFragment

class TecsCancelTransactionDialogFragment : BasicTwoActionsDialogFragment() {

    companion object {
        private const val TAG = "TecsCancelTransactionDialogFragment"

        fun showTecsCancelTransactionDialog(
            fm: FragmentManager,
        ) =
            if (fm.findFragmentByTag(TAG) == null)
                TecsCancelTransactionDialogFragment().apply {
                    isCancelable = true
                    this.viewModel.configure(
                        "dialog_top_up_amount_break_header",
                        "dialog_top_up_amount_break_content",
                        "dialog_top_up_amount_break_stop",
                        "dialog_top_up_amount_break_cancel",
                        "ic_tecs_cancel",
                        ButtonStyles.RED,
                        ButtonStyles.BLUE,
                        "dialogHeaderTextPrimary"
                    )
                    show(fm, TAG)
                }.dialogResult else null
    }
}