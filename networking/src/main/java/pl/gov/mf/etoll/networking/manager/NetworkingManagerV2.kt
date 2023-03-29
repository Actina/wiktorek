package pl.gov.mf.etoll.networking.manager

import androidx.annotation.Keep
import kotlinx.coroutines.Job
import pl.gov.mf.etoll.ViewErrorCause
import java.io.Serializable

interface NetworkingManagerV2 {


    @Keep
    sealed class NetworkResponse : Serializable {
        @Keep
        object OK : NetworkResponse()

        @Keep
        class ERROR(val cause: NetworkErrorCause, val message: String? = null) : NetworkResponse()
    }

    enum class NetworkErrorCause {
        @Keep
        GENERIC,

        @Keep
        APP_VERSION_PROBLEM,

        @Keep
        TIME_PROBLEM;

        fun map(): ViewErrorCause = when (this) {
            GENERIC -> ViewErrorCause.NETWORK_ERROR
            APP_VERSION_PROBLEM -> ViewErrorCause.APP_TOO_OLD
            TIME_PROBLEM -> ViewErrorCause.WRONG_SYSTEM_TIME
        }
    }

    suspend fun authenticate(executionJob: Job): NetworkResponse
    suspend fun register(executionJob: Job): NetworkResponse
}