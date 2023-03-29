package pl.gov.mf.etoll.networking

import kotlinx.coroutines.Job
import pl.gov.mf.etoll.networking.manager.NetworkingManagerV2

interface RegistrationProvider {
    suspend fun register(
        job: Job,
        returnDelegate: () -> Unit,
        errorDelegate: (NetworkingManagerV2.NetworkErrorCause) -> Unit,
    )
}