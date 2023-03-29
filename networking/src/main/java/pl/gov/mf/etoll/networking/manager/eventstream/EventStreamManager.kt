package pl.gov.mf.etoll.networking.manager.eventstream

import io.reactivex.Completable
import pl.gov.mf.etoll.networking.api.model.ApiEventStreamConfiguration
import pl.gov.mf.etoll.networking.api.model.EventStreamObject

interface EventStreamManager {
    fun sendDataToStream(data: EventStreamObject): Completable
    fun updateConfigurationAndRebuild(newConfiguration: ApiEventStreamConfiguration)
    fun setDelegate(delegate: EventStreamManagerStatusDelegate?)
}

interface EventStreamManagerStatusDelegate {
    fun onUpdateStatusRequested(): Completable
}