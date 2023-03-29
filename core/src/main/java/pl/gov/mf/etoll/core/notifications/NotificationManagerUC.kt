package pl.gov.mf.etoll.core.notifications

sealed class NotificationManagerUC {

    class CreateNotificationsChannelsUseCase(private val ds: NotificationManager) :
        NotificationManagerUC() {
        fun execute() {
            ds.createNotificationChannels()
        }
    }

}