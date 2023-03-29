package pl.gov.mf.mobile.ui.components.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import io.reactivex.Single
import pl.gov.mf.mobile.utils.translate
import javax.inject.Inject

class DialogsHelperImpl @Inject constructor(private val context: Context) : DialogsHelper {

    override fun showTranslatedOkDialog(
        fm: FragmentManager,
        titleResource: String,
        textResource: String,
        okButtonText: String
    ): Single<DialogClickedButton> = Single.create { emitter ->
        DummyTestDialogFragment.createOkDialog(
            textResource.translate(context),
            okButtonText.translate(context),
            object : OnDialogOptionClicked {
                override fun onOptionSelected(option: DialogClickedButton) {
                    emitter.onSuccess(option)
                }
            })
            .show(fm, "OK")
    }

    override fun showUntranslatedOkDialog(
        fm: FragmentManager,
        title: String,
        text: String,
        okButton: String
    ): Single<DialogClickedButton> = Single.create { emitter ->
        DummyTestDialogFragment.createOkDialog(
            text,
            okButton,
            object : OnDialogOptionClicked {
                override fun onOptionSelected(option: DialogClickedButton) {
                    emitter.onSuccess(option)
                }
            })
            .show(fm, "OK")
    }

    override fun showTranslatedYesNoDialog(
        fm: FragmentManager,
        titleResource: String,
        textResource: String,
        yesButtonText: String,
        noButtonText: String
    ): Single<DialogClickedButton> = Single.create { emitter ->
        DummyTestDialogFragment.createYesNoDialog(
            textResource.translate(context),
            yesButtonText.translate(context),
            noButtonText.translate(context),
            object : OnDialogOptionClicked {
                override fun onOptionSelected(option: DialogClickedButton) {
                    emitter.onSuccess(option)
                }
            })
            .show(fm, "YESNO")
    }

    override fun showUntranslatedYesNoDialog(
        fm: FragmentManager,
        titleResource: String,
        textResource: String,
        yesButtonText: String,
        noButtonText: String
    ): Single<DialogClickedButton> = Single.create { emitter ->
        DummyTestDialogFragment.createYesNoDialog(
            textResource,
            yesButtonText,
            noButtonText,
            object : OnDialogOptionClicked {
                override fun onOptionSelected(option: DialogClickedButton) {
                    emitter.onSuccess(option)
                }
            })
            .show(fm, "YESNO")
    }
}

internal class DummyTestDialogFragment : DialogFragment() {

    companion object {
        fun createOkDialog(
            text: String,
            ok: String,
            clickListener: OnDialogOptionClicked
        ): DummyTestDialogFragment {
            val output = DummyTestDialogFragment()
            val bundle = Bundle()
            bundle.putString(ARG_TEXT, text)
            bundle.putString(ARG_OK, ok)
            output.arguments = bundle
            output.clickListener = clickListener
            return output
        }

        fun createYesNoDialog(
            text: String,
            yes: String,
            no: String,
            clickListener: OnDialogOptionClicked
        ): DummyTestDialogFragment {
            val output = DummyTestDialogFragment()
            val bundle = Bundle()
            bundle.putString(ARG_TEXT, text)
            bundle.putString(ARG_OK, yes)
            bundle.putString(ARG_CANCEL, no)
            output.arguments = bundle
            output.clickListener = clickListener
            return output
        }

        private const val ARG_TEXT = "ARG_TITLE"
        private const val ARG_OK = "ARG_OK"
        private const val ARG_CANCEL = "ARG_CANCEL"
    }

    private var clickListener: OnDialogOptionClicked? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context!!)
            .setMessage(arguments!!.getString(ARG_TEXT))
        if (arguments!!.containsKey(ARG_CANCEL)) {
            builder.setNegativeButton(
                arguments!!.getString(ARG_CANCEL)
            ) { p0, p1 -> clickListener?.onOptionSelected(DialogClickedButton.NO) }
                .setPositiveButton(
                    arguments!!.getString(ARG_OK)
                ) { p0, p1 -> clickListener?.onOptionSelected(DialogClickedButton.YES) }
        } else {
            builder.setPositiveButton(
                arguments!!.getString(ARG_OK)
            ) { p0, p1 -> clickListener?.onOptionSelected(DialogClickedButton.OK) }
        }
        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        clickListener?.onOptionSelected(DialogClickedButton.DISMISSED)
        super.onDismiss(dialog)
    }
}

internal interface OnDialogOptionClicked {
    fun onOptionSelected(option: DialogClickedButton)
}