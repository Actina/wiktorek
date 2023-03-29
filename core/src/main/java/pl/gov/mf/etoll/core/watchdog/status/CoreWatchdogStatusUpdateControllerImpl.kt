package pl.gov.mf.etoll.core.watchdog.status

import android.content.Intent
import android.util.Log
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime
import pl.gov.mf.etoll.core.BuildConfig
import pl.gov.mf.etoll.core.CoreComposedUC
import pl.gov.mf.etoll.core.NetworkManagerUC
import pl.gov.mf.etoll.core.crmmessages.CrmMessagesManager
import pl.gov.mf.etoll.core.model.CoreStatus
import pl.gov.mf.etoll.core.notifications.NotificationManager
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.core.ridecoordinatorv2.sent.selector.SentAlgorithmSelector
import pl.gov.mf.etoll.core.watchdog.CoreWatchdogData
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.networking.api.interceptors.WrongSystemTimeException
import pl.gov.mf.mobile.utils.addSafe
import pl.gov.mf.mobile.utils.toObject
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class CoreWatchdogStatusUpdateControllerImpl @Inject constructor(
    private val rideCoordinatorV3: RideCoordinatorV3,
    private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase,
    private val getSentList: NetworkManagerUC.CheckSentListUseCase,
    private val updateStatusUseCase: NetworkManagerUC.UpdateStatusUseCase,
    private val crmMessagesManager: CrmMessagesManager,
    private val notificationManager: NotificationManager,
    private val timeIssuesDetectedUseCase: CoreComposedUC.TimeIssuesDetectedUseCase
) :
    CoreWatchdogStatusUpdateController {

    private val compositeDisposable = CompositeDisposable()
    private val statusSubject: BehaviorSubject<CoreWatchdogData> = BehaviorSubject.create()
    private val updatingStatusFlag = AtomicBoolean(false)
    private var lastUsedIntent = 0
    private var forceUpdate = AtomicBoolean(false)

    override fun initialize() {
        if (BuildConfig.DEBUG)
            compositeDisposable.addSafe(statusSubject.subscribe {
                Log.d(LOGCAT_TAG, "STATUS UPDATE: $it")
            })
        val rideConfiguration = rideCoordinatorV3.getConfiguration()
        // calculate starting conditions
        val rideMode = rideConfiguration?.duringRide ?: false
        val businessNumberIsPresent =
            !readSettingsUseCase.executeForString(Settings.BUSINESS_NUMBER).isEmpty()
        val statusIsPresent = readSettingsUseCase.executeForString(Settings.STATUS)
        val date =
            if (statusIsPresent.isEmpty()) null else
                DateTime(statusIsPresent.toObject<CoreStatus>().dateTimestamp)

        if (rideMode) {
            updateStatusSubject(
                date
            )
        } else {
            if (businessNumberIsPresent) {
                updateStatusSubject(
                    date
                )

            } else {
                updateStatusSubject(
                    date
                )
            }
        }
    }


    override fun observeUpdates(): Observable<CoreWatchdogData> = statusSubject

    override fun onUpdateStatusRemoteRequest() {
        if (updatingStatusFlag.getAndSet(true))
            return
        updateStatus()
    }

    override fun shouldForceUpdate(): Boolean =
        (forceUpdate.getAndSet(false) || statusSubject.value!!.lastStatusUpdate == null) && !updatingStatusFlag.get()


    override fun updateNow(appInForeground: CoreWatchdogStatusUpdateController.AppInForegroundInfoProvider) {
        if (updatingStatusFlag.getAndSet(true))
            return
        // check if we should also try to update sent list
        updateSentList(appInForeground)
        // update status
        updateStatus()
    }

    override fun checkIfUpdateShouldBeDoneNow(intent: Intent?) {
        if (intent == null || intent.extras == null || intent.hashCode() == lastUsedIntent)
            return
        lastUsedIntent = intent.hashCode()
        var shouldUpdate = false
        intent.extras?.keySet()?.forEach {
            shouldUpdate = shouldUpdate || (it?.uppercase()?.contains("GOOGLE") ?: false)
        }
        if (shouldUpdate) {
            forceUpdate.set(true)
        }
    }

    private fun updateSentList(appInForeground: CoreWatchdogStatusUpdateController.AppInForegroundInfoProvider) {
        rideCoordinatorV3.getConfiguration()?.let { rideConfiguration ->
            if (rideConfiguration.duringSent && rideConfiguration.sentConfiguration?.sentSelectionWasPossibleAndDone == false) {
                // try to update sent list
                compositeDisposable.addSafe(getSentList.execute().subscribe({ sentList ->
                    rideConfiguration.sentConfiguration?.availableSentList = sentList
                    rideConfiguration.sentConfiguration?.sentListWasDownloaded = true
                    rideCoordinatorV3.onConfigurationUpdated(rideConfiguration)
                    // if app is in background, show notification about this change
                    if (!appInForeground.appIsInForeground()) {
                        // show notification
                        notificationManager.createNotificationForSentOnlineConfigurationAvailable()
                    }
                }, { error ->
                    // do nothing, we'll retry this call later on
                    if (error is WrongSystemTimeException) {
                        timeIssuesDetectedUseCase.execute()
                    }
                }))
            }
        }
    }

    private fun updateStatus() {
        Log.d(LOGCAT_TAG, "Updating status")
        compositeDisposable.addSafe(updateStatusUseCase.execute().subscribe({
            updateRideConfiguration(it)
            updateSampling(it)
            updateStatusSubject(DateTime.now())

            // update messages queue
            crmMessagesManager.onMessageIdsQueued(it.messageIds.toList())

            updatingStatusFlag.set(false)
        }, {
            if (it is WrongSystemTimeException) {
                timeIssuesDetectedUseCase.execute()
            }
            // network error, ignore main part, but try to update rate's to not be default ones
            Log.d(LOGCAT_TAG, "Updating status error: ${it.localizedMessage}")
            try {
                val status =
                    readSettingsUseCase.executeForString(Settings.STATUS).toObject<CoreStatus>()
                updateSampling(status)
            } catch (_: Exception) {

            }
            updatingStatusFlag.set(false)
            forceUpdate.set(true)
        }))
    }

    private fun updateSampling(it: CoreStatus) {
        // sampling updates
        RideCoordinatorV3.SECONDS_BETWEEN_SEND_TRY_TOLLED =
            it.configuration.sendingSamplingRateInSeconds
        RideCoordinatorV3.SECONDS_BETWEEN_COLLECTING_TOLLED =
            it.configuration.collectionSamplingRateInSeconds
        RideCoordinatorV3.SECONDS_BETWEEN_COLLECTING_SENT =
            it.configuration.sentCollectionRate
        RideCoordinatorV3.SECONDS_BETWEEN_SEND_TRY_SENT =
            it.configuration.sentSamplingRate
        Log.d(
            LOGCAT_TAG,
            "Current collection rate: ${RideCoordinatorV3.SECONDS_BETWEEN_COLLECTING_TOLLED} and sending: ${RideCoordinatorV3.SECONDS_BETWEEN_SEND_TRY_TOLLED}"
        )
        Log.d(
            LOGCAT_TAG,
            "Current collection rate SENT: ${RideCoordinatorV3.SECONDS_BETWEEN_COLLECTING_SENT} and sending: ${RideCoordinatorV3.SECONDS_BETWEEN_SEND_TRY_SENT}"
        )
        // update sent variables
        SentAlgorithmSelector.updateConfiguration(it.configuration)
    }


    private fun updateRideConfiguration(coreStatus: CoreStatus?) {
        // update balance info if tolled ride is configured
        coreStatus?.let { status ->
            rideCoordinatorV3.getConfiguration()?.let { configuration ->
                configuration.tolledConfiguration?.vehicle?.let { vehicle ->
                    status.vehicles.find { it.id == vehicle.id }?.let { updatedVehicle ->
                        configuration.tolledConfiguration!!.vehicle!!.accountInfo =
                            updatedVehicle.accountInfo
                        rideCoordinatorV3.onConfigurationUpdated(configuration)
                    }
                }
            }
        }
    }


    private fun updateStatusSubject(
        lastStatusUpdate: DateTime?
    ) {
        statusSubject.onNext(
            CoreWatchdogData(
                lastStatusUpdate
            )
        )
    }

    companion object {
        private const val LOGCAT_TAG = "StatusUpdater"
    }
}