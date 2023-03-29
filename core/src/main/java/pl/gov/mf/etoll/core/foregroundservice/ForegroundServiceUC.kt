package pl.gov.mf.etoll.core.foregroundservice

import android.content.Context
import android.content.Intent
import pl.gov.mf.etoll.core.beacon.BeaconService
import pl.gov.mf.etoll.core.overlay.controller.OverlayController
import javax.inject.Inject


sealed class ForegroundServiceUC {
    class StartForegroundServiceUseCase(
        private val overlayController: OverlayController
    ) : ForegroundServiceUC() {

        fun execute(viewContext: Context, overlayServiceIntent: Intent) {
            BeaconService.startService(viewContext)
            if (overlayController.overlayIsEnabled(viewContext)) {
                viewContext.startService(overlayServiceIntent)
            }
        }

    }

    class StopForegroundServiceUseCase @Inject constructor(
        private val overlayController: OverlayController
    ) : ForegroundServiceUC() {
        fun execute(viewContext: Context, overlayServiceIntent: Intent) {
            BeaconService.stopService(viewContext)
            if (overlayController.overlayIsEnabled(viewContext)) {
                viewContext.stopService(overlayServiceIntent)
            }
        }
    }
}