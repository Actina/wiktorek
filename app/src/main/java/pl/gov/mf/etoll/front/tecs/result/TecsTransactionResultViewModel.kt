package pl.gov.mf.etoll.front.tecs.result

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.commons.CurrencyUtils.formatAccountValue
import pl.gov.mf.mobile.utils.translate

class TecsTransactionResultViewModel : BaseDatabindingViewModel() {

    private val _navigationTarget = MutableLiveData(NavigationTarget.NONE)
    val navigationTarget: LiveData<NavigationTarget>
        get() = _navigationTarget

    private val _selectedAmount = MutableLiveData("0.00")
    val selectedAmount: LiveData<String>
        get() = _selectedAmount

    private val _transactionErrorTitle = MutableLiveData<String>()
    val transactionErrorTitle: LiveData<String>
        get() = _transactionErrorTitle

    private val _transactionErrorContent = MutableLiveData<String>()
    val transactionErrorContent: LiveData<String>
        get() = _transactionErrorContent

    private val _status = MutableLiveData<Boolean>()
    val transactionStatus: LiveData<Boolean>
        get() = _status

    fun onToolbarCrossClick() {
        _navigationTarget.postValue(NavigationTarget.DASHBOARD)
    }

    fun onTryAgainClick() {
        _navigationTarget.postValue(NavigationTarget.AMOUNT_SELECTION)
    }

    fun interpretInput(arguments: Bundle, contextForTranslations: Context) {
        _selectedAmount.postValue(arguments.getString("amount")!!.formatAccountValue())
        _status.postValue(arguments.getBoolean("statusCorrect"))

        _transactionErrorTitle.postValue(getErrorTitle(arguments, contextForTranslations))
        _transactionErrorContent.postValue(getErrorContent(arguments, contextForTranslations))
    }

    private fun getErrorContent(arguments: Bundle, contextForTranslations: Context): String {
        val argumentsErrorContent: String = arguments.getString("errorContent") ?: ""

        return if (argumentsErrorContent.isEmpty())
            "payment_completed_failure_content".translate(contextForTranslations)
        else
            argumentsErrorContent
    }

    private fun getErrorTitle(arguments: Bundle, contextForTranslations: Context): String {
        val argumentsErrorTitle: String = arguments.getString("errorTitle") ?: ""

        return if (argumentsErrorTitle.isEmpty())
            "payment_completed_failure_header".translate(contextForTranslations)
        else
            argumentsErrorTitle
    }

    fun resetNavigation() {
        _navigationTarget.postValue(NavigationTarget.NONE)
    }

    enum class NavigationTarget {
        NONE, DASHBOARD, AMOUNT_SELECTION
    }
}