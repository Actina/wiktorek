package pl.gov.mf.mobile.networking.api.interceptors

import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response
import pl.gov.mf.etoll.commons.TimeUtils

// TODO: check & remove
class EventStreamInterceptor : Interceptor {

    var authValue: String = TimeUtils.EMPTY_STRING

    override fun intercept(chain: Chain): Response {
        val original = chain.request()
        val originalHttpUrl = original.url
        val requestBuilder = original.newBuilder()
            .addHeader("Authorization", authValue)
            .url(originalHttpUrl)

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}