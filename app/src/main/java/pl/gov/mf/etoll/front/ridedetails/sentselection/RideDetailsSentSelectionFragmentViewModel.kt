package pl.gov.mf.etoll.front.ridedetails.sentselection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.commons.Constants
import pl.gov.mf.etoll.commons.TimeUtils.EMPTY_STRING
import pl.gov.mf.etoll.core.model.CoreSent
import pl.gov.mf.etoll.core.model.CoreSentMap
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorUC
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.RideCoordinatorV3
import pl.gov.mf.etoll.front.configsentridesselection.details.SentMapDetailsData
import pl.gov.mf.etoll.front.ridedetails.sentselection.adapter.OnActiveSentItemListener
import pl.gov.mf.etoll.front.ridedetails.sentselection.adapter.OnAvailableSentItemListener
import pl.gov.mf.etoll.front.ridedetails.sentselection.adapter.SentRideItem
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.utils.addSafe
import pl.gov.mf.mobile.utils.containsOnly
import pl.gov.mf.mobile.utils.deepCopy
import pl.gov.mf.mobile.utils.toObject
import javax.inject.Inject

class RideDetailsSentSelectionFragmentViewModel : BaseDatabindingViewModel(), OnActiveSentItemListener,
    OnAvailableSentItemListener {
    @Inject
    lateinit var readSettingsUseCase: SettingsUC.ReadSettingsUseCase

    @Inject
    lateinit var rideCoordinatorV3: RideCoordinatorV3

    @Inject
    lateinit var getRideCoordinatorConfigurationUseCase: RideCoordinatorUC.GetRideCoordinatorConfigurationUseCase

    private var monitoringByApp = false
    private var sentRides = mutableListOf<SentRideItem>()
    private var activeSentMap = HashMap<String, MutableList<CoreSent>>()
    private var checkedSentList = mutableListOf<RideDetailsSentData>()
    private var allSentMap: Map<String, List<CoreSent>> = emptyMap()

    private val _viewState: MutableLiveData<ViewState> = MutableLiveData(ViewState.Nothing)
    val viewState: LiveData<ViewState> = _viewState
    private val _activateButtonEnabled = MutableLiveData(false)
    val activateButtonEnabled: LiveData<Boolean> = _activateButtonEnabled

    val shouldFinishRideAfterActionOnList: Boolean
        get() = !monitoringByApp && rideCoordinatorV3.getConfiguration()?.sentConfiguration?.selectedSentList?.size == 1

    override fun onCreate() {
        monitoringByApp =
            rideCoordinatorV3.getConfiguration()?.monitoringDeviceConfiguration?.monitoringByApp
                ?: false
        allSentMap = initSentMap()
        activeSentMap = initActiveSentMap()

        updateSentRides()
    }

    private fun initSentMap(): Map<String, List<CoreSent>> =
        rideCoordinatorV3.getConfiguration()!!.sentConfiguration!!
            .availableSentList
            .data
            ?.toSortedMap { group1, group2 -> compareGroups(group1, group2) }
            ?.mapValues {
                it.value.sortedBy { data -> data.sentNumber }
            } ?: emptyMap()

    private fun initActiveSentMap(): HashMap<String, MutableList<CoreSent>> {
        val activeSentList = rideCoordinatorV3.getConfiguration()!!.sentConfiguration!!
            .selectedSentList
        val result = HashMap<String, MutableList<CoreSent>>()

        activeSentList.forEach {
            val group = findSentGroup(it)

            if (!result.containsKey(group))
                result[group] = mutableListOf()
            result[group]!!.add(it)
        }
        return result
    }

    private fun findSentGroup(sent: CoreSent): String {
        allSentMap.forEach { entry ->
            if (entry.value.any { it.sentNumber == sent.sentNumber })
                return entry.key
        }

        return EMPTY_STRING
    }

    private fun updateSentRides() {
        val result = mutableListOf<SentRideItem>()

        result.add(SentRideItem.SentHeader("ride_details_sent_selection_section_active"))

        if (activeSentMap.isEmpty())
            result.add(SentRideItem.EmptyActiveGroup)

        activeSentMap.keys
            .sortedWith { group1, group2 -> compareGroups(group1, group2) }
            .forEach { groupName ->
                result.add(SentRideItem.SentGroup(groupName))
                result.addAll(activeSentMap[groupName]!!
                    .sortedBy { it.sentNumber }
                    .map { createActiveSentContent(it, groupName) })
            }

        result.add(SentRideItem.SentHeader("ride_details_sent_selection_section_available"))
        allSentMap.keys
            .forEach { groupName ->
                allSentMap[groupName]!!
                    .filter { !it.isActive(groupName) }       // filtering duplicated elements in available and active sents
                    .takeIf { it.isNotEmpty() }     // dont show empty groups in available section
                    ?.map { sent ->
                        createAvailableSentContent(
                            item = sent,
                            enabled = itemShouldBeEnabled(groupName),
                            checked = checkedSentList.containsSent(sent),
                            group = groupName
                        )
                    }
                    ?.apply {
                        result.add(SentRideItem.SentGroup(groupName))
                        result.addAll(this)
                    }
            }

        sentRides = result
        _viewState.postValue(ViewState.ListUpdate(result))
    }

    private fun compareGroups(group1: String, group2: String) =
        if (group2 == Constants.SENT_GROUP_OTHER) AS_LAST_ITEM else group1.compareTo(group2)

    private fun createActiveSentContent(
        sent: CoreSent,
        group: String
    ): SentRideItem.ActiveSentContent {
        return SentRideItem.ActiveSentContent(
            RideDetailsSentData(sent, enabled = true, checked = true)
                .apply { this.group = group })
            .apply {
                listener = this@RideDetailsSentSelectionFragmentViewModel
                duringRide = isRideInProgress()
            }
    }

    private fun createAvailableSentContent(
        item: CoreSent,
        enabled: Boolean,
        checked: Boolean,
        group: String
    ): SentRideItem.AvailableSentContent {
        return SentRideItem.AvailableSentContent(
            RideDetailsSentData(item, enabled, checked)
                .apply { this.group = group })
            .apply { listener = this@RideDetailsSentSelectionFragmentViewModel }
    }

    override fun onSentItemClick(item: SentRideItem.AvailableSentContent) {
        if (itemShouldBeEnabled(item.data.group)) {
            item.select()
            if (item.data.checked)
                checkedSentList.add(item.data)
            else
                checkedSentList.removeAll { it.item == item.data.item }
            _activateButtonEnabled.postValue(checkedSentList.isNotEmpty())
            updateSentRides()
        }
    }

    private fun startSent(item: RideDetailsSentData) {
        if (!activeSentMap.containsKey(item.group))
            activeSentMap[item.group] = mutableListOf()
        activeSentMap[item.group]!!.add(item.item)

        // get config
        val config = rideCoordinatorV3.getConfiguration()!!.deepCopy()
        // update
        config.sentConfiguration!!.selectedSentList.add(item.item)
        // save
        rideCoordinatorV3.onConfigurationUpdated(config)
    }

    fun finishSent(item: CoreSent) {
        // get config
        val config = rideCoordinatorV3.getConfiguration()!!.deepCopy()
        // update config
        for (i in config.sentConfiguration!!.selectedSentList.indices)
            if (config.sentConfiguration!!.selectedSentList[i].sentNumber.contentEquals(item.sentNumber)) {
                config.sentConfiguration!!.selectedSentList.removeAt(i)
                break
            }
        config.sentConfiguration!!.finishedSentList.add(item)
        // should it be also removed from available sent list? If so - do it here
        config.sentConfiguration!!.availableSentList.remove(item)
        // save config
        rideCoordinatorV3.onConfigurationUpdated(config)

        activeSentMap.keys.forEach { key ->
            activeSentMap[key]!!.remove(item)
        }

        if (config.duringRide && !config.trackByApp && config.sentConfiguration!!.sentWithoutPackage) {
            // we just finished our last sent, this should be end of ride
            if (config.duringRide) {
                compositeDisposable.addSafe(rideCoordinatorV3.stopRide().subscribe({
                    // we don't wait for result, just finish ride
                    _viewState.postValue(ViewState.FinishedRide)
                }, {
                    // silent crash
                }))
            } else {
                rideCoordinatorV3.clearConfiguration()
                _viewState.postValue(ViewState.FinishedRide)
            }
        } else {
            allSentMap = initSentMap()
            activeSentMap = initActiveSentMap()
            updateSentRides()
        }

    }

    fun cancelSent(item: CoreSent) {
        // get config
        val config = rideCoordinatorV3.getConfiguration()!!.deepCopy()
        // update config
        for (i in config.sentConfiguration!!.selectedSentList.indices)
            if (config.sentConfiguration!!.selectedSentList[i].sentNumber.contentEquals(item.sentNumber)) {
                config.sentConfiguration!!.selectedSentList.removeAt(i)
                break
            }
        // save config
        rideCoordinatorV3.onConfigurationUpdated(config)

        if (!config.trackByApp && config.sentConfiguration!!.sentWithoutPackage) {
            // we just cancelled our last sent, this should be end of ride
            if (config.duringRide) {
                compositeDisposable.addSafe(rideCoordinatorV3.stopRide().subscribe({
                    // we don't wait for result, just finish ride
                    _viewState.postValue(ViewState.FinishedRide)
                }, {
                    // silent crash
                }))
            } else {
                rideCoordinatorV3.clearConfiguration()
                _viewState.postValue(ViewState.FinishedRide)
            }
        } else {
            removeActiveItem(item)
        }
    }

    private fun removeActiveItem(item: CoreSent) {
        activeSentMap.keys.forEach { key ->
            activeSentMap[key]!!.remove(item)
        }

        removeEmptyActiveGroups()
        updateSentRides()
    }

    override fun onAvailableItemDetailsClick(item: SentRideItem.AvailableSentContent) {
        sentDetails(item.data)
    }

    private fun itemShouldBeEnabled(group: String): Boolean {
        val activeGroups = activeSentMap.keys
        val selectedGroups = checkedSentList.map { it.group }.toSet()
        // ultimate param
        if (!monitoringByApp && group == Constants.SENT_GROUP_OTHER)
            return false
        return activeGroups.contains(group)
                || checkedSentList.map { it.group }.contains(group)
                || (group == Constants.SENT_GROUP_OTHER && monitoringByApp)
                || (activeGroups.isEmpty() && checkedSentList.isEmpty())
                || (monitoringByApp
                && activeGroups.containsOnly(Constants.SENT_GROUP_OTHER)
                && (selectedGroups.containsOnly(Constants.SENT_GROUP_OTHER) || selectedGroups.isEmpty()))
    }

    fun resetNavigation() {
        _viewState.postValue(ViewState.Nothing)
    }

    private fun SentMapDetailsData.toSentData(): RideDetailsSentData {
        return RideDetailsSentData(item, enabled, checked)
    }

    fun onSentRideInfoResult(result: String) {
        val sentMapDetailsData = result.toObject<SentMapDetailsData>()
        val sendRideItem = findItem(sentMapDetailsData.toSentData())
        sendRideItem?.let {
            sendRideItem.select()
            checkedSentList.add(it.data)
            _activateButtonEnabled.postValue(checkedSentList.isNotEmpty())
            updateSentRides()
        }
    }

    private fun findItem(sentRideData: RideDetailsSentData): SentRideItem.AvailableSentContent? {
        return sentRides.find { it is SentRideItem.AvailableSentContent && it.data.item == sentRideData.item } as SentRideItem.AvailableSentContent?
    }

    private fun removeEmptyActiveGroups() {
        activeSentMap
            .filter { it.value.isEmpty() }
            .keys
            .forEach {
                activeSentMap.remove(it)
            }
    }

    override fun onActiveItemDetailsClick(item: RideDetailsSentData) {
        sentDetails(item)
    }

    override fun onActiveItemCancelClick(item: RideDetailsSentData) {
        _viewState.postValue(ViewState.ShowCancelSentDialog(item.item))
    }

    override fun onActiveItemFinishClick(item: RideDetailsSentData) {
        _viewState.postValue(ViewState.ShowEndSentDialog(item.item))
    }

    private fun sentDetails(item: RideDetailsSentData) {
        val data = SentMapDetailsData(item.item, item.enabled, item.checked, true)
        _viewState.postValue(
            ViewState.Details(data.toJSON())
        )
    }

    private fun isRideInProgress(): Boolean =
        getRideCoordinatorConfigurationUseCase.execute()?.duringRide ?: false

    fun onActivateClick() {
        checkedSentList.forEach {
            it.checked = false
            startSent(it)
        }

        checkedSentList.clear()
        _activateButtonEnabled.postValue(checkedSentList.isNotEmpty())
        updateSentRides()
    }

    private fun CoreSentMap.remove(item: CoreSent) {
        data?.let {
            for (key in it.keys) {
                for (i in it.getValue(key).indices) {
                    if (it.getValue(key)[i].sentNumber.contentEquals(item.sentNumber)) {
                        it.getValue(key).removeAt(i)
                        break
                    }
                }
            }
        }
    }

    private fun List<RideDetailsSentData>.containsSent(item: CoreSent) =
        find { it.item == item } != null

    private fun CoreSent.isActive(group: String) =
        activeSentMap.containsKey(group) && activeSentMap[group]!!.contains(this)

    sealed class ViewState {
        object Nothing : ViewState()
        object FinishedRide : ViewState()
        data class ShowCancelSentDialog(val item: CoreSent) : ViewState()
        data class ShowEndSentDialog(val item: CoreSent) : ViewState()
        data class Details(val data: String) : ViewState()
        data class ListUpdate(val data: List<SentRideItem>) : ViewState()
    }

    companion object {
        private const val AS_LAST_ITEM = -1
    }
}

