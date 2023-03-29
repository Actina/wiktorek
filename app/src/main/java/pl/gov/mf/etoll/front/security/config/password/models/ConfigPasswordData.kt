package pl.gov.mf.etoll.front.security.config.password.models

data class ConfigPasswordData(
    var password: String = "",
    var confirmationPassword: String = ""
)