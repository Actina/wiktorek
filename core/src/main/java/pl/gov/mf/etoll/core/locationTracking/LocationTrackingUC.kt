package pl.gov.mf.etoll.core.locationTracking

import io.reactivex.Completable
import pl.gov.mf.etoll.core.ridecoordinatorv2.location.LocationServiceProvider

sealed class LocationTrackingUC {

    class StartLocationTrackingUseCase(private val locationServiceProvider: LocationServiceProvider) {
        fun execute(): Completable = Completable.fromRunnable {
            locationServiceProvider.start()
        }
    }

    class StopLocationTrackingUseCase(private val locationServiceProvider: LocationServiceProvider) {
        fun execute(): Completable = Completable.fromRunnable {
            locationServiceProvider.stop()
        }
    }

    class GetLastLocationUseCase(private val locationServiceProvider: LocationServiceProvider) {
        fun execute() = locationServiceProvider.getLastKnownSavedLocation()
    }

    class GetStartingLocationUseCase(private val locationServiceProvider: LocationServiceProvider) {
        fun execute() = locationServiceProvider.getStartLocation()
    }

}