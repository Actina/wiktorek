package pl.gov.mf.etoll.commons

object Constants {
    const val SENT_GROUP_OTHER = "other"

    val BATTERY_LEVEL_LOW_BORDER = 20f // 20%
    val BATTERY_LEVEL_HIGH_BORDER = 50f

    // time after which icon should change background to yellow
    val GPS_COLLECTOR_WARNING: Int = if (BuildConfig.DEBUG) 10 else 60

    // time after which icon should change background to red
    val GPS_COLLECTOR_ERROR: Int = if (BuildConfig.DEBUG) 15 else 60 * 3

    // time after which icon should change background to yellow
    val GPS_SENDER_WARNING: Int = if (BuildConfig.DEBUG) 30 else 60 * 5

    // time after which icon should change background to red
    val GPS_SENDER_ERROR: Int = if (BuildConfig.DEBUG) 45 else 60 * 10

    const val DASHBOARD_UI_CHECKS_UPDATER_TIMER_IN_SECS: Long = 15
    const val DASHBOARD_UI_TIMER_UPDATE_FREQUENCY_IN_MILLI_SECS: Long = 250
    const val ACCOUNT_BALANCE_LOW_BORDER: Double = 100.00

    const val DEFAULT_LATITUDE = 0.0
    const val DEFAULT_LONGITUDE = 0.0

    //Security
    const val PIN_MAX_INVALID_ATTEMPTS = 3
    const val PASSWORD_MAX_INVALID_ATTEMPTS = 3
    const val MAX_INACTIVITY_TIME_IN_BACKGROUND_MILLIS = 180000L //180s
    const val LOCK_TIME_OF_PIN_ATTEMPTS_MILLIS = 180000L //180s
    const val LOCK_TIME_OF_PASSWORD_ATTEMPTS_MILLIS = 180000L //180s
}