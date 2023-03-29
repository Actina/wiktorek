package pl.gov.mf.mobile.ui.components.dialogs

import androidx.fragment.app.FragmentManager
import io.reactivex.Single

interface DialogsHelper {

    fun showTranslatedOkDialog(
        fm: FragmentManager,
        titleResource: String,
        textResource: String,
        okButtonText: String
    ): Single<DialogClickedButton>

    fun showUntranslatedOkDialog(
        fm: FragmentManager,
        title: String,
        text: String,
        okButton: String
    ): Single<DialogClickedButton>

    fun showTranslatedYesNoDialog(
        fm: FragmentManager,
        titleResource: String,
        textResource: String,
        yesButtonText: String,
        noButtonText: String
    ): Single<DialogClickedButton>

    fun showUntranslatedYesNoDialog(
        fm: FragmentManager,
        titleResource: String,
        textResource: String,
        yesButtonText: String,
        noButtonText: String
    ): Single<DialogClickedButton>

}

enum class DialogClickedButton {
    DISMISSED,
    OK,
    YES,
    NO
}