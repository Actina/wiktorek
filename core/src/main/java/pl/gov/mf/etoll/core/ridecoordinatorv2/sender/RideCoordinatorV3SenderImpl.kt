package pl.gov.mf.etoll.core.ridecoordinatorv2.sender

import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.DateTime
import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.etoll.core.CoreComposedUC
import pl.gov.mf.etoll.core.NetworkManagerUC
import pl.gov.mf.etoll.core.ridecoordinatorv2.events.ServiceEventType
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.networking.api.model.*
import pl.gov.mf.etoll.storage.database.ridecache.RideCacheDatabase
import pl.gov.mf.etoll.storage.database.ridecache.model.RideCacheModel
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.networking.api.interceptors.WrongSystemTimeException
import pl.gov.mf.mobile.utils.CallbacksContainer
import pl.gov.mf.mobile.utils.addSafe
import pl.gov.mf.mobile.utils.toObject
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

class RideCoordinatorV3SenderImpl @Inject constructor(
    private val rideCacheDatabase: RideCacheDatabase,
    private val sendDataToEventStreamUseCase: NetworkManagerUC.SendDataToEventStreamUseCase,
    private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase,
    private val timeIssuesDetectedUseCase: CoreComposedUC.TimeIssuesDetectedUseCase
) : RideCoordinatorV3Sender {

    private val callbacksContainer: CallbacksContainer<RideCoordinatorV3SenderCallbacks> =
        CallbacksContainer()
    private var isSendingTolled = AtomicBoolean(false)
    private var isSendingSent = AtomicBoolean(false)
    private val compositeDisposable = CompositeDisposable()

    private val lastSentSendTime = AtomicLong(0)
    private val lastTolledSendTime = AtomicLong(0)

    override fun resetTimers() {
        lastSentSendTime.set(System.currentTimeMillis())
        lastTolledSendTime.set(System.currentTimeMillis())
    }

    private fun resetSingleTimer(sent: Boolean) {
        if (sent)
            lastSentSendTime.set(System.currentTimeMillis())
        else
            lastTolledSendTime.set(System.currentTimeMillis())
    }

    override fun checkAndSend(forceTolled: Boolean, forceSent: Boolean) {
        // check and send separate batches
        Log.d("SENDERT1", "ENTRY: ${isSendingTolled.get()}")
        if (!isSendingTolled.getAndSet(true)) {
            compositeDisposable.addSafe(
                rideCacheDatabase.isAnyServiceEventPending(false).subscribe({ forced ->
                    val timePassed =
                        System.currentTimeMillis() - lastTolledSendTime.get() >= RideCoordinatorV3.SECONDS_BETWEEN_SEND_TRY_TOLLED * 1000
                    Log.d("SENDERT1", "$forced | $forceTolled | $timePassed")
                    if (forced || forceTolled || timePassed) {
                        compositeDisposable.addSafe(
                            rideCacheDatabase.getDataPackage(
                                RideCoordinatorV3.MAX_AMOUNT_OF_ITEMS_PER_PACKAGE, false
                            )
                                .subscribe({ items ->
                                    sendItems(items, false)
                                }, {
                                    // do nothing
                                    Log.e(javaClass.name, it?.localizedMessage ?: "")
                                    resetSendingFlag(false)
                                })
                        )
                    } else
                        resetSendingFlag(false)
                }, {
                    Log.d("SENDERT1", "Error: ${it?.localizedMessage ?: ""}")
                    Log.e(javaClass.name, it?.localizedMessage ?: "")
                    resetSendingFlag(false)
                })
            )

        }
        if (!isSendingSent.getAndSet(true)) {
            compositeDisposable.addSafe(
                rideCacheDatabase.isAnyServiceEventPending(true).subscribe({ forced ->
                    val timePassed =
                        System.currentTimeMillis() - lastSentSendTime.get() >= RideCoordinatorV3.SECONDS_BETWEEN_SEND_TRY_SENT * 1000
                    if (forced || forceSent || timePassed) {
                        compositeDisposable.add(
                            rideCacheDatabase.getDataPackage(
                                RideCoordinatorV3.MAX_AMOUNT_OF_ITEMS_PER_PACKAGE, true
                            )
                                .subscribe({ items ->
                                    sendItems(items, true)
                                }, {
                                    // do nothing
                                    Log.e(javaClass.name, it?.localizedMessage ?: "")
                                    resetSendingFlag(true)
                                })
                        )
                    } else
                        resetSendingFlag(true)
                }, {
                    Log.e(javaClass.name, it?.localizedMessage ?: "")
                    resetSendingFlag(true)
                })
            )
        }
    }

    private fun resetSendingFlag(sent: Boolean) {
        if (sent)
            isSendingSent.set(false)
        else
            isSendingTolled.set(false)
    }

    private fun sendItems(items: List<RideCacheModel>?, sent: Boolean) {
        // if list is empty or items size is lower than min and it's not forced
        // OR not( list is not empty and max time for sending passed )
        // important - for SENT start timer AFTER first element appears on list
        // if there are no items, reset sending timer
        Log.d("SENDERT1", "SENDING EVENTS CALLED -> ${items?.size ?: 0}")
        if (items.isNullOrEmpty())
            resetSingleTimer(sent)
        if (items.isNullOrEmpty()) {
            // cancel process
            resetSendingFlag(sent)
            return
        }

        // continue with sending
        val convertedItems = items.toEventStreamObject()
        Log.d("SENDERT1", "SENDING EVENTS")
        compositeDisposable.addSafe(
            sendDataToEventStreamUseCase.execute(convertedItems).subscribe(
                {
                    Log.d("SENDERT1", "Events sent")
                    // reset selected timer
                    resetSingleTimer(sent)
                    val ids = mutableListOf<Long>()
                    val dataIds = mutableListOf<Long>()
                    items.forEach { ids.add(it.id) }
                    convertedItems.events.forEach {
                        try {
                            when (it) {
                                is EventStreamLocation -> dataIds.add(it.dataId.toLong())
                                is EventStreamLocationWithoutLocation -> dataIds.add(it.dataId.toLong())
                                else -> dataIds.add(-1L)
                            }
                        } catch (ex: Exception) {
                            // do nothing
                        }
                    }
                    // we do not manage this observable
                    writeSettingsUseCase.execute(
                        Settings.LAST_CORRECT_DATA_SENDING_TIMESTAMP,
                        System.currentTimeMillis().toString()
                    ).subscribe()

                    callbacksContainer.get().values.forEach {
                        it.onPackageSent(
                            items.size,
                            dataIds, sent
                        )
                    }

                    rideCacheDatabase.removeDataByIds(ids).subscribe({
                        resetSendingFlag(sent)
                        if (ids.size == RideCoordinatorV3.MAX_AMOUNT_OF_ITEMS_PER_PACKAGE) {
                            // flush all rest
                            checkAndSend(!sent, sent)
                        }
                    }, {
                        resetSendingFlag(sent)
                        if (ids.size == RideCoordinatorV3.MAX_AMOUNT_OF_ITEMS_PER_PACKAGE) {
                            // flush all rest
                            checkAndSend(!sent, sent)
                        }
                    })
                },
                { error ->
                    if (error is WrongSystemTimeException) {
                        timeIssuesDetectedUseCase.execute()
                    }
                    Log.d("SENDERT1", "error -> ${error.localizedMessage}")
                    callbacksContainer.get().values.forEach {
                        it.onPackageSendingError(error.localizedMessage)
                    }
                    resetSendingFlag(sent)
                })
        )
    }

    override fun setCallbacks(callbacks: RideCoordinatorV3SenderCallbacks?, tag: String) {
        callbacksContainer.set(callbacks, tag)
    }
}

private fun List<RideCacheModel>.toEventStreamObject(): EventStreamObject {
    val date = DateTime.now()
        .toString(TimeUtils.DefaultDateTimeFormatter)
    val items = mutableListOf<EventStreamBaseEvent>()
    forEach {
        when (ServiceEventType.fromInt(it.serviceTypeId)) {
            ServiceEventType.DEFAULT -> {
                val output = if (it.withLocation)
                    it.payload.toObject<EventStreamLocation>()
                else
                    it.payload.toObject<EventStreamLocationWithoutLocation>()
                items.add(output)
            }
            ServiceEventType.ACTIVATE_VEHICLE -> {
                val output = it.payload.toObject<EventStreamActivateVehicle>()
                items.add(output)
            }
            ServiceEventType.START_SENT -> {
                val output = it.payload.toObject<EventStreamStartSent>()
                items.add(output)
            }
            ServiceEventType.STOP_SENT -> {
                val output = it.payload.toObject<EventStreamStopSent>()
                items.add(output)
            }
            ServiceEventType.CANCEL_SENT -> {
                val output = it.payload.toObject<EventStreamCancelSent>()
                items.add(output)
            }
            ServiceEventType.CHANGE_DEVICE -> {
                val output = it.payload.toObject<EventStreamChangeTrackingDevice>()
                items.add(output)
            }
            ServiceEventType.APPLICATION_RESUME ->{
                val output = it.payload.toObject<EventStreamResumeApplication>()
                items.add(output)
            }
        }
    }
    return EventStreamObject(date, items)
}
