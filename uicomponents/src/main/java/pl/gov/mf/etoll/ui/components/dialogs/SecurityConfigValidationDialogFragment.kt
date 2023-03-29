package pl.gov.mf.etoll.ui.components.dialogs

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import pl.gov.mf.etoll.interfaces.WarningsBasicLevels
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicOneActionDialogFragment

class SecurityConfigValidationDialogFragment : BasicOneActionDialogFragment() {


    companion object {
        private const val TAG = "SecurityConfigValidationDialogFragment"
        const val HEADER_TEXT_RES = "header"
        const val CONTENT_TEXT_RES = "content"
        const val BUTTON_TEXT_RES = "buttonText"
        const val ICON_TYPE = "iconType"

        fun createAndShow(fm: FragmentManager, bundle: Bundle) =
            if (fm.findFragmentByTag(TAG) == null)
                SecurityConfigValidationDialogFragment().apply {
                    isCancelable = true
                    this.viewModel.configure(
                        startHeaderText = bundle.getString(HEADER_TEXT_RES) ?: "",
                        startContentText = bundle.getString(CONTENT_TEXT_RES) ?: "",
                        startButtonText = bundle.getString(BUTTON_TEXT_RES) ?: "",
                        startIconRes =
                        when (WarningsBasicLevels.fromString(bundle.getString(
                            ICON_TYPE, ""))) {
                            WarningsBasicLevels.YELLOW -> R.drawable.ic_warning_light
                            else -> R.drawable.ic_wrongpin_light
                        },
                        startButtonStyle = ButtonStyles.BLUE,
                        startHeaderColor = "dialogHeaderTextPrimary"
                    )
                    show(fm, TAG)
                }.dialogResult else null

    }
}