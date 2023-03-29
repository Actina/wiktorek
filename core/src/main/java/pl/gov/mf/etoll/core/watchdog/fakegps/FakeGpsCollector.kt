package pl.gov.mf.etoll.core.watchdog.fakegps

import io.reactivex.Observable

interface FakeGpsCollector {
    fun changeLastKnownLocationState(fake: Boolean)
    fun setDialogWasShown(showed: Boolean)
    fun shouldFakeGpsDialogBeShown(): Boolean
    fun observeChanges(): Observable<Boolean>
}