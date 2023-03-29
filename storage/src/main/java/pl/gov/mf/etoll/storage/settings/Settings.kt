package pl.gov.mf.etoll.storage.settings

import pl.gov.mf.etoll.storage.settings.SettingsType.BOOLEAN
import pl.gov.mf.etoll.storage.settings.SettingsType.INT
import pl.gov.mf.etoll.storage.settings.SettingsType.LONG
import pl.gov.mf.etoll.storage.settings.SettingsType.STRING

enum class Settings(val type: SettingsType) {
    DEFAULT_INT(INT),
    DEFAULT_STRING(STRING),
    DEFAULT_BOOLEAN(BOOLEAN),
    APPLICATION_STATE(INT),
    TEST_COUNTER(INT),
    BUSINESS_NUMBER(STRING),
    REALM_KEY(STRING),
    API_KEYS(STRING),
    GENERATED_JWT_TOKEN(STRING),
    RECEIVED_ACCESS_TOKEN(STRING),
    ACTIVATION_TRANS_SUCCEED(BOOLEAN),
    SECURITY_TRANS_SUCCEED(BOOLEAN),
    STATUS(STRING),
    SUPPORT_FOR_SENT_ENABLED(BOOLEAN),
    RIDE_START_TIMESTAMP(STRING),
    RIDE_END_TIMESTAMP(STRING),
    RECENT_VEHICLES(STRING),
    RIDE_COORDINATOR_STATUS(INT),
    DEVICE_ID(STRING),
    FIREBASE_ID(STRING),
    VIBRATION_NOTIFICATIONS(BOOLEAN),
    SOUND_NOTIFICATIONS(BOOLEAN),
    RIDE_COORDINATOR_CONFIGURATION(STRING),
    APP_IN_FOREGROUND(BOOLEAN),
    RIDE_SUMMARY_SHOULD_BE_SHOWN(BOOLEAN),
    LAST_CORRECT_DATA_SENDING_TIMESTAMP(STRING),
    PIN(STRING),
    PASSWORD(STRING),
    IS_BIOMETRIC_AUTH_ON(BOOLEAN),
    MOVE_TO_BACKGROUND_TIMESTAMP(LONG),
    IS_LOCKED(BOOLEAN),
    INVALID_PIN_ATTEMPTS(INT),
    LOCK_PIN_UNLOCK_PROCESS_TIMESTAMP(LONG),
    INVALID_PASSWORD_ATTEMPTS(INT),
    LOCK_RESET_PIN_PROCESS_TIMESTAMP(LONG),
    GPS_PERMISSIONS_ASKED(BOOLEAN),
    CRM_MESSAGE_QUEUE(STRING),
    STARTING_LOCATION(STRING),
    NETWORK_COMMUNICATION_LOCKED(BOOLEAN),
    RIDE_TICKS_REF_TIME(LONG),
    RIDE_TICKS_COUNTER(LONG),
    RIDE_TICKS_LAST_TICK_TIME(LONG),
    MIN_SUPPORTED_VERSION_BY_API(INT),
    OVERLAY_INFO_SHOWN(BOOLEAN),
    OVERLAY_ENABLED_BY_USER(BOOLEAN),
    SKIP_DRIVER_WARNING(BOOLEAN),
    ACCEPTED_REGULATIONS_FILENAME(STRING),
    SELECTED_LANGUAGE_WEAK_SAVE(BOOLEAN),
    SENT_FLAG_OVERRIDDEN_V122(BOOLEAN),
}

enum class SettingsType(val defaultVal: Any) {
    BOOLEAN(false),
    INT(0),
    STRING(""),
    LONG(0L)
}