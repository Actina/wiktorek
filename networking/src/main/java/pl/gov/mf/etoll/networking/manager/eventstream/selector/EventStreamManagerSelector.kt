package pl.gov.mf.etoll.networking.manager.eventstream.selector

import io.reactivex.Completable
import pl.gov.mf.etoll.networking.api.model.ApiEventStreamConfiguration
import pl.gov.mf.etoll.networking.api.model.EventStreamObject
import pl.gov.mf.etoll.networking.manager.eventstream.EventStreamManager
import pl.gov.mf.etoll.networking.manager.eventstream.EventStreamManagerStatusDelegate
import pl.gov.mf.etoll.networking.manager.netswitch.NetworkSwitchConditionsCheck
import pl.gov.mf.mobile.networking.api.interceptors.ShouldNotHaveAccessToServerException
import javax.inject.Inject

class EventStreamManagerSelector @Inject constructor(
    private val selector: NetworkSwitchConditionsCheck,
    private val realImplementation: EventStreamManager,
    private val dummyImpl: EventStreamManager
) : EventStreamManager {

    override fun updateConfigurationAndRebuild(newConfiguration: ApiEventStreamConfiguration) {
        getManager().updateConfigurationAndRebuild(newConfiguration)
    }

    override fun sendDataToStream(data: EventStreamObject): Completable =
        getManager().sendDataToStream(data).doOnError {
            // this should never happen
            if (it is ShouldNotHaveAccessToServerException) {
                disableNetworkForever()
            }
        }

    private fun getManager(): EventStreamManager =
        if (selector.shouldSelectRealManager())
            realImplementation
        else {
            setDelegate(null)
            dummyImpl
        }


    private fun disableNetworkForever() {
        realImplementation.setDelegate(null)
        selector.lockToDummy()
    }

    override fun setDelegate(delegate: EventStreamManagerStatusDelegate?) {
        realImplementation.setDelegate(delegate)
    }
}