package pl.gov.mf.etoll.networking

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pl.gov.mf.etoll.app.di.ApplicationScope
import pl.gov.mf.etoll.networking.api.NkspoApi
import pl.gov.mf.etoll.networking.api.NkspoApiV2
import pl.gov.mf.etoll.networking.manager.NetworkingManager
import pl.gov.mf.etoll.networking.manager.NetworkingManagerImpl
import pl.gov.mf.etoll.networking.manager.NetworkingManagerV2
import pl.gov.mf.etoll.networking.manager.NetworkingManagerV2Impl
import pl.gov.mf.etoll.networking.manager.dummy.DummyNetworkManager
import pl.gov.mf.etoll.networking.manager.eventstream.EventStreamGson
import pl.gov.mf.etoll.networking.manager.eventstream.EventStreamGsonImpl
import pl.gov.mf.etoll.networking.manager.eventstream.EventStreamManager
import pl.gov.mf.etoll.networking.manager.eventstream.EventStreamManagerImpl
import pl.gov.mf.etoll.networking.manager.eventstream.apibuilder.EventStreamApiBuilder
import pl.gov.mf.etoll.networking.manager.eventstream.apibuilder.EventStreamApiBuilderImpl
import pl.gov.mf.etoll.networking.manager.eventstream.dummy.DummyEventStreamManager
import pl.gov.mf.etoll.networking.manager.eventstream.selector.EventStreamManagerSelector
import pl.gov.mf.etoll.networking.manager.netswitch.NetworkSwitchConditionsCheck
import pl.gov.mf.etoll.networking.manager.netswitch.NetworkSwitchConditionsCheckImpl
import pl.gov.mf.etoll.networking.manager.netswitch.NetworkingManagerSelector
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.etoll.storage.settings.SettingsUC.ReadSettingsUseCase
import pl.gov.mf.mobile.networking.api.interceptors.*
import pl.gov.mf.mobile.networking.api.interceptors.v2.JwtTokenInterceptorV2
import pl.gov.mf.mobile.networking.api.interceptors.v2.ResponseCheckInterceptorV2
import pl.gov.mf.mobile.networking.api.jwt.JwtToken
import pl.gov.mf.mobile.networking.api.jwt.JwtTokenGenerator
import pl.gov.mf.mobile.networking.api.jwt.JwtTokenGeneratorImpl
import pl.gov.mf.mobile.security.apikey.GeneratedKeyPair
import pl.gov.mf.mobile.utils.toObject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named


@Module
class NetworkingModule {

    @ApplicationScope
    @Provides
    fun providesRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER_URL)
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    fun providesLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Named(OLD_OKHTTP)
    @ApplicationScope
    @Provides
    fun providesOkHttp(
        loggingInterceptor: HttpLoggingInterceptor,
        jwtTokenInterceptor: JwtTokenInterceptorV2,
        basicHeadersInterceptor: BasicHeadersInterceptor,
        responseCheckInterceptor: ResponseCheckInterceptor,
    ): OkHttpClient {
        val client = OkHttpClient.Builder()
            .readTimeout(NetworkingManager.TIMEOUT_VALUE_IN_S, TimeUnit.SECONDS)
            .callTimeout(NetworkingManager.TIMEOUT_VALUE_IN_S, TimeUnit.SECONDS)
            .connectTimeout(NetworkingManager.TIMEOUT_VALUE_IN_S, TimeUnit.SECONDS)
            .writeTimeout(NetworkingManager.TIMEOUT_VALUE_IN_S, TimeUnit.SECONDS)
        client.addInterceptor(basicHeadersInterceptor)
        client.addInterceptor(jwtTokenInterceptor)
        client.addInterceptor(responseCheckInterceptor)
        if (BuildConfig.DEBUG) client.addInterceptor(loggingInterceptor)
        return client.build()
    }

    @Named(NEW_OKHTTP)
    @ApplicationScope
    @Provides
    fun providesOkHttpV2(
        loggingInterceptor: HttpLoggingInterceptor,
        jwtTokenInterceptor: JwtTokenInterceptorV2,
        basicHeadersInterceptor: BasicHeadersInterceptor,
        responseCheckInterceptor: ResponseCheckInterceptorV2,
    ): OkHttpClient {
        val client = OkHttpClient.Builder()
            .readTimeout(NetworkingManager.TIMEOUT_VALUE_IN_S, TimeUnit.SECONDS)
            .callTimeout(NetworkingManager.TIMEOUT_VALUE_IN_S, TimeUnit.SECONDS)
            .connectTimeout(NetworkingManager.TIMEOUT_VALUE_IN_S, TimeUnit.SECONDS)
            .writeTimeout(NetworkingManager.TIMEOUT_VALUE_IN_S, TimeUnit.SECONDS)
        client.addInterceptor(basicHeadersInterceptor)
        client.addInterceptor(jwtTokenInterceptor)
        client.addInterceptor(responseCheckInterceptor)
        if (BuildConfig.DEBUG) client.addInterceptor(loggingInterceptor)
        return client.build()
    }

    @Provides
    fun providesBasicHeadersInterceptor(basicHeadersProvider: BasicHeadersProvider): BasicHeadersInterceptor =
        BasicHeadersInterceptor(basicHeadersProvider)

    @Provides
    fun providesJwtTokenInterceptor(jwtTokenValueProvider: JwtTokenInterceptorV2.JwtTokenValueProvider): JwtTokenInterceptorV2 =
        JwtTokenInterceptorV2(jwtTokenValueProvider)

    @ApplicationScope
    @Provides
    fun providesApi(
        gson: Gson,
        @Named(OLD_OKHTTP) okHttpClient: OkHttpClient,
    ): NkspoApi {
        // this is not using shared retrofit instance, also items won't be shared for this case
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        return retrofit.create(NkspoApi::class.java)
    }

    @ApplicationScope
    @Provides
    fun providesApiV2(
        gson: Gson,
        @Named(NEW_OKHTTP) okHttpClient: OkHttpClient,
    ): NkspoApiV2 {
        // this is not using shared retrofit instance, also items won't be shared for this case
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        return retrofit.create(NkspoApiV2::class.java)
    }

    @Provides
    @ApplicationScope
    fun providesGson(): Gson =
        GsonBuilder()
            .setLenient()
            .create()


    @Provides
    @ApplicationScope
    fun providesEventStreamGson(impl: EventStreamGsonImpl): EventStreamGson = impl

    @Provides
    @ApplicationScope
    fun providesTokenGenerator(impl: JwtTokenGeneratorImpl): JwtTokenGenerator = impl

    @Provides
    fun providesEventStreamInterceptor(): EventStreamInterceptor = EventStreamInterceptor()

    @Provides
    fun providesCompressionInterceptor(): CompressionInterceptor = CompressionInterceptor()


    @ApplicationScope
    @Provides
    fun providesJwtTokenProvider(
        readSettingsUseCase: ReadSettingsUseCase,
        writeSettingsUseCase: SettingsUC.WriteSettingsUseCase,
        jwtTokenGenerator: JwtTokenGenerator,
    ): JwtTokenInterceptorV2.JwtTokenValueProvider =
        object : JwtTokenInterceptorV2.JwtTokenValueProvider {
            /**
             * JWT token is generated once every authenticate request, and then not needed for any
             * process - so we can externalize it this way to avoid races
             *
             * Problem is that we need to use it twice - in generator and then in request body.
             * To avoid races (when those two values would have different time by 1s), we need
             * to store it in persistence and make sure to refresh it when needed.
             */
            override suspend fun generateNewJwtToken(scope: CoroutineScope, job: Job) {
                val applicationId = readSettingsUseCase.executeForString(
                    Settings.BUSINESS_NUMBER
                )
                val token = jwtTokenGenerator.generate(
                    applicationId = applicationId,
                    privateKey = getApiKeys(scope, job).rsaPrivateKey
                )
                // we need to store this info now
                writeSettingsUseCase.execute(
                    settings = Settings.GENERATED_JWT_TOKEN,
                    value = token.toJSON(),
                ).subscribe()
                // we do not return anything - we just store new value
                Log.d("JWT_TOKEN", "GENERATED AND SAVED! ${token.toJSON()}")
            }

            override suspend fun getJwtToken(scope: CoroutineScope, job: Job): String =
                readSettingsUseCase.executeForString(
                    Settings.GENERATED_JWT_TOKEN
                ).toObject<JwtToken>().token

            override suspend fun getReceivedToken(scope: CoroutineScope, job: Job): String =
                readSettingsUseCase.executeForString(Settings.RECEIVED_ACCESS_TOKEN)

            private suspend fun getApiKeys(scope: CoroutineScope, job: Job) =
                readSettingsUseCase.executeForString(Settings.API_KEYS)
                    .toObject<GeneratedKeyPair>()
        }

    @ApplicationScope
    @Provides
    fun providesNetworkingManager(networkingManager: NetworkingManagerSelector): NetworkingManager =
        networkingManager

    @ApplicationScope
    @Provides
    fun providesNetworkingSelector(
        switch: NetworkSwitchConditionsCheck,
        netMan: NetworkingManagerImpl,
        dummyImpl: DummyNetworkManager,
    ): NetworkingManagerSelector = NetworkingManagerSelector(switch, netMan, dummyImpl)

    @ApplicationScope
    @Provides
    fun providesNetworkSelectorConditionChecker(implementation: NetworkSwitchConditionsCheckImpl): NetworkSwitchConditionsCheck =
        implementation

    @ApplicationScope
    @Provides
    fun providesEventStreamProvider(impl: EventStreamManagerSelector): EventStreamManager = impl

    @ApplicationScope
    @Provides
    fun providesEventStreamManagerSelector(
        selector: NetworkSwitchConditionsCheck,
        realImpl: EventStreamManagerImpl,
        dummy: DummyEventStreamManager,
    ): EventStreamManagerSelector = EventStreamManagerSelector(selector, realImpl, dummy)

    @ApplicationScope
    @Provides
    fun providesEventStreamApiBuilder(impl: EventStreamApiBuilderImpl): EventStreamApiBuilder = impl

    @ApplicationScope
    @Provides
    fun providesNewNetworkManager(impl: NetworkingManagerV2Impl): NetworkingManagerV2 = impl

    // TODO: etoll2: move higher in modules
    @Provides
    fun providesCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.IO)

    @Provides
    fun providesRegistrationProvider(impl: RegistrationProviderImpl): RegistrationProvider = impl

    companion object {
        private const val OLD_OKHTTP = "OldOkHttp"
        private const val NEW_OKHTTP = "NewOkHttp"
    }
}