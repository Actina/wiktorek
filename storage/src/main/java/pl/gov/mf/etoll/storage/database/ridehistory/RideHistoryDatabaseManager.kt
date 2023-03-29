package pl.gov.mf.etoll.storage.database.ridehistory

import io.reactivex.Completable
import io.reactivex.Single
import pl.gov.mf.etoll.storage.database.ridehistory.model.RideHistoryModel

interface RideHistoryDatabaseManager {
    fun addData(item: RideHistoryModel): Completable

    fun getAll(): Single<List<RideHistoryModel>>

    fun getForPeriod(from: Long, to: Long): Single<List<RideHistoryModel>>

    fun deleteObsolete(): Single<Boolean>

    fun getById(id: Int): Single<RideHistoryModel>
}