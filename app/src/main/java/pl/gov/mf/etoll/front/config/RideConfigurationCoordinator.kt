package pl.gov.mf.etoll.front.config

import pl.gov.mf.etoll.core.model.CoreSent
import pl.gov.mf.etoll.core.model.CoreSentMap
import pl.gov.mf.etoll.core.model.CoreStatus
import pl.gov.mf.etoll.core.model.CoreVehicle
import pl.gov.mf.etoll.front.configmonitoringdevice.SelectedMonitoringDevice
import pl.gov.mf.etoll.front.configridetypeselection.RideSelectionState

interface RideConfigurationCoordinator {
    /**
     * Reset helper before starting new configuration
     */
    fun resetCoordinator()

    /**
     * Set data if parts should be used only for setting up single element
     */
    fun setDataBasedOnExistingConfiguration(
        destination: RideConfigurationDestination
    )

    /**
     * Let know coordinator which view is currently showing
     */
    fun onViewShowing(destination: RideConfigurationDestination)

    // functions as reactions to user actions(finishing views)

    /**
     * Set data from step - ride mode selection
     */
    fun onRideModeSelected(tolled: Boolean, sent: Boolean)

    /**
     * Set data from step - initial configuration
     */
    fun onConfigurationStarted(
        sentIsPossible: Boolean,
        tolledIsPossible: Boolean,
        sentWasOffline: Boolean,
        sentList: CoreSentMap?,
        status: CoreStatus
    )

    /**
     * Set data from step - trailer data
     */
    fun onTrailerSelected(increaseFlagDeclared: Boolean)

    /**
     * Set data from step - vehicle selection
     */
    fun onVehicleSelected(vehicle: CoreVehicle)

    /**
     * Set data from step - monitoring device selection
     */
    fun onMonitoringDeviceSelected(selectedPhone: Boolean)

    /**
     * Set data from step - sent package selection
     */
    fun onSentPackagesSelected(selectedObeId: String?, selectedSentList: MutableList<CoreSent>)

    /**
     * Ask coordinator about next expected step to navigate to
     */
    fun getNextStep(): RideConfigurationDestination


    /**
     * Prepare data for step - ride selection
     */
    fun generateViewDataForRideSelection(): RideSelectionState

    /**
     * Prepare data for step - sent rides selection
     */
    fun generateViewDataForSentRidesSelection(): SentListData

    /**
     * Prepare data for step - trailer
     */
    fun vehicleCategoryCanBeIncreased(): Boolean

    /**
     * Prepare data for dialog - sent configuration was offline
     */
    fun sentConfigurationIsDoneOffline(): Boolean

    /**
     * Prepare data for step - monitoring device
     */
    fun generateViewDataForMonitoringDevice(): SelectedMonitoringDevice?

    /**
     * Prepare data for step - vehicle selection
     */
    fun generateViewDataForVehicleSelection(): CoreVehicle?

    /**
     * Prepare data for step - trailer
     */
    fun generateViewDataForTrailerInfo(): Boolean

    /**
     * Prepare data for step - ride selection, sent not available for motorcycles
     */
    fun generateMotorcycleStepVehiclesList(): Array<CoreVehicle>
    fun isSentRideSelected(): Boolean

    /**
     * All navigation targets available for configuration
     */
    enum class RideConfigurationDestination {
        VEHICLE_SELECTION, TRAILER, MONITORING_DEVICE, SENT_LIST, SAVE_AND_FINISH, RIDE_MODE_SELECTION
    }
}

/**
 * Helper data class for sent packages view
 */
data class SentListData(
    val monitoringByApp: Boolean,
    val sentList: CoreSentMap,
    val backButtonShouldBeHidden: Boolean
)