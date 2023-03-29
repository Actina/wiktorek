package pl.gov.mf.etoll.front.notificationdetails

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.joda.time.DateTime
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.etoll.front.notificationhistory.NotificationHistoryFragmentUC
import pl.gov.mf.etoll.translations.AppLanguageManagerUC
import pl.gov.mf.mobile.utils.addSafe
import pl.gov.mf.mobile.utils.translate
import javax.inject.Inject

class NotificationDetailsFragmentViewModel : BaseDatabindingViewModel() {

    @Inject
    lateinit var getNotificationUseCase: NotificationDetailsFragmentUC.GetNotificationUseCase

    @Inject
    lateinit var getCurrentLanguageUseCase: AppLanguageManagerUC.GetCurrentLanguageUseCase

    @Inject
    lateinit var deleteNotificationUseCase: NotificationHistoryFragmentUC.DeleteNotificationUseCase

    private val _formattedDate: MutableLiveData<String> = MutableLiveData()
    val formattedDate: LiveData<String> = _formattedDate

    private val _content: MutableLiveData<String> = MutableLiveData()
    val content: LiveData<String> = _content

    private val _title: MutableLiveData<String> = MutableLiveData()
    val title: LiveData<String> = _title

    private val _navigation = MutableLiveData<NavigationTarget>(NavigationTarget.None)
    val navigation: LiveData<NavigationTarget> = _navigation

    private var itemId: Long = -1L

    fun initialize(id: Long, contextForTranslations: Context) {
        itemId = id
        compositeDisposable.addSafe(getNotificationUseCase.execute(id).subscribe({
            if (it.payload == null) {
                _content.postValue(
                    if (it.payload == null)
                        it.content.translate(contextForTranslations, it.contentExtraValue)
                    else it.content
                )
                _title.postValue(
                    if (it.payload == null) it.title.translate(
                        contextForTranslations,
                        it.contentExtraValue
                    )
                    else it.title
                )
            } else {
                val msg = it.payload!!
                _content.postValue(msg.contents?.getForLanguage(getCurrentLanguageUseCase.execute()))
                _title.postValue(msg.headers?.getForLanguage(getCurrentLanguageUseCase.execute()))
            }
            // date is taken from "show" time, not creation time.
            _formattedDate.postValue(TimeUtils.DateTimeFormatterForRideDetails.print(DateTime(it.timestamp)))
        }, {

        }))
    }

    fun deleteItem() {
        if (itemId != -1L) {
            compositeDisposable.addSafe(
                deleteNotificationUseCase.execute(itemId)
                    .subscribe(
                        { _navigation.value = NavigationTarget.Back },
                        { /* error handling */ }
                    )
            )
        }
    }

    sealed class NavigationTarget {
        object None : NavigationTarget()
        object Back : NavigationTarget()
    }
}