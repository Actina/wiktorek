package pl.gov.mf.etoll.front.security.resettounlock.models

data class SecurityResetPinCodeToUnlockConfiguration(
    val errorText: String? = null,
    val isErrorVisible: Boolean = false,
    val isInputLocked: Boolean = false,
    val isNavigationBackAvailable: Boolean = true
)