package pl.gov.mf.mobile.ui.components.dialogs

import android.content.Context
import android.os.Bundle
import android.view.View
import pl.gov.mf.etoll.app.BaseApplication
import pl.gov.mf.etoll.interfaces.SoundNotificationController

abstract class BlurableWithSoundDatabindingDialogFragment : BlurableDatabindingDialogFragment() {

    lateinit var soundNotificationController: SoundNotificationController

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // fragments mostly do not use DI, so we need to "inject" manually
        soundNotificationController =
            (context.applicationContext as BaseApplication).getApplicationComponent()
                .soundController()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        soundNotificationController.onEventTriggered()
    }
}