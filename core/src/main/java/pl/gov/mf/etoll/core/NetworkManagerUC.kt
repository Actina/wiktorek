package pl.gov.mf.etoll.core

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pl.gov.mf.etoll.core.model.CoreMessage
import pl.gov.mf.etoll.core.model.CoreStatus
import pl.gov.mf.etoll.core.model.CoreTransitType
import pl.gov.mf.etoll.core.model.toCoreModel
import pl.gov.mf.etoll.networking.api.model.EventStreamObject
import pl.gov.mf.etoll.networking.api.model.TecsTransactionResult
import pl.gov.mf.etoll.networking.manager.NetworkingManager
import pl.gov.mf.etoll.networking.manager.eventstream.EventStreamManager
import javax.inject.Inject

sealed class NetworkManagerUC {

    class AuthenticateUseCase @Inject constructor(private val netMan: NetworkingManager) :
        NetworkManagerUC() {
        fun execute(): Completable = netMan.authenticate()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    class UpdateStatusUseCase @Inject constructor(private val netMan: NetworkingManager) :
        NetworkManagerUC() {
        fun execute(): Single<CoreStatus> = netMan.updateStatus().map { it.toCoreModel() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    class SendDataToEventStreamUseCase @Inject constructor(private val eventStreamManager: EventStreamManager) :
        NetworkManagerUC() {
        fun execute(data: EventStreamObject): Completable =
            eventStreamManager.sendDataToStream(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    class CheckSentListUseCase @Inject constructor(private val netMan: NetworkingManager) :
        NetworkManagerUC() {
        fun execute() = netMan.checkSentList().map {
            it.toCoreModel()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    class OpenTransactionUseCase @Inject constructor(private val netMan: NetworkingManager) :
        NetworkManagerUC() {
        fun execute(
            billingAccountId: Long,
            amount: Double,
            returnUrl: String,
            category: Int
        ) = netMan.openTransaction(billingAccountId, amount, returnUrl, category)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    class CloseTransactionUseCase @Inject constructor(private val netMan: NetworkingManager) :
        NetworkManagerUC() {
        fun execute(tecsTransactionResult: TecsTransactionResult) =
            netMan.closeTransaction(tecsTransactionResult)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    class GetLastPositionsSentUseCase @Inject constructor(private val netMan: NetworkingManager) :
        NetworkManagerUC() {
        fun execute(
            coreTransitType: CoreTransitType,
            zslBusinessId: String?
        ) = netMan.getLastPositions(coreTransitType.value, zslBusinessId).map { it.toCoreModel() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    class ConfirmMessagesUseCase @Inject constructor(private val netMan: NetworkingManager) :
        NetworkManagerUC() {
        fun execute(ids: Array<String>) = netMan.confirmMessages(ids)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    class GetMessagesUseCase @Inject constructor(private val netMan: NetworkingManager) :
        NetworkManagerUC() {
        fun execute(ids: Array<String>): Single<List<CoreMessage>> = netMan.getMessages(ids).map {
            it.toCoreModel()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}


