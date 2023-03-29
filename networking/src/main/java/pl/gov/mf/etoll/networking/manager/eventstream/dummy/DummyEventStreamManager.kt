package pl.gov.mf.etoll.networking.manager.eventstream.dummy

import io.reactivex.Completable
import pl.gov.mf.etoll.networking.api.EventStreamAPI
import pl.gov.mf.etoll.networking.api.model.ApiEventStreamConfiguration
import pl.gov.mf.etoll.networking.api.model.EventStreamObject
import pl.gov.mf.etoll.networking.manager.eventstream.EventStreamManager
import pl.gov.mf.etoll.networking.manager.eventstream.EventStreamManagerStatusDelegate
import javax.inject.Inject

class DummyEventStreamManager @Inject constructor() : EventStreamManager {

    override fun sendDataToStream(data: EventStreamObject): Completable = Completable.complete()
    override fun updateConfigurationAndRebuild(newConfiguration: ApiEventStreamConfiguration) {
    }

    override fun setDelegate(delegate: EventStreamManagerStatusDelegate?) {
    }

    class DummyApi : EventStreamAPI {
        override fun postDataToStream(data: EventStreamObject): Completable = Completable.complete()
    }
}