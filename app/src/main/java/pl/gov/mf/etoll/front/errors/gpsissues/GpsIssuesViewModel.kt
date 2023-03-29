package pl.gov.mf.etoll.front.errors.gpsissues

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.core.ridecoordinatorv2.location.issues.LocationIssuesDetector
import javax.inject.Inject

class GpsIssuesViewModel : BaseDatabindingViewModel() {

    @Inject
    lateinit var locationIssuesDetector: LocationIssuesDetector

    private val _issuesLowBattery = MutableLiveData(false)
    val issuesLowBattery: LiveData<Boolean> = _issuesLowBattery
    private val _issuesCriticalLowBattery = MutableLiveData(false)
    val issuesCriticalLowBattery: LiveData<Boolean> = _issuesCriticalLowBattery
    private val _issuesCriticalLowBatteryButLoading = MutableLiveData(false)
    val issuesCriticalLowBatteryButLoading: LiveData<Boolean> = _issuesCriticalLowBatteryButLoading
    private val _issuesOverheat = MutableLiveData(false)
    val issuesOverheat: LiveData<Boolean> = _issuesOverheat
    private val _anyCriticalBatteryIssues = MutableLiveData(false)
    val anyCriticalBatteryIssues : LiveData<Boolean> = _anyCriticalBatteryIssues
    val shouldGoBack = MutableLiveData(false)

    fun setModel(model: GpsIssuesModel) {
        Log.d("LocIssues", "In-fragment Issues to show: $model")
        model.issues.forEach { issue ->
            when (issue) {
                LocationIssuesDetector.ISSUE_OVERHEAT -> {
                    _issuesOverheat.value = true
                }
                LocationIssuesDetector.ISSUE_CRITICAL_BATTERY_LOADING -> {
                    // hide basic low battery
                    if (_issuesCriticalLowBattery.value == true) {
                        _issuesCriticalLowBattery.value = false
                    }
                    // and second one
                    if (_issuesLowBattery.value == true) {
                        _issuesLowBattery.value = false
                    }
                    // and set up value
                    _anyCriticalBatteryIssues.value = true
                    _issuesCriticalLowBatteryButLoading.value = true
                }
                LocationIssuesDetector.ISSUE_CRITICAL_BATTERY -> {
                    // hide low battery
                    if (_issuesLowBattery.value == true) {
                        _issuesLowBattery.value = false
                    }
                    // don't show if we're showing already normaln battery info
                    if (_issuesCriticalLowBatteryButLoading.value != true) {
                        _anyCriticalBatteryIssues.value = true
                        _issuesCriticalLowBattery.value = true
                    }
                }
                LocationIssuesDetector.ISSUE_LOW_BATTERY -> {
                    if (_issuesCriticalLowBatteryButLoading.value != true && _issuesCriticalLowBattery.value != true)
                        _issuesLowBattery.value = true
                }
                LocationIssuesDetector.ISSUE_BACKGROUND -> {
                    // not used at this build
                }
            }
        }
    }

    fun onConfirmClick(){
        locationIssuesDetector.onIssueDealt()
        shouldGoBack.value = true
    }
}