package pl.gov.mf.etoll.networking.manager

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import pl.gov.mf.etoll.networking.api.NkspoApiV2
import pl.gov.mf.etoll.networking.api.model.ApiAuthenticateRequest
import pl.gov.mf.etoll.networking.api.model.ApiRegisterRequest
import pl.gov.mf.etoll.security.SecurityUC
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.networking.api.interceptors.MinimumVersionTriggersCallbacks
import pl.gov.mf.mobile.networking.api.interceptors.TimeIssuesTriggersCallbacks
import pl.gov.mf.mobile.networking.api.interceptors.v2.JwtTokenInterceptorV2
import pl.gov.mf.mobile.networking.api.interceptors.v2.ResponseCheckInterceptorV2
import pl.gov.mf.mobile.networking.api.jwt.JwtToken
import pl.gov.mf.mobile.security.apikey.GeneratedKeyPair
import pl.gov.mf.mobile.utils.toObject
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.net.ssl.SSLHandshakeException

class NetworkingManagerV2Impl @Inject constructor(
    private val api: NkspoApiV2,
    private val jwtTokenValueProvider: JwtTokenInterceptorV2.JwtTokenValueProvider,
    private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase,
    private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase,
    private val timeIssueCallbacks: TimeIssuesTriggersCallbacks,
    private val minimumVersionTriggersCallbacks: MinimumVersionTriggersCallbacks,
    private val generateDeviceIdUseCase: SecurityUC.GenerateDeviceIdUseCase,
    private val libraryScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : NetworkingManagerV2 {

    private var authenticateJob: Job? = null
    private var lastAuthenticateRsponse: NetworkingManagerV2.NetworkResponse? = null

    override suspend fun register(executionJob: Job): NetworkingManagerV2.NetworkResponse {
        val deviceId = generateDeviceIdUseCase.execute()
        val firebaseId = readSettingsUseCase.executeForStringV2(Settings.FIREBASE_ID)
        val apiKeys =
            readSettingsUseCase.executeForStringV2(Settings.API_KEYS).toObject<GeneratedKeyPair>()
        // TODO: change in future to allow registration WITHOUT firebase id

        if (firebaseId.isBlank() || firebaseId.isEmpty()) return NetworkingManagerV2.NetworkResponse.ERROR(
            NetworkingManagerV2.NetworkErrorCause.GENERIC
        )
        try {
            val response = api.register(
                ApiRegisterRequest(
                    deviceId = deviceId,
                    publicKeyInPem = apiKeys.getPublicKeyInPEM(),
                    firebaseUserId = firebaseId
                )
            )
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return NetworkingManagerV2.NetworkResponse.ERROR(NetworkingManagerV2.NetworkErrorCause.GENERIC)
                writeSettingsUseCase.executeV2(Settings.DEVICE_ID, deviceId)
                writeSettingsUseCase.executeV2(Settings.BUSINESS_NUMBER, body.refreshToken)
                return NetworkingManagerV2.NetworkResponse.OK
            } else {
                return parseCode(response.code(), executionJob)
            }
        } catch (ex: IOException) {
            return getNetworkResponseForApiException(ex)
        }
    }

    override suspend fun authenticate(executionJob: Job): NetworkingManagerV2.NetworkResponse {
        if (authenticateJob != null && authenticateJob?.isActive == true && authenticateJob?.isCompleted == false && authenticateJob?.isCancelled == false) {
            authenticateJob?.join()
            return lastAuthenticateRsponse!!
        } else {
            try {
                // generate and save jwt token
                jwtTokenValueProvider.generateNewJwtToken(job = executionJob, scope = libraryScope)
                // and now use it
                val businessNumber = readSettingsUseCase.executeForString(Settings.BUSINESS_NUMBER)
                val currentTokenString = readSettingsUseCase.executeForString(
                    Settings.GENERATED_JWT_TOKEN,
                )
                Log.d("JWT_TOKEN", "Current $currentTokenString")
                val currentToken = currentTokenString.toObject<JwtToken>()
                Log.d("JWT_TOKEN", "USING TOKEN in auth req! ${currentToken.toJSON()}")
                // and use it again in header of this call (by interceptor
                val response = api.authenticate(
                    ApiAuthenticateRequest(
                        businessNumber,
                        currentToken.date
                    )
                )
                lastAuthenticateRsponse = if (response.isSuccessful && response.body() != null) {
                    val accessToken = response.body()!!.accessToken
                    writeSettingsUseCase.execute(
                        Settings.RECEIVED_ACCESS_TOKEN,
                        accessToken
                    ).subscribe() // TODO: etoll2: flatten!
                    timeIssueCallbacks.onNoTimeIssueIntercepted() // TODO: etoll2: flatten!
                    NetworkingManagerV2.NetworkResponse.OK
                } else {
                    parseCode(response.code(), executionJob)
                }
                return lastAuthenticateRsponse!!
            } catch (ex: Exception) {
                Log.d("JWT_TOKEN", "Exception during deserialization $ex")
                lastAuthenticateRsponse = getNetworkResponseForApiException(ex)
                return lastAuthenticateRsponse!!
            }
        }
    }


    private fun parseCode(code: Int, job: Job): NetworkingManagerV2.NetworkResponse =
        // TODO: for fast-integration purposes, types were flattened to generic type
        if (code == ResponseCheckInterceptorV2.TIME_ISSUES_CODE) {
            timeIssueCallbacks.onTimeIssueIntercepted()
//            NetworkingManagerV2.NetworkResponse.ERROR(NetworkingManagerV2.NetworkErrorCause.GENERIC)
            NetworkingManagerV2.NetworkResponse.ERROR(NetworkingManagerV2.NetworkErrorCause.TIME_PROBLEM)
        } else if (code >= ResponseCheckInterceptorV2.APP_TOO_OLD_MIN_CODE) {
            val minSupportedAppVersion =
                code - ResponseCheckInterceptorV2.APP_TOO_OLD_MIN_CODE
            // notify about new version read - etoll version
            minimumVersionTriggersCallbacks.onNewMinVersionDetected(minSupportedAppVersion)
            NetworkingManagerV2.NetworkResponse.ERROR(NetworkingManagerV2.NetworkErrorCause.APP_VERSION_PROBLEM)
//            NetworkingManagerV2.NetworkResponse.ERROR(NetworkingManagerV2.NetworkErrorCause.GENERIC)
        } else
            NetworkingManagerV2.NetworkResponse.ERROR(NetworkingManagerV2.NetworkErrorCause.GENERIC)

    private suspend fun dealWithErrors(
        executionJob: Job,
        response: Response<*>,
        retryDelegate: suspend () -> Unit,
        responseDelegate: (response: NetworkingManagerV2.NetworkResponse) -> Unit,
    ) {
        if (response.code() == 401 || response.code() == 403) {
            // unauthorized
            val responseForAuth = authenticate(executionJob)
            if (responseForAuth == NetworkingManagerV2.NetworkResponse.OK) {
                retryDelegate()
            } else {
                withContext(Dispatchers.Main) {
                    responseDelegate(responseForAuth)
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                responseDelegate(parseCode(response.code(), executionJob))
            }
        }
    }

    private fun getNetworkResponseForApiException(apiException: Exception): NetworkingManagerV2.NetworkResponse {
        return if (apiException is SSLHandshakeException)
            NetworkingManagerV2.NetworkResponse.ERROR(NetworkingManagerV2.NetworkErrorCause.TIME_PROBLEM)
        else
            NetworkingManagerV2.NetworkResponse.ERROR(NetworkingManagerV2.NetworkErrorCause.GENERIC)
    }
}