package pl.gov.mf.etoll.ui.components.dialogs

import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.interfaces.NotificationHistoryController
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicOneActionDialogFragment

class SentConfigurationContinuationDialogFragment : BasicOneActionDialogFragment() {

    companion object {
        private const val TAG = "SentConfigurationContinuationDialogFragment"

        fun createAndShow(fm: FragmentManager, sentListEmpty: Boolean) =
            if (fm.findFragmentByTag(TAG) == null) {
                SentConfigurationContinuationDialogFragment().apply {
                    isCancelable = false
                    this.viewModel.configure(
                        startHeaderText = if (sentListEmpty) "dialog_complete_current_ride_configuration_no_sent_header" else "dialog_complete_current_ride_configuration_header",
                        startContentText = if (sentListEmpty) "dialog_complete_current_ride_configuration_no_sent_content" else "dialog_complete_current_ride_configuration_content",
                        startButtonText = if (sentListEmpty) "dialog_complete_current_ride_configuration_no_sent_configure" else "dialog_complete_current_ride_configuration_configure",
                        startIconRes = R.drawable.ic_autoconfig_light,
                        startButtonStyle = ButtonStyles.BLUE,
                        startHeaderColor = "dialogHeaderTextPrimary"
                    )
                    show(fm, TAG)
                }.dialogResult
            } else null
    }

    override fun getTypeForHistory(): NotificationHistoryController.Type =
        NotificationHistoryController.Type.GOOD
}
