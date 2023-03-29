package pl.gov.mf.etoll.ui.components.dialogs

import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicOneActionDialogFragment

class NetworkErrorDialogFragment : BasicOneActionDialogFragment() {

    companion object {
        private const val TAG = "NetworkErrorDialogFragment"

        fun createAndShow(fm: FragmentManager) = if (fm.findFragmentByTag(TAG) == null)
            NetworkErrorDialogFragment().apply {
                setStyle(STYLE_NORMAL, R.style.Theme_Spoe_Dialog_FullScreen)
                isCancelable = false
                viewModel.configure(
                    startHeaderText = "dialog_network_error_header",
                    startContentText = "dialog_network_error_content",
                    startButtonText = "dialog_retry_android",
                    startButtonStyle = ButtonStyles.BLUE,
                    startHeaderColor = "dialogHeaderTextPrimary",
                    contentStyle = ContentStyles.EXPANDED
                )
                show(fm, TAG)
            }.dialogResult else null
    }
}