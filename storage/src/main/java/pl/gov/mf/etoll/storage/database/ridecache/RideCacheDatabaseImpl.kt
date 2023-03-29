package pl.gov.mf.etoll.storage.database.ridecache

import android.util.Log
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.Sort
import pl.gov.mf.etoll.storage.database.ridecache.model.RideCacheModel
import pl.gov.mf.etoll.storage.database.ridecache.model.RideCacheRealmModel
import pl.gov.mf.etoll.storage.database.ridecache.model.RideConfigRealmModel
import pl.gov.mf.mobile.storage.database.providers.realm.RealmDatabaseProvider
import javax.inject.Inject

class RideCacheDatabaseImpl @Inject constructor(
    private val databaseProvider: RealmDatabaseProvider
) : RideCacheDatabase {

    companion object {
        private const val TARGET_SYSTEM_SENT = 2
        private const val TARGET_SYSTEM_TOLLED_OR_LIGHT = 1
    }

    override fun getAndIncrementLatestTolledId(): Single<Long> = Single.create<Long> { emitter ->
        var lastKnownId: Long = 0
        databaseProvider.executeTransaction({ dbInstance ->
            val realmResultForId = dbInstance.where(RideConfigRealmModel::class.java)
                .equalTo("sent", false)
                .limit(1)
                .findAll()
            if (!realmResultForId.isEmpty()) {
                lastKnownId = realmResultForId[0]!!.lastUsedSpoeKasId
                realmResultForId.deleteAllFromRealm()
            }
            if (lastKnownId < Long.MAX_VALUE)
                lastKnownId++
            else
                lastKnownId = 0
            dbInstance.insert(RideConfigRealmModel(lastKnownId, false))
        }, {
            emitter.onSuccess(lastKnownId)
        }, {
            emitter.onError(it)
        })
        // realm works correctly only if all operations are on one thread, also they are supposed to be run on main thread, therefore we're doing this
    }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun getAndIncrementLatestSentId(): Single<Long> = Single.create<Long> { emitter ->
        var lastKnownId: Long = 0
        databaseProvider.executeTransaction({ dbInstance ->
            val realmResultForId = dbInstance.where(RideConfigRealmModel::class.java)
                .equalTo("sent", true)
                .limit(1)
                .findAll()
            if (!realmResultForId.isEmpty()) {
                lastKnownId = realmResultForId[0]!!.lastUsedSpoeKasId
                realmResultForId.deleteAllFromRealm()
            }
            if (lastKnownId < Long.MAX_VALUE)
                lastKnownId++
            else
                lastKnownId = 0
            dbInstance.insert(RideConfigRealmModel(lastKnownId, true))
        }, {
            emitter.onSuccess(lastKnownId)
        }, {
            emitter.onError(it)
        })
        // realm works correctly only if all operations are on one thread, also they are supposed to be run on main thread, therefore we're doing this
    }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun getDataPackage(
        size: Int,
        forSent: Boolean
    ): Single<List<RideCacheModel>> =
        Single.create<List<RideCacheModel>> { emitter ->
            var listRideDataModel = listOf<RideCacheModel>()
            databaseProvider.executeTransaction({ dbInstance ->
                val targetSystem =
                    if (forSent) TARGET_SYSTEM_SENT else TARGET_SYSTEM_TOLLED_OR_LIGHT
                val realmResult = dbInstance.where(RideCacheRealmModel::class.java)
                    .equalTo("targetSystem", targetSystem)
                    .sort("id", Sort.ASCENDING)
                    .limit(size.toLong())
                    .findAll()
                listRideDataModel = dbInstance.copyFromRealm(realmResult)
                    .toList()
                    .map { it.toModel() }
            }, {
                emitter.onSuccess(listRideDataModel)
            }, {
                emitter.onError(it)
            })
            // realm works correctly only if all operations are on one thread, also they are supposed to be run on main thread, therefore we're doing this
        }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun addData(rideCacheModel: RideCacheModel): Single<Boolean> =
        Single.create<Boolean> { emitter ->
            databaseProvider.executeTransaction({ dbInstance ->
                var minId = 0L
                // get last in ID:
                val realmResult =
                    dbInstance.where(RideCacheRealmModel::class.java)
                        .sort("id", Sort.DESCENDING)
                        .limit(1)
                        .findAll()
                if (realmResult.size > 0)
                    minId = realmResult[0]!!.id + 1
                val dbInsertionObject = RideCacheRealmModel(
                    minId,
                    rideCacheModel.timestamp,
                    rideCacheModel.typeId,
                    rideCacheModel.serviceTypeId,
                    rideCacheModel.payload,
                    rideCacheModel.withLocation,
                    rideCacheModel.fromPureGps,
                    rideCacheModel.targetSystem.mapForBatches()
                )
                dbInstance.insert(
                    dbInsertionObject
                )
                Log.d(
                    "Database_analyze",
                    "Added object with id ${dbInsertionObject.id} for targetSystem=${dbInsertionObject.targetSystem} as ${dbInsertionObject.payload}"
                )
            }, {
                emitter.onSuccess(true)
            }, {
                emitter.onError(it)
            })
            // realm works correctly only if all operations are on one thread, also they are supposed to be run on main thread, therefore we're doing this
        }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun removeDataByIds(ids: List<Long>): Single<Boolean> =
        Single.create<Boolean> { emitter ->
            var success = true
            databaseProvider.executeTransaction({ dbInstance ->
                for (id in ids) {
                    val realmResult = dbInstance.where(RideCacheRealmModel::class.java)
                        .equalTo("id", id)
                        .findAll()
                    success = success && realmResult.deleteAllFromRealm()
                }
                if (success) {
                    Log.d("Database_analyze", "Removed objects -> $ids")
                } else
                    Log.d("Database_analyze", "Removal was NOT SUCCEED -> $ids")
            }, {
                emitter.onSuccess(success)
            }, {
                // one time-retry
                removeDataByIds(ids).subscribe({
                    emitter.onSuccess(success)
                }, {
                    emitter.onError(it)
                })
            })
            // realm works correctly only if all operations are on one thread, also they are supposed to be run on main thread, therefore we're doing this
        }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())


    override fun count(
    ): Single<Int> = Single.create<Int> { emitter ->
        var realmResult: Int = 0
        databaseProvider.executeTransaction({ dbInstance ->
            var query = dbInstance.where(RideCacheRealmModel::class.java)
                .sort("timestamp", Sort.ASCENDING)
            realmResult = query
                .findAll()
                .count()
        }, {
            emitter.onSuccess(realmResult)
        }, {
            emitter.onError(it)
        })
        // realm works correctly only if all operations are on one thread, also they are supposed to be run on main thread, therefore we're doing this
    }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun clearDatabase(): Single<Boolean> = Single.create<Boolean> { emitter ->
        var output = false
        databaseProvider.executeTransaction({ dbInstance ->
            val realmResult = dbInstance.where(RideCacheRealmModel::class.java)
                .findAll()
            val realmResult2 = dbInstance.where(RideConfigRealmModel::class.java)
                .findAll()
            output = realmResult.deleteAllFromRealm() && realmResult2.deleteAllFromRealm()
        }, {
            emitter.onSuccess(output)
        }, {
            emitter.onError(it)
        })
        // realm works correctly only if all operations are on one thread, also they are supposed to be run on main thread, therefore we're doing this
    }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    override fun isAnyServiceEventPending(sent: Boolean): Single<Boolean> =
        Single.create<Boolean> { emitter ->
            var output = false
            databaseProvider.executeTransaction({ dbInstance ->
                val targetSystem =
                    if (sent) TARGET_SYSTEM_SENT else TARGET_SYSTEM_TOLLED_OR_LIGHT
                val realmResult = dbInstance.where(RideCacheRealmModel::class.java)
                    .equalTo("targetSystem", targetSystem)
                    .greaterThan("serviceTypeId", 0)
                    .limit(1)
                    .findAll()
                output = dbInstance.copyFromRealm(realmResult).isNotEmpty()
            }, {
                emitter.onSuccess(output)
            }, {
                emitter.onError(it)
            })
            // realm works correctly only if all operations are on one thread, also they are supposed to be run on main thread, therefore we're doing this
        }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())

    private fun Int.mapForBatches(): Int {
        if (this == TARGET_SYSTEM_TOLLED_OR_LIGHT || this == TARGET_SYSTEM_SENT) return this
        // else return tolled - as light is also tolled
        return TARGET_SYSTEM_TOLLED_OR_LIGHT
    }
}