package pl.gov.mf.etoll.storage.database.ridehistory

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.Realm
import io.realm.Sort
import org.joda.time.DateTime
import pl.gov.mf.etoll.storage.database.ridehistory.model.RideHistoryModel
import pl.gov.mf.etoll.storage.database.ridehistory.model.RideHistoryRealmModel
import pl.gov.mf.mobile.storage.database.providers.realm.RealmDatabaseProvider
import javax.inject.Inject

class RideHistoryDatabaseManagerImpl @Inject constructor(
    private val databaseProvider: RealmDatabaseProvider
) : RideHistoryDatabaseManager {

    private fun getLastId(db: Realm): Int = db.where(RideHistoryRealmModel::class.java)
        .sort("id", Sort.DESCENDING)
        .limit(1)
        .findAll()
        .takeIf { it.size > 0 }
        ?.let { it[0]!!.id + 1 } ?: 0

    override fun addData(item: RideHistoryModel): Completable =
        Completable.create { emitter ->
            databaseProvider.executeTransaction({ db ->
                item.run { id = getLastId(db) }
                db.insert(item.toRealmModel())
            }, { emitter.onComplete() },
                { emitter.onError(it) })
        }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun getAll(): Single<List<RideHistoryModel>> =
        Single.create<List<RideHistoryModel>> { emitter ->
            var result: MutableList<RideHistoryModel> = mutableListOf()

            databaseProvider.executeTransaction({ db ->
                val realmResult = db.where(RideHistoryRealmModel::class.java)
                    .sort("timestamp", Sort.DESCENDING)
                    .findAll()
                db.copyFromRealm(realmResult)
                    .toList().forEach { result.add(it.toModel()) }
            }, { emitter.onSuccess(result) },
                { emitter.onError(it) })
        }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun getForPeriod(from: Long, to: Long): Single<List<RideHistoryModel>> =
        Single.create<List<RideHistoryModel>> { emitter ->
            var result: MutableList<RideHistoryModel> = mutableListOf()

            databaseProvider.executeTransaction(
                { db ->
                    val realmResult = db.where(RideHistoryRealmModel::class.java)
                        .between("timestamp", from, to)
                        .sort("timestamp", Sort.DESCENDING)
                        .findAll()
                    db.copyFromRealm(realmResult)
                        .toList().forEach { result.add(it.toModel()) }
                }, { emitter.onSuccess(result) },
                { emitter.onError(it) })
        }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun deleteObsolete(): Single<Boolean> = Single.create<Boolean> { emitter ->
        databaseProvider.executeTransaction({ db ->
            val threeMonthsFromNow = DateTime.now()
                .minusMonths(3)
                .millis

            db.where(RideHistoryRealmModel::class.java)
                .lessThan("timestamp", threeMonthsFromNow)
                .findAll()
                .deleteAllFromRealm()

        }, {
            emitter.onSuccess(true)
        },
            { emitter.onError(it) })
    }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun getById(id: Int): Single<RideHistoryModel> =
        Single.create<RideHistoryModel> { emitter ->
            var result: RideHistoryModel? = null

            databaseProvider.executeTransaction(
                { db ->
                    val realmResult = db.where(RideHistoryRealmModel::class.java)
                        .equalTo("id", id)
                        .findFirst()

                    result = db.copyFromRealm(realmResult)?.toModel()
                },
                {
                    result?.let { emitter.onSuccess(it) }
                        ?: emitter.onError(NoSuchElementException())
                },
                { emitter.onError(it) })
        }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())
}

private fun RideHistoryRealmModel.toModel(): RideHistoryModel =
    RideHistoryModel(id, timestamp, type, frameType, additionalData)

private fun RideHistoryModel.toRealmModel(): RideHistoryRealmModel =
    RideHistoryRealmModel(id, timestamp, type, frameType, additionalData)
