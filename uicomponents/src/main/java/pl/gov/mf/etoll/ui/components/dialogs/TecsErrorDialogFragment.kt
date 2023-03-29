package pl.gov.mf.etoll.ui.components.dialogs

import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicOneActionDialogFragment

class TecsErrorDialogFragment : BasicOneActionDialogFragment() {
    companion object {
        private const val TAG = "TecsErrorDialogFragment"

        fun showGenericError(title: String?, content: String?, fm: FragmentManager) =
            if (fm.findFragmentByTag(TAG) == null)
                TecsErrorDialogFragment().apply {
                    isCancelable = true
                    this.viewModel.configure(
                        startHeaderText = title ?: "dialog_error_generic_error_title",
                        startContentText = content ?: "dialog_error_generic_error",
                        startButtonText = "dialog_ok",
                        startIconRes = R.drawable.ic_warning_light,
                        startButtonStyle = ButtonStyles.BLUE,
                        startHeaderColor = "dialogHeaderTextPrimary"
                    )
                    show(fm, TAG)
                }.dialogResult else null
    }

}