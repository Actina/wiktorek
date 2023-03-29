package pl.gov.mf.etoll.core.watchdog.counter

interface TimeShiftDetectorCounter {
    fun onNextSecond()
    fun resetCounter()
    fun getTimestampForLastEvent(): Long
    fun setCallback(callback: TimeShiftDetectorCounterCallback?)
    fun lockCounter()
    fun unlockCounter()
    fun isLocked(): Boolean
}

interface TimeShiftDetectorCounterCallback {
    fun onTimeShiftDetected(lastKnownTime: Long)
}