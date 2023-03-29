package pl.gov.mf.etoll.core.watchdog.wakelock

import android.content.Context
import android.os.PowerManager
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class WakelockControllerImpl @Inject constructor(private val context: Context) :
    WakelockController {
    var wakeLock: PowerManager.WakeLock? = null
    val wakeLockAquired = AtomicBoolean(false)

    override fun applyLock() {
        if (wakeLockAquired.getAndSet(true)) return
        Log.d("WAKELOCK_CONTROLLER", "AQUIRED")
        wakeLock =
            (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "eToll::RideWakeLock").apply {
                    acquire()
                }
            }
    }

    override fun releaseLock() {
        Log.d("WAKELOCK_CONTROLLER", "RELEASED")
        wakeLock?.release()
        wakeLock = null
        wakeLockAquired.set(false)
    }
}