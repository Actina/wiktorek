package pl.gov.mf.etoll.core.ridehistory.collector

import io.reactivex.Completable
import pl.gov.mf.etoll.core.ridehistory.model.ActivityAdditionalData

interface RideHistoryCollector {
    fun onInternetStrengthMoveFromFairToCritical(): Completable
    fun onInternetStrengthMoveFromCriticalToFair(): Completable
    fun onBatteryLevelMoveFromFairToRed(): Completable
    fun onBatteryLevelMoveFromRedToFair(): Completable
    fun onGpsSignalLost(): Completable
    fun onGpsSignalRetrieve(): Completable
    fun onPrePaidTopUp(data: ActivityAdditionalData.PrePaidAccountTopUp): Completable
    fun start()
    fun onFakeGpsDetected():Completable
}