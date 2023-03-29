package pl.gov.mf.mobile.networking.api.interceptors.v2

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.util.*
import javax.inject.Inject

class JwtTokenInterceptorV2 @Inject constructor(private val tokenProvider: JwtTokenValueProvider) :
    Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val original = chain.request()

        if (original.url.encodedPath.contains("register") && original.method.uppercase(Locale.ROOT)
                .contentEquals("POST")
        ) {
            return chain.proceed(original)
        }

        // jwt refresh token
        if (original.url.encodedPath.contains("authenticate") && original.method.uppercase(Locale.ROOT)
                .contentEquals(
                    "POST"
                )
        ) {
            // this is one of 2 risky places where we use blocking method for reading stuff from settings
            val request = runBlocking {
                Log.d("JWT_TOKEN", "USING TOKEN! ${tokenProvider.getJwtToken(this, Job())}")
                val originalHttpUrl = original.url
                val requestBuilder = original.newBuilder()
                    .addHeader(
                        "x-jws-signature",
                        tokenProvider.getJwtToken(this, Job())
                    )
                    .url(originalHttpUrl)

                requestBuilder.build()
            }
            return chain.proceed(request)
        }
        // jwt access token
        val request = runBlocking {
            val originalHttpUrl = original.url
            val requestBuilder = original.newBuilder()
                .addHeader("Authorization", "Bearer ${tokenProvider.getReceivedToken(this, Job())}")
                .url(originalHttpUrl)
            requestBuilder.build()
        }
        return chain.proceed(request)
    }

    interface JwtTokenValueProvider {
        suspend fun generateNewJwtToken(scope: CoroutineScope, job: Job)
        suspend fun getJwtToken(scope: CoroutineScope, job: Job): String
        suspend fun getReceivedToken(scope: CoroutineScope, job: Job): String
    }
}