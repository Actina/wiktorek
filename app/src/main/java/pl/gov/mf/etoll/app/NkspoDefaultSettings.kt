package pl.gov.mf.etoll.app

import pl.gov.mf.etoll.security.SecurityUC.GenerateKeyPairUseCase
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.Settings.ACCEPTED_REGULATIONS_FILENAME
import pl.gov.mf.etoll.storage.settings.Settings.ACTIVATION_TRANS_SUCCEED
import pl.gov.mf.etoll.storage.settings.Settings.API_KEYS
import pl.gov.mf.etoll.storage.settings.Settings.APPLICATION_STATE
import pl.gov.mf.etoll.storage.settings.Settings.APP_IN_FOREGROUND
import pl.gov.mf.etoll.storage.settings.Settings.BUSINESS_NUMBER
import pl.gov.mf.etoll.storage.settings.Settings.CRM_MESSAGE_QUEUE
import pl.gov.mf.etoll.storage.settings.Settings.DEFAULT_BOOLEAN
import pl.gov.mf.etoll.storage.settings.Settings.DEFAULT_INT
import pl.gov.mf.etoll.storage.settings.Settings.DEFAULT_STRING
import pl.gov.mf.etoll.storage.settings.Settings.DEVICE_ID
import pl.gov.mf.etoll.storage.settings.Settings.FIREBASE_ID
import pl.gov.mf.etoll.storage.settings.Settings.GENERATED_JWT_TOKEN
import pl.gov.mf.etoll.storage.settings.Settings.GPS_PERMISSIONS_ASKED
import pl.gov.mf.etoll.storage.settings.Settings.INVALID_PASSWORD_ATTEMPTS
import pl.gov.mf.etoll.storage.settings.Settings.INVALID_PIN_ATTEMPTS
import pl.gov.mf.etoll.storage.settings.Settings.IS_BIOMETRIC_AUTH_ON
import pl.gov.mf.etoll.storage.settings.Settings.IS_LOCKED
import pl.gov.mf.etoll.storage.settings.Settings.LAST_CORRECT_DATA_SENDING_TIMESTAMP
import pl.gov.mf.etoll.storage.settings.Settings.LOCK_PIN_UNLOCK_PROCESS_TIMESTAMP
import pl.gov.mf.etoll.storage.settings.Settings.LOCK_RESET_PIN_PROCESS_TIMESTAMP
import pl.gov.mf.etoll.storage.settings.Settings.MIN_SUPPORTED_VERSION_BY_API
import pl.gov.mf.etoll.storage.settings.Settings.MOVE_TO_BACKGROUND_TIMESTAMP
import pl.gov.mf.etoll.storage.settings.Settings.NETWORK_COMMUNICATION_LOCKED
import pl.gov.mf.etoll.storage.settings.Settings.OVERLAY_ENABLED_BY_USER
import pl.gov.mf.etoll.storage.settings.Settings.OVERLAY_INFO_SHOWN
import pl.gov.mf.etoll.storage.settings.Settings.PASSWORD
import pl.gov.mf.etoll.storage.settings.Settings.PIN
import pl.gov.mf.etoll.storage.settings.Settings.REALM_KEY
import pl.gov.mf.etoll.storage.settings.Settings.RECEIVED_ACCESS_TOKEN
import pl.gov.mf.etoll.storage.settings.Settings.RECENT_VEHICLES
import pl.gov.mf.etoll.storage.settings.Settings.RIDE_COORDINATOR_CONFIGURATION
import pl.gov.mf.etoll.storage.settings.Settings.RIDE_COORDINATOR_STATUS
import pl.gov.mf.etoll.storage.settings.Settings.RIDE_END_TIMESTAMP
import pl.gov.mf.etoll.storage.settings.Settings.RIDE_START_TIMESTAMP
import pl.gov.mf.etoll.storage.settings.Settings.RIDE_SUMMARY_SHOULD_BE_SHOWN
import pl.gov.mf.etoll.storage.settings.Settings.RIDE_TICKS_COUNTER
import pl.gov.mf.etoll.storage.settings.Settings.RIDE_TICKS_LAST_TICK_TIME
import pl.gov.mf.etoll.storage.settings.Settings.RIDE_TICKS_REF_TIME
import pl.gov.mf.etoll.storage.settings.Settings.SECURITY_TRANS_SUCCEED
import pl.gov.mf.etoll.storage.settings.Settings.SELECTED_LANGUAGE_WEAK_SAVE
import pl.gov.mf.etoll.storage.settings.Settings.SENT_FLAG_OVERRIDDEN_V122
import pl.gov.mf.etoll.storage.settings.Settings.SKIP_DRIVER_WARNING
import pl.gov.mf.etoll.storage.settings.Settings.SOUND_NOTIFICATIONS
import pl.gov.mf.etoll.storage.settings.Settings.STARTING_LOCATION
import pl.gov.mf.etoll.storage.settings.Settings.STATUS
import pl.gov.mf.etoll.storage.settings.Settings.SUPPORT_FOR_SENT_ENABLED
import pl.gov.mf.etoll.storage.settings.Settings.TEST_COUNTER
import pl.gov.mf.etoll.storage.settings.Settings.VIBRATION_NOTIFICATIONS
import pl.gov.mf.etoll.storage.settings.defaults.SettingsDefaultsProvider
import javax.inject.Inject

class NkspoDefaultSettings @Inject constructor(
    private val generateKeyPairUseCase: GenerateKeyPairUseCase,
) :
    SettingsDefaultsProvider {
    override fun getDefaultValueFor(settingsName: Settings): Any = when (settingsName) {
        DEFAULT_INT -> 0
        DEFAULT_STRING -> ""
        DEFAULT_BOOLEAN -> false
        APPLICATION_STATE -> 0
        TEST_COUNTER -> 0
        BUSINESS_NUMBER -> ""
        REALM_KEY -> generateKeyPairUseCase.execute()
            .getPublicKeyInPEM(false)
            .subSequence(0, 64)

        API_KEYS -> generateKeyPairUseCase.execute()
            .toJSON()

        GENERATED_JWT_TOKEN -> ""
        RECEIVED_ACCESS_TOKEN -> ""
        ACTIVATION_TRANS_SUCCEED -> false
        SECURITY_TRANS_SUCCEED -> false
        STATUS -> ""
        SUPPORT_FOR_SENT_ENABLED -> true
        RECENT_VEHICLES -> ""
        RIDE_START_TIMESTAMP -> "0"
        RIDE_END_TIMESTAMP -> "0"
        RIDE_COORDINATOR_STATUS -> 0
        DEVICE_ID -> ""
        FIREBASE_ID -> ""
        VIBRATION_NOTIFICATIONS -> false
        SOUND_NOTIFICATIONS -> true
        APP_IN_FOREGROUND -> false
        RIDE_COORDINATOR_CONFIGURATION -> "{}"
        RIDE_SUMMARY_SHOULD_BE_SHOWN -> false
        LAST_CORRECT_DATA_SENDING_TIMESTAMP -> "0"
        PIN -> ""
        PASSWORD -> ""
        IS_BIOMETRIC_AUTH_ON -> false
        MOVE_TO_BACKGROUND_TIMESTAMP -> 0L
        INVALID_PIN_ATTEMPTS -> 0
        LOCK_PIN_UNLOCK_PROCESS_TIMESTAMP -> 0L
        IS_LOCKED -> false
        INVALID_PASSWORD_ATTEMPTS -> 0
        LOCK_RESET_PIN_PROCESS_TIMESTAMP -> 0L
        GPS_PERMISSIONS_ASKED -> false
        CRM_MESSAGE_QUEUE -> ""
        STARTING_LOCATION -> ""
        NETWORK_COMMUNICATION_LOCKED -> false
        RIDE_TICKS_COUNTER -> 0L
        RIDE_TICKS_REF_TIME -> 0L
        RIDE_TICKS_LAST_TICK_TIME -> 0L
        MIN_SUPPORTED_VERSION_BY_API -> 0
        OVERLAY_INFO_SHOWN -> false
        OVERLAY_ENABLED_BY_USER -> false
        SKIP_DRIVER_WARNING -> false
        ACCEPTED_REGULATIONS_FILENAME -> ""
        SELECTED_LANGUAGE_WEAK_SAVE -> false
        SENT_FLAG_OVERRIDDEN_V122 -> false
    }
}