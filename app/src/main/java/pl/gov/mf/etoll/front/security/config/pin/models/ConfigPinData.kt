package pl.gov.mf.etoll.front.security.config.pin.models

data class ConfigPinData(
    var pin: String = "",
    var confirmationPin: String = ""
)