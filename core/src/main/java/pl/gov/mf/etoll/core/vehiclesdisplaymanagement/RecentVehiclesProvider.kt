package pl.gov.mf.etoll.core.vehiclesdisplaymanagement

import io.reactivex.Completable
import io.reactivex.Single
import pl.gov.mf.etoll.core.model.CoreVehicle

interface RecentVehiclesProvider {
    fun getVehiclesDividedIntoRecentAndOther(): Single<Pair<List<CoreVehicle>, List<CoreVehicle>>>
    fun addRecentVehicle(coreVehicleId: Long): Completable
}