package pl.gov.mf.etoll.ui.components.dialogs

import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.interfaces.NotificationHistoryController
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicOneActionDialogFragment

class RideCantBeContinuedDialogFragment : BasicOneActionDialogFragment() {

    companion object {
        const val TAG = "RideCantBeContinuedDialogFragment"

        fun createAndShow(fm: FragmentManager) = if (fm.findFragmentByTag(TAG) == null)
            RideCantBeContinuedDialogFragment().apply {
                isCancelable = false
                this.viewModel.configure(
                    startHeaderText = "ride_cant_be_continued_header",
                    startContentText = "ride_cant_be_continued_content",
                    startButtonText = "dialog_ok",
                    startIconRes = R.drawable.ic_confirm_finish_ride_light,
                    startButtonStyle = ButtonStyles.BLUE,
                    startHeaderColor = "dialogHeaderTextPrimary"
                )
                show(fm, TAG)
            }.dialogResult else null
    }

    override fun getTypeForHistory(): NotificationHistoryController.Type =
        NotificationHistoryController.Type.BAD
}

