package pl.gov.mf.etoll.demo.watchdog

import io.reactivex.Observable
import pl.gov.mf.etoll.front.watchdog.DemoListItem
import pl.gov.mf.etoll.front.watchdog.DemoWatchdog
import javax.inject.Inject

class DemoWatchdogImpl @Inject constructor() : DemoWatchdog {
    override fun start() {
        // do nothing
    }

    override fun observeChanges(): Observable<List<DemoListItem>> = Observable.just(emptyList())
}