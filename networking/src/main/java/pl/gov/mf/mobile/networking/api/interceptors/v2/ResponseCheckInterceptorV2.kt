package pl.gov.mf.mobile.networking.api.interceptors.v2

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class ResponseCheckInterceptorV2 constructor(private val currentAppVersion: Int) :
    Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val response = chain.proceed(request)
        // check for overall block
        if (response.code == 410) {
            return response
        }
        // check for time issues
        for (header in response.headers) {
            if (header.first.lowercase()
                    .contains("x-minimum-system-version")
            ) {
                val minSupportedAppVersionByApi =
                    try {
                        header.second.toDouble().toInt()
                    } catch (_: Exception) {
                        // do nothing
                        -1
                    }
                // notify about new version read
                if (minSupportedAppVersionByApi > 0 && minSupportedAppVersionByApi > currentAppVersion)
                    return response.newBuilder()
                        .code(APP_TOO_OLD_MIN_CODE + minSupportedAppVersionByApi).build()
            } else
                try {
                    if (header.first.lowercase()
                            .contains("x-timestamp-accepted")
                    ) {
                        if (!header.second.toBoolean()) {
                            return response.newBuilder().code(TIME_ISSUES_CODE).build()
                        }
                    }
                } catch (_: Exception) {
                    // do nothing
                }
        }
        return response
    }

    companion object {
        const val TIME_ISSUES_CODE = 799
        const val APP_TOO_OLD_MIN_CODE = 800
    }
}
