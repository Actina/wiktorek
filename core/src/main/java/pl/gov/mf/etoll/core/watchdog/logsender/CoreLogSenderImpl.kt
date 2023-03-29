package pl.gov.mf.etoll.core.watchdog.logsender

import android.util.Log
import pl.gov.mf.etoll.networking.manager.NetworkingManager
import pl.gov.mf.etoll.storage.database.logging.LoggingDatabase
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class CoreLogSenderImpl @Inject constructor(
    private val networkingManager: NetworkingManager,
    private val logDb: LoggingDatabase
) : CoreLogSender {

    companion object {
        private const val SECONDS_BETWEEN_SENDING_TRY = 30
    }

    private val counter = AtomicInteger(0)
    private val lock = AtomicBoolean(false)

    override fun intervalCheck() {
        if (lock.getAndSet(true)) return
        if (counter.incrementAndGet() >= SECONDS_BETWEEN_SENDING_TRY) {
            Log.d("LOGGER_CACHE", "TRYING TO SEND EVENTS!")
            counter.set(0)
            val tmp = logDb.getNextChunk().subscribe({ items ->
                Log.d("LOGGER_CACHE", "WE HAVE ${items.size} items!")
                if (items.isNullOrEmpty()) {
                    lock.set(false)
                    return@subscribe
                } else {
                    networkingManager.log(items).subscribe({
                        logDb.removeOldest(items.size).subscribe({
                            Log.d("LOGGER_CACHE", "ALL SENT AND REMOVED CORRECTLY!")
                            lock.set(false)
                        }, {
                            lock.set(false)
                        })
                    }, {
                        // do nothing, just remove lock
                        lock.set(false)
                    })
                }
            }, {
                lock.set(false)
            })
        } else
            lock.set(false)
    }
}