package pl.gov.mf.etoll.networking.manager.dummy

import io.reactivex.Completable
import io.reactivex.Single
import pl.gov.mf.etoll.networking.api.model.*
import pl.gov.mf.etoll.networking.manager.NetworkingManager
import pl.gov.mf.etoll.storage.database.logging.model.LoggingModel
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.networking.api.interceptors.ShouldNotHaveAccessToServerException
import pl.gov.mf.mobile.utils.toObject
import javax.inject.Inject

class DummyNetworkManager @Inject constructor(private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase) :
    NetworkingManager {

    override fun authenticate(): Completable = Completable.complete()

    override fun updateStatus(): Single<ApiStatusResponse> = Single.just(
        readSettingsUseCase.executeForString(Settings.STATUS).toObject<ApiStatusResponse>()
    )

    override fun checkSentList(): Single<Map<String, List<ApiSentItem>>> = Single.just(emptyMap())

    override fun openTransaction(
        billingAccountId: Long,
        amount: Double,
        returnUrl: String,
        category: Int
    ): Single<ApiTecsOpenTransactionResponse> = Single.error(ShouldNotHaveAccessToServerException())

    override fun closeTransaction(tecsTransactionResult: TecsTransactionResult): Single<ApiTecsCloseTransactionResponse> =
        Single.error(ShouldNotHaveAccessToServerException())

    override fun getLastPositions(
        coreTransitType: String,
        zslBusinessId: String?
    ): Single<ApiLastPositionsSentResponse> = Single.error(ShouldNotHaveAccessToServerException())

    override fun getMessages(ids: Array<String>): Single<List<ApiServerMessage>> = Single.just(
        emptyList()
    )

    override fun confirmMessages(ids: Array<String>): Completable = Completable.complete()

    override fun log(items: List<LoggingModel>): Completable = Completable.complete()

}