package pl.gov.mf.etoll.ui.components.compose.combined.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import pl.gov.mf.mobile.ui.components.utils.BlurDelegate

abstract class BlurableComposeDialogFragment : BaseComposeDialogFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { act ->
            if (act is BlurDelegate)
                act.showBlur()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.let { act ->
            if (act is BlurDelegate)
                act.hideBlur()
        }
    }
}