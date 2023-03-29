package pl.gov.mf.etoll.networking.manager

import android.annotation.SuppressLint
import android.util.Log
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.Seconds
import pl.gov.mf.etoll.commons.CurrencyUtils.format
import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.etoll.logging.LogUseCase
import pl.gov.mf.etoll.networking.api.NkspoApi
import pl.gov.mf.etoll.networking.api.model.*
import pl.gov.mf.etoll.networking.manager.NetworkingManager.Companion.DELAY_BETWEEN_RETRY
import pl.gov.mf.etoll.networking.manager.eventstream.EventStreamManager
import pl.gov.mf.etoll.networking.manager.eventstream.EventStreamManagerStatusDelegate
import pl.gov.mf.etoll.storage.database.logging.model.LoggingModel
import pl.gov.mf.etoll.storage.settings.Settings.*
import pl.gov.mf.etoll.storage.settings.SettingsUC.WriteSettingsUseCase
import pl.gov.mf.mobile.networking.api.interceptors.UnauthorizedNetworkException
import pl.gov.mf.mobile.networking.api.interceptors.WrongSystemTimeException
import pl.gov.mf.mobile.utils.addSafe
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class NetworkingManagerImpl @Inject constructor(
    private val writeSettingsUseCase: WriteSettingsUseCase,
    private val api: NkspoApi,
    private val logUseCase: LogUseCase,
    private val eventStreamManager: EventStreamManager,
    private val networkingManagerV2: NetworkingManagerV2,
    private val libraryScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : NetworkingManager, EventStreamManagerStatusDelegate {

    private var lastStatusResponse: Pair<ApiStatusResponse?, java.lang.Exception?>? = null
    private var lastCorrectStatusTimestamp = 0L
    private val statusLock = AtomicBoolean(false)

    init {
        eventStreamManager.setDelegate(this)
    }

    override fun authenticate(): Completable =
        Completable.create { emitter ->
            libraryScope.launch {
                when (val response = networkingManagerV2.authenticate(Job())) {
                    is NetworkingManagerV2.NetworkResponse.ERROR -> emitter.onError(response.cause.mapError())
                    NetworkingManagerV2.NetworkResponse.OK -> emitter.onComplete()
                }
            }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    @SuppressLint("CheckResult")
    override fun updateStatus(): Single<ApiStatusResponse> =
        Single.create<ApiStatusResponse> { emitter ->
            val localCompositeDisposable = CompositeDisposable()
            emitter.setCancellable { localCompositeDisposable.dispose() }
            if (statusLock.get()) {
                try {
                    // wait 10s
                    Thread.sleep(10000)
                } catch (ex: InterruptedException) {
                    // do nothing
                    return@create
                }
                // check if we have correct answer to use
                if (lastCorrectStatusTimestamp > 0L) {
                    val lastLocationTime =
                        DateTime(lastCorrectStatusTimestamp)
                    val currentTime = DateTime.now()
                    val secondsDiff = Seconds.secondsBetween(lastLocationTime, currentTime).seconds
                    // check if response came less than 12s before
                    if (secondsDiff < 12 && lastStatusResponse != null) {
                        if (lastStatusResponse!!.first != null)
                            emitter.onSuccess(lastStatusResponse!!.first!!)
                        else
                            emitter.onError(lastStatusResponse!!.second!!)
                        statusLock.set(false)
                        return@create
                    }
                }
                statusLock.set(false)
                if (lastStatusResponse != null) {
                    if (lastStatusResponse!!.first != null)
                        emitter.onSuccess(lastStatusResponse!!.first!!)
                    else
                        emitter.onError(lastStatusResponse!!.second!!)
                } else
                    emitter.onError(FlowControlException())
                return@create
            }
            statusLock.set(true)
            if (lastCorrectStatusTimestamp > 0L) {
                val lastLocationTime =
                    DateTime(lastCorrectStatusTimestamp)
                val currentTime = DateTime.now()
                val secondsDiff = Seconds.secondsBetween(lastLocationTime, currentTime).seconds
                if (secondsDiff in 0..4 && lastStatusResponse != null) {
                    if (lastStatusResponse!!.first != null)
                        emitter.onSuccess(lastStatusResponse!!.first!!)
                    else
                        emitter.onError(lastStatusResponse!!.second!!)
                    statusLock.set(false)
                    return@create
                }
            }
            localCompositeDisposable.addSafe(
                api.getStatus()
                    .subscribeOn(Schedulers.io())
                    .subscribe({ status ->
                        writeSettingsUseCase.execute(STATUS, status.toJSON())
                            .subscribe({
                                eventStreamManager.updateConfigurationAndRebuild(status.eventStream)
                                lastStatusResponse = Pair(status, null)
                                lastCorrectStatusTimestamp = System.currentTimeMillis()
                                logUseCase.log(LogUseCase.NETWORK, "status() correct!")
                                statusLock.set(false)
                                emitter.onSuccess(status)
                            }, {
                                if (it is WrongSystemTimeException) {
                                    lastStatusResponse = Pair(null, it)
                                }
                                statusLock.set(false)
                                logUseCase.log(
                                    LogUseCase.NETWORK,
                                    "status() error! ${it.localizedMessage}"
                                )
                                emitter.onError(it)
                            })
                    }, { error ->
                        if (error is WrongSystemTimeException || error is javax.net.ssl.SSLHandshakeException) {
                            logUseCase.log(
                                LogUseCase.NETWORK,
                                "status() wrong time issue!}"
                            )
                            lastStatusResponse =
                                Pair(null, WrongSystemTimeException(error.message ?: "Wrong time"))
                            emitter.onError(error)
                            return@subscribe
                        }

                        try {
                            Thread.sleep(DELAY_BETWEEN_RETRY)
                        } catch (ex: InterruptedException) {
                            // do nothing
                        }
                        statusLock.set(false)
                        if (error is UnauthorizedNetworkException) {
                            authenticate().subscribe({
                                updateStatus().subscribe(
                                    { emitter.onSuccess(it) },
                                    {
                                        logUseCase.log(
                                            LogUseCase.NETWORK,
                                            "status() error! ${it.localizedMessage}"
                                        )
                                        emitter.onError(it)
                                    })
                            }, {
                                logUseCase.log(
                                    LogUseCase.NETWORK,
                                    "status() error! ${it.localizedMessage}"
                                )
                                emitter.onError(it)
                            })
                        } else {
                            logUseCase.log(
                                LogUseCase.NETWORK,
                                "status() error! ${error.localizedMessage}"
                            )
                            emitter.onError(error)
                        }
                    })
            )
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    override fun checkSentList(): Single<Map<String, List<ApiSentItem>>> =
        Single.create<Map<String, List<ApiSentItem>>> { emitter ->
            api.getSentList()
                .subscribeOn(Schedulers.io())
                .subscribe({ response ->
                    if (response is ApiSentResponse) {
                        try {
                            logUseCase.log(LogUseCase.NETWORK, "getSentList() correct!")
                            emitter.onSuccess(response.data)
                        } catch (ex: Exception) {
                            logUseCase.log(
                                LogUseCase.NETWORK,
                                "getSentList() error! ${ex.localizedMessage}"
                            )
                            emitter.onError(ex)
                        }
                    } else emitter.onError(Exception("Wrong response from server"))
                }, { error ->
                    if (error is WrongSystemTimeException || error is javax.net.ssl.SSLHandshakeException) {
                        logUseCase.log(
                            LogUseCase.NETWORK,
                            "getSentList() wrong time issue!}"
                        )
                        emitter.onError(WrongSystemTimeException(error.message ?: "Wrong time"))
                        return@subscribe
                    }

                    try {
                        Thread.sleep(DELAY_BETWEEN_RETRY)
                    } catch (ex: InterruptedException) {
                        // do nothing
                    }
                    if (error is UnauthorizedNetworkException) {
                        authenticate().subscribe({
                            checkSentList().subscribe(
                                { emitter.onSuccess(it) },
                                {
                                    logUseCase.log(
                                        LogUseCase.NETWORK,
                                        "getSentList() error! ${it.localizedMessage}"
                                    )
                                    emitter.onError(it)
                                })
                        }, {
                            logUseCase.log(
                                LogUseCase.NETWORK,
                                "getSentList() error! ${it.localizedMessage}"
                            )
                            emitter.onError(it)
                        })
                    } else {
                        logUseCase.log(
                            LogUseCase.NETWORK,
                            "getSentList() error! ${error.localizedMessage}"
                        )
                        emitter.onError(error)
                    }
                })
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    override fun openTransaction(
        billingAccountId: Long,
        amount: Double,
        returnUrl: String,
        category: Int
    ): Single<ApiTecsOpenTransactionResponse> =
        Single.create<ApiTecsOpenTransactionResponse> { emitter ->
            api.tecsOpenTransaction(
                ApiTecsOpenTransactionRequest(
                    billingAccountId,
                    amount.format(2),
                    returnUrl,
                    category
                )
            )
                .subscribeOn(Schedulers.io())
                .subscribe({
                    logUseCase.log(LogUseCase.NETWORK, "openTransaction() correct!")
                    emitter.onSuccess(it)
                }, { error ->
                    if (error is WrongSystemTimeException || error is javax.net.ssl.SSLHandshakeException) {
                        logUseCase.log(
                            LogUseCase.NETWORK,
                            "openTransaction() wrong time issue!}"
                        )
                        emitter.onError(WrongSystemTimeException(error.message ?: "Wrong time"))
                        return@subscribe
                    }

                    try {
                        Thread.sleep(DELAY_BETWEEN_RETRY)
                    } catch (ex: InterruptedException) {
                        // do nothing
                    }
                    if (error is UnauthorizedNetworkException) {
                        authenticate().subscribe({
                            openTransaction(
                                billingAccountId,
                                amount,
                                returnUrl,
                                category
                            ).subscribe({ emitter.onSuccess(it) }, {
                                logUseCase.log(
                                    LogUseCase.NETWORK,
                                    "openTransaction() error! ${it.localizedMessage}"
                                )
                                emitter.onError(it)
                            })
                        }, {
                            logUseCase.log(
                                LogUseCase.NETWORK,
                                "openTransaction() error! ${it.localizedMessage}"
                            )
                            emitter.onError(it)
                        })
                    } else {
                        logUseCase.log(
                            LogUseCase.NETWORK,
                            "openTransaction() error! ${error.localizedMessage}"
                        )
                        emitter.onError(error)
                    }

                })
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    override fun closeTransaction(tecsTransactionResult: TecsTransactionResult): Single<ApiTecsCloseTransactionResponse> =
        Single.create<ApiTecsCloseTransactionResponse> { emitter ->
            api.tecsCloseTransaction(
                ApiTecsCloseTransactionRequest(
                    tecsTransactionResult.txid!!.toLong(),
                    tecsTransactionResult.sign,
                    tecsTransactionResult.responseCode!!.toLong(),
                    tecsTransactionResult.responseText,
                    tecsTransactionResult.cardRefNumber,
                    tecsTransactionResult.userData,
                    tecsTransactionResult.cardType,
                    tecsTransactionResult.authorizationNumber,
                    tecsTransactionResult.stan,
                    TimeUtils.convertTecsDates(tecsTransactionResult.dateTime!!),
                    tecsTransactionResult.acquirerName,
                    tecsTransactionResult.operatorId
                )
            )
                .subscribeOn(Schedulers.io()).subscribe({
                    logUseCase.log(LogUseCase.NETWORK, "closeTransaction() correct!")
                    emitter.onSuccess(it)
                }, { error ->
                    if (error is WrongSystemTimeException || error is javax.net.ssl.SSLHandshakeException) {
                        logUseCase.log(
                            LogUseCase.NETWORK,
                            "closeTransaction() wrong time issue!}"
                        )
                        emitter.onError(WrongSystemTimeException(error.message ?: "Wrong time"))
                        return@subscribe
                    }

                    try {
                        Thread.sleep(DELAY_BETWEEN_RETRY)
                    } catch (ex: InterruptedException) {
                        // do nothing
                    }
                    if (error is UnauthorizedNetworkException) {
                        authenticate().subscribe({
                            closeTransaction(
                                tecsTransactionResult
                            ).subscribe({ emitter.onSuccess(it) }, {
                                logUseCase.log(
                                    LogUseCase.NETWORK,
                                    "closeTransaction() error! ${it.localizedMessage}"
                                )
                                emitter.onError(it)
                            })
                        }, {
                            logUseCase.log(
                                LogUseCase.NETWORK,
                                "closeTransaction() error! ${it.localizedMessage}"
                            )
                            emitter.onError(it)
                        })
                    } else {
                        logUseCase.log(
                            LogUseCase.NETWORK,
                            "closeTransaction() error! ${error.localizedMessage}"
                        )
                        emitter.onError(error)
                    }

                })
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    override fun getLastPositions(
        coreTransitType: String,
        zslBusinessId: String?
    ): Single<ApiLastPositionsSentResponse> =
        Single.create<ApiLastPositionsSentResponse> { emitter ->
            api.getLastPositions(coreTransitType, zslBusinessId)
                .subscribeOn(Schedulers.io())
                .subscribe({ response ->
                    if (response is ApiLastPositionsSentResponse) {
                        try {
                            logUseCase.log(LogUseCase.NETWORK, "getLastPosition() correct!")
                            emitter.onSuccess(response)
                        } catch (ex: Exception) {
                            logUseCase.log(
                                LogUseCase.NETWORK,
                                "getLastPosition() error! ${ex.localizedMessage}"
                            )
                            emitter.onError(ex)
                        }
                    } else emitter.onError(Exception("Wrong response from server"))
                }, { error ->
                    if (error is WrongSystemTimeException || error is javax.net.ssl.SSLHandshakeException) {
                        logUseCase.log(
                            LogUseCase.NETWORK,
                            "getLastPosition() wrong time issue!}"
                        )
                        emitter.onError(WrongSystemTimeException(error.message ?: "Wrong time"))
                        return@subscribe
                    }


                    try {
                        Thread.sleep(DELAY_BETWEEN_RETRY)
                    } catch (ex: InterruptedException) {
                        // do nothing
                    }
                    if (error is UnauthorizedNetworkException) {
                        authenticate().subscribe({
                            getLastPositions(coreTransitType, zslBusinessId).subscribe(
                                { emitter.onSuccess(it) },
                                {
                                    logUseCase.log(
                                        LogUseCase.NETWORK,
                                        "getLastPosition() error! ${it.localizedMessage}"
                                    )
                                    emitter.onError(it)
                                })
                        }, {
                            logUseCase.log(
                                LogUseCase.NETWORK,
                                "getLastPosition() error! ${it.localizedMessage}"
                            )
                            emitter.onError(it)
                        })
                    } else {
                        logUseCase.log(
                            LogUseCase.NETWORK,
                            "getLastPosition() error! ${error.localizedMessage}"
                        )
                        emitter.onError(error)
                    }
                })
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    override fun getMessages(ids: Array<String>): Single<List<ApiServerMessage>> =
        Single.create<List<ApiServerMessage>> { emitter ->
            api.getMessages(ids)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    logUseCase.log(LogUseCase.NETWORK, "getMessages() correct!")
                    emitter.onSuccess(it)
                }, { error ->
                    if (error is WrongSystemTimeException || error is javax.net.ssl.SSLHandshakeException) {
                        logUseCase.log(
                            LogUseCase.NETWORK,
                            "getMessages() wrong time issue!}"
                        )
                        emitter.onError(WrongSystemTimeException(error.message ?: "Wrong time"))
                        return@subscribe
                    }

                    try {
                        Thread.sleep(DELAY_BETWEEN_RETRY)
                    } catch (ex: InterruptedException) {
                        // do nothing
                    }
                    if (error is UnauthorizedNetworkException) {
                        authenticate().subscribe({
                            getMessages(ids).subscribe(
                                { emitter.onSuccess(it) },
                                {
                                    logUseCase.log(
                                        LogUseCase.NETWORK,
                                        "getMessages() error! ${it.localizedMessage}"
                                    )
                                    emitter.onError(it)
                                })
                        }, {
                            logUseCase.log(
                                LogUseCase.NETWORK,
                                "getMessages() error! ${it.localizedMessage}"
                            )
                            emitter.onError(it)
                        })
                    } else {
                        logUseCase.log(
                            LogUseCase.NETWORK,
                            "getMessages() error! ${error.localizedMessage}"
                        )
                        emitter.onError(error)
                    }
                })
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    override fun confirmMessages(ids: Array<String>): Completable = Completable.create { emitter ->
        api.confirmMessages(ApiConfirmMessagesRequest(ids))
            .subscribeOn(Schedulers.io())
            .subscribe({
                logUseCase.log(LogUseCase.NETWORK, "confirmMessages() correct!")
                emitter.onComplete()
            }, { error ->
                if (error is WrongSystemTimeException || error is javax.net.ssl.SSLHandshakeException) {
                    logUseCase.log(
                        LogUseCase.NETWORK,
                        "confirmMessages() wrong time issue!}"
                    )
                    emitter.onError(WrongSystemTimeException(error.message ?: "Wrong time"))
                    return@subscribe
                }

                try {
                    Thread.sleep(DELAY_BETWEEN_RETRY)
                } catch (ex: InterruptedException) {
                    // do nothing
                }
                if (error is UnauthorizedNetworkException) {
                    authenticate().subscribe({
                        confirmMessages(ids).subscribe(
                            { emitter.onComplete() },
                            {
                                logUseCase.log(
                                    LogUseCase.NETWORK,
                                    "confirmMessages() error! ${it.localizedMessage}"
                                )
                                emitter.onError(it)
                            })
                    }, {
                        logUseCase.log(
                            LogUseCase.NETWORK,
                            "confirmMessages() error! ${it.localizedMessage}"
                        )
                        emitter.onError(it)
                    })
                } else {
                    logUseCase.log(
                        LogUseCase.NETWORK,
                        "confirmMessages() error! ${error.localizedMessage}"
                    )
                    emitter.onError(error)
                }
            }

            )
    }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    override fun log(
        items: List<LoggingModel>
    ): Completable = Completable.create { emitter ->
        api.log(
            ApiLogRequest(
                items.toApiModel()
            )
        ).subscribeOn(Schedulers.io())
            .subscribe({
                emitter.onComplete()
            }, { error ->
                try {
                    Thread.sleep(DELAY_BETWEEN_RETRY)
                } catch (ex: InterruptedException) {
                    // do nothing
                }
                // this one need to be with flow control
                if (error is WrongSystemTimeException || error is javax.net.ssl.SSLHandshakeException) {
                    logUseCase.log(
                        LogUseCase.NETWORK,
                        "log() wrong time issue!}"
                    )
                    emitter.onError(WrongSystemTimeException(error.message ?: "Wrong time"))
                    return@subscribe
                }
                if (error is UnauthorizedNetworkException) {
                    authenticate().subscribe({
                        log(items).subscribe(
                            { emitter.onComplete() },
                            { emitter.onError(it) })
                    }, {
                        emitter.onError(it)
                    })
                } else {
                    emitter.onError(error)
                }
            }

            )
    }

    override fun onUpdateStatusRequested(): Completable {
        Log.d("EventStreamManager", "Requested status update, trying to update!")
        // just update status
        return updateStatus().ignoreElement()
    }
}

private fun NetworkingManagerV2.NetworkErrorCause.mapError(): Throwable =
    when (this) {
        NetworkingManagerV2.NetworkErrorCause.GENERIC -> Exception("Network error")
        NetworkingManagerV2.NetworkErrorCause.APP_VERSION_PROBLEM -> Exception("Wrong app version")
        NetworkingManagerV2.NetworkErrorCause.TIME_PROBLEM -> WrongSystemTimeException("")
    }

private fun List<LoggingModel>.toApiModel(): List<ApiLog> {
    val output = mutableListOf<ApiLog>()
    this.forEach {
        output.add(it.toApiModel())
    }
    return output
}

private fun LoggingModel.toApiModel(): ApiLog = ApiLog(date, tag, description)


