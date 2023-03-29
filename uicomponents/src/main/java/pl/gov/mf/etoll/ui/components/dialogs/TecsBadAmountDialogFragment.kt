package pl.gov.mf.etoll.ui.components.dialogs

import androidx.annotation.DrawableRes
import androidx.fragment.app.FragmentManager
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicOneActionDialogFragment

class TecsBadAmountDialogFragment: BasicOneActionDialogFragment() {
    companion object {
        private const val TAG = "TecsBadAmountDialogFragment"

        fun createAndShow(title: String?, content: String?, @DrawableRes startIconRes: Int, fm: FragmentManager) =
            if (fm.findFragmentByTag(TAG) == null)
                TecsErrorDialogFragment().apply {
                    isCancelable = true
                    this.viewModel.configure(
                        startHeaderText = title ?: "dialog_error_generic_error_title",
                        startContentText = content ?: "dialog_error_generic_error",
                        startButtonText = "dialog_ok",
                        startIconRes = startIconRes,
                        startButtonStyle = ButtonStyles.BLUE,
                        startHeaderColor = "dialogHeaderOrangeText"
                    )
                    show(fm, TAG)
                }.dialogResult else null
    }
}