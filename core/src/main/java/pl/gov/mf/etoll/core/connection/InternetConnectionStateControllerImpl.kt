package pl.gov.mf.etoll.core.connection

import android.annotation.SuppressLint
import android.util.Log
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import pl.gov.mf.etoll.core.devicecompatibility.DeviceInfoProvider
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

class InternetConnectionStateControllerImpl @Inject constructor(private val deviceInfoProvider: DeviceInfoProvider) :
    InternetConnectionStateController {

    companion object {
        private const val MINIMAL_TIME_BETWEEN_NETWORK_CALLS_AFTER_GOOD = 30 * 1000 // 30s
        private const val MINIMAL_TIME_BETWEEN_NETWORK_CALLS_AFTER_BAD =
            5 * 1000 // anything less than FLOW_CONTROL_MIN_S_BETWEEN_CHECKs will lead to FLOW_CONTROL_MIN_S_BETWEEN_CHECKs
        private const val FLOW_CONTROL_MIN_S_BETWEEN_CHECK = 10
    }

    private val ds: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(true)
    private val checkingConnection = AtomicBoolean(false)
    private val lastSuccessfulCheckTimestamp = AtomicLong(0)
    private val checkCounter = AtomicInteger(0)

    @SuppressLint("CheckResult")
    override fun intervalCheck() {
        if (ds.value == false || checkCounter.incrementAndGet() >= FLOW_CONTROL_MIN_S_BETWEEN_CHECK) {
            Log.d("INTERNET_STATUS", "CHEKING!")
            checkCounter.set(0)
            if (!deviceInfoProvider.isConnectedToNetwork()) {
                ds.onNext(false)
                Log.d("INTERNET_STATUS", "CHECKED -> false (no physical connection)")
            } else {
                if (!checkingConnection.getAndSet(true)) {
                    if (System.currentTimeMillis() - lastSuccessfulCheckTimestamp.get() >= if (ds.value!!) MINIMAL_TIME_BETWEEN_NETWORK_CALLS_AFTER_GOOD else MINIMAL_TIME_BETWEEN_NETWORK_CALLS_AFTER_BAD) {
                        Log.d("INTERNET_STATUS", "CHECKING VIA NET(GOOGLE)")
                        deviceInfoProvider.isInternetConnection().subscribe({ connected ->
                            Log.d("INTERNET_STATUS", "CHECKED -> $connected")
                            if (connected != ds.value)
                                ds.onNext(connected)
                            if (connected)
                                lastSuccessfulCheckTimestamp.set(System.currentTimeMillis())
                            checkingConnection.set(false)
                        }, {
                            Log.d("INTERNET_STATUS", "CHECKED -> error $it")
                            ds.onNext(false)
                            checkingConnection.set(false)
                        })
                    } else {
                        checkingConnection.set(false)
                    }
                }
            }
        }
    }

    override fun observeConnectionState(): Observable<Boolean> = ds
}