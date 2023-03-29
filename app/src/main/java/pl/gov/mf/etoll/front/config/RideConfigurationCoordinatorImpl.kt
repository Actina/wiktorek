package pl.gov.mf.etoll.front.config

import pl.gov.mf.etoll.core.model.CoreSent
import pl.gov.mf.etoll.core.model.CoreSentMap
import pl.gov.mf.etoll.core.model.CoreStatus
import pl.gov.mf.etoll.core.model.CoreVehicle
import pl.gov.mf.etoll.core.ridecoordinatorv2.rc.*
import pl.gov.mf.etoll.front.configmonitoringdevice.SelectedMonitoringDevice
import pl.gov.mf.etoll.front.configridetypeselection.RideSelectionState
import pl.gov.mf.mobile.utils.deepCopy
import javax.inject.Inject

class RideConfigurationCoordinatorImpl @Inject constructor(private val rideCoordinatorV3: RideCoordinatorV3) :
    RideConfigurationCoordinator {

    private var step0SentIsPossible: Boolean = false
    private var step0TolledIsPossible: Boolean = false
    private var step0SentIsOffline: Boolean = false
    private var step0SentList: CoreSentMap? = null
    private var step0Status: CoreStatus? = null
    private var step1TolledSelected: Boolean = false
    private var step1SentSelected: Boolean = false
    private var step2SelectedVehicle: CoreVehicle? = null
    private var step3TrailerExceedsWeight: Boolean = false
    private var step4MonitoringByApp: Boolean = true
    private var step4MonitoringCanBeChanged: Boolean = true
    private var step5selectedObeId: String? = null
    private var step5selectedSentList: MutableList<CoreSent>? = null
    private var presetConfiguration: RideCoordinatorConfiguration? = null
    private var presetStartingPoint: RideConfigurationCoordinator.RideConfigurationDestination? =
        null
    private var configurationMode: ConfigurationMode = ConfigurationMode.INITIAL_CONFIGURATION

    private var currentStep: RideConfigurationCoordinator.RideConfigurationDestination =
        RideConfigurationCoordinator.RideConfigurationDestination.RIDE_MODE_SELECTION

    override fun setDataBasedOnExistingConfiguration(
        destination: RideConfigurationCoordinator.RideConfigurationDestination
    ) {
        resetCoordinator()
        presetConfiguration = rideCoordinatorV3.getConfiguration()!!.deepCopy()
        presetStartingPoint = destination
        configurationMode = ConfigurationMode.CHANGE_DURING_RIDE
        step0SentList = presetConfiguration!!.sentConfiguration?.availableSentList
        step4MonitoringByApp = presetConfiguration!!.trackByApp
        currentStep = destination
        step2SelectedVehicle = presetConfiguration!!.tolledConfiguration?.vehicle
            ?: presetConfiguration!!.disabledTolledConfiguration?.vehicle
        step3TrailerExceedsWeight =
            presetConfiguration!!.tolledConfiguration?.trailerUsedAndCategoryWillBeIncreased
                ?: presetConfiguration!!.disabledTolledConfiguration?.trailerUsedAndCategoryWillBeIncreased
                        ?: false
    }

    override fun resetCoordinator() {
        // set default values
        step0SentIsPossible = false
        step0TolledIsPossible = false
        step0SentIsOffline = false
        step0Status = null
        step0SentList = null
        step1SentSelected = false
        step1TolledSelected = false
        step2SelectedVehicle = null
        step3TrailerExceedsWeight = false
        step4MonitoringByApp = true
        step4MonitoringCanBeChanged = true
        presetConfiguration = null
        step5selectedObeId = null
        step5selectedSentList = null
        configurationMode = ConfigurationMode.INITIAL_CONFIGURATION
        presetStartingPoint = null
    }

    override fun onRideModeSelected(tolled: Boolean, sent: Boolean) {
        step1TolledSelected = tolled
        step1SentSelected = sent
    }

    override fun sentConfigurationIsDoneOffline(): Boolean =
        step0SentIsOffline && step0SentIsPossible && step1SentSelected

    override fun onConfigurationStarted(
        sentIsPossible: Boolean,
        tolledIsPossible: Boolean,
        sentWasOffline: Boolean,
        sentList: CoreSentMap?,
        status: CoreStatus
    ) {
        step0SentIsOffline = sentWasOffline
        step0SentIsPossible = sentIsPossible
        step0TolledIsPossible = tolledIsPossible
        step0Status = status
        step0SentList = sentList
    }

    override fun getNextStep(): RideConfigurationCoordinator.RideConfigurationDestination =
        if (configurationMode == ConfigurationMode.CHANGE_DURING_RIDE) {
            // remember that coordinator won't have all values in this path !
            // live configurations always lead to finishing
            if (currentStep == RideConfigurationCoordinator.RideConfigurationDestination.VEHICLE_SELECTION && step2SelectedVehicle!!.categoryCanBeIncreased) {
                step3TrailerExceedsWeight = step2SelectedVehicle?.tollCategoryIncreaseFlag ?: false
                RideConfigurationCoordinator.RideConfigurationDestination.TRAILER
                // TODO: remember to limit vehicles to just those with same monitoring options as selected one
            } else if (currentStep == RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE &&
                presetConfiguration!!.duringSent && presetConfiguration!!.sentConfiguration!!.sentWithoutPackage && !step4MonitoringByApp
            ) {
                // we should go to sent packages selection
                step5selectedSentList = presetConfiguration!!.sentConfiguration!!.selectedSentList
                step5selectedObeId = if (step4MonitoringByApp) null else step5selectedObeId
                step0SentList = presetConfiguration!!.sentConfiguration!!.availableSentList
                RideConfigurationCoordinator.RideConfigurationDestination.SENT_LIST
            } else {
                saveAndAdaptNewChangesForConfiguration()
                RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH
            }
        } else
            when (currentStep) {
                RideConfigurationCoordinator.RideConfigurationDestination.VEHICLE_SELECTION -> getNextStepAfterVehicleSelection()
                RideConfigurationCoordinator.RideConfigurationDestination.TRAILER -> getNextStepAfterTrailerSelection()
                RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE -> getNextStepAfterMonitoringDeviceSelection()
                RideConfigurationCoordinator.RideConfigurationDestination.SENT_LIST -> getNextStepAfterSentListSelection()
                RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH -> throw RuntimeException(
                    "Navigation to SAVE_AND_FINISH not possible - error in flow of configuration coordinator"
                )
                RideConfigurationCoordinator.RideConfigurationDestination.RIDE_MODE_SELECTION -> getNextStepAfterRideSelection()
            }

    private fun getNextStepAfterSentListSelection(): RideConfigurationCoordinator.RideConfigurationDestination {
        saveAndAdaptNewChangesForConfiguration()
        return RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH
    }

    private fun getNextStepAfterMonitoringDeviceSelection(): RideConfigurationCoordinator.RideConfigurationDestination {
        if (step1SentSelected) {
            if (step0SentIsOffline) {
                saveAndAdaptNewChangesForConfiguration()
                return RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH
            }
            // are there any sent packages to be selected?
            if (step0SentList?.data?.size ?: 0 > 0) {
                return RideConfigurationCoordinator.RideConfigurationDestination.SENT_LIST
            } else {
                // no packages to select, just start sent without package
                saveAndAdaptNewChangesForConfiguration()
                return RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH
            }
        } else {
            // end of tolled path
            saveAndAdaptNewChangesForConfiguration()
            return RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH
        }
    }

    private fun getNextStepAfterTrailerSelection(): RideConfigurationCoordinator.RideConfigurationDestination {
        if (step1SentSelected) {
            // mixed ride
            if (step2SelectedVehicle!!.geolocator != null && !step0SentIsOffline) {
                // we should show geolocator selection
                return RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE
            } else {
                step4MonitoringByApp = true
                step4MonitoringCanBeChanged = false
                if (step0SentIsOffline) {
                    saveAndAdaptNewChangesForConfiguration()
                    return RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH
                } else {
                    // go to monitoring device selection, even as there would be just one option possible
                    return RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE
                }
            }
        } else {
            // tolled
            if (step2SelectedVehicle!!.geolocator != null) {
                // we should show geolocator selection
                return RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE
            } else {
                step4MonitoringByApp = true
                step4MonitoringCanBeChanged = false
                // finish configuration
                saveAndAdaptNewChangesForConfiguration()
                return RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH
            }
        }
    }

    private fun getNextStepAfterVehicleSelection(): RideConfigurationCoordinator.RideConfigurationDestination {
        // pass info about trailer flag to next view if possible
        step3TrailerExceedsWeight = step2SelectedVehicle?.tollCategoryIncreaseFlag ?: false
        if (step1SentSelected) {
            // mixed ride
            if (step2SelectedVehicle!!.categoryCanBeIncreased) {
                // we should show trailer question
                return RideConfigurationCoordinator.RideConfigurationDestination.TRAILER
            } else {
                if (step2SelectedVehicle!!.geolocator != null && !step0SentIsOffline) {
                    // we should show geolocator selection
                    return RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE
                } else {
                    step4MonitoringByApp = true
                    step4MonitoringCanBeChanged = false
                    if (step0SentIsOffline) {
                        saveAndAdaptNewChangesForConfiguration()
                        return RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH
                    } else {
                        // go to monitoring device selection, even as there would be just one option possible
                        return RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE
                    }
                }
            }
        } else {
            // tolled
            if (step2SelectedVehicle!!.categoryCanBeIncreased) {
                // we should show trailer question
                return RideConfigurationCoordinator.RideConfigurationDestination.TRAILER
            } else
                if (step2SelectedVehicle!!.geolocator != null) {
                    // we should show geolocator selection
                    return RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE
                } else {
                    step4MonitoringByApp = true
                    step4MonitoringCanBeChanged = false
                    // finish configuration
                    saveAndAdaptNewChangesForConfiguration()
                    return RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH
                }
        }
    }

    private fun getNextStepAfterRideSelection(): RideConfigurationCoordinator.RideConfigurationDestination {
        // check if we could auto-select some stuff, and select destination
        // we assume there is no need to check if step0 is in collision with step1 decisions
        if (step1SentSelected && step1TolledSelected) {
            // mix ride
            // first - check stuff for tolled
            if (step0Status != null && step0Status!!.vehicles.size == 1) {
                // we can skip vehicle configuration
                step2SelectedVehicle = step0Status!!.vehicles[0]
                // check if we can skip monitoring device selection
                // this is mandatory if we're offline in mixed mode
                if (step2SelectedVehicle!!.categoryCanBeIncreased) {
                    // we should show trailer question
                    return RideConfigurationCoordinator.RideConfigurationDestination.TRAILER
                } else {
                    if (step2SelectedVehicle!!.geolocator != null && !step0SentIsOffline) {
                        // we should show geolocator selection
                        return RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE
                    } else {
                        step4MonitoringByApp = true
                        step4MonitoringCanBeChanged = false
                        if (step0SentIsOffline) {
                            saveAndAdaptNewChangesForConfiguration()
                            return RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH
                        } else {
                            // go to monitoring device selection, even as there would be just one option possible
                            return RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE
                        }
                    }
                }
            } else {
                // go to vehicle configuration
                return RideConfigurationCoordinator.RideConfigurationDestination.VEHICLE_SELECTION
            }
        } else if (step1SentSelected) {
            // just sent
            // check if we're offline
            if (step0SentIsOffline) {
                saveAndAdaptNewChangesForConfiguration()
                return RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH
            } else {
                // we are not offline, go to monitoring device selection
                return RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE
            }
        } else if (step1TolledSelected) {
            // just tolled
            if (step0Status != null && step0Status!!.vehicles.size == 1) {
                // we can skip vehicle configuration
                step2SelectedVehicle = step0Status!!.vehicles[0]
                step3TrailerExceedsWeight = step2SelectedVehicle?.tollCategoryIncreaseFlag ?: false
                // check if we can skip trailer selection
                if (step2SelectedVehicle!!.categoryCanBeIncreased) {
                    // we should show trailer question
                    return RideConfigurationCoordinator.RideConfigurationDestination.TRAILER
                } else {
                    // trailer could be skipped
                    // check if we can skip monitoring device selection
                    if (step2SelectedVehicle!!.geolocator != null) {
                        // we should show geolocator selection
                        return RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE
                    } else {
                        step4MonitoringByApp = true
                        step4MonitoringCanBeChanged = false
                        // finish configuration
                        saveAndAdaptNewChangesForConfiguration()
                        return RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH
                    }
                }
            } else {
                // go to vehicle configuration
                return RideConfigurationCoordinator.RideConfigurationDestination.VEHICLE_SELECTION
            }
        }
        // impossible option, left for compatibility
        // maybe we should add error here ?
        return RideConfigurationCoordinator.RideConfigurationDestination.RIDE_MODE_SELECTION
    }

    override fun onViewShowing(destination: RideConfigurationCoordinator.RideConfigurationDestination) {
        currentStep = destination

        // we want to reset some values, but only in new configuration mode
        if (configurationMode == ConfigurationMode.INITIAL_CONFIGURATION)
            when (destination) {
                RideConfigurationCoordinator.RideConfigurationDestination.VEHICLE_SELECTION -> {
                    step3TrailerExceedsWeight = false
                    step4MonitoringCanBeChanged = true
                    step4MonitoringByApp = true
                    if (step1SentSelected) {
                        step5selectedObeId = null
                        step5selectedSentList = null
                    }
                }
                RideConfigurationCoordinator.RideConfigurationDestination.TRAILER -> {
                    step4MonitoringCanBeChanged = true
                    step4MonitoringByApp = true
                    if (step1SentSelected) {
                        step5selectedObeId = null
                        step5selectedSentList = null
                    }
                }
                RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE -> {
                    if (step1SentSelected) {
                        step5selectedObeId = null
                        step5selectedSentList = null
                    }
                }
                RideConfigurationCoordinator.RideConfigurationDestination.SENT_LIST -> {
                    // do nothing, this is last step
                }
                RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH -> {
                }
                RideConfigurationCoordinator.RideConfigurationDestination.RIDE_MODE_SELECTION -> {
                    step2SelectedVehicle = null
                    step3TrailerExceedsWeight = false
                    step4MonitoringCanBeChanged = true
                    step4MonitoringByApp = true
                    if (step1SentSelected) {
                        step5selectedObeId = null
                        step5selectedSentList = null
                    }
                }
            }
    }

    override fun onSentPackagesSelected(
        selectedObeId: String?,
        selectedSentList: MutableList<CoreSent>
    ) {
        step5selectedObeId = selectedObeId
        step5selectedSentList = selectedSentList
        step0SentIsOffline = false
    }

    override fun onVehicleSelected(vehicle: CoreVehicle) {
        step2SelectedVehicle = vehicle
        step4MonitoringCanBeChanged = step2SelectedVehicle!!.geolocator != null
    }

    override fun onMonitoringDeviceSelected(selectedPhone: Boolean) {
        step4MonitoringByApp = selectedPhone
    }

    override fun generateViewDataForRideSelection(): RideSelectionState =
        RideSelectionState(
            tolledRideIsEnabled = false,
            tolledRideShouldBeVisible = step0TolledIsPossible,
            sentRideIsEnabled = false,
            sentShouldBeVisible = step0SentIsPossible
        )

    override fun generateViewDataForSentRidesSelection(): SentListData =
        SentListData(
            step4MonitoringByApp,
            step0SentList ?: CoreSentMap(mutableMapOf()),
            presetStartingPoint == RideConfigurationCoordinator.RideConfigurationDestination.SENT_LIST
        )

    override fun generateViewDataForVehicleSelection(): CoreVehicle? {
        return step2SelectedVehicle
    }

    override fun onTrailerSelected(increaseFlagDeclared: Boolean) {
        step3TrailerExceedsWeight = increaseFlagDeclared
    }

    override fun generateViewDataForMonitoringDevice(): SelectedMonitoringDevice? {
        return if (configurationMode == ConfigurationMode.INITIAL_CONFIGURATION) {
            if (step1TolledSelected) {
                if (step2SelectedVehicle!!.zslIsPrimaryGeolocator)
                    SelectedMonitoringDevice.OBE
                else
                    SelectedMonitoringDevice.PHONE
            } else {
                null
            }
        } else {
            if (step4MonitoringByApp) SelectedMonitoringDevice.PHONE else SelectedMonitoringDevice.OBE
        }
    }

    override fun vehicleCategoryCanBeIncreased(): Boolean =
        step2SelectedVehicle!!.categoryCanBeIncreased


    private fun saveAndAdaptNewChangesForConfiguration() {
        // generate new ride configuration and save it here
        if (configurationMode == ConfigurationMode.INITIAL_CONFIGURATION) {
            // fresh configuration
            val sentConfiguration: SentConfiguration? =
                if (!step1SentSelected) null else SentConfiguration(
                    step5selectedSentList ?: mutableListOf(),
                    step0SentList ?: CoreSentMap(
                        mutableMapOf()
                    ),
                    mutableListOf(),
                    !step0SentIsOffline,
                    !step0SentIsOffline
                )
            val tolledConfiguration: TolledConfiguration? =
                if (!step1TolledSelected) null else TolledConfiguration(
                    step2SelectedVehicle, step3TrailerExceedsWeight
                )
            val obeId = presetConfiguration?.tolledConfiguration?.vehicle?.geolocator?.number
                ?: presetConfiguration?.sentConfiguration?.availableSentList.findObe(
                    presetConfiguration?.sentConfiguration?.selectedSentList?.firstOrNull()
                )

            var output = RideCoordinatorConfiguration(
                duringRide = false,
                generatedAtLeastOneEventInSessionForTolled = false,
                generatedAtLeastOneEventInSessionForSent = false,
                monitoringDeviceConfiguration = MonitoringDeviceConfiguration(
                    step4MonitoringByApp,
                    if (step4MonitoringByApp) "" else obeId ?: "",
                    step4MonitoringCanBeChanged
                ),
                sentConfiguration,
                tolledConfiguration,
                tolledIsPossibleToBeEnabled = step0TolledIsPossible
            )
            rideCoordinatorV3.onConfigurationUpdated(output)
        } else {
            // use preset configuration (!!)
            if (presetStartingPoint == RideConfigurationCoordinator.RideConfigurationDestination.VEHICLE_SELECTION) {
                // save vehicle info
                presetConfiguration!!.tolledConfiguration =
                    TolledConfiguration(step2SelectedVehicle)

                if (currentStep == RideConfigurationCoordinator.RideConfigurationDestination.TRAILER) {
                    // we need also to update info about trailer
                    presetConfiguration!!.tolledConfiguration!!.trailerUsedAndCategoryWillBeIncreased =
                        step3TrailerExceedsWeight
                }
            } else if (presetStartingPoint == RideConfigurationCoordinator.RideConfigurationDestination.TRAILER) {
                // save trailer info
                presetConfiguration!!.tolledConfiguration!!.trailerUsedAndCategoryWillBeIncreased =
                    step3TrailerExceedsWeight
            } else if (presetStartingPoint == RideConfigurationCoordinator.RideConfigurationDestination.SENT_LIST) {
                // save SENT info
                // FYI: this variable is save, as screen is possible to be entered only in those cases
                presetConfiguration?.sentConfiguration?.sentSelectionWasPossibleAndDone = true //
                presetConfiguration?.sentConfiguration?.selectedSentList =
                    step5selectedSentList ?: mutableListOf()
            } else if (presetStartingPoint == RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE) {
                if (currentStep == RideConfigurationCoordinator.RideConfigurationDestination.SENT_LIST) {
                    // save SENT info
                    presetConfiguration!!.sentConfiguration!!.selectedSentList =
                        step5selectedSentList!!
                }
                // save info about monitoring device changes
                // get obeId from vehicle or selected sent or finished sent
                val obeId = presetConfiguration?.tolledConfiguration?.vehicle?.geolocator?.number
                    ?: presetConfiguration?.sentConfiguration?.availableSentList.findObe(
                        presetConfiguration?.sentConfiguration?.selectedSentList?.firstOrNull()
                    ) ?: presetConfiguration?.sentConfiguration?.availableSentList.findObe(
                        presetConfiguration?.sentConfiguration?.finishedSentList?.firstOrNull()
                    )
                presetConfiguration!!.monitoringDeviceConfiguration = MonitoringDeviceConfiguration(
                    step4MonitoringByApp,
                    if (step4MonitoringByApp) "" else (obeId ?: ""),
                    step4MonitoringCanBeChanged
                )
            }

            // save preset configuration
            rideCoordinatorV3.onConfigurationUpdated(presetConfiguration!!)
            presetConfiguration = null
        }
    }

    override fun generateViewDataForTrailerInfo(): Boolean = step3TrailerExceedsWeight

    override fun generateMotorcycleStepVehiclesList(): Array<CoreVehicle> =
        step0Status?.vehicles ?: emptyArray()

    override fun isSentRideSelected(): Boolean = step1SentSelected
}

private fun CoreSentMap?.findObe(coreSent: CoreSent?): String? {
    if (coreSent == null || this == null) return null
    try {
        for (key in this.data!!.keys) {
            for (sent in this.data!![key]!!) {
                if (sent.sentNumber.equals(coreSent.sentNumber))
                    return key
            }
        }
    } catch (_: Exception) {
    }
    return null
}

enum class ConfigurationMode {
    INITIAL_CONFIGURATION, CHANGE_DURING_RIDE
}