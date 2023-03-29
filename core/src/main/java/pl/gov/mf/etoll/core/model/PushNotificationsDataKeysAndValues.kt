package pl.gov.mf.etoll.core.model

enum class NotificationKeys(val key: String) {
    ACTION("action"),
    ASSIGNED("assigned"),
}

enum class NotificationKeysValues(val value: String) {
    ACTION_VALUE_UPDATE_STATUS("update_status"),
    ASSIGNED_VALUE_TRUE("1")
}