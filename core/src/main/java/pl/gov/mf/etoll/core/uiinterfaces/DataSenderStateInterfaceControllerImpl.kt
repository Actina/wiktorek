package pl.gov.mf.etoll.core.uiinterfaces

import android.annotation.SuppressLint
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.joda.time.DateTime
import org.joda.time.Minutes
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.interfaces.DataSenderStateInterface
import pl.gov.mf.etoll.interfaces.DataSenderStateInterfaceController
import pl.gov.mf.etoll.interfaces.WarningsBasicLevels
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.utils.CallbacksContainer
import pl.gov.mf.mobile.utils.disposeSafe
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DataSenderStateInterfaceControllerImpl @Inject constructor(
    private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase,
    private val rideCoordinatorV3: RideCoordinatorV3
) :
    DataSenderStateInterfaceController {

    private var updateDisposable: Disposable? = null
    private val callbacks: CallbacksContainer<DataSenderStateInterface> = CallbacksContainer()

    override fun setCallback(dataSenderInterface: DataSenderStateInterface, tag: String) {
        callbacks.set(dataSenderInterface, tag)
        dataSenderInterface.onDataSenderStateChanged(getValue())
    }

    override fun removeCallback(tag: String) {
        callbacks.set(null, tag)
    }

    @SuppressLint("CheckResult")
    override fun start() {
        updateDisposable.disposeSafe()
        updateDisposable = Observable.interval(0, 1, TimeUnit.SECONDS).subscribe {
            callbacks.get()
                .forEach { it.value.onDataSenderStateChanged(getValue()) }
        }
    }

    override fun stop() {
        callbacks.get().let { callbacksSafe ->
            callbacksSafe.forEach { it.value.onDataSenderStateChanged(WarningsBasicLevels.UNKNOWN) }
        }
        updateDisposable.disposeSafe()
        updateDisposable = null
    }

    private fun getValue(): WarningsBasicLevels {
        val config = rideCoordinatorV3.getConfiguration()
        if (config == null || !config.duringRide || !config.trackByApp) {
            return WarningsBasicLevels.UNKNOWN
        } else {
            val timestamp: Long =
                readSettingsUseCase.executeForString(Settings.LAST_CORRECT_DATA_SENDING_TIMESTAMP)
                    .toLong()
            if (timestamp <= 0L) {
                return WarningsBasicLevels.UNKNOWN
            } else {
                val lastLocationTime =
                    DateTime(timestamp)
                val currentTime = DateTime.now()
                val minutesDiff =
                    Minutes.minutesBetween(lastLocationTime, currentTime).minutes
                return when {
                    minutesDiff < rideCoordinatorV3.timeTresholdsForNotifications().dataConnectionYellowMinutes -> {
                        WarningsBasicLevels.GREEN
                    }
                    minutesDiff < rideCoordinatorV3.timeTresholdsForNotifications().dataConnectionRedMinutes -> {
                        WarningsBasicLevels.YELLOW
                    }
                    else ->
                        WarningsBasicLevels.RED
                }
            }
        }
    }

}