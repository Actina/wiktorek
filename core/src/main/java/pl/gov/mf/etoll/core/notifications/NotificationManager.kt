package pl.gov.mf.etoll.core.notifications

import android.app.Notification
import android.content.Intent
import com.google.firebase.messaging.RemoteMessage

interface NotificationManager {

    companion object {
        const val CRM_ACTIVATED_NOTIFICATION_ID: Int = 3
        const val GPS_NOT_ACCESSIBLE_NOTIFICATION_ID: Int = 4
        const val SENT_ONLINE_CONFIGURATION_NOTIFICATION_ID: Int = 5
        const val FOREGROUND_NOTIFICATION_ID: Int = 121
        const val ALTERNATE_DURING_RIDE_NOTIFICATION_ID: Int = 122
        const val LOCATION_ISSUES_NOTIFICATION_ID: Int = 199
        const val TIME_ISSUES_NOTIFICATION_ID: Int = 198
        const val UNSENT_DATA_NOTIFICATION_ID: Int = 197
        const val FAKE_GPS_NOTIFICATION_ID: Int = 200
        const val CRITICAL_MESSAGES_STARTING_NOTIFICATION_ID: Int = 201
        const val BATTERY_OPTIMIZATION_COUNTER_NOTIFICATION_ID: Int = 500
    }

    /**
     * Create required notification channels on android >= oreo
     */
    fun createNotificationChannels()

    /**
     * Create or update notification for foreground service
     */
    fun createForegroundServiceNotification(
        additionalData: List<String>,
        actionOnClick: Intent
    ): Notification

    fun clearNotifications()

    fun createCriticalMessageNotification(id: Int, untranslatedBody: String)

    fun clearCriticalNotification(id: Int)

    fun createNotificationForCrmActivatedForFirstTime(notificationMsg: RemoteMessage.Notification)

    fun createNotificationForGpsNotAccessible()

    fun createNotificationForAirplaneModeIsOn()

    fun createNotificationForSentOnlineConfigurationAvailable()

    fun createNotificationForBatteryOptimizationEnabled()

    fun createNotificationForFakeGps()

    /**
     * Alternate notification for foreground service
     */
    fun createDuringRideNotification()

    /**
     * Check if foreground service notification or alternate notification is visible
     */
    fun isAnyDuringRideNotificationVisible(): Boolean

    fun createNotificationForGpsIssuesDetected()

    fun createNotificationForTimeIssuesDetected()

    fun createNotificationForUnsentData()
}