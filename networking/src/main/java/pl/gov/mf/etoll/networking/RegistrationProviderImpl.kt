package pl.gov.mf.etoll.networking

import kotlinx.coroutines.Job
import pl.gov.mf.etoll.networking.manager.NetworkingManagerV2
import javax.inject.Inject

class RegistrationProviderImpl @Inject constructor(private val networkManager: NetworkingManagerV2) :
    RegistrationProvider {
    override suspend fun register(
        job: Job,
        returnDelegate: () -> Unit,
        errorDelegate: (NetworkingManagerV2.NetworkErrorCause) -> Unit,
    ) {
        when (val response = networkManager.register(job)) {
            is NetworkingManagerV2.NetworkResponse.ERROR -> errorDelegate(response.cause)
            NetworkingManagerV2.NetworkResponse.OK -> returnDelegate()
        }
    }

}