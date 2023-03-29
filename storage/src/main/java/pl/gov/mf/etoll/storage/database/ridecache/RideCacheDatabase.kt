package pl.gov.mf.etoll.storage.database.ridecache

import io.reactivex.Single
import pl.gov.mf.etoll.storage.database.ridecache.model.RideCacheModel

interface RideCacheDatabase {
    fun getAndIncrementLatestTolledId(): Single<Long>
    fun getAndIncrementLatestSentId(): Single<Long>
    fun getDataPackage(
        size: Int,
        forSent: Boolean
    ): Single<List<RideCacheModel>>

    fun addData(rideCacheModel: RideCacheModel): Single<Boolean>
    fun removeDataByIds(ids: List<Long>): Single<Boolean>
    fun count(): Single<Int>
    fun clearDatabase(): Single<Boolean>
    fun isAnyServiceEventPending(sent: Boolean): Single<Boolean>
}