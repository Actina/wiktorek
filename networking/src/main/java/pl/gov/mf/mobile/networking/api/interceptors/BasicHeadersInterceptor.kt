package pl.gov.mf.mobile.networking.api.interceptors

import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response
import pl.gov.mf.etoll.commons.TimeUtils
import javax.inject.Inject

class BasicHeadersInterceptor @Inject constructor(private val basicHeadersProvider: BasicHeadersProvider) :
    Interceptor {
    override fun intercept(chain: Chain): Response {
        val original = chain.request()
        val originalHttpUrl = original.url
        val requestBuilder = original.newBuilder()
            .addHeader("X-Application-Version", basicHeadersProvider.getAppVersion())
            .addHeader("X-Device-Language", basicHeadersProvider.getAppLanguage())
            .addHeader("X-client-timestamp", TimeUtils.getCurrentTimestampForApi())
            .url(originalHttpUrl)
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}

interface BasicHeadersProvider {
    fun getAppVersion(): String
    fun getAppLanguage(): String
}