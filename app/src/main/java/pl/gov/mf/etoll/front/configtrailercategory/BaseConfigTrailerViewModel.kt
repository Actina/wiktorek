package pl.gov.mf.etoll.front.configtrailercategory

import pl.gov.mf.etoll.base.BaseDatabindingViewModel

abstract class BaseConfigTrailerViewModel : BaseDatabindingViewModel() {
//    var tempConfiguration = ConfigTrailerData()
////
////    @Inject
////    lateinit var getRideConfigurationUseCase: CoreComposedUC.GetRideConfigurationUseCase
////
////    @Inject
////    lateinit var updateRideConfigurationUseCase: CoreComposedUC.UpdateRideConfigurationUseCase
//
//    @Inject
//    lateinit var configurationCoordinator: RideConfigurationCoordinator
//
//    protected fun entryFromConfig() =
//        tempConfiguration.entryScreenId == R.id.configVehicleSelectionFragment
//
//    private fun entryFromRideDetails() = tempConfiguration.entryScreenId == R.id.rideDetailsFragment
//
//    private fun finishConfigurationAndGoToDashboard() {
//        TODO()
////        compositeDisposable.addSafe(
////            updateRideConfigurationUseCase.execute(getRideConfigurationUseCase.execute().apply {
////                this.rideConfigured = true
////                // for support of mix reconfiguration during ride
////                this.tolledSelectedXorConfigured = true
////            }).subscribe(
////                { navigateToDashboard() },
////                { navigateToError() },
////            )
////        )
//    }
//
//    override fun onResume() {
//        super.onResume()
//        configurationCoordinator.onViewShowing(RideConfigurationCoordinator.RideConfigurationDestination.TRAILER)
//    }
//
//    fun rideWithoutTrailer() {
//        configurationCoordinator.onTrailerSelected(noTrailer = true)
//        configurationCoordinator.getNextStep()
//    }
//
//    fun initArgs(arguments: Bundle) {
//        tempConfiguration = arguments.getString("data")!!.toObject()
//    }
//
//    fun getTrailerData(): String {
//        return tempConfiguration.toJSON()
//    }
//
////    protected fun completeTrailerConfig() {
////        val rideConfiguration = getRideConfigurationUseCase.execute()
////
////        if (entryFromConfig()) {
////            if (rideConfiguration.rideConfigured) {
////                compositeDisposable.addSafe(
////                    updateRideConfigurationUseCase.execute(
////                        getRideConfigurationUseCase.execute().apply {
////                            this.tolledSelectedXorConfigured = true
////                        }).subscribe({ navigateToRideDetails() }, {})
////                )
////            } else
////                if (rideConfiguration.sentSelected)
////                    navigateToMonitoringDevice()
////                else {
////                    finishConfigurationAndGoToDashboard()
////                }
////        } else if (entryFromRideDetails()) {
////            navigateToEntryScreen()
////        }
////    }
//
//    abstract fun navigateToRideDetails()
//
//    abstract fun navigateToDashboard()
//
//    abstract fun navigateToMonitoringDevice()
//
//    abstract fun navigateToEntryScreen()
//
//    abstract fun navigateToError()
}