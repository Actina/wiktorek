package pl.gov.mf.etoll.front.security.unlock.models

data class SecurityUnlockWithPinCodeConfiguration(
    val errorText: String? = null,
    val isErrorVisible: Boolean = false,
    val isInputLocked: Boolean = false
)