package pl.gov.mf.etoll.networking.api

import io.reactivex.Completable
import pl.gov.mf.etoll.networking.api.model.EventStreamObject
import retrofit2.http.Body
import retrofit2.http.POST

interface EventStreamAPI {

    @POST("messages")
    fun postDataToStream(@Body data: EventStreamObject): Completable
}