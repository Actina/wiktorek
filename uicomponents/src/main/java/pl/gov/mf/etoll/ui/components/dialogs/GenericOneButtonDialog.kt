package pl.gov.mf.etoll.ui.components.dialogs

import androidx.fragment.app.FragmentManager
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicOneActionDialogFragment

class GenericOneButtonDialog : BasicOneActionDialogFragment() {

    companion object {
        private const val TAG = "GenericOneButtonDialog"

        fun createAndShow(fm: FragmentManager, title: String, body: String, button: String) =
            if (fm.findFragmentByTag(TAG) == null)
                GenericOneButtonDialog().apply {
                    isCancelable = true
                    this.viewModel.configure(
                        startHeaderText = title,
                        startContentText = body,
                        startButtonText = button,
                        startButtonStyle = ButtonStyles.BLUE,
                        startHeaderColor = "dialogHeaderTextPrimary",
                        iconVisible = false
                    )
                    show(fm, TAG + System.currentTimeMillis())
                }.dialogResult else null
    }
}