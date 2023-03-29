package pl.gov.mf.etoll.networking.manager.eventstream.apibuilder

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pl.gov.mf.etoll.networking.BuildConfig
import pl.gov.mf.etoll.networking.api.EventStreamAPI
import pl.gov.mf.etoll.networking.api.model.ApiEventStreamConfiguration
import pl.gov.mf.etoll.networking.manager.NetworkingManager
import pl.gov.mf.etoll.networking.manager.eventstream.EventStreamGson
import pl.gov.mf.mobile.networking.api.interceptors.BasicHeadersInterceptor
import pl.gov.mf.mobile.networking.api.interceptors.CompressionInterceptor
import pl.gov.mf.mobile.networking.api.interceptors.EventStreamInterceptor
import pl.gov.mf.mobile.networking.api.interceptors.ResponseCheckInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EventStreamApiBuilderImpl @Inject constructor(
    private val eventStreamInterceptor: EventStreamInterceptor,
    private val basicHeadersInterceptor: BasicHeadersInterceptor,
    private val responseCheckInterceptor: ResponseCheckInterceptor,
    private val compressionInterceptor: CompressionInterceptor,
    private val loggingInterceptor: HttpLoggingInterceptor,
    private val gson: EventStreamGson
) : EventStreamApiBuilder {

    override fun buildApiFrom(configuration: ApiEventStreamConfiguration): EventStreamAPI {
        eventStreamInterceptor.authValue = configuration.authorizationHeader
        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(basicHeadersInterceptor)
            .addInterceptor(eventStreamInterceptor)
            .addInterceptor(responseCheckInterceptor)
            .readTimeout(NetworkingManager.EVENTSTREAM_TIMEOUT_VALUE_IN_S, TimeUnit.SECONDS)
            .callTimeout(NetworkingManager.EVENTSTREAM_TIMEOUT_VALUE_IN_S, TimeUnit.SECONDS)
            .connectTimeout(NetworkingManager.EVENTSTREAM_TIMEOUT_VALUE_IN_S, TimeUnit.SECONDS)
            .writeTimeout(NetworkingManager.EVENTSTREAM_TIMEOUT_VALUE_IN_S, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG)
            clientBuilder.addInterceptor(loggingInterceptor)
        else
            clientBuilder.addInterceptor(compressionInterceptor)
        val client = clientBuilder.build()

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(configuration.formattedAddress)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson.getGson()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        return retrofit.create(EventStreamAPI::class.java)
    }
}