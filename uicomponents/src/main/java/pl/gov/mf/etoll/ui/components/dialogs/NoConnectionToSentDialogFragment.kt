package pl.gov.mf.etoll.ui.components.dialogs

import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.interfaces.NotificationHistoryController
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicOneActionDialogFragment


class NoConnectionToSentDialogFragment : BasicOneActionDialogFragment() {

    companion object {
        private const val TAG = "NoConnectionToSentDialogFragment"

        fun createAndShow(fm: FragmentManager) = if (fm.findFragmentByTag(TAG) == null)
            NoConnectionToSentDialogFragment().apply {
                isCancelable = true
                this.viewModel.configure(
                    startHeaderText = "dialog_no_connection_to_sent_server_header",
                    startContentText = "dialog_no_connection_to_sent_server_content",
                    startButtonText = "dialog_no_connection_to_sent_server_continue",
                    startIconRes = R.drawable.ic_network_error_light,
                    startButtonStyle = ButtonStyles.BLUE,
                    startHeaderColor = "dialogHeaderTextPrimary"
                )
                show(fm, TAG)
            }.dialogResult else null
    }

    override fun getTypeForHistory(): NotificationHistoryController.Type =
        NotificationHistoryController.Type.INFO
}