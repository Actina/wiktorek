package pl.gov.mf.etoll.core.devicecompatibility.types

enum class BiometricState {
    NOT_AVAILABLE,

    //Means: "device has proper hardware but fingerprint hasn't been added"
    AVAILABLE_BUT_SWITCHED_OFF,

    //Means: "device has proper hardware and fingerprint has been added"
    AVAILABLE_AND_SWITCHED_ON
}