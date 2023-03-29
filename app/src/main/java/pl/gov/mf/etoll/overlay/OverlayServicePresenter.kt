package pl.gov.mf.etoll.overlay

import android.content.Context
import pl.gov.mf.etoll.interfaces.*
import javax.inject.Inject

class OverlayServicePresenter @Inject constructor(
    private val gpsStateInterfaceController: GpsStateInterfaceController,
    private val dataSenderStateInterfaceController: DataSenderStateInterfaceController,
    private val batteryStateInterfaceController: BatteryStateInterfaceController
) : OverlayServiceContract.Presenter, GpsStateInterface,
    DataSenderStateInterface,
    BatteryStateInterface {

    private var view: OverlayServiceContract.View? = null
    private var lastKnownGpsState: WarningsBasicLevels = WarningsBasicLevels.UNKNOWN
    private var lastKnownDataConnectionState: WarningsBasicLevels = WarningsBasicLevels.UNKNOWN

    override fun takeView(view: OverlayServiceContract.View, context: Context) {
        this.view = view
        view.initialize(context)
        setupDataCallbacks()
    }

    override fun dropView() {
        removeDataCallbacks()
        view?.deinitialize()
        view = null
    }

    override fun onBatteryChanged(newLevel: WarningsBasicLevels) {
        view?.setBatteryState(newLevel)
    }

    override fun onDataSenderStateChanged(newLevel: WarningsBasicLevels) {
        view?.setDataTransferState(newLevel)
        if (newLevel != lastKnownDataConnectionState) {
            lastKnownDataConnectionState = newLevel
            if (lastKnownDataConnectionState == WarningsBasicLevels.RED || lastKnownDataConnectionState == WarningsBasicLevels.YELLOW) {
                view?.openOverlay()
            }
        }
    }

    override fun onGpsChanged(newLevel: WarningsBasicLevels) {
        view?.setGPSState(newLevel)
        if (newLevel != lastKnownGpsState) {
            lastKnownGpsState = newLevel
            if (lastKnownGpsState == WarningsBasicLevels.RED || lastKnownGpsState == WarningsBasicLevels.YELLOW) {
                view?.openOverlay()
            }
        }
    }


    private fun setupDataCallbacks() {
        batteryStateInterfaceController.setCallback(this, TAG)
        dataSenderStateInterfaceController.setCallback(this, TAG)
        gpsStateInterfaceController.setCallback(this, TAG)
    }

    private fun removeDataCallbacks() {
        batteryStateInterfaceController.removeCallback(TAG)
        dataSenderStateInterfaceController.removeCallback(TAG)
        gpsStateInterfaceController.removeCallback(TAG)
    }

    companion object {
        private const val TAG = "BUBBLE"
    }
}