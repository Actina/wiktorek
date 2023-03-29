package pl.gov.mf.etoll.core.vehiclesdisplaymanagement

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

sealed class VehiclesDisplayManagementUC {
    class GetVehiclesDividedIntoRecentAndOtherUseCase(private val recentVehiclesProvider: RecentVehiclesProvider) :
        VehiclesDisplayManagementUC() {
        fun execute() = recentVehiclesProvider.getVehiclesDividedIntoRecentAndOther()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    }

    class AddRecentVehiclesUseCase(private val recentVehiclesProvider: RecentVehiclesProvider) :
        VehiclesDisplayManagementUC() {
        fun executeAsync(coreVehicleId: Long) =
            recentVehiclesProvider.addRecentVehicle(coreVehicleId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}