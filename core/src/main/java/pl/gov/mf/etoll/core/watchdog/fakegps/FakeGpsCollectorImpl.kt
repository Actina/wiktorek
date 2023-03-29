package pl.gov.mf.etoll.core.watchdog.fakegps

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class FakeGpsCollectorImpl @Inject constructor() : FakeGpsCollector {

    private var fakeGpsDialogShownAfterDetection = AtomicBoolean(true)
    private val detectionSource = BehaviorSubject.createDefault(false)

    override fun changeLastKnownLocationState(fake: Boolean) {
//        if (detectionSource.value!! != fake) {
        detectionSource.onNext(fake)
        if (fake)
            fakeGpsDialogShownAfterDetection.set(false)
//        }
    }

    override fun observeChanges(): Observable<Boolean> = detectionSource

    override fun shouldFakeGpsDialogBeShown(): Boolean = !fakeGpsDialogShownAfterDetection.get()

    override fun setDialogWasShown(showed: Boolean) {
        fakeGpsDialogShownAfterDetection.set(true)
    }

}