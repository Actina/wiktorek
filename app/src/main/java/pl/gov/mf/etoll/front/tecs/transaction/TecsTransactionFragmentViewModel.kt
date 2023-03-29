package pl.gov.mf.etoll.front.tecs.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.core.CoreComposedUC
import pl.gov.mf.etoll.core.NetworkManagerUC
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.core.ridehistory.collector.RideHistoryCollector
import pl.gov.mf.etoll.core.ridehistory.model.ActivityAdditionalData
import pl.gov.mf.etoll.front.tecs.transaction.TransactionConfiguration.TECS_TIMER_LIMIT_VISIBILITY_FROM_SECS
import pl.gov.mf.etoll.front.tecs.transaction.cache.TecsCache
import pl.gov.mf.etoll.front.tecs.transaction.counter.TecsCounter
import pl.gov.mf.etoll.interfaces.CommonInterfacesUC
import pl.gov.mf.etoll.interfaces.NotificationHistoryController
import pl.gov.mf.etoll.networking.api.model.ApiTecsCloseTransactionResponseStatus
import pl.gov.mf.etoll.networking.api.model.TecsTransactionResult
import pl.gov.mf.mobile.networking.api.interceptors.WrongSystemTimeException
import pl.gov.mf.mobile.utils.addSafe
import javax.inject.Inject

class TecsTransactionFragmentViewModel : BaseDatabindingViewModel() {

    @Inject
    lateinit var tecsCloseTransactionUseCase: NetworkManagerUC.CloseTransactionUseCase

    @Inject
    lateinit var rideHistoryCollector: RideHistoryCollector

    @Inject
    lateinit var addToHistoryUseCase: CommonInterfacesUC.AddNotificationToHistoryUseCase

    @Inject
    lateinit var rideCoordinatorV3: RideCoordinatorV3

    @Inject
    lateinit var timeIssuesDetectedUseCase: CoreComposedUC.TimeIssuesDetectedUseCase

    @Inject
    lateinit var tecsCounter: TecsCounter

    private val _leftSeconds =
        MutableLiveData(TransactionConfiguration.TECS_TIMER_LIMIT_IN_SECS.toLong())
    val leftSeconds: LiveData<Long> = _leftSeconds

    private val _navigationTarget: MutableLiveData<NavigationDestinations> =
        MutableLiveData(NavigationDestinations.NONE)
    val navigationDestinations: LiveData<NavigationDestinations>
        get() = _navigationTarget

    private val _timerVisibility = MutableLiveData(false)
    val timerVisibility: LiveData<Boolean> = _timerVisibility

    lateinit var webViewUrl: String
    lateinit var selectedAmount: String
    private var bonusSeconds = 0
    private var calledFinalize = false
    private var tecsCache: TecsCache? = null

    override fun onCreate() {
        super.onCreate()
        tecsCache = TecsCache()
        tecsCounter.initializeTransaction(System.currentTimeMillis())
    }

    override fun onPause() {
        super.onPause()
        // For bonus time when user minimised app and onResume was called
        bonusSeconds = -1
    }

    override fun onResume() {
        super.onResume()
        calledFinalize = false
        bonusSeconds = 0

        tecsCache?.getCache()?.let { cache ->
            onTecsResult(cache)
        }

        compositeDisposable.addSafe(tecsCounter.observeStatus().subscribe { leftSeconds ->
            if (tecsCache?.getCache() == null) {
                _leftSeconds.value = leftSeconds
                _timerVisibility.value = leftSeconds <= TECS_TIMER_LIMIT_VISIBILITY_FROM_SECS

                if (leftSeconds <= 0 && !calledFinalize) {
                    if (bonusSeconds >= 0) {
                        bonusSeconds++
                    }

                    if (bonusSeconds > TransactionConfiguration.TECS_TIMER_BONUS_AFTER_RETURNING_FROM_BACKGROUND) {
                        _navigationTarget.postValue(NavigationDestinations.TRANSACTION_SESSION_EXPIRED)
                    }
                }
            }
        })

    }

    private fun onTecsResult(tecsOutput: Map<String, String>) {
        // store cache in case of going to background while waiting for response
        tecsCache?.updateCache(tecsOutput)
        cancelTransaction()
    }

    fun updateCache(value: Map<String, String>) {
        tecsCache?.updateCache(value)
    }

    fun onToolbarCrossClick() {
        _navigationTarget.postValue(NavigationDestinations.USER_CANCEL_REQUEST)
    }

    fun interpretTransactionOutput(output: HashMap<String, String>) {
        val transactionModel = TecsTransactionResult.from(output)
        compositeDisposable.addSafe(
            tecsCloseTransactionUseCase.execute(transactionModel)
                .subscribe({
                    when (ApiTecsCloseTransactionResponseStatus.fromString(it.status)) {
                        ApiTecsCloseTransactionResponseStatus.SUCCESS -> {
                            addTopUpToHistory()
                            addTopUpToNotificationHistory()
                            _navigationTarget.postValue(NavigationDestinations.TRANSACTION_SUCCESS)
                        }
                        ApiTecsCloseTransactionResponseStatus.FAILURE -> {
                            _navigationTarget.postValue(
                                NavigationDestinations.TRANSACTION_FAILED(
                                    it.errorHeader,
                                    it.errorContent
                                )
                            )
                        }
                        ApiTecsCloseTransactionResponseStatus.CANCELLED -> {
                            _navigationTarget.postValue(NavigationDestinations.USER_CANCEL_REQUEST)
                        }
                        null -> {
                            _navigationTarget.postValue(NavigationDestinations.TRANSACTION_FAILED())
                        }
                    }
                }, {
                    if (it is WrongSystemTimeException) {
                        timeIssuesDetectedUseCase.execute(true)
                    }
                    _navigationTarget.postValue(NavigationDestinations.TRANSACTION_FAILED())
                })
        )
    }

    private fun addTopUpToHistory() {
        compositeDisposable.addSafe(
            rideHistoryCollector.onPrePaidTopUp(
                ActivityAdditionalData.PrePaidAccountTopUp(
                    selectedAmount,
                    isInitialized = rideCoordinatorV3.getConfiguration()?.isAccountInitialized
                        ?: false
                )
            ).subscribe()
        )
    }

    private fun addTopUpToNotificationHistory() {
        compositeDisposable.addSafe(
            addToHistoryUseCase.execute(
                NotificationHistoryController.Type.CRM,
                "ride_history_top_up_amount_header",
                "ride_history_top_up_amount_android",
                contentExtraValue = selectedAmount
            ).subscribe()
        )
    }

    fun resetNavigation() {
        _navigationTarget.postValue(NavigationDestinations.NONE)
    }

    fun cancelTransaction() {
        // TODO: we should send info to backend
        // and then
        calledFinalize = true
        _navigationTarget.postValue(NavigationDestinations.TRANSACTION_CANCELLED)
    }

    sealed class NavigationDestinations {
        object NONE : NavigationDestinations()
        object TRANSACTION_CANCELLED : NavigationDestinations()
        object TRANSACTION_SUCCESS : NavigationDestinations()
        object TRANSACTION_SESSION_EXPIRED : NavigationDestinations()
        data class TRANSACTION_FAILED(val title: String? = null, val content: String? = null) :
            NavigationDestinations()

        object USER_CANCEL_REQUEST : NavigationDestinations()
    }
}