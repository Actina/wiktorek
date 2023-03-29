package pl.gov.mf.etoll.core.ridecoordinatorv2.location.startloc

import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.utils.JsonConvertible
import pl.gov.mf.mobile.utils.LocationWrapper
import pl.gov.mf.mobile.utils.toObject
import javax.inject.Inject

class StartLocationContainerImpl @Inject constructor(
    private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase,
    private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase,
) : StartLocationContainer {

    private var localValue: Pair<Double, Double>? = null
    private var initialized = false

    override fun setLocation(location: LocationWrapper?) {
        initialized = true
        if (location == null) {
            localValue = null
            writeSettingsUseCase.execute(Settings.STARTING_LOCATION, "").subscribe()
        } else {
            localValue = Pair(location.latitude, location.longitude)
            writeSettingsUseCase.execute(
                Settings.STARTING_LOCATION,
                StartLocation(location.latitude, location.longitude).toJSON()
            ).subscribe()
        }
    }

    override fun getLocation(): Pair<Double, Double>? {
        if (!initialized) {
            val stringInput = readSettingsUseCase.executeForString(Settings.STARTING_LOCATION)
            if (stringInput.length <= 3) localValue = null else {
                val inputObject = stringInput.toObject<StartLocation>()
                localValue = Pair(inputObject.lat, inputObject.lon)
            }
            initialized = true
        }
        return localValue
    }

    data class StartLocation(val lat: Double, val lon: Double) : JsonConvertible
}