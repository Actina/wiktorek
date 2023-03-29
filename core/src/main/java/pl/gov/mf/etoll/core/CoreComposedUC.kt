package pl.gov.mf.etoll.core

import android.util.Log
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.UndeliverableException
import pl.gov.mf.etoll.app.CommonGetBusinessNumberUseCase
import pl.gov.mf.etoll.core.NetworkManagerUC.UpdateStatusUseCase
import pl.gov.mf.etoll.core.model.CoreStatus
import pl.gov.mf.etoll.core.watchdog.netlock.CoreNetlockChecker
import pl.gov.mf.etoll.core.watchdog.timeissues.TimeIssuesController
import pl.gov.mf.etoll.storage.database.ridecache.RideCacheDatabase
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC.ReadSettingsUseCase
import pl.gov.mf.mobile.networking.api.interceptors.WrongSystemTimeException
import pl.gov.mf.mobile.utils.toObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed class CoreComposedUC {

    class GetDeviceIdUseCase(private val readSettingsUseCase: ReadSettingsUseCase) :
        CoreComposedUC() {
        fun execute() = readSettingsUseCase.executeForString(Settings.DEVICE_ID)
    }

    class GetCoreStatusUseCase(
        private val updateStatusUseCase: UpdateStatusUseCase,
        private val readSettingsUseCase: ReadSettingsUseCase,
        private val timeIssuesDetectedUseCase: TimeIssuesDetectedUseCase
    ) : CoreComposedUC() {
        fun executeAsync(): Single<CoreStatus> = Single.create { emitter ->
            readSettingsUseCase.executeForStringAsync(Settings.STATUS)
                .subscribe({
                    try {
                        emitter.onSuccess(it.toObject())
                    } catch (_: Exception) {
                        updateStatusUseCase.execute()
                            .subscribe({ status ->
                                emitter.onSuccess(status)
                            }, { error ->
                                if (error is WrongSystemTimeException)
                                    timeIssuesDetectedUseCase.execute()
                                try {
                                    emitter.onError(error)
                                } catch (_: UndeliverableException) {
                                    // do nothing
                                }
                            })
                    }
                }, {
                    try {
                        emitter.onError(it)
                    } catch (_: UndeliverableException) {
                        // do nothing, it's expected if time issue on registration screen
                    }
                })
        }

        fun execute(): CoreStatus? {
            return try {
                readSettingsUseCase.executeForString(Settings.STATUS).toObject()
            } catch (_: Exception) {
                null
            }
        }
    }

    class GetBusinessNumberUseCase @Inject constructor(private val readSettingsUseCase: ReadSettingsUseCase) :
        CoreComposedUC(),
        CommonGetBusinessNumberUseCase {
        override fun execute(): String =
            readSettingsUseCase.executeForString(Settings.BUSINESS_NUMBER)
    }

    class TimeIssuesDetectedUseCase(private val controller: TimeIssuesController) :
        CoreComposedUC() {
        fun execute(forceShow: Boolean = false) {
            controller.onTimeIssueDetected(forceShow)
        }
    }

    class ShowInstanceIssueUseCase(private val controller: CoreNetlockChecker) : CoreComposedUC() {
        fun execute() {
            controller.markStateAsNotShowed()
        }
    }

    class IsDataSyncInProgressUseCase @Inject constructor(private val rideCacheDatabase: RideCacheDatabase) :
        CoreComposedUC() {
        fun execute(): Observable<Boolean> = Observable.interval(0, 2, TimeUnit.SECONDS)
            .map {
                // check current sending state
                val output = rideCacheDatabase.count().blockingGet()
                Log.d("DATA_SYNC_STATUS", "$output")
                output > 0
            }.observeOn(AndroidSchedulers.mainThread())
    }
}