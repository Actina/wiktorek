package pl.gov.mf.etoll.ui.components.compose.combined.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.ui.components.compose.combined.dialogs.EbiletOneButtonDialog.DialogType.Companion.toDialogType
import pl.gov.mf.etoll.ui.components.compose.helpers.dialogNotExists
import pl.gov.mf.etoll.ui.components.compose.helpers.getExistingDialog
import pl.gov.mf.etoll.ui.components.compose.theme.DialogAcceptTile
import pl.gov.mf.etoll.ui.components.compose.theme.EtollTheme
import pl.gov.mf.etoll.ui.components.compose.theme.positiveGreen
import pl.gov.mf.etoll.ui.components.compose.theme.warningOrange
import pl.gov.mf.mobile.utils.getAppMode

class EbiletOneButtonDialog : DialogFragment() {

    private val _dialogResult = MutableLiveData<DialogResult>()
    val dialogResult: LiveData<DialogResult>
        get() = _dialogResult

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val iconRLight = requireArguments().getInt(DIALOG_ICON_LIGHT, -1)
        val iconLight = if (iconRLight == -1) null else iconRLight
        val iconRDark = requireArguments().getInt(DIALOG_ICON_DARK, -1)
        val iconDark = if (iconRDark == -1) null else iconRDark
        val title = requireArguments().getString(DIALOG_TITLE)
        val description = requireArguments().getString(DIALOG_DESCRIPTION)
        val buttonText = requireArguments().getString(DIALOG_BUTTON_TEXT)
        val dismissible = requireArguments().getBoolean(DIALOG_DISMISSIBLE)
        val type = requireArguments().getInt(DIALOG_TYPE).toDialogType()
        return ComposeView(requireContext()).apply {
            setContent {
                EtollTheme(getAppMode = { getAppMode(requireContext()) }) {
                    val titleColor = when (type) {
                        DialogType.BLUE_DEFAULT -> DialogAcceptTile
                        DialogType.WARNING -> warningOrange
                        DialogType.SUCCESS -> positiveGreen
                        DialogType.ERROR -> EtollTheme.colors.textColorRed
                    }
                    EbiletOneButtonComposeDialog(
                        iconLight = iconLight,
                        iconDark = iconDark,
                        titleText = title!!,
                        descriptionText = description!!,
                        buttonText = buttonText!!,
                        dismissable = dismissible,
                        titleColor = titleColor,
                        onDismiss = {
                            dismiss()
                            _dialogResult.value = DialogResult.DISMISSED
                        },
                        click = {
                            dismiss()
                            _dialogResult.value = DialogResult.CONFIRMED
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val TAG = "EbiletOneButtonDialog"
        private const val DIALOG_TITLE = "dialog_title"
        private const val DIALOG_DESCRIPTION = "dialog_description"
        private const val DIALOG_ICON_LIGHT = "dialog_icon_light"
        private const val DIALOG_ICON_DARK = "dialog_icon_dark"
        private const val DIALOG_BUTTON_TEXT = "dialog_button_text"
        private const val DIALOG_DISMISSIBLE = "dialog_dismissible"
        private const val DIALOG_TYPE = "dialog_type"

        fun createAndShow(
            fragmentManager: FragmentManager,
            title: String,
            description: String,
            buttonText: String,
            dialogType: DialogType,
            iconLightRes: Int?,
            iconDarkRes: Int?,
            dismissible: Boolean = true,
            uniqueTag: String = TAG,
        ): LiveData<DialogResult>? = fragmentManager.run {
            (
                    if (dialogNotExists(uniqueTag))
                        EbiletOneButtonDialog().apply {
                            arguments = Bundle().apply {
                                putString(DIALOG_TITLE, title)
                                putString(DIALOG_DESCRIPTION, description)
                                putString(DIALOG_BUTTON_TEXT, buttonText)
                                putInt(DIALOG_ICON_LIGHT, iconLightRes ?: -1)
                                putInt(DIALOG_ICON_DARK, iconDarkRes ?: -1)
                                putBoolean(DIALOG_DISMISSIBLE, dismissible)
                                putInt(DIALOG_TYPE, dialogType.toInt())
                            }
                            show(this@run, uniqueTag)
                        }
                    else
                        getExistingDialog(uniqueTag)
                    )
                ?.dialogResult
        }

        fun dismiss(
            fragmentManager: FragmentManager,
            uniqueTag: String = TAG,
        ) {
            fragmentManager.getExistingDialog<EbiletOneButtonDialog>(uniqueTag)?.dismiss()
        }

    }

    enum class DialogResult {
        @Keep
        CONFIRMED,

        @Keep
        DISMISSED
    }

    enum class DialogType {

        @Keep
        BLUE_DEFAULT,

        @Keep
        WARNING,

        @Keep
        SUCCESS,

        @Keep
        ERROR;

        fun toInt(): Int {
            for (i in 0..values().size)
                if (values()[i] == this) return i
            return 0
        }

        companion object {
            fun Int.toDialogType(): DialogType {
                if (this < values().size) return values()[this]
                return BLUE_DEFAULT
            }
        }
    }
}