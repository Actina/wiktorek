package pl.gov.mf.etoll

import androidx.annotation.Keep
import java.io.Serializable


data class PendingError(
    val shouldBeShown: Boolean = false,
    val error: ViewErrorCause? = null,
    val customMessage: String? = null,
) : Serializable

enum class ViewErrorCause {
    @Keep
    NETWORK_ERROR,

    @Keep
    APP_TOO_OLD,

    @Keep
    WRONG_SYSTEM_TIME
}