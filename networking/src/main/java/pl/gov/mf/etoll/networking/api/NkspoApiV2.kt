package pl.gov.mf.etoll.networking.api

import pl.gov.mf.etoll.networking.api.model.ApiAuthenticateRequest
import pl.gov.mf.etoll.networking.api.model.ApiAuthenticateResponse
import pl.gov.mf.etoll.networking.api.model.ApiRegisterRequest
import pl.gov.mf.etoll.networking.api.model.ApiRegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.IOException

interface NkspoApiV2 {

    @POST("authenticate")
    suspend fun authenticate(
        @Body body: ApiAuthenticateRequest,
    ): Response<ApiAuthenticateResponse>

    @Throws(IOException::class)
    @POST("register")
    suspend fun register(
        @Body body: ApiRegisterRequest,
    ): Response<ApiRegisterResponse>
}