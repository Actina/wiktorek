package pl.gov.mf.etoll.core.uiinterfaces

import android.content.Context
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.interfaces.BatteryStateInterface
import pl.gov.mf.etoll.interfaces.BatteryStateInterfaceController
import pl.gov.mf.etoll.interfaces.WarningsBasicLevels
import pl.gov.mf.mobile.utils.BatteryUtils
import pl.gov.mf.mobile.utils.CallbacksContainer
import pl.gov.mf.mobile.utils.disposeSafe
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BatteryStateInterfaceControllerImpl @Inject constructor(
    private val context: Context,
    private val rideCoordinatorV3: RideCoordinatorV3
) :
    BatteryStateInterfaceController {

    private var updateDisposable: Disposable? = null
    private val callbacks: CallbacksContainer<BatteryStateInterface> = CallbacksContainer()

    override fun setCallback(batteryInterface: BatteryStateInterface, tag: String) {
        callbacks.set(batteryInterface, tag)
        batteryInterface.onBatteryChanged(
            getBatteryValue(
                BatteryUtils.getCurrentBatteryState(
                    context
                )
            )
        )
    }

    override fun removeCallback(tag: String) {
        callbacks.set(null, tag)
    }

    override fun start() {
        updateDisposable.disposeSafe()
        updateDisposable = Observable.interval(0, 1, TimeUnit.SECONDS).subscribe {
            // update
            val batteryLevel = BatteryUtils.getCurrentBatteryState(context)
            if (rideCoordinatorV3.getConfiguration()!!.duringRide) {
                callbacks.get()
                    .forEach { it.value.onBatteryChanged(getBatteryValue(batteryLevel)) }
            } else {
                callbacks.get()
                    .forEach { it.value.onBatteryChanged(WarningsBasicLevels.UNKNOWN) }
            }
        }
    }

    private fun getBatteryValue(batteryLevel: Int?): WarningsBasicLevels =
        if (batteryLevel == null || batteryLevel <= 0 || batteryLevel > 100) {
            WarningsBasicLevels.UNKNOWN
        } else if (batteryLevel > rideCoordinatorV3.timeTresholdsForNotifications().batteryYellowPercent) {
            WarningsBasicLevels.GREEN
        } else if (batteryLevel > rideCoordinatorV3.timeTresholdsForNotifications().batteryRedPercent) {
            WarningsBasicLevels.YELLOW
        } else
            WarningsBasicLevels.RED


    override fun stop() {
        callbacks.get().let { callbacksSafe ->
            callbacksSafe.forEach { it.value.onBatteryChanged(WarningsBasicLevels.UNKNOWN) }
        }
        updateDisposable.disposeSafe()
        updateDisposable = null
    }
}