package pl.gov.mf.etoll.storage.database.notifications.model

enum class NotificationHistoryItemType {
    INFO,
    CRITICAL,
    GOOD,
    BAD,
    CRM;

    fun toInt(): Int {
        for (i in values().indices)
            if (this == values()[i])
                return i
        return 0
    }

    companion object {
        fun fromInt(value: Int): NotificationHistoryItemType {
            if (value < values().size)
                return values()[value]
            return INFO
        }
    }
}