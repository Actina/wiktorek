package pl.gov.mf.etoll.front.security.settings.models

data class SecuritySettingsConfig(
    var isBiometricAvailable: Boolean? = null,
    var isSecurityConfigValid: Boolean? = null
)