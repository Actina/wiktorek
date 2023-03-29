package pl.gov.mf.etoll.demo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.front.watchdog.DemoListItem
import pl.gov.mf.etoll.front.watchdog.DemoWatchdog
import pl.gov.mf.mobile.utils.addSafe
import javax.inject.Inject

class DemoFragmentViewModel : BaseDatabindingViewModel() {

    @Inject
    lateinit var demoWatchdog: DemoWatchdog

    private var _data: MutableLiveData<List<DemoListItem>> = MutableLiveData()
    val data: LiveData<List<DemoListItem>>
        get() = _data

    override fun onResume() {
        super.onResume()
        compositeDisposable.addSafe(demoWatchdog.observeChanges().subscribe {
            _data.postValue(it)
        })
    }
}