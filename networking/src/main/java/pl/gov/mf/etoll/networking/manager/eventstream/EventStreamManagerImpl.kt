package pl.gov.mf.etoll.networking.manager.eventstream

import android.util.Log
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import pl.gov.mf.etoll.logging.LogUseCase
import pl.gov.mf.etoll.networking.api.EventStreamAPI
import pl.gov.mf.etoll.networking.api.model.ApiEventStreamConfiguration
import pl.gov.mf.etoll.networking.api.model.EventStreamObject
import pl.gov.mf.etoll.networking.manager.eventstream.apibuilder.EventStreamApiBuilder
import pl.gov.mf.mobile.networking.api.interceptors.UnauthorizedNetworkException
import pl.gov.mf.mobile.networking.api.interceptors.WrongSystemTimeException
import javax.inject.Inject
import javax.net.ssl.SSLHandshakeException

class EventStreamManagerImpl @Inject constructor(
    private val eventStreamApiBuilder: EventStreamApiBuilder,
    private val logUseCase: LogUseCase
) : EventStreamManager {

    private var statusUpdateDelegate: EventStreamManagerStatusDelegate? = null
    private var eventStreamConfiguration: ApiEventStreamConfiguration? =
        null
    private var eventStreamApi: EventStreamAPI? = null

    override fun updateConfigurationAndRebuild(newConfiguration: ApiEventStreamConfiguration) {
        if (eventStreamApi != null
            && eventStreamConfiguration != null
            && newConfiguration.authorizationHeader.contentEquals(eventStreamConfiguration!!.authorizationHeader)
            && newConfiguration.address.contentEquals(eventStreamConfiguration!!.address)
        ) {
            Log.d("EventStreamManager", "Requested rebuild, but nothing changed")
            logUseCase.log(
                LogUseCase.NETWORK,
                "Event stream has not changed, nothing to do"
            )
        } else {
            Log.d("EventStreamManager", "Requested rebuild")
            // we got new object to use
            try {
                generateNewApiFrom(newConfiguration)
                Log.d("EventStreamManager", "Rebuild completed")
            } catch (e: Exception) {
                Log.d("EventStreamManager", "Rebuild error ${e.localizedMessage}")
                logUseCase.log(
                    LogUseCase.NETWORK,
                    "Error creating event stream client ${e.localizedMessage}"
                )
            }
            logUseCase.log(LogUseCase.NETWORK, "Created event stream client!")
        }
    }

    override fun setDelegate(delegate: EventStreamManagerStatusDelegate?) {
        statusUpdateDelegate = delegate
    }

    private fun getEventStreamApi() = Single.create<EventStreamAPI> { emitter ->
        if (eventStreamApi == null) {
            // update status
            statusUpdateDelegate?.onUpdateStatusRequested()?.let { loc ->
                loc.subscribe({
                    Log.d(
                        "EventStreamManager",
                        "Status updated correctly"
                    )
                    emitter.onError(Exception("Will retry soon"))
                }, { error ->
                    // status error
                    Log.d(
                        "EventStreamManager",
                        "Updating status error"
                    )
                    if (!emitter.isDisposed)
                        emitter.onError(error)
                })
            }
        } else {
            eventStreamApi?.let { emitter.onSuccess(it) }
        }
    }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    private fun generateNewApiFrom(configuration: ApiEventStreamConfiguration) {
        eventStreamConfiguration = configuration
        eventStreamApi = eventStreamApiBuilder.buildApiFrom(configuration)
    }

    override fun sendDataToStream(data: EventStreamObject): Completable =
        Completable.create { emitter ->
            val localCompositeDisposable = CompositeDisposable()
            localCompositeDisposable.add(
                getEventStreamApi().subscribeOn(Schedulers.io())
                    .subscribe({ api ->
                        localCompositeDisposable.add(
                            api.postDataToStream(data)
                                .subscribeOn(Schedulers.io())
                                .subscribe({
                                    Log.d("EventStreamManager", "Sending event completed")
                                    logUseCase.log(
                                        LogUseCase.NETWORK,
                                        "sendEventsToStream() correct!"
                                    )
                                    emitter.onComplete()
                                }, {
                                    Log.d(
                                        "EventStreamManager",
                                        "Sending events error ${it.localizedMessage}"
                                    )
                                    if (it is UnauthorizedNetworkException) {
                                        // reset event stream api
                                        eventStreamApi = null
                                        // force network manager to update status
                                        statusUpdateDelegate?.onUpdateStatusRequested()
                                            ?.let { loc ->
                                                localCompositeDisposable.add(loc.subscribe({
                                                    Log.d(
                                                        "EventStreamManager",
                                                        "Status updated correctly"
                                                    )
                                                    emitter.onError(it)
                                                }, { error ->
                                                    // status error
                                                    Log.d(
                                                        "EventStreamManager",
                                                        "Updating status error"
                                                    )
                                                    if (!emitter.isDisposed)
                                                        emitter.onError(error)
                                                }))
                                            }
                                    } else {
                                        logUseCase.log(
                                            LogUseCase.NETWORK,
                                            "sendEventsToStream() error! ${it.localizedMessage}"
                                        )
                                        emitter.onError(it)
                                    }
                                })
                        )
                    }, {
                        Log.d(
                            "EventStreamManager",
                            "Sending events error#2 ${it.localizedMessage} -> $it"
                        )
                        if (it is UnauthorizedNetworkException || it is SSLHandshakeException || it is WrongSystemTimeException) {
                            // reset event stream api
                            eventStreamApi = null
                            // force network manager to update status
                            statusUpdateDelegate?.onUpdateStatusRequested()?.let { loc ->
                                localCompositeDisposable.add(loc.subscribe({
                                    Log.d(
                                        "EventStreamManager",
                                        "Status updated correctly"
                                    )
                                    emitter.onError(it)
                                }, { error ->
                                    // status error
                                    Log.d(
                                        "EventStreamManager",
                                        "Updating status error"
                                    )
                                    if (!emitter.isDisposed)
                                        emitter.onError(error)
                                }))
                            }
                        } else {
                            logUseCase.log(
                                LogUseCase.NETWORK,
                                "sendEventsToStream() error! ${it.localizedMessage}"
                            )
                            emitter.onError(it)
                        }
                    })
            )
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

}