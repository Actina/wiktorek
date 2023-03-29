package pl.gov.mf.etoll.networking.manager.netswitch

import io.reactivex.Completable
import io.reactivex.Single
import pl.gov.mf.etoll.networking.api.model.*
import pl.gov.mf.etoll.networking.manager.NetworkingManager
import pl.gov.mf.etoll.storage.database.logging.model.LoggingModel
import pl.gov.mf.mobile.networking.api.interceptors.ShouldNotHaveAccessToServerException
import javax.inject.Inject

class NetworkingManagerSelector @Inject constructor(
    private val selector: NetworkSwitchConditionsCheck,
    private val realImplementation: NetworkingManager,
    private val dummyImpl: NetworkingManager
) : NetworkingManager {

    override fun authenticate(): Completable = getManager().authenticate().doOnError {
        // proceed with chain
        if (it is ShouldNotHaveAccessToServerException) {
            disableNetworkForever()
        }
    }

    override fun updateStatus(): Single<ApiStatusResponse> =
        // proceed with chain - return dummy value
        getManager().updateStatus().onErrorResumeNext {
            if (it is ShouldNotHaveAccessToServerException) {
                disableNetworkForever()
                dummyImpl.updateStatus()
            } else Single.error(it)
        }

    override fun checkSentList(): Single<Map<String, List<ApiSentItem>>> =
        // proceed with chain - return dummy value
        getManager().checkSentList().onErrorResumeNext {
            if (it is ShouldNotHaveAccessToServerException) {
                disableNetworkForever()
                dummyImpl.checkSentList()
            } else
                Single.error(it)
        }

    override fun openTransaction(
        billingAccountId: Long,
        amount: Double,
        returnUrl: String,
        category: Int
    ): Single<ApiTecsOpenTransactionResponse> =
        // proceed with chain - stop next steps
        getManager().openTransaction(
            billingAccountId = billingAccountId,
            amount = amount, returnUrl = returnUrl, category = category
        ).doOnError {
            if (it is ShouldNotHaveAccessToServerException) {
                disableNetworkForever()
            }
        }

    override fun closeTransaction(tecsTransactionResult: TecsTransactionResult): Single<ApiTecsCloseTransactionResponse> =
    // proceed with chain - stop next steps
        // TODO: this one could require little more analysis and workaround if happens - different screen? We should stop this process
        getManager().closeTransaction(tecsTransactionResult = tecsTransactionResult)
            .onErrorResumeNext {
                if (it is ShouldNotHaveAccessToServerException) {
                    disableNetworkForever()
                    dummyImpl.closeTransaction(tecsTransactionResult)
                } else Single.error(it)
            }

    override fun getLastPositions(
        coreTransitType: String,
        zslBusinessId: String?
    ): Single<ApiLastPositionsSentResponse> = getManager().getLastPositions(
        coreTransitType = coreTransitType,
        zslBusinessId = zslBusinessId
    ).onErrorResumeNext {
        if (it is ShouldNotHaveAccessToServerException) {
            disableNetworkForever()
            dummyImpl.getLastPositions(coreTransitType, zslBusinessId)
        } else Single.error(it)
    }

    override fun getMessages(ids: Array<String>): Single<List<ApiServerMessage>> =
        getManager().getMessages(ids = ids).onErrorResumeNext {
            if (it is ShouldNotHaveAccessToServerException) {
                disableNetworkForever()
                dummyImpl.getMessages(ids)
            } else Single.error(it)
        }

    override fun confirmMessages(ids: Array<String>): Completable =
        getManager().confirmMessages(ids = ids).doOnError {
            if (it is ShouldNotHaveAccessToServerException) {
                disableNetworkForever()
            }
        }

    override fun log(items: List<LoggingModel>): Completable =
        getManager().log(items = items).doOnError {
            if (it is ShouldNotHaveAccessToServerException) {
                disableNetworkForever()
            }
        }

    private fun disableNetworkForever() {
        selector.lockToDummy()
    }

    private fun getManager() =
        if (selector.shouldSelectRealManager()) realImplementation else dummyImpl
}