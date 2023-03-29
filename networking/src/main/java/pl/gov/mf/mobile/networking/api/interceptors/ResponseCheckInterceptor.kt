package pl.gov.mf.mobile.networking.api.interceptors

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Request
import okhttp3.Response
import org.joda.time.DateTime

class ResponseCheckInterceptor(
    private val callbacks: TimeIssuesTriggersCallbacks,
    private val minimumVersionTriggersCallbacks: MinimumVersionTriggersCallbacks
) :
    Interceptor {
    override fun intercept(chain: Chain): Response {
        val startNs = System.currentTimeMillis()
        val request: Request = chain.request()
        val response = chain.proceed(request)
        val currNs = System.currentTimeMillis()
        val tookMs = currNs - startNs
        Log.d(
            "NETWORK_TIMINGS",
            "Okhttp took: $tookMs ms for ${request.url.pathSegments.last()} start:${DateTime(startNs)} curr: ${
                DateTime(
                    currNs
                )
            }"
        )
        // check for overall block
        if (response.code == 410) {
            throw ShouldNotHaveAccessToServerException()
        }
        // check for time issues
        for (header in response.headers) {
            if (header.first.lowercase()
                    .contains("x-timestamp-accepted")
            ) {
                if (!header.second.toBoolean()) {
                    callbacks.onTimeIssueIntercepted()
                    throw WrongSystemTimeException("wrong system time")
                } else {
                    callbacks.onNoTimeIssueIntercepted()
                }
            } else if (header.first.lowercase()
                    .contains("x-minimum-system-version")
            ) {
                try {
                    // notify about new version read
                    minimumVersionTriggersCallbacks.onNewMinVersionDetected(header.second.toInt())
                } catch (_: Exception) {
                    // do nothing
                }
            }
        }
        // check for authorization issue
        if (response.code == 401 || response.code == 403)
            throw UnauthorizedNetworkException()
        return response
    }
}

class UnauthorizedNetworkException : Exception()
class WrongSystemTimeException(cause: String) : Exception(cause)
class ShouldNotHaveAccessToServerException : Exception()

interface TimeIssuesTriggersCallbacks {
    fun onTimeIssueIntercepted()
    fun onNoTimeIssueIntercepted()
}

interface MinimumVersionTriggersCallbacks {
    fun onNewMinVersionDetected(version: Int)
}