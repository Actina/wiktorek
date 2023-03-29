package pl.gov.mf.etoll.commons

import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import pl.gov.mf.mobile.utils.LocationWrapper

object TimeUtils {
    fun getCurrentTimeForEvents(): Long = System.currentTimeMillis() * 1000
    fun getCurrentTimeForEventsFromLocation(location: LocationWrapper): Long =
        location.time * 1000 // 13 + 3 zeroes

    fun convertTecsDates(tecsDateTime: String): String {
        val dateIn = TecsDateFormatter.parseDateTime(tecsDateTime)
        return dateIn.toString(DefaultDateTimeFormatterForBillingAccount)
    }

    fun getCurrentTimestampForApi(): String {
        return "${System.currentTimeMillis()}"
    }

    fun getCurrentTimestampMillis(): Long = System.currentTimeMillis()


    const val EMPTY_STRING: String = ""
    private const val TECS_OUTPUT_DATE_FORMAT = "yyyyMMddHHmmss" // 20210129004430
    private const val DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ"
    private const val DEFAULT_DATETIME_FORMAT_FROM_STATUS = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZ"
    private const val DEFAULT_DATETIME_FORMAT_UI = "dd.MM.yyyy HH:mm:ss"  //  "18.08.2020 12:14:30"
    private const val DEFAULT_DATE_FORMAT_UI = "dd.MM.yyyy"  //  "18.08.2020 12:14:30"
    private const val DEFAULT_DATETIME_FORMAT_RIDE_SUMMARY = "dd/MM/yyyy; HH:mm"
    private const val DEFAULT_DATETIME_FOR_RIDE_DETAILS = "HH:mm; dd.MM.yyyy"
    private const val DEFAULT_DATETIME_FOR_NOTIFICATIONS_HISTORY = "dd.MM.yyyy"
    private const val DEFAULT_DATETIME_FOR_NOTIFICATIONS_HISTORY_HOURS = "HH:mm"
    private const val DEFAULT_DATETIME_FOR_NOTIFICATIONS_HISTORY_HOURS_SECONDS = "HH:mm:ss"
    private const val RIDE_HISTORY_DATE_FORMAT = "dd/MM/yyyy"
    private const val RIDE_HISTORY_TIME_FORMAT = "HH:mm"

    private val TecsDateFormatter: DateTimeFormatter =
        DateTimeFormat.forPattern(TECS_OUTPUT_DATE_FORMAT)
    val DefaultDateTimeFormatterForBillingAccount: DateTimeFormatter = ISODateTimeFormat.dateTime()
    val DefaultDateTimeFormatter: DateTimeFormatter =
        DateTimeFormat.forPattern(DEFAULT_DATETIME_FORMAT)

    val DefaultDateTimeFormatterFromStatus: DateTimeFormatter = DateTimeFormat.forPattern(
        DEFAULT_DATETIME_FORMAT_FROM_STATUS
    )
    val DefaultDateTimeFormatterForUi: DateTimeFormatter = DateTimeFormat.forPattern(
        DEFAULT_DATETIME_FORMAT_UI
    )
    val DateTimeFormatterForRideSummaryView =
        DateTimeFormat.forPattern(DEFAULT_DATETIME_FORMAT_RIDE_SUMMARY)

    val DateTimeFormatterForRideDetails =
        DateTimeFormat.forPattern(DEFAULT_DATETIME_FOR_RIDE_DETAILS)

    val DefaultDateFormatterForUi = DateTimeFormat.forPattern(DEFAULT_DATE_FORMAT_UI)

    val DefaultDateFormatterForNotificationsHistory = DateTimeFormat.forPattern(
        DEFAULT_DATETIME_FOR_NOTIFICATIONS_HISTORY
    )

    val DefaultDateFormatterForNotificationsHistoryHours = DateTimeFormat.forPattern(
        DEFAULT_DATETIME_FOR_NOTIFICATIONS_HISTORY_HOURS
    )

    val DefaultDateFormatterForNotificationsHistoryHoursSeconds = DateTimeFormat.forPattern(
        DEFAULT_DATETIME_FOR_NOTIFICATIONS_HISTORY_HOURS_SECONDS
    )

    val RideHistoryDateFormatter = DateTimeFormat.forPattern(RIDE_HISTORY_DATE_FORMAT)
    val RideHistoryTimeFormatter = DateTimeFormat.forPattern(RIDE_HISTORY_TIME_FORMAT)
}

fun Long.formatMillisToReadableUiTime(): String? {
    if (this < 0) return null
    val secsDiff = this / 1000
    val secs = secsDiff % 60
    val mins = (secsDiff / 60) % 60
    val hours = (secsDiff / 3600)
    val output = StringBuilder()
    if (hours < 10)
        output.append("0$hours:")
    else
        output.append("$hours:")
    if (mins < 10)
        output.append("0$mins:")
    else
        output.append("$mins:")
    if (secs < 10)
        output.append("0$secs")
    else
        output.append("$secs")
    return output.toString()
}

fun Long.formatMillisToMinutesAndSeconds(): String {
    val millisAsSeconds = this / 1000
    val seconds = millisAsSeconds % 60
    val minutes = millisAsSeconds / 60 % 60
    return String.format("%d:%02d", minutes, seconds)
}