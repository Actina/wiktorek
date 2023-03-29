package pl.gov.mf.etoll.core

import pl.gov.mf.etoll.core.devicecompatibility.DeviceCompatibilityUC.*
import pl.gov.mf.etoll.core.foregroundservice.ForegroundServiceUC.StartForegroundServiceUseCase
import pl.gov.mf.etoll.core.foregroundservice.ForegroundServiceUC.StopForegroundServiceUseCase

interface CoreComponent {
    fun useCaseStartForegroundService(): StartForegroundServiceUseCase
    fun useCaseStopForegroundService(): StopForegroundServiceUseCase

    fun useCaseCheckBatteryOptimisation(): CheckBatteryOptimisationUseCase
    fun useCaseCheckInternetConnection(): CheckInternetConnectionUseCase
    fun useCaseCheckGpsType(): CheckGpsTypeUseCase
}