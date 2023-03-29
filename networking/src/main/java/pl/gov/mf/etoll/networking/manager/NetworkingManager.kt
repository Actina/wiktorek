package pl.gov.mf.etoll.networking.manager

import io.reactivex.Completable
import io.reactivex.Single
import pl.gov.mf.etoll.networking.api.model.*
import pl.gov.mf.etoll.storage.database.logging.model.LoggingModel

interface NetworkingManager {

//    fun register(): Single<String>

    fun authenticate(): Completable

    fun updateStatus(): Single<ApiStatusResponse>

    fun checkSentList(): Single<Map<String, List<ApiSentItem>>>

    fun openTransaction(
        billingAccountId: Long,
        amount: Double,
        returnUrl: String,
        category: Int
    ): Single<ApiTecsOpenTransactionResponse>

    fun closeTransaction(tecsTransactionResult: TecsTransactionResult): Single<ApiTecsCloseTransactionResponse>

    fun getLastPositions(
        coreTransitType: String,
        zslBusinessId: String?
    ): Single<ApiLastPositionsSentResponse>

    fun getMessages(ids: Array<String>): Single<List<ApiServerMessage>>

    fun confirmMessages(ids: Array<String>): Completable

    fun log(
        items: List<LoggingModel>
    ): Completable

    companion object {
        const val DELAY_BETWEEN_RETRY = 5 * 1000L // 5s
        const val TIMEOUT_VALUE_IN_S = 15L // 15s
        const val EVENTSTREAM_TIMEOUT_VALUE_IN_S = 90L
        const val MAX_FIREBASE_ID_RETRY_COUNT = 5
    }
}

class FlowControlException : Exception("Flow control")