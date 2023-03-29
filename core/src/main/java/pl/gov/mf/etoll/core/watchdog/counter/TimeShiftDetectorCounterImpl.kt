package pl.gov.mf.etoll.core.watchdog.counter

import android.util.Log
import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

class TimeShiftDetectorCounterImpl @Inject constructor(
    private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase,
    private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase
) : TimeShiftDetectorCounter {
    companion object {
        private const val MSECS_FOR_DETECTED_SLEEP = 1000 * 60 * 5 // 5 min
        private const val TAG = "WATCHDOG_COUNTER"
    }

    private var callbacks: TimeShiftDetectorCounterCallback? = null
    private val counter = AtomicLong(-1)
    private val locked = AtomicBoolean(true)
    private val writeLock = AtomicBoolean(false)

    override fun isLocked(): Boolean = locked.get()

    override fun unlockCounter() {
        locked.set(false)
        Log.d(TAG, "UNLOCKED")
    }

    override fun lockCounter() {
        locked.set(true)
        Log.d(TAG, "LOCKED")
    }

    override fun setCallback(callback: TimeShiftDetectorCounterCallback?) {
        callbacks = callback
    }

    override fun onNextSecond() {
        if (locked.get()) return
        if (counter.get() < 0) {
            counter.set(readSettingsUseCase.executeForLong(Settings.RIDE_TICKS_COUNTER))
        }
        val sleepDetected = detectSleepAndGetLastTime()
        // write current timer value
        writeSettingsUseCase.execute(Settings.RIDE_TICKS_COUNTER, counter.incrementAndGet())
            .subscribe()

        // write current time as last known
        writeSettingsUseCase.execute(
            Settings.RIDE_TICKS_LAST_TICK_TIME,
            TimeUtils.getCurrentTimestampMillis()
        )
            .subscribe()
        if (sleepDetected > 0L) {
            // app was in sleep mode for some time, react(!)
            callbacks?.onTimeShiftDetected(sleepDetected)
        }
        Log.d(TAG, "COUNTER ${counter.get()}")
    }

    private fun detectSleepAndGetLastTime(): Long {
        // additional lock setup
        if (writeLock.getAndSet(true))
            return 0L
        // get ref time
        val refTime = readSettingsUseCase.executeForLong(Settings.RIDE_TICKS_REF_TIME)
        // get current time
        val currentTime = TimeUtils.getCurrentTimestampMillis()
        // calculate what time it should be right now
        val calculatedCurrentTime =
            refTime + (counter.get() + 1) * 1000
        // get last known timer value
        val lastKnownTime = readSettingsUseCase.executeForLong(Settings.RIDE_TICKS_LAST_TICK_TIME)
        // check last known time for unknown sleep
        // for second check we need to add 1s to have same comparison
        val diff1 = currentTime - lastKnownTime - 1000
        val diff2 =
            maxOf(calculatedCurrentTime, currentTime) - minOf(calculatedCurrentTime, currentTime)
        Log.d(
            TAG, "SLEEP 1DIFF-> ${lastKnownTime > 0} $diff1 2DIFF-> ${refTime >= 0} $diff2"
        )
        // unlock
        writeLock.set(false)

        if ((lastKnownTime > 0 && diff1 >= MSECS_FOR_DETECTED_SLEEP
                    ) || (refTime >= 0 && diff2 >= MSECS_FOR_DETECTED_SLEEP)
        ) {
            return if (lastKnownTime > 0) lastKnownTime else calculatedCurrentTime
        }
        return 0
    }

    override fun resetCounter() {
        counter.set(0)
        val currentTime = TimeUtils.getCurrentTimestampMillis()
        writeSettingsUseCase.execute(
            Settings.RIDE_TICKS_REF_TIME,
            currentTime
        ).subscribe()
        writeSettingsUseCase.execute(
            Settings.RIDE_TICKS_LAST_TICK_TIME,
            currentTime
        ).subscribe()
        writeSettingsUseCase.execute(Settings.RIDE_TICKS_COUNTER, counter.get())
            .subscribe()
        Log.d(TAG, "RESET COUNTER(!)")
    }

    /**
     * Get timestamp from last event for sending purposes
     * WARNING! If passed time is less than 5s (last event to now) then we would return 0
     */
    override fun getTimestampForLastEvent(): Long {
        if (counter.get() < 0) {
            counter.set(readSettingsUseCase.executeForLong(Settings.RIDE_TICKS_COUNTER))
        }
        val returnValue = detectSleepAndGetLastTime()
        Log.d(
            TAG,
            "CALCULATED_VALUE: $returnValue counter=${counter.get()}}"
        )
        return returnValue
    }
}