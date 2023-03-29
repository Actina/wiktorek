package pl.gov.mf.etoll.core.notifications

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Builder
import com.google.firebase.messaging.RemoteMessage
import org.joda.time.DateTime
import pl.gov.mf.etoll.commons.TimeUtils
import pl.gov.mf.etoll.core.R
import pl.gov.mf.etoll.core.app.NkspoApplication
import pl.gov.mf.etoll.core.notifications.NotificationManager.Companion.ALTERNATE_DURING_RIDE_NOTIFICATION_ID
import pl.gov.mf.etoll.core.notifications.NotificationManager.Companion.BATTERY_OPTIMIZATION_COUNTER_NOTIFICATION_ID
import pl.gov.mf.etoll.core.notifications.NotificationManager.Companion.CRITICAL_MESSAGES_STARTING_NOTIFICATION_ID
import pl.gov.mf.etoll.core.notifications.NotificationManager.Companion.CRM_ACTIVATED_NOTIFICATION_ID
import pl.gov.mf.etoll.core.notifications.NotificationManager.Companion.FAKE_GPS_NOTIFICATION_ID
import pl.gov.mf.etoll.core.notifications.NotificationManager.Companion.FOREGROUND_NOTIFICATION_ID
import pl.gov.mf.etoll.core.notifications.NotificationManager.Companion.GPS_NOT_ACCESSIBLE_NOTIFICATION_ID
import pl.gov.mf.etoll.core.notifications.NotificationManager.Companion.LOCATION_ISSUES_NOTIFICATION_ID
import pl.gov.mf.etoll.core.notifications.NotificationManager.Companion.SENT_ONLINE_CONFIGURATION_NOTIFICATION_ID
import pl.gov.mf.etoll.core.notifications.NotificationManager.Companion.TIME_ISSUES_NOTIFICATION_ID
import pl.gov.mf.etoll.core.notifications.NotificationManager.Companion.UNSENT_DATA_NOTIFICATION_ID
import pl.gov.mf.etoll.translations.AppLanguageManager
import pl.gov.mf.mobile.utils.translate
import javax.inject.Inject


class NotificationManagerImpl @Inject constructor(
    private val applicationContext: Context,
    private val systemNotificationManager: android.app.NotificationManager,
    private val languageManager: AppLanguageManager,
) :
    NotificationManager {

    companion object {
        private const val CRITICAL_MESSAGES_CHANNEL_ID = "102"
        private const val CRITICAL_MESSAGES_CHANNEL_NAME = "102"
        private const val FOREGROUND_CHANNEL_ID = "101"
        private const val FOREGROUND_CHANNEL_NAME = "eToll"
    }

    /**
     * This function should create all required channels (with segregation, as we do not want
     * to have all stuff in just one
     */
    override fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            systemNotificationManager.createNotificationChannel(
                NotificationChannel(
                    FOREGROUND_CHANNEL_ID,
                    FOREGROUND_CHANNEL_NAME,
                    android.app.NotificationManager.IMPORTANCE_HIGH
                )
            )
            systemNotificationManager.createNotificationChannel(
                NotificationChannel(
                    CRITICAL_MESSAGES_CHANNEL_ID,
                    CRITICAL_MESSAGES_CHANNEL_NAME,
                    android.app.NotificationManager.IMPORTANCE_HIGH //TODO high or default?
                )
            )
        }
    }

    override fun createForegroundServiceNotification(
        additionalData: List<String>,
        actionOnClick: Intent,
    ): Notification {
        actionOnClick.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0, actionOnClick, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Builder(
            applicationContext,
            FOREGROUND_CHANNEL_ID
        )
            .setContentTitle("foreground_service_title".translate(applicationContext))
            .setContentText("foreground_service_content".translate(applicationContext))
            .setSmallIcon(R.drawable.ic_foregroundservice_icon)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    override fun createCriticalMessageNotification(
        id: Int,
        untranslatedBody: String,
    ) = showNotification(
        CRITICAL_MESSAGES_STARTING_NOTIFICATION_ID + id,
        CRITICAL_MESSAGES_CHANNEL_ID, "critical_messages_title".translate(applicationContext),
        untranslatedBody.translate(applicationContext)
    )

    override fun clearCriticalNotification(id: Int) {
        systemNotificationManager.cancel(CRITICAL_MESSAGES_STARTING_NOTIFICATION_ID + id)
    }

    @SuppressLint("CheckResult")
    override fun createNotificationForCrmActivatedForFirstTime(notificationMsg: RemoteMessage.Notification) {
        if (languageManager.isLoaded()) {
            proceedWithCreateNotificationForCrmActivatedForFirstTime(notificationMsg)
        } else {
            // load language manager and continue
            // we do not manage this disposable
            languageManager.load().subscribe({
                if (it)
                    proceedWithCreateNotificationForCrmActivatedForFirstTime(notificationMsg)
            }, {
                // do nothing
            })
        }
    }

    private fun proceedWithCreateNotificationForCrmActivatedForFirstTime(notificationMsg: RemoteMessage.Notification) =
        showNotification(
            CRM_ACTIVATED_NOTIFICATION_ID,
            CRITICAL_MESSAGES_CHANNEL_ID, notificationMsg.title ?: "", notificationMsg.body ?: ""
        )

    override fun clearNotifications() {
        systemNotificationManager.cancelAll()
    }

    override fun createNotificationForGpsNotAccessible() = showNotification(
        GPS_NOT_ACCESSIBLE_NOTIFICATION_ID, CRITICAL_MESSAGES_CHANNEL_ID,
        "critical_error_gps_disabled_header_android".translate(
            applicationContext
        ),

        "critical_error_gps_disabled_title_android".translate(
            applicationContext
        )
    )

    override fun createNotificationForAirplaneModeIsOn() = showNotification(
        GPS_NOT_ACCESSIBLE_NOTIFICATION_ID, CRITICAL_MESSAGES_CHANNEL_ID,
        "critical_error_airplane_mode_header_android".translate(
            applicationContext
        ),

        "critical_error_airplane_mode_title_android".translate(
            applicationContext
        )
    )

    override fun createNotificationForSentOnlineConfigurationAvailable() =
        showNotification(
            SENT_ONLINE_CONFIGURATION_NOTIFICATION_ID, CRITICAL_MESSAGES_CHANNEL_ID,
            "dialog_complete_current_ride_configuration_header".translate(
                applicationContext
            ),
            "dialog_complete_current_ride_configuration_content".translate(
                applicationContext
            )
        )

    private var batteryOptimizationNotificationCounter =
        BATTERY_OPTIMIZATION_COUNTER_NOTIFICATION_ID

    override fun createNotificationForBatteryOptimizationEnabled() = showNotification(
        batteryOptimizationNotificationCounter++,
        CRITICAL_MESSAGES_CHANNEL_ID,
        "Zmieniono ust. optymalizacji baterii",
        "Wł. opt. ${
            DateTime.now().toString(TimeUtils.DateTimeFormatterForRideDetails)
        }"
    )

    override fun createNotificationForFakeGps() = showNotification(
        FAKE_GPS_NOTIFICATION_ID,
        CRITICAL_MESSAGES_CHANNEL_ID,
        "notification_fakegps_title_android".translate(applicationContext),
        "notification_fakegps_content_android".translate(applicationContext)
    )

    override fun createDuringRideNotification() = showNotification(
        ALTERNATE_DURING_RIDE_NOTIFICATION_ID,
        FOREGROUND_CHANNEL_ID,
        "foreground_service_title".translate(applicationContext),
        "foreground_service_content".translate(applicationContext)
    )

    override fun isAnyDuringRideNotificationVisible(): Boolean {
        for (notification in systemNotificationManager.activeNotifications) {
            if (notification.id == FOREGROUND_NOTIFICATION_ID || notification.id == ALTERNATE_DURING_RIDE_NOTIFICATION_ID) {
                return true
            }
        }
        return false
    }

    override fun createNotificationForGpsIssuesDetected() = showNotification(
        LOCATION_ISSUES_NOTIFICATION_ID,
        CRITICAL_MESSAGES_CHANNEL_ID,
        "notification_gps_issues_title_android".translate(applicationContext),
        "notification_gps_issues_content_android".translate(applicationContext)
    )

    override fun createNotificationForTimeIssuesDetected() = showNotification(
        TIME_ISSUES_NOTIFICATION_ID,
        CRITICAL_MESSAGES_CHANNEL_ID,
        "notification_time_issues_title".translate(applicationContext),
        "notification_time_issues_content".translate(applicationContext)
    )

    override fun createNotificationForUnsentData() = showNotification(
        UNSENT_DATA_NOTIFICATION_ID,
        CRITICAL_MESSAGES_CHANNEL_ID,
        "sync_notification_title".translate(applicationContext),
        "sync_notification_content".translate(applicationContext)
    )

    private fun showNotification(id: Int, channelId: String, title: String, text: String) {
        // do not show again actually visible notification
        systemNotificationManager.activeNotifications.forEach {
            if (it.id == id) return
        }
        systemNotificationManager.notify(
            id, Builder(
                applicationContext,
                channelId
            ) //channel id is required for compatibility with Android 8.0 (api 26) and higher, but is ignored by older versions.
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_foregroundservice_icon)
                .setPriority(NotificationCompat.PRIORITY_HIGH) //to support Android 7.1 and lower, for android 8.0 and higher you must set the channel importance
                .setContentIntent(
                    PendingIntent.getActivity(
                        applicationContext,
                        0, (applicationContext as NkspoApplication).getForegroundActionIntent(),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true) //automatically removes notification when user taps it
                .build()
        )
    }
}