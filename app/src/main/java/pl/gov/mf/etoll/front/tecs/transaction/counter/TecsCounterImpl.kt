package pl.gov.mf.etoll.front.tecs.transaction.counter

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import pl.gov.mf.etoll.front.tecs.transaction.TransactionConfiguration.TECS_TIMER_LIMIT_IN_SECS
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TecsCounterImpl @Inject constructor() : TecsCounter {

    private var lastKnownId = -1L
    private var entryTime: Long = 0L

    override fun initializeTransaction(id: Long) {
        // if it's same as last, do nothing
        if (lastKnownId == id) return
        lastKnownId = id
        // otherwise, reset known time & counter
        entryTime = System.currentTimeMillis()
    }

    override fun observeStatus(): Observable<Long> =
        Observable.interval(0, 1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                var leftSeconds =
                    TECS_TIMER_LIMIT_IN_SECS - (System.currentTimeMillis() - entryTime) / 1000
                // normalize for case of time manipulation by user
                if (leftSeconds > TECS_TIMER_LIMIT_IN_SECS || leftSeconds < 0) {
                    leftSeconds = 0
                }
                leftSeconds
            }
}