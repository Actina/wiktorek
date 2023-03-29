package pl.gov.mf.etoll.networking.api

import io.reactivex.Completable
import io.reactivex.Single
import pl.gov.mf.etoll.networking.api.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface NkspoApi {

    @POST("register")
    fun register(
        @Body body: ApiRegisterRequest
    ): Single<ApiRegisterResponse>

    @GET("status")
    fun getStatus(): Single<ApiStatusResponse>

    @GET("sent")
    fun getSentList(): Single<ApiSentResponse>

    @POST("tecs/open-transaction")
    fun tecsOpenTransaction(@Body body: ApiTecsOpenTransactionRequest): Single<ApiTecsOpenTransactionResponse>

    @POST("tecs/close-transaction")
    fun tecsCloseTransaction(@Body body: ApiTecsCloseTransactionRequest): Single<ApiTecsCloseTransactionResponse>

    @GET("transit/last-positions/")
    fun getLastPositions(
        @Query("TransitType") transitType: String,
        @Query("ZslBusinessId") zslBusinessId: String?
    ): Single<ApiLastPositionsSentResponse>

    @GET("messages")
    fun getMessages(@Query("messageIds") ids: Array<String>): Single<List<ApiServerMessage>>

    @POST("messages/confirm")
    fun confirmMessages(@Body body: ApiConfirmMessagesRequest): Completable

    @POST("mobile-application/log")
    fun log(@Body body: ApiLogRequest): Completable

}