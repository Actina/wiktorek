package pl.gov.mf.etoll.core.ridecoordinatorv2.rc

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

sealed class RideCoordinatorUC {

    class StartRideUseCase @Inject constructor(private val rc: RideCoordinatorV3) :
        RideCoordinatorUC() {
        fun execute() {
            rc.startRide()
        }
    }

    class StopRideUseCase @Inject constructor(private val rc: RideCoordinatorV3) :
        RideCoordinatorUC() {
        fun execute(): Single<Boolean> =
            rc.stopRide().observeOn(AndroidSchedulers.mainThread())
    }

    class GetRideCoordinatorConfigurationUseCase @Inject constructor(private val rc: RideCoordinatorV3) :
        RideCoordinatorUC() {
        fun execute() = rc.getConfiguration()
    }

    class CheckIfRideIsInProgressUseCase @Inject constructor(private val rc: RideCoordinatorV3) :
        RideCoordinatorUC() {
        fun execute() = rc.getConfiguration()?.duringRide
    }
}