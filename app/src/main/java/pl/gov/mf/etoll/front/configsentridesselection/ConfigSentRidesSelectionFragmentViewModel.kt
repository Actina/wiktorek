package pl.gov.mf.etoll.front.configsentridesselection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.commons.Constants
import pl.gov.mf.etoll.core.model.CoreSent
import pl.gov.mf.etoll.core.model.CoreSentMap
import pl.gov.mf.etoll.front.config.RideConfigurationCoordinator
import pl.gov.mf.etoll.front.config.SentListData
import pl.gov.mf.etoll.front.configsentridesselection.adapter.OnSentRideItemListener
import pl.gov.mf.etoll.front.configsentridesselection.adapter.SentRideItem
import pl.gov.mf.etoll.front.configsentridesselection.details.SentMapDetailsData
import pl.gov.mf.mobile.utils.containsOnly
import pl.gov.mf.mobile.utils.toObject
import javax.inject.Inject

class ConfigSentRidesSelectionFragmentViewModel : BaseDatabindingViewModel(), OnSentRideItemListener {
    @Inject
    lateinit var configurationCoordinator: RideConfigurationCoordinator

    private val selectedRides = mutableListOf<SentRideItem.SentRideContent>()
    private val _confirmButtonEnabled = MutableLiveData<Boolean>()
    val confirmButtonEnabled: LiveData<Boolean> = _confirmButtonEnabled

    private val _monitoringByApp = MutableLiveData<Boolean>()
    val monitoringByApp: LiveData<Boolean> = _monitoringByApp

    private val _navigate: MutableLiveData<SentRidesNavigation> =
        MutableLiveData(SentRidesNavigation(SentRidesNavigation.Location.IDLE))
    val navigate: LiveData<SentRidesNavigation> = _navigate

    private val _sentRides = MutableLiveData<List<SentRideItem>>()
    val sentRides: LiveData<List<SentRideItem>> = _sentRides

    private val _backShouldBeVisible = MutableLiveData(true)
    val backShouldBeVisible: LiveData<Boolean> = _backShouldBeVisible

    override fun onCreate() {
        val data = configurationCoordinator.generateViewDataForSentRidesSelection()
        _monitoringByApp.value = data.monitoringByApp
        _confirmButtonEnabled.value = data.monitoringByApp
        _sentRides.value = initSentRides(data)
    }

    override fun onResume() {
        super.onResume()
        configurationCoordinator.onViewShowing(RideConfigurationCoordinator.RideConfigurationDestination.SENT_LIST)
    }

    private fun initSentRides(data: SentListData): MutableList<SentRideItem> {
        val coreSentMap: CoreSentMap = data.sentList
        val result = mutableListOf<SentRideItem>()
        _backShouldBeVisible.postValue(!data.backButtonShouldBeHidden)
        coreSentMap.data?.let { data ->
            data.keys
                .sortedWith { o1, o2 ->
                    if (o2 == Constants.SENT_GROUP_OTHER) -1 else o1.compareTo(
                        o2
                    )
                }
                .forEach { groupName ->
                    data[groupName]?.let { sentRides ->
                        sentRides.sortedBy { it.sentNumber }
                        result.add(SentRideItem.SentRideHeader(groupName))
                        result.addAll(sentRides.map { sent ->
                            createSentRideContent(sent, groupName)
                        })
                    }
                }
        }

        return result
    }

    private fun createSentRideContent(sent: CoreSent, group: String): SentRideItem.SentRideContent {
        return SentRideItem.SentRideContent(
            SentRideData(sent, initSentItemEnabledState(group)),
            group,
            this
        )
    }

    fun onConfirmClick() {
        val selectedObeId = if (monitoringByApp.value == false) selectedRides[0].group else null
        configurationCoordinator.onSentPackagesSelected(
            selectedObeId,
            selectedRides.map { it.data.item }.toMutableList()
        )
        _navigate.postValue(configurationCoordinator.getNextStep().map())
        _navigate.value = SentRidesNavigation(SentRidesNavigation.Location.FINISH)
    }

    private fun updateConfirmButtonState() {
        _confirmButtonEnabled.postValue(monitoringByApp.value == true || (monitoringByApp.value == false && selectedRides.size > 0))
    }

    override fun onSentItemClick(item: SentRideItem.SentRideContent) {
        selectRide(item)
    }

    private fun selectRide(item: SentRideItem.SentRideContent) {
        if (item.data.checked)
            selectedRides.add(item)
        else
            selectedRides.remove(item)
        updateItemsState()
        updateConfirmButtonState()
    }

    override fun onSentItemInfoClick(item: SentRideItem.SentRideContent) {
        _navigate.value = SentRidesNavigation(
            SentRidesNavigation.Location.SENT_DETAILS,
            SentMapDetailsData(item.data.item, item.data.enabled, item.data.checked).toJSON()
        )
    }

    private fun initSentItemEnabledState(group: String): Boolean {
        return !(monitoringByApp.value == false && group == Constants.SENT_GROUP_OTHER)
    }

    private fun updateItemsState() {
        val enabledGroups =
            if (monitoringByApp.value == true) mutableSetOf(Constants.SENT_GROUP_OTHER) else mutableSetOf()
        enabledGroups.addAll(selectedRides.filter { it.data.checked }.map { it.group })

        sentRides.value?.forEach { item ->
            if (item is SentRideItem.SentRideContent) {
                when {
                    monitoringByApp.value == true && enabledGroups.containsOnly(Constants.SENT_GROUP_OTHER) ->
                        item.viewModel.updateEnabledState(true)
                    monitoringByApp.value == false && enabledGroups.isEmpty() ->
                        item.viewModel.updateEnabledState(item.group != Constants.SENT_GROUP_OTHER)
                    else ->
                        item.viewModel.updateEnabledState(enabledGroups.contains(item.group))
                }
            }
        }
    }

    data class SentRidesNavigation(val location: Location, val data: String = "") {
        enum class Location {
            IDLE,
            SENT_DETAILS,
            FINISH,
            ERROR,
        }
    }

    fun resetNavigation() {
        _navigate.value = SentRidesNavigation(SentRidesNavigation.Location.IDLE)
    }

    private fun SentMapDetailsData.toSentData(): SentRideData {
        return SentRideData(item, enabled, checked)
    }

    fun onSentRideInfoResult(result: String) {
        val sentMapDetailsData = result.toObject<SentMapDetailsData>()
        val sendRideItem = findItem(sentMapDetailsData.toSentData())
        sendRideItem?.let {
            it.viewModel.updateCheckedState(true)
            selectRide(it)
        }
    }

    private fun findItem(sentRideData: SentRideData): SentRideItem.SentRideContent? {
        return sentRides.value?.find { it is SentRideItem.SentRideContent && it.data.item == sentRideData.item } as SentRideItem.SentRideContent?
    }
}

private fun RideConfigurationCoordinator.RideConfigurationDestination.map(): ConfigSentRidesSelectionFragmentViewModel.SentRidesNavigation =
    when (this) {
        RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH -> {
            pl.gov.mf.etoll.front.configsentridesselection.ConfigSentRidesSelectionFragmentViewModel.SentRidesNavigation(
                pl.gov.mf.etoll.front.configsentridesselection.ConfigSentRidesSelectionFragmentViewModel.SentRidesNavigation.Location.FINISH
            )
        }
        else -> {
            pl.gov.mf.etoll.front.configsentridesselection.ConfigSentRidesSelectionFragmentViewModel.SentRidesNavigation(
                pl.gov.mf.etoll.front.configsentridesselection.ConfigSentRidesSelectionFragmentViewModel.SentRidesNavigation.Location.IDLE
            )
        }
    }
