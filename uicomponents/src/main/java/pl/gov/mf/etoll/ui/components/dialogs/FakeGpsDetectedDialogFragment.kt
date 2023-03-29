package pl.gov.mf.etoll.ui.components.dialogs

import androidx.fragment.app.FragmentManager
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicOneActionDialogFragment

class FakeGpsDetectedDialogFragment : BasicOneActionDialogFragment() {

    companion object {
        const val TAG = "FakeGpsDetectedDialogFragment"

        fun createAndShow(fm: FragmentManager) =
        // FYI: detected during ride is prepared to be used if we would like to inform user before
            // starting ride that device has installed location mocking apps
            if (fm.findFragmentByTag(TAG) == null)
                FakeGpsDetectedDialogFragment().apply {
                    isCancelable = true
                    this.viewModel.configure(
                        startHeaderText = "dialog_fakegps_title_android",
                        startContentText = "dialog_fakegps_content_android",
                        startButtonText = "dialog_ok",
                        startButtonStyle = ButtonStyles.BLUE,
                        startHeaderColor = "dialogHeaderTextPrimary",
                        iconVisible = true
                    )
                    show(fm, TAG)
                }.dialogResult else null
    }
}