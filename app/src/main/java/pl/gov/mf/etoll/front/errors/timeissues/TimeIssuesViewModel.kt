package pl.gov.mf.etoll.front.errors.timeissues

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.core.devicecompatibility.DeviceCompatibilityUC
import pl.gov.mf.etoll.core.watchdog.timeissues.TimeIssuesController
import javax.inject.Inject

class TimeIssuesViewModel : BaseDatabindingViewModel() {

    @Inject
    lateinit var timeIssuesController: TimeIssuesController

    @Inject
    lateinit var checkAutoTimeEnabledUseCase: DeviceCompatibilityUC.CheckAutoTimeEnabledUseCase

    private val _navigationTarget: MutableLiveData<NavigationTargets> =
        MutableLiveData(NavigationTargets.NONE)
    val navigationTarget: LiveData<NavigationTargets> = _navigationTarget

    override fun onResume() {
        super.onResume()
        if (checkAutoTimeEnabledUseCase.execute())
            _navigationTarget.value = NavigationTargets.FINISH
    }

    fun onFinishClicked() {
        timeIssuesController.onTimeIssueScreenShown()
        _navigationTarget.value = NavigationTargets.FINISH
    }

    fun onShowSettingsClicked() {
        _navigationTarget.value = NavigationTargets.SHOW_SETTINGS
    }

    fun resetNavigationTarget() {
        _navigationTarget.value = NavigationTargets.NONE
    }

    enum class NavigationTargets {
        NONE, SHOW_SETTINGS, FINISH
    }

}