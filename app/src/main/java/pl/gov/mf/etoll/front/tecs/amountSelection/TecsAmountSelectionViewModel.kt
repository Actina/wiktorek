package pl.gov.mf.etoll.front.tecs.amountSelection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.commons.CurrencyUtils.toAmount
import pl.gov.mf.etoll.core.CoreComposedUC
import pl.gov.mf.etoll.core.NetworkManagerUC
import pl.gov.mf.etoll.core.devicecompatibility.DeviceCompatibilityUC
import pl.gov.mf.etoll.core.model.CoreAccountInfo
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.front.tecs.amountSelection.AmountOrderKey.*
import pl.gov.mf.etoll.networking.api.model.ApiTecsOpenTransactionResponseError
import pl.gov.mf.etoll.networking.api.model.ApiTecsOpenTransactionResponseStatus
import pl.gov.mf.mobile.networking.api.interceptors.WrongSystemTimeException
import pl.gov.mf.mobile.utils.addSafe
import java.util.*
import javax.inject.Inject

class TecsAmountSelectionViewModel : BaseDatabindingViewModel() {

    @Inject
    lateinit var rideCoordinatorV3: RideCoordinatorV3

    @Inject
    lateinit var openTransactionUseCase: NetworkManagerUC.OpenTransactionUseCase

    @Inject
    lateinit var timeIssuesDetectedUseCase: CoreComposedUC.TimeIssuesDetectedUseCase

    @Inject
    lateinit var checkAutoTimeEnabledUseCase: DeviceCompatibilityUC.CheckAutoTimeEnabledUseCase

    private val _loadingStatus: MutableLiveData<Boolean> = MutableLiveData(false)
    val loadingStatus: LiveData<Boolean>
        get() = _loadingStatus

    private val _navigationTarget: MutableLiveData<NavigationTarget> =
        MutableLiveData(NavigationTarget.NONE)
    val navigationTarget: LiveData<NavigationTarget>
        get() = _navigationTarget

    var browserUrl: String? = null

    private var _selectedAmount: MutableLiveData<Double> = MutableLiveData(0.0)
    val selectedAmount: LiveData<Double> = _selectedAmount

    val accountInfo: CoreAccountInfo
        get() = rideCoordinatorV3.getConfiguration()!!.tolledConfiguration!!.vehicle!!.accountInfo

    val possibleAmountValues: EnumMap<AmountOrderKey, PossibleAmount>
        get() = rideCoordinatorV3.getConfiguration()!!.tolledConfiguration!!.vehicle!!.let {
            EnumMap<AmountOrderKey, PossibleAmount>(AmountOrderKey::class.java).apply {
                if (it.lowTopUpFlag) {
                    put(LOWEST, PossibleAmount("20"))
                    put(LOW_MIDDLE, PossibleAmount("50"))
                    put(HIGH_MIDDLE, PossibleAmount("100"))
                    put(HIGHEST, PossibleAmount("200"))
                } else {
                    put(LOWEST, PossibleAmount("120"))
                    put(LOW_MIDDLE, PossibleAmount("200"))
                    put(HIGH_MIDDLE, PossibleAmount("300"))
                    put(HIGHEST, PossibleAmount("500"))
                }
            }
        }

    val isAmountValid: LiveData<Boolean> = Transformations.map(_selectedAmount) { newAmount ->
        try {
            possibleAmountValues[LOWEST]?.amount?.toDouble()?.let { lowestAmount ->
                if (newAmount < lowestAmount)
                    return@map false
            }
            return@map true
        } catch (_: Exception) {
            return@map false
        }
    }

    override fun onResume() {
        super.onResume()
        if (!checkAutoTimeEnabledUseCase.execute()) {
            timeIssuesDetectedUseCase.execute(forceShow = true)
            _navigationTarget.postValue(NavigationTarget.BACK)
        }
    }

    fun onContinueClick() {
        isAmountValid.value?.let {
            if (it) startTransaction()
        }
    }

    fun onToolbarCrossClick() {
        _navigationTarget.postValue(NavigationTarget.USER_CANCEL_REQUEST)
    }

    fun updateSelectedAmount(amount: String) {
        try {
            _selectedAmount.value = amount.toAmount()
        } catch (_: Exception) {
            // do nothing
        }
    }

    private fun startTransaction(): Boolean {
        val configuration = rideCoordinatorV3.getConfiguration()!!
        val accountId = configuration.tolledConfiguration?.vehicle?.accountInfo?.id
        val category = configuration.tolledConfiguration?.vehicle?.category
        val returnUrl = "etoll://finished"
        accountId?.let {
            _loadingStatus.postValue(true)
            compositeDisposable.addSafe(
                openTransactionUseCase.execute(
                    accountId,
                    selectedAmount.value!!,
                    returnUrl,
                    category!!
                ).subscribe({
                    _loadingStatus.postValue(false)
                    when (ApiTecsOpenTransactionResponseStatus.fromString(it.status)) {
                        ApiTecsOpenTransactionResponseStatus.SUCCESS -> {
                            // transaction was succeed, continue with new url
                            browserUrl = it.url
                            _navigationTarget.postValue(NavigationTarget.FORWARD)
                        }
                        ApiTecsOpenTransactionResponseStatus.FAILURE -> {
                            _navigationTarget.postValue(
                                NavigationTarget.ERROR(
                                    it.errorHeader,
                                    it.errorContent,
                                    ApiTecsOpenTransactionResponseError.fromString(it.validationError)
                                )
                            )
                        }
                        ApiTecsOpenTransactionResponseStatus.CANCELLED -> {
                            // do nothing different, it's not a case for this flow, but we need to support it
                            _navigationTarget.postValue(NavigationTarget.ERROR())
                        }
                        null -> {
                            _navigationTarget.postValue(NavigationTarget.ERROR())
                        }
                    }
                }, {
                    if (it is WrongSystemTimeException) {
                        timeIssuesDetectedUseCase.execute(true)
                    }
                    _navigationTarget.postValue(NavigationTarget.ERROR())
                    _loadingStatus.postValue(false)
                })
            )
        }
        return (accountId != null)
    }

    override fun onPause() {
        super.onPause()
        _loadingStatus.postValue(false)
    }

    fun resetNavigation() {
        _navigationTarget.postValue(NavigationTarget.NONE)
    }

    sealed class NavigationTarget {
        object NONE : NavigationTarget()
        object BACK : NavigationTarget()
        object FORWARD : NavigationTarget()
        object USER_CANCEL_REQUEST : NavigationTarget()
        data class ERROR(
            val title: String? = null,
            val content: String? = null,
            val errorType: ApiTecsOpenTransactionResponseError? = null,
        ) : NavigationTarget()
    }

}

