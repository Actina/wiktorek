package pl.gov.mf.etoll.core.ridecoordinatorv2.rc

import androidx.annotation.Keep
import io.reactivex.Observable
import io.reactivex.Single
import pl.gov.mf.etoll.commons.Constants
import pl.gov.mf.etoll.core.model.CoreAccountInfo
import pl.gov.mf.etoll.core.model.CoreSent
import pl.gov.mf.etoll.core.model.CoreSentMap
import pl.gov.mf.etoll.core.model.CoreVehicle
import pl.gov.mf.etoll.core.ridecoordinatorv2.events.EventHelperCallbacks
import pl.gov.mf.etoll.core.ridecoordinatorv2.events.SentChangeActionType
import pl.gov.mf.etoll.initialization.LoadableSystem
import pl.gov.mf.mobile.utils.JsonConvertible

interface RideCoordinatorV3 : LoadableSystem {
    companion object {
        val MAX_AMOUNT_OF_ITEMS_PER_PACKAGE = 60
        var SECONDS_BETWEEN_COLLECTING_TOLLED = 5
        var SECONDS_BETWEEN_COLLECTING_SENT = 25
        var SECONDS_BETWEEN_SEND_TRY_TOLLED = 60
        var SECONDS_BETWEEN_SEND_TRY_SENT = 60
    }

    fun startRide()

    fun stopRide(ignoreNotStartedStatus: Boolean = false): Single<Boolean>

    fun checkAndResumeRideAfterAppRestart()

    fun setCallbacks(callbacks: RideCoordinatorV3Callbacks?, tag: String)

    fun sanity(): RideCoordinatorSanityCheck

    fun getConfiguration(): RideCoordinatorConfiguration?

    fun observeConfiguration(): Observable<RideCoordinatorConfiguration>

    fun hardStopRideWithoutEvents()

    fun timeTresholdsForNotifications(): RideCoordinatorThresholds

    fun onConfigurationUpdated(newConfig: RideCoordinatorConfiguration)

    fun clearConfiguration()

    /**
     * Resume ride that was disabled after configuration
     */
    fun resumeDisabledRide(): Boolean

    /**
     * Pause tolled ride during mixed one
     */
    fun pauseTolledRide()
}

interface RideCoordinatorTolledController {
    fun startTolled(notifyRideStarted: Boolean)

    fun stopTolled()
}

interface RideCoordinatorSentController {
    fun startSent(sentName: CoreSent?)

    fun stopSent(sent: CoreSent?)

    fun cancelSent(sent: CoreSent?)
}

interface RideCoordinatorDeviceController {
    fun changeDeviceToOBU()

    fun changeDeviceToMobileApp()
}

interface RideCoordinatorSanityCheck {
    fun isReallyRunning(): Boolean
}

interface RideCoordinatorGpsConfiguration {
    fun useOnlyGps()

    fun useOnlyAgps()

    fun debugUseBoth()
}

interface RideCoordinatorV3Callbacks : EventHelperCallbacks {
    fun onGpsWarmingStateChanged(warming: Boolean, readyToUse: Boolean)

    fun onTolledRideChange(
        started: Boolean,
        usedVehicle: Long?,
        exceedingWeightDeclared: Boolean = false
    )

    fun onRideCoordinatorError(cause: String?)

    fun onRideStart(
        rideType: RideType,
        vehicle: CoreVehicle?,
        monitoring: MonitoringDeviceConfiguration,
        exceedingWeightDeclared: Boolean = false,
        accountInfo: CoreAccountInfo?,
        selectedSentList: MutableList<CoreSent>? = null
    )

    fun onRideStop(
        type: RideType,
        vehicle: CoreVehicle? = null,
        monitoring: MonitoringDeviceConfiguration,
        exceedingWeightDeclared: Boolean = false,
        accountInfo: CoreAccountInfo?,
        selectedSentList: MutableList<CoreSent>? = null
    )

    fun onSentRideChange(
        action: SentChangeActionType,
        sentItem: CoreSent?,
        monitoring: MonitoringDeviceConfiguration,
        savedSuccessfully: () -> Unit = {}
    )

    fun onObservationDeviceChanged(newOneIsMobileApp: Boolean, obeId: String)

    fun onTrailerChange(exceedingWeightDeclared: Boolean = false)

    enum class RideType {
        @Keep
        TOLLED,

        @Keep
        SENT
    }
}

data class RideCoordinatorConfiguration(
    var duringRide: Boolean,
    var generatedAtLeastOneEventInSessionForTolled: Boolean = false,
    var generatedAtLeastOneEventInSessionForSent: Boolean = false,
    var monitoringDeviceConfiguration: MonitoringDeviceConfiguration? = null,
    var sentConfiguration: SentConfiguration? = null,
    var tolledConfiguration: TolledConfiguration? = null,
    var tolledIsPossibleToBeEnabled: Boolean,
    var disabledTolledConfiguration: TolledConfiguration? = null,
    val uniqueTimestamp: Long = System.currentTimeMillis()
) : JsonConvertible {
    val isConfigured: Boolean
        get() = duringTolled || duringSent

    val duringTolled: Boolean
        get() = tolledConfiguration != null // probably there would be also "tolled disabled" flag which would be used also in this check
    val duringSent: Boolean
        get() = sentConfiguration != null
    val tolledVehicleId: Long?
        get() = tolledConfiguration?.vehicleId
    val tolledAccountNumberId: Long?
        get() = tolledConfiguration?.vehicle?.accountInfo?.id
    val trackByApp: Boolean
        get() = monitoringDeviceConfiguration?.monitoringByApp ?: true
    val lightMode: Boolean
        get() = tolledConfiguration?.lightMode ?: false
    val isAccountInitialized: Boolean
        get() = tolledConfiguration?.vehicle?.accountInfo?.balance?.isInitialized ?: false

    // sent will override tolled settings in mixed mode
    val monitoringDeviceCanBeChanged: Boolean
        get() = ((!duringSent && tolledConfiguration?.vehicle?.geolocator != null) ||
                // if we're monitored by app, then we can change only if selected list is not empty and no "other" packages are selected
                (duringSent && monitoringDeviceConfiguration?.monitoringByApp == true && !sentConfiguration!!.selectedSentList.isNullOrEmpty() && sentConfiguration!!.noOtherPackagesSelected()) ||
                // always when currently with zsl and in sent
                (duringSent && monitoringDeviceConfiguration?.monitoringByApp == false))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RideCoordinatorConfiguration

        if (duringRide != other.duringRide) return false
        if (generatedAtLeastOneEventInSessionForTolled != other.generatedAtLeastOneEventInSessionForTolled) return false
        if (generatedAtLeastOneEventInSessionForSent != other.generatedAtLeastOneEventInSessionForSent) return false
        if (monitoringDeviceConfiguration != other.monitoringDeviceConfiguration) return false
        if (sentConfiguration != other.sentConfiguration) return false
        if (tolledConfiguration != other.tolledConfiguration) return false
        if (tolledIsPossibleToBeEnabled != other.tolledIsPossibleToBeEnabled) return false
        if (disabledTolledConfiguration != other.disabledTolledConfiguration) return false
        if (uniqueTimestamp != other.uniqueTimestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = duringRide.hashCode()
        result = 31 * result + generatedAtLeastOneEventInSessionForSent.hashCode()
        result = 31 * result + generatedAtLeastOneEventInSessionForTolled.hashCode()
        result = 31 * result + (monitoringDeviceConfiguration?.hashCode() ?: 0)
        result = 31 * result + (sentConfiguration?.hashCode() ?: 0)
        result = 31 * result + (tolledConfiguration?.hashCode() ?: 0)
        result = 31 * result + tolledIsPossibleToBeEnabled.hashCode()
        result = 31 * result + (disabledTolledConfiguration?.hashCode() ?: 0)
        result = 31 * result + uniqueTimestamp.hashCode()
        return result
    }


}


data class MonitoringDeviceConfiguration(
    // we're monitoring by app or OBE
    var monitoringByApp: Boolean = true,
    // if by OBE, then this id should not be null or empty
    var monitoringOBEId: String = "",
    var monitoringCanBeChanged: Boolean = true
) : JsonConvertible

data class SentConfiguration(
    // we're in mode without active packages
    var selectedSentList: MutableList<CoreSent> = mutableListOf(),
    var availableSentList: CoreSentMap = CoreSentMap(mutableMapOf()),
    var finishedSentList: MutableList<CoreSent> = mutableListOf(),
    var sentSelectionWasPossibleAndDone: Boolean,
    var sentListWasDownloaded: Boolean
) : JsonConvertible {
    val sentWithoutPackage: Boolean
        get() = selectedSentList.size == 0
}

data class TolledConfiguration(
    var vehicle: CoreVehicle? = null,
    var trailerUsedAndCategoryWillBeIncreased: Boolean = false
) : JsonConvertible {
    val lightMode: Boolean
        get() = vehicle?.category == 13 || vehicle?.category == 14

    // selected vehicleId
    val vehicleId: Long?
        get() = vehicle?.id
}

data class RideCoordinatorThresholds(
    val gpsYellowMinutes: Int,
    val gpsRedMinutes: Int,
    val dataConnectionYellowMinutes: Int,
    val dataConnectionRedMinutes: Int,
    val batteryYellowPercent: Int,
    val batteryRedPercent: Int,
    val timeForGpsToWarmUp: Int
)

private fun SentConfiguration.noOtherPackagesSelected(): Boolean {
    if (availableSentList.data?.keys?.contains(Constants.SENT_GROUP_OTHER) != true) return true
    if (availableSentList.data.isNullOrEmpty()) return true
    for (sent in selectedSentList) {
        for (orig in availableSentList.data!![Constants.SENT_GROUP_OTHER]!!)
            if (orig.sentNumber.contentEquals(sent.sentNumber))
                return false
    }
    return true
}
