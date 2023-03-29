package pl.gov.mf.etoll.core.uiinterfaces

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.joda.time.DateTime
import org.joda.time.Minutes
import pl.gov.mf.etoll.core.ridecoordinatorv2.location.LocationServiceProvider
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.interfaces.GpsStateInterface
import pl.gov.mf.etoll.interfaces.GpsStateInterfaceController
import pl.gov.mf.etoll.interfaces.WarningsBasicLevels
import pl.gov.mf.mobile.utils.CallbacksContainer
import pl.gov.mf.mobile.utils.disposeSafe
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GpsStateInterfaceControllerImpl @Inject constructor(
    private val locationServiceProvider: LocationServiceProvider,
    private val rideCoordinatorV3: RideCoordinatorV3
) : GpsStateInterfaceController {
    private var updateDisposable: Disposable? = null
    private val callbacks: CallbacksContainer<GpsStateInterface> = CallbacksContainer()
    private var warmUpCounter = 0
    private var warmUpLimit = rideCoordinatorV3.timeTresholdsForNotifications().timeForGpsToWarmUp

    override fun setCallback(gpsInterface: GpsStateInterface, tag: String) {
        callbacks.set(gpsInterface, tag)
    }

    override fun removeCallback(tag: String) {
        callbacks.set(null, tag)
    }

    override fun start() {
        warmUpCounter = 0
        updateDisposable.disposeSafe()
        updateDisposable = Observable.interval(0, 1, TimeUnit.SECONDS).subscribe {
            callbacks.get().let { callbacksSafe ->
                callbacksSafe.forEach {
                    it.value.onGpsChanged(
                        getValue()
                    )
                }
            }
        }
    }

    override fun stop() {
        callbacks.get().let { callbacksSafe ->
            callbacksSafe.forEach { it.value.onGpsChanged(WarningsBasicLevels.UNKNOWN) }
        }
        updateDisposable.disposeSafe()
        updateDisposable = null
    }

    private fun getValue(): WarningsBasicLevels {
        val config = rideCoordinatorV3.getConfiguration()
        if (config == null || !config.duringRide || !config.trackByApp) {
            return WarningsBasicLevels.UNKNOWN
        } else {
            val timestamp = locationServiceProvider.getLastLocationReceiveTimestamp()
            if (timestamp <= 0L && warmUpCounter++ < warmUpLimit) {
                return WarningsBasicLevels.UNKNOWN
            } else {
                val lastLocationTime =
                    DateTime(timestamp)
                val currentTime = DateTime.now()
                val minutesDiff =
                    Minutes.minutesBetween(lastLocationTime, currentTime).minutes
                when {
                    minutesDiff < rideCoordinatorV3.timeTresholdsForNotifications().gpsYellowMinutes -> {
                        return WarningsBasicLevels.GREEN
                    }
                    minutesDiff < rideCoordinatorV3.timeTresholdsForNotifications().gpsRedMinutes -> {
                        return WarningsBasicLevels.YELLOW
                    }
                    else ->
                        return WarningsBasicLevels.RED
                }
            }
        }
    }
}
