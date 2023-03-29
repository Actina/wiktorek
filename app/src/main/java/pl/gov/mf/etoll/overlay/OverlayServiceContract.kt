package pl.gov.mf.etoll.overlay

import android.content.Context
import pl.gov.mf.etoll.interfaces.WarningsBasicLevels

interface OverlayServiceContract {
    interface View {
        fun initialize(context: Context)
        fun deinitialize()
        fun openOverlay()
        fun setGPSState(newLevel: WarningsBasicLevels)
        fun setDataTransferState(newLevel: WarningsBasicLevels)
        fun setBatteryState(newLevel: WarningsBasicLevels)
    }

    interface Presenter {
        fun takeView(view: View, context: Context)
        fun dropView()
    }
}