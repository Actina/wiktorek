package pl.gov.mf.etoll.front.notificationhistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.joda.time.DateTime
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.etoll.core.crmmessages.CrmMessagesManager
import pl.gov.mf.etoll.core.model.CoreMessage
import pl.gov.mf.etoll.front.notificationhistory.list.NotificationHistoryItem
import pl.gov.mf.etoll.front.notificationhistory.list.NotificationHistoryItemOnClickListener
import pl.gov.mf.etoll.storage.database.notifications.model.NotificationHistoryItemModel
import pl.gov.mf.mobile.utils.addSafe
import javax.inject.Inject

class NotificationHistoryFragmentViewModel : BaseDatabindingViewModel(),
    NotificationHistoryItemOnClickListener, CrmMessagesManager.Callbacks {

    @Inject
    lateinit var getNotificationsHistoryUseCase: NotificationHistoryFragmentUC.GetNotificationsHistoryUseCase

    @Inject
    lateinit var clearNotificationsHistoryUseCase: NotificationHistoryFragmentUC.ClearNotificationsHistoryUseCase

    @Inject
    lateinit var deleteNotificationsUseCase: NotificationHistoryFragmentUC.DeleteNotificationsUseCase

    @Inject
    lateinit var observeNewNotificationsUseCase: NotificationHistoryFragmentUC.ObserveNewNotificationsUseCase

    private val _items = MutableLiveData<List<NotificationHistoryItem>>(emptyList())
    val items: LiveData<List<NotificationHistoryItem>> = _items

    private val _selectedMode = MutableLiveData(false)
    val selectedMode: LiveData<Boolean> = _selectedMode

    private val _navigation = MutableLiveData<NavigationTarget>(NavigationTarget.None)
    val navigation: LiveData<NavigationTarget> = _navigation

    private var lastNotificationListSize = -1

    override fun onResume() {
        super.onResume()
        fetchNotifications()
        observeNewNotificationsUseCase.startObservation(this)
    }

    override fun onPause() {
        super.onPause()
        observeNewNotificationsUseCase.stopObservation()
    }

    private fun fetchNotifications() {
        compositeDisposable.addSafe(
            getNotificationsHistoryUseCase.execute()
                .subscribe(
                    { prepareList(it) },
                    { /* error handling ? */ }
                )
        )
    }

    override fun onItemClicked(item: NotificationHistoryItemModel) {
        _navigation.value = NavigationTarget.ItemDetails(item)
    }

    fun resetNavigation() {
        _navigation.value = NavigationTarget.None
    }

    private fun prepareList(items: List<NotificationHistoryItemModel>) {
        lastNotificationListSize = items.size
        val output = mutableListOf<NotificationHistoryItem>()
        var lastKnownDate = DateTime(0L)

        for (i in items.indices) {
            val newDate = DateTime(items[i].timestamp)

            if (newDate.isDatesDifferent(lastKnownDate)) {
                lastKnownDate = newDate
                output.add(
                    NotificationHistoryItem.HeaderItem(
                        TimeUtils.DefaultDateFormatterForNotificationsHistory.print(newDate)
                    )
                )
            }

            output.add(NotificationHistoryItem.DataItem(items[i], this))
        }
        _items.value = output
    }

    fun enableSelectMode(enabled: Boolean) {
        items.value?.forEach {
            if (it is NotificationHistoryItem.DataItem) {
                it.enableSelectMode(enabled)
                it.setSelected(false)
            }
        }

        _items.value = _items.value
        _selectedMode.value = enabled
    }

    private fun DateTime.isDatesDifferent(other: DateTime) =
        dayOfMonth() != other.dayOfMonth()
                || monthOfYear() != other.monthOfYear()
                || year() != other.year()

    fun navigationIconClick() {
        if (selectedMode.value == true)
            enableSelectMode(false)
        else
            _navigation.value = NavigationTarget.Back
    }

    fun deleteSelectedItems() {
        val selectedItems = items.value
            ?.filter { it is NotificationHistoryItem.DataItem && it.isSelected() }
            ?.map { (it as NotificationHistoryItem.DataItem).data }
            ?: emptyList()

        if (selectedItems.isEmpty())
            enableSelectMode(false)

        compositeDisposable.addSafe(
            deleteNotificationsUseCase.execute(selectedItems)
                .subscribe(
                    {
                        enableSelectMode(false)
                        fetchNotifications()
                    },
                    { /* todo: error handling ? */ }
                )
        )
    }

    fun deleteAllItems() {
        compositeDisposable.addSafe(
            clearNotificationsHistoryUseCase.execute()
                .subscribe(
                    { fetchNotifications() },
                    { /* todo: error handling ? */ }
                )
        )
    }

    sealed class NavigationTarget {
        object None : NavigationTarget()
        object Back : NavigationTarget()
        data class ItemDetails(val data: NotificationHistoryItemModel) : NavigationTarget()
    }

    override fun onNewMessageToShow(message: CoreMessage) {
        compositeDisposable.addSafe(
            getNotificationsHistoryUseCase.execute()
                .subscribe(
                    {
                        if (it.size != lastNotificationListSize)
                            prepareList(it)
                    },
                    { /* error handling ? */ }
                )
        )
    }
}