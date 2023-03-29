package pl.gov.mf.etoll.ui.components.dialogs

import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.interfaces.NotificationHistoryController
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicOneActionDialogFragment

class NewFeatureOverlayInfoDialog : BasicOneActionDialogFragment() {

    companion object {
        private const val TAG = "SentConfigurationContinuationDialogFragment"

        fun createAndShow(fm: FragmentManager) =
            if (fm.findFragmentByTag(TAG) == null) {
                NewFeatureOverlayInfoDialog().apply {
                    isCancelable = false
                    this.viewModel.configure(
                        "bubble_warning_header_android",
                        "bubble_new_functionality_text_android",
                        "bubble_new_functionality_button_android",
                        R.drawable.ic_warning_light,
                        ButtonStyles.BLUE,
                        "dialogHeaderTextPrimary"
                    )
                    show(fm, TAG)
                }.dialogResult
            } else null
    }

    override fun getTypeForHistory(): NotificationHistoryController.Type =
        NotificationHistoryController.Type.GOOD
}
